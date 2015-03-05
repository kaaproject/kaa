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

#ifdef KAA_USE_LOGGING

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/common/UuidGenerator.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

LogCollector::LogCollector(IKaaChannelManagerPtr manager)
        : storage_(nullptr), status_(nullptr), configuration_(nullptr), uploadStrategy_(nullptr),
          failoverStrategy_(nullptr), transport_(nullptr), isUploading_(false)
{
    defaultConfiguration_.reset(new DefaultLogUploadConfiguration);
    defaultLogStorage_.reset(new MemoryLogStorage(defaultConfiguration_->getBlockSize()));
    defaultUploadStrategy_.reset(new SizeUploadStrategy);
    defaultFailoverStrategy_.reset(new LogUploadFailoverStrategy(manager));

    storage_ = defaultLogStorage_.get();
    status_ = defaultLogStorage_.get();
    configuration_ = defaultConfiguration_.get();
    uploadStrategy_ = defaultUploadStrategy_.get();
    failoverStrategy_ = defaultFailoverStrategy_.get();
}

LogCollector::LogCollector(ILogStorage* storage, ILogStorageStatus* status, ILogUploadConfiguration* configuration,
                           ILogUploadStrategy* uploadStrategy, ILogUploadFailoverStrategy* failoverStrategy)
        : storage_(storage), status_(status), configuration_(configuration), uploadStrategy_(uploadStrategy),
          failoverStrategy_(failoverStrategy), transport_(nullptr), isUploading_(false)
{

}

void LogCollector::makeLogRecord(const LogRecord& record)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    if (storage_ != nullptr) {
        storage_->addLogRecord(record);

        if (isDeliveryTimeout()) {
            return;
        }

        if (uploadStrategy_ != nullptr) {
            LogUploadStrategyDecision decision = uploadStrategy_->isUploadNeeded(configuration_, status_);
            switch (decision) {
            case LogUploadStrategyDecision::CLEANUP: {
                KAA_LOG_INFO(boost::format("Need to cleanup log storage."));
                doCleanup();
                break;
            }
            case LogUploadStrategyDecision::UPLOAD: {
                if (failoverStrategy_ == nullptr || failoverStrategy_->isUploadApproved()) {
                    if (!isUploading_) {
                        KAA_LOG_INFO(boost::format("Going to start logs upload."));
                        isUploading_ = true;
                        doUpload();
                    }
                }
                break;
            }
            case LogUploadStrategyDecision::NOOP:
            default:
                KAA_LOG_DEBUG(boost::format("Nothing to do now."))
                ;
                break;
            }
        } else {
            KAA_LOG_ERROR(
                    boost::format(
                            "Can not decide if log upload is needed. Strategy: %1%, Configuration: %2%, Status: %3%)")
                    % uploadStrategy_ % configuration_ % status_);
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
    if (strategy != nullptr) {
        KAA_MUTEX_LOCKING("storageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
        KAA_MUTEX_LOCKED("storageGuard_");

        KAA_LOG_DEBUG(boost::format("Replacing log upload strategy from %1% to %2%") % uploadStrategy_ % strategy);
        uploadStrategy_ = strategy;
    }
}

void LogCollector::setFailoverStrategy(ILogUploadFailoverStrategy * strategy)
{
    if (strategy != nullptr) {
        KAA_MUTEX_LOCKING("storageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
        KAA_MUTEX_LOCKED("storageGuard_");

        KAA_LOG_DEBUG(boost::format("Replacing log failover strategy from %1% to %2%") % failoverStrategy_ % strategy);
        failoverStrategy_ = strategy;
    }
}

void LogCollector::doUpload()
{
    size_t blockSize = configuration_->getBlockSize();
    while (status_->getConsumedVolume() > 0) {
        LogSyncRequest request;
        request.requestId = UuidGenerator::generateRandomInt();
        ILogStorage::container_type recordsBlock = storage_->getRecordBlock(blockSize, request.requestId);
        if (recordsBlock.empty()) {
            break;
        }
        for (auto it = recordsBlock.begin(); it != recordsBlock.end(); ++it) {
            LogEntry entry;
            entry.data.assign(it->getData().begin(), it->getData().end());
            request.logEntries.get_array().push_back(entry);
        }
        KAA_MUTEX_UNIQUE_DECLARE(lock, requestsGuard_);
        KAA_LOG_INFO(boost::format("Generated log upload request: id= %1%") % request.requestId);
        requests_.insert(std::make_pair(request.requestId, request));
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

bool LogCollector::isDeliveryTimeout()
{
    bool isTimeout = false;
    const auto& now = clock_t::now();

    for (const auto& request : timeoutsMap_) {
        if (now >= request.second) {
            isTimeout = true;
            break;
        }
    }

    if (isTimeout) {
        KAA_LOG_INFO("Log delivery timeout detected");

        for (const auto& request : timeoutsMap_) {
            storage_->notifyUploadFailed(request.first);
        }

        timeoutsMap_.clear();
        failoverStrategy_->onTimeout();
    }

    return isTimeout;
}

LogSyncRequest LogCollector::getLogUploadRequest()
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, requestsGuard_);
    LogSyncRequest request;
    KAA_LOG_INFO(boost::format("Trying to fill in log upload request. Have %1% requests") % requests_.size());
    if (!requests_.empty()) {
        auto it = requests_.begin();
        request = it->second;

        KAA_LOG_INFO(boost::format("Added log upload request id %1%") % request.requestId);
        timeoutsMap_.insert(
                std::make_pair(request.requestId,
                               clock_t::now() + std::chrono::seconds(configuration_->getLogUploadTimeout())));
        requests_.erase(it);
    }
    return request;
}

void LogCollector::onLogUploadResponse(const LogSyncResponse& response)
{
    if (!response.deliveryStatuses.is_null()) {
        const auto& deliveryStatuses = response.deliveryStatuses.get_array();
        for (const auto& status : deliveryStatuses) {
            if (status.result == SyncResponseResultType::SUCCESS) {
                storage_->removeRecordBlock(status.requestId);
                if (!requests_.empty() || uploadStrategy_->isUploadNeeded(configuration_, status_)
                        == LogUploadStrategyDecision::UPLOAD) {
                    doUpload();
                } else {
                    isUploading_ = false;
                }
            } else {
                KAA_LOG_ERROR("Failed to upload logs. Try again later.");
                isUploading_ = false;
                storage_->notifyUploadFailed(status.requestId);
                if (!status.errorCode.is_null()) {
                    failoverStrategy_->onFailure(status.errorCode.get_LogDeliveryErrorCode());
                } else {
                    KAA_LOG_ERROR("Log delivery failed, but no error code received!!!");
                }
            }

            timeoutsMap_.erase(status.requestId);
        }
    }
}

void LogCollector::setTransport(LoggingTransport *transport)
{
    transport_ = transport;
}

}  // namespace kaa

#endif

