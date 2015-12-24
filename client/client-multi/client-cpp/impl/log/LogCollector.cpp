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
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/utils/IThreadPool.hpp"
#include "kaa/KaaClientProperties.hpp"

#ifdef KAA_USE_SQLITE_LOG_STORAGE
#include "kaa/log/SQLiteDBLogStorage.hpp"
#else
#include "kaa/log/MemoryLogStorage.hpp"
#endif

namespace kaa {

LogCollector::LogCollector(IKaaChannelManagerPtr manager, IExecutorContext& executorContext, const KaaClientProperties& clientProperties)
    : transport_(nullptr), channelManager_(manager), timeoutAccessPointId_(0),
      logUploadCheckTimer_("LogCollector logUploadCheckTimer"), scheduledUploadTimer_("LogCollector uploadTimer"),
      timeoutTimer_("LogCollector timeoutTimer"), executorContext_(executorContext)
{
#ifdef KAA_USE_SQLITE_LOG_STORAGE
    storage_.reset(new SQLiteDBLogStorage(clientProperties.getLogsDatabaseFileName()));
#else
    storage_.reset(new MemoryLogStorage());
#endif
    uploadStrategy_.reset(new DefaultLogUploadStrategy());

    startTimeoutTimer();
}

void LogCollector::startTimeoutTimer() {
    timeoutTimer_.stop();
    timeoutTimer_.start(uploadStrategy_->getTimeoutCheckPeriod(), [this]
                    {
                            if (isDeliveryTimeout()) {
                                processTimeout();
                            }
                            startTimeoutTimer();
                    });
}

void LogCollector::startLogUploadCheckTimer()
{
    logUploadCheckTimer_.stop();
    logUploadCheckTimer_.start(uploadStrategy_->getLogUploadCheckPeriod(),[this]
    {
        processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
    });
}

void LogCollector::processTimeout()
{
    executorContext_.getCallbackExecutor().add([this] () { uploadStrategy_->onTimeout(*this); });

    KAA_LOG_WARN(boost::format("Going to notify log storage of logs delivery timeout..."));

    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    for (const auto& request : timeouts_) {
        storage_->notifyUploadFailed(request.first);
    }

    timeouts_.clear();

    KAA_MUTEX_UNLOCKING("timeoutsGuard_");
    KAA_UNLOCK(timeoutsGuardLock);
    KAA_MUTEX_UNLOCKED("timeoutsGuard_");

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
}

void LogCollector::addLogRecord(const KaaUserLogRecord& record)
{
    /*
     * To avoid overhead on copying big-sized log records while capturing in lambdas,
     * serialization has been performed before adding task to executor.
     */
    LogRecordPtr serializedRecord(new LogRecord(record));

    executorContext_.getApiExecutor().add([this, serializedRecord] ()
            {
                KAA_MUTEX_LOCKING("storageGuard_");
                KAA_MUTEX_UNIQUE_DECLARE(lock, storageGuard_);
                KAA_MUTEX_LOCKED("storageGuard_");

                storage_->addLogRecord(serializedRecord);

                KAA_MUTEX_UNLOCKING("storageGuard_");
                KAA_UNLOCK(lock);
                KAA_MUTEX_UNLOCKED("storageGuard_");

                processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
            });
}

void LogCollector::processLogUploadDecision(LogUploadStrategyDecision decision)
{
    switch (decision) {
    case LogUploadStrategyDecision::UPLOAD: {
        if (isUploadAllowed()) {
            KAA_LOG_DEBUG("Going to upload logs");
            doSync();
        }
        break;
    }
    case LogUploadStrategyDecision::NOOP:
        KAA_LOG_TRACE("Nothing to do now");
        if (storage_->getStatus().getRecordsCount() > 0) {
            startLogUploadCheckTimer();
        }
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

    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    KAA_LOG_INFO("New log storage was set");
    storage_ = storage;
}

void LogCollector::setUploadStrategy(ILogUploadStrategyPtr strategy)
{
    if (!strategy) {
        KAA_LOG_ERROR("Failed to set log upload strategy: bad data");
        throw KaaException("Bad log upload strategy");
    }

    KAA_LOG_INFO("New log upload strategy was set");
    uploadStrategy_ = strategy;

    rescheduleTimers();
}

void LogCollector::doSync()
{
    KAA_MUTEX_LOCKING("transportGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(transportGuardLock, transportGuard_);
    KAA_MUTEX_LOCKED("transportGuard_");

    if (transport_) {
        transport_->sync();
    } else {
        KAA_LOG_ERROR("Failed to upload logs: log transport isn't initialized");
        throw TransportNotFoundException("Log transport isn't set");
    }
}

bool LogCollector::isDeliveryTimeout()
{

    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    auto now = clock_t::now();

    IDataChannelPtr logChannel = channelManager_->getChannelByTransportType(TransportType::LOGGING);
    std::int32_t currentAccessPointId  = 0;
    if (logChannel && logChannel->getServer()) {
        currentAccessPointId = logChannel->getServer()->getAccessPointId();
    }

    bool isTimeout = false;
    timeoutAccessPointId_ = 0;

    for (const auto& request : timeouts_) {
        if (now >= request.second.getTimeoutTime()) {
            KAA_LOG_WARN(boost::format("Log delivery timeout detected, bucket id %li") % request.first);
            isTimeout = true;
            timeoutAccessPointId_ = request.second.getTransportAccessPointId();
            // Check if current access point already has timeout
            if (timeoutAccessPointId_ == currentAccessPointId) {
                break;
            }
        }
    }

    return isTimeout;
}

void LogCollector::addDeliveryTimeout(std::int32_t requestId)
{
    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    IDataChannelPtr logChannel = channelManager_->getChannelByTransportType(TransportType::LOGGING);
    std::int32_t currentAccessPointId  = 0;
    if (logChannel) {
        currentAccessPointId = logChannel->getServer()->getAccessPointId();
    }
    TimeoutInfo timeoutInfo(currentAccessPointId,
            clock_t::now() + std::chrono::seconds(uploadStrategy_->getTimeout()));

    timeouts_.insert(std::make_pair(requestId, timeoutInfo));
}

bool LogCollector::removeDeliveryTimeout(std::int32_t requestId)
{
    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    return timeouts_.erase(requestId);
}

bool LogCollector::isUploadAllowed()
{
    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    if (timeouts_.size() >= uploadStrategy_->getMaxParallelUploads()) {
        KAA_LOG_INFO(boost::format("Ignore log upload: too much pending requests %u, max allowed %u"  )
                                                       % timeouts_.size() % uploadStrategy_->getMaxParallelUploads());
        return false;
    }

    return true;
}

std::shared_ptr<LogSyncRequest> LogCollector::getLogUploadRequest()
{
    std::shared_ptr<LogSyncRequest> request;

    if (!isUploadAllowed()) {
        return request;
    }

    ILogStorage::RecordPack recordPack;

    {
        KAA_MUTEX_LOCKING("storageGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard_);
        KAA_MUTEX_LOCKED("storageGuard_");

        recordPack = storage_->getRecordBlock(uploadStrategy_->getBatchSize(), uploadStrategy_->getRecordsBatchCount());
    }

    if (!recordPack.second.empty()) {
        request.reset(new LogSyncRequest);
        request->requestId = recordPack.first;

        std::vector<LogEntry> logs;
        logs.reserve(recordPack.second.size());
        for (const auto& log : recordPack.second) {
            logs.push_back(log->getLogEntry());
        }

        request->logEntries.set_array(std::move(logs));
        addDeliveryTimeout(request->requestId);
    }

    return request;
}

void LogCollector::onLogUploadResponse(const LogSyncResponse& response)
{
    KAA_MUTEX_LOCKING("storageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(storageGuardLock, storageGuard_);
    KAA_MUTEX_LOCKED("storageGuard_");

    if (!response.deliveryStatuses.is_null()) {
        const auto& deliveryStatuses = response.deliveryStatuses.get_array();
        for (const auto& status : deliveryStatuses) {
            if (!removeDeliveryTimeout(status.requestId)) {
                continue;
            }

            if (status.result == SyncResponseResultType::SUCCESS) {
                KAA_LOG_INFO(boost::format("Logs (requestId %ld) successfully delivered") % status.requestId);
                storage_->removeRecordBlock(status.requestId);
            } else {
                storage_->notifyUploadFailed(status.requestId);

                KAA_MUTEX_UNLOCKING("storageGuard_");
                KAA_UNLOCK(storageGuardLock);
                KAA_MUTEX_UNLOCKED("storageGuard_");

                if (!status.errorCode.is_null()) {
                    auto errocCode = status.errorCode.get_LogDeliveryErrorCode();
                    KAA_LOG_WARN(boost::format("Logs (requestId %ld) failed to deliver (error %d)")
                                            % status.requestId % (int)errocCode);

                    executorContext_.getCallbackExecutor().add([this, errocCode] ()
                            {
                                uploadStrategy_->onFailure(*this, errocCode);
                            });
                } else {
                    KAA_LOG_WARN("Log delivery failed, but no error code received");
                }

                KAA_MUTEX_LOCKING("storageGuard_");
                KAA_LOCK(storageGuardLock);
                KAA_MUTEX_LOCKED("storageGuard_");
            }
        }
    }

    KAA_MUTEX_UNLOCKING("storageGuard_");
    KAA_UNLOCK(storageGuardLock);
    KAA_MUTEX_UNLOCKED("storageGuard_");

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
}

void LogCollector::setTransport(LoggingTransport* transport)
{
    KAA_MUTEX_LOCKING("transportGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(transportGuardLock, transportGuard_);
    KAA_MUTEX_LOCKED("transportGuard_");

    transport_ = transport;
}

void LogCollector::retryLogUpload()
{
    KAA_LOG_INFO("Going to retry log upload...");
    doSync();
}

void LogCollector::retryLogUpload(std::size_t delay)
{
    KAA_LOG_INFO(boost::format("Schedule log upload with %u second(s) delay ...") % delay);
    scheduledUploadTimer_.stop();
    scheduledUploadTimer_.start(delay, [&] { doSync(); });
}

void LogCollector::switchAccessPoint()
{
    IDataChannelPtr logChannel = channelManager_->getChannelByTransportType(TransportType::LOGGING);
    if (logChannel && logChannel->getServer()) {
        if (timeoutAccessPointId_ == logChannel->getServer()->getAccessPointId()) {
            KAA_LOG_WARN("Try to switch to another Operations server...");
            channelManager_->onServerFailed(logChannel->getServer());
        }
    } else {
        KAA_LOG_ERROR("Can't find LOGGING data channel");
    }
}

void LogCollector::rescheduleTimers()
{
    startTimeoutTimer();
    startLogUploadCheckTimer();
}

}  // namespace kaa

