/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License") {}
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "kaa/log/LogCollector.hpp"

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/UuidGenerator.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

LogCollector::LogCollector()
    : storage_(nullptr)
    , status_(nullptr)
    , configuration_(nullptr)
    , strategy_(nullptr)
    , transport_(nullptr)
    , isUploading_(false)
{
    defaultConfiguration_.reset(new DefaultLogUploadConfiguration);
    defaultLogStorage_.reset(new MemoryLogStorage(defaultConfiguration_->getBlockSize()));
    defaultUploadStrategy_.reset(new SizeUploadStrategy);

    storage_ = defaultLogStorage_.get();
    status_ = defaultLogStorage_.get();
    configuration_ = defaultConfiguration_.get();
    strategy_ = defaultUploadStrategy_.get();
}

LogCollector::LogCollector  ( ILogStorage* storage
                            , ILogStorageStatus* status
                            , ILogUploadConfiguration* configuration
                            , ILogUploadStrategy* strategy)
    : storage_(storage)
    , status_(status)
    , configuration_(configuration)
    , strategy_(strategy)
    , transport_(nullptr)
    , isUploading_(false)
{

}

void LogCollector::makeLogRecord(const LogRecord& record)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    if (storage_ != nullptr) {
        storage_->addLogRecord(record);
        if (strategy_ != nullptr) {
            LogUploadStrategyDecision decision = strategy_->isUploadNeeded(configuration_, status_);
            switch (decision) {
                case LogUploadStrategyDecision::CLEANUP: {
                    KAA_LOG_INFO(boost::format("Need to cleanup log storage."));
                    doCleanup();
                    break;
                }
                case LogUploadStrategyDecision::UPLOAD: {
                    if (!isUploading_) {
                        KAA_LOG_INFO(boost::format("Going to start logs upload."));
                        isUploading_ = true;
                        doUpload();
                    }
                    break;
                }
                case LogUploadStrategyDecision::NOOP:
                default:
                    KAA_LOG_DEBUG(boost::format("Nothing to do now."));
                    break;
            }
        } else {
            KAA_LOG_ERROR(boost::format("Can not decide if log upload is needed. Strategy: %1%, Configuration: %2%, Status: %3%)")
                % strategy_ % configuration_ % status_);
        }
    } else {
        KAA_LOG_ERROR("Can not add log to an empty storage");
    }
}

void LogCollector::setStorage(ILogStorage * storage)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    KAA_LOG_DEBUG(boost::format("Replacing log storage from %1% to %2%") % storage_ % storage);
    storage_ = storage;
}

void LogCollector::setStorageStatus(ILogStorageStatus * status)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    KAA_LOG_DEBUG(boost::format("Replacing log storage status from %1% to %2%") % status_ % status);
    status_ = status;
}

void LogCollector::setConfiguration(ILogUploadConfiguration * configuration)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    KAA_LOG_DEBUG(boost::format("Replacing log upload configurations from %1% to %2%") % configuration_ % configuration);
    configuration_ = configuration;
}

void LogCollector::setUploadStrategy(ILogUploadStrategy * strategy)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    KAA_LOG_DEBUG(boost::format("Replacing log upload strategy from %1% to %2%") % strategy_ % strategy);
    strategy_ = strategy;
}

void LogCollector::doUpload()
{
    size_t blockSize = configuration_->getBlockSize();
    while (status_->getConsumedVolume() > 0) {
        LogSyncRequest request;
        std::string requestId;
        UuidGenerator::generateUuid(requestId);
        request.requestId.set_string(requestId);
        ILogStorage::container_type recordsBlock = storage_->getRecordBlock(blockSize, request.requestId.get_string());
        if (recordsBlock.empty()) {
            break;
        }
        for (auto it = recordsBlock.begin(); it != recordsBlock.end(); ++it) {
            LogEntry entry;
            entry.data.assign(it->getData().begin(), it->getData().end());
            request.logEntries.get_array().push_back(entry);
        }
        KAA_MUTEX_UNIQUE_DECLARE(lock, requestsGuard_);
        KAA_LOG_INFO(boost::format("Generated log upload request: id= %1%") % request.requestId.get_string());
        requests_.insert(std::make_pair(request.requestId.get_string(), request));
    }

    if (transport_ != nullptr) {
        transport_->sync();
    } else {
        KAA_LOG_WARN("Log upload postponed: transport was not set");
    }
}

void LogCollector::doCleanup()
{
    if (configuration_ != nullptr) {
        storage_->removeOldestRecords(configuration_->getMaxStorageVolume());
    } else {
        KAA_LOG_ERROR("Can not perform cleanup: unable to fetch maximal allowed volume size.");
    }
}

LogSyncRequest LogCollector::getLogUploadRequest()
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, requestsGuard_);
    LogSyncRequest request;
    request.requestId.set_null();
    KAA_LOG_INFO(boost::format("Trying to populate log upload request. Have %1% requests") % requests_.size());
    if (!requests_.empty()) {
        auto it = requests_.begin();
        request = it->second;
        KAA_LOG_INFO(boost::format("Added log upload request id %1%") % it->second.requestId.get_string());
        requests_.erase(requests_.begin());
    }
    return request;
}

void LogCollector::onLogUploadResponse(const LogSyncResponse& response)
{
    if (response.result == SyncResponseResultType::SUCCESS) {
        storage_->removeRecordBlock(response.requestId);
        if (!requests_.empty() || strategy_->isUploadNeeded(configuration_, status_) == LogUploadStrategyDecision::UPLOAD) {
            doUpload();
        } else {
            isUploading_ = false;
        }
    } else {
        KAA_LOG_ERROR("Failed to upload logs. Try again later.");
        isUploading_ = false;
        storage_->notifyUploadFailed(response.requestId);
    }
}

void LogCollector::setTransport(LoggingTransport *transport)
{
    transport_ = transport;
}

}  // namespace kaa
