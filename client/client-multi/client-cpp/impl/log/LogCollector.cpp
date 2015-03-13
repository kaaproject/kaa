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
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/log/MemoryLogStorage.hpp"
#include "kaa/log/DefaultLogUploadStrategy.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"
#include "kaa/log/LogRecord.hpp"

namespace kaa {

LogCollector::LogCollector(IKaaChannelManagerPtr manager)
    : requestId_(0), transport_(nullptr)
{
    storage_.reset(new MemoryLogStorage());
    uploadStrategy_.reset(new DefaultLogUploadStrategy(manager));
}

void LogCollector::addLogRecord(const KaaUserLogRecord& record)
{
    LogRecordPtr serializedRecord(new LogRecord(record));

    {
        KAA_MUTEX_LOCKING(storageGuard_);
        KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
        KAA_MUTEX_LOCKED(storageGuard_);

        storage_->addLogRecord(serializedRecord);
    }

    if (isDeliveryTimeout()) {
        return;
    }

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));

}

void LogCollector::processLogUploadDecision(LogUploadStrategyDecision decision)
{
    switch (decision) {
    case LogUploadStrategyDecision::UPLOAD: {
        KAA_LOG_DEBUG("Going to upload logs");
        doSync();
        break;
    }
    case LogUploadStrategyDecision::NOOP:
        KAA_LOG_TRACE("Nothing to do now");
        break;
    default:
        KAA_LOG_WARN("Unknown log upload decision");
        break;
    }
}

void LogCollector::setStorage(ILogStoragePtr storage)
{
    if (!storage) {
        KAA_LOG_ERROR("Failed to set log storage: bad data");
        throw KaaException("Bad log storage");
    }

    KAA_MUTEX_LOCKING(storageGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED(storageGuard_);

    KAA_LOG_INFO("New log storage was set");
    storage_ = storage;
}

void LogCollector::setUploadStrategy(ILogUploadStrategyPtr strategy)
{
    if (!strategy) {
        KAA_LOG_ERROR("Failed to set log upload strategy: bad data");
        throw KaaException("Bad log upload strategy");
    }

    KAA_MUTEX_LOCKING(storageGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
    KAA_MUTEX_LOCKED(storageGuard_);

    KAA_LOG_INFO("New log upload strategy was set");
    uploadStrategy_ = strategy;
}

void LogCollector::doSync()
{
    if (transport_) {
        transport_->sync();
    } else {
        KAA_LOG_ERROR("Failed to upload logs: log transport isn't initialized");
        throw TransportNotFoundException("Log transport isn't set");
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
        uploadStrategy_->onTimeout();
    }

    return isTimeout;
}

std::shared_ptr<LogSyncRequest> LogCollector::getLogUploadRequest()
{
    ILogStorage::RecordPack recordPack;
    std::shared_ptr<LogSyncRequest> request;

    {
        KAA_MUTEX_LOCKING(storageGuard_);
        KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
        KAA_MUTEX_LOCKED(storageGuard_);

        recordPack = storage_->getRecordBlock(uploadStrategy_->getBatchSize());
    }

    if (!recordPack.second.empty()) {
        request.reset(new LogSyncRequest);
        request->requestId = requestId_++;

        std::vector<LogEntry> logs;
        logs.reserve(recordPack.second.size());
        for (const auto& log : recordPack.second) {
            logs.push_back(log->getLogEntry());
        }

        request->logEntries.set_array(std::move(logs));
        timeoutsMap_.insert(std::make_pair(request->requestId,
                                           clock_t::now() + std::chrono::seconds(uploadStrategy_->getTimeout())));
    }

    return request;
}

void LogCollector::onLogUploadResponse(const LogSyncResponse& response)
{
    KAA_MUTEX_LOCKING(storageGuard_);
    KAA_MUTEX_UNIQUE_DECLARE(storageLock, storageGuard_);
    KAA_MUTEX_LOCKED(storageGuard_);

    if (!response.deliveryStatuses.is_null()) {
        const auto& deliveryStatuses = response.deliveryStatuses.get_array();
        for (const auto& status : deliveryStatuses) {
            if (!timeoutsMap_.erase(status.requestId)) {
                continue;
            }

            if (status.result == SyncResponseResultType::SUCCESS) {
                KAA_LOG_INFO(boost::format("Logs (requestId %1%) successfully delivered") % status.requestId);
                storage_->removeRecordBlock(status.requestId);
            } else {
                KAA_LOG_WARN(boost::format("Logs (requestId %1%) failed to deliver") % status.requestId);
                storage_->notifyUploadFailed(status.requestId);

                KAA_MUTEX_UNLOCKING(storageGuard_);
                KAA_UNLOCK(storageLock);
                KAA_MUTEX_UNLOCKED(storageGuard_);

                if (!status.errorCode.is_null()) {
                    uploadStrategy_->onFailure(status.errorCode.get_LogDeliveryErrorCode());
                } else {
                    KAA_LOG_ERROR("Log delivery failed, but no error code received");
                }

                KAA_MUTEX_LOCKING(storageGuard_);
                KAA_LOCK(storageLock);
                KAA_MUTEX_LOCKED(storageGuard_);
            }
        }
    }

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
}

void LogCollector::setTransport(LoggingTransport* transport)
{
    transport_ = transport;
}

}  // namespace kaa

