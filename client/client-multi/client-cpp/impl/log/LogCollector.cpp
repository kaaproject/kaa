/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
#include "kaa/log/LogBucket.hpp"
#include "kaa/log/ILogDeliveryListener.hpp"

#ifdef KAA_USE_SQLITE_LOG_STORAGE
#include "kaa/log/SQLiteDBLogStorage.hpp"
#else
#include "kaa/log/MemoryLogStorage.hpp"
#endif

namespace kaa {

LogCollector::LogCollector(IKaaChannelManagerPtr manager, IKaaClientContext &context)
    : transport_(nullptr), channelManager_(manager), timeoutAccessPointId_(0),
      logUploadCheckTimer_("LogCollector logUploadCheckTimer"), scheduledUploadTimer_("LogCollector uploadTimer"),
      timeoutTimer_("LogCollector timeoutTimer"), context_(context)
{
#ifdef KAA_USE_SQLITE_LOG_STORAGE
    storage_.reset(new SQLiteDBLogStorage(context_));
#else
    storage_.reset(new MemoryLogStorage(context_));
#endif
    uploadStrategy_.reset(new DefaultLogUploadStrategy(context_));

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
    context_.getExecutorContext().getCallbackExecutor().add([this] ()
            {
                uploadStrategy_->onTimeout(*this);
            });

    KAA_LOG_WARN(boost::format("Going to notify log storage of logs delivery timeout..."));

    KAA_MUTEX_LOCKING("timeoutsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
    KAA_MUTEX_LOCKED("timeoutsGuard_");

    auto now = clock_t::now();

    for (auto request = timeouts_.begin(); request != timeouts_.end();) {
        if (now >= request->second.getTimeoutTime()) {
            storage_->rollbackBucket(request->first);
            request = timeouts_.erase(request);
        } else {
            request++;
        }
    }

    KAA_MUTEX_UNLOCKING("timeoutsGuard_");
    KAA_UNLOCK(timeoutsGuardLock);
    KAA_MUTEX_UNLOCKED("timeoutsGuard_");

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
}

RecordFuture LogCollector::addLogRecord(const KaaUserLogRecord& record)
{
    RecordInfo recordInfo;
    auto promisePtr = std::make_shared<std::promise<RecordInfo>>();
    RecordDeliveryInfo recordDeliveryInfo(promisePtr, recordInfo);

    context_.getExecutorContext().getApiExecutor().add([this, record, recordDeliveryInfo] ()
            {
                try {
                    auto bucketInfo = storage_->addLogRecord(LogRecord(record));
                    updateBucketInfo(bucketInfo, recordDeliveryInfo);
                } catch (...) {
                    try {
                        KAA_LOG_WARN("Failed to add log record");
                        recordDeliveryInfo.deliveryFuture_->set_exception(std::current_exception());
                    } catch(...) {}
                }

                processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
            });

    return RecordFuture(promisePtr->get_future());
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
    transport_->sync();
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

            if (logDeliverylistener_) {
                auto bucketId = request.first;
                context_.getExecutorContext().getCallbackExecutor().add([this, bucketId] ()
                        {
                            logDeliverylistener_->onLogDeliveryTimeout(getBucketInfo(bucketId));
                        });
            }

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

    LogBucket bucket = storage_->getNextBucket();
    if (bucket.getRecords().empty()) {
        KAA_LOG_TRACE("No logs to send");
        return request;
    }

    KAA_LOG_TRACE(boost::format("Sending %1% log records") % bucket.getRecords().size());

    request.reset(new LogSyncRequest);
    request->requestId = bucket.getBucketId();

    std::vector<LogEntry> logsToSend;
    logsToSend.resize(bucket.getRecords().size());

    std::size_t i = 0;
    for (auto& record : bucket.getRecords()) {
        logsToSend[i++].data = std::move(record.getRvalueData());
    }

    request->logEntries.set_array(std::move(logsToSend));
    addDeliveryTimeout(request->requestId);

    return request;
}

void LogCollector::onLogUploadResponse(const LogSyncResponse& response, std::size_t deliveryTime)
{
    if (!response.deliveryStatuses.is_null()) {
        const auto& deliveryStatuses = response.deliveryStatuses.get_array();

        for (const auto& status : deliveryStatuses) {
            if (!removeDeliveryTimeout(status.requestId)) {
                KAA_LOG_WARN(boost::format("Received unknown delivery status, id %1%. Ignoring...") % status.requestId);
                continue;
            }

            auto bucketInfo = getBucketInfo(status.requestId);

            if (status.result == SyncResponseResultType::SUCCESS) {
                KAA_LOG_INFO(boost::format("Logs (requestId %ld) successfully delivered") % status.requestId);

                storage_->removeBucket(status.requestId);

                if (logDeliverylistener_) {
                    context_.getExecutorContext().getCallbackExecutor().add([this, bucketInfo] ()
                            {
                                logDeliverylistener_->onLogDeliverySuccess(bucketInfo);
                            });
                }

                context_.getExecutorContext().getCallbackExecutor().add([this, bucketInfo, deliveryTime] ()
                        {
                            notifyDeliveryFuturesOnSuccess(bucketInfo.getBucketId(), deliveryTime);
                            removeBucketInfo(bucketInfo.getBucketId());
                        });
            } else {
                storage_->rollbackBucket(status.requestId);

                if (!status.errorCode.is_null()) {
                    auto errocCode = status.errorCode.get_LogDeliveryErrorCode();
                    KAA_LOG_WARN(boost::format("Logs (requestId %ld) failed to deliver (error %d)")
                                            % status.requestId % (int)errocCode);

                    context_.getExecutorContext().getCallbackExecutor().add([this, errocCode] ()
                            {
                                uploadStrategy_->onFailure(*this, errocCode);
                            });
                } else {
                    KAA_LOG_WARN("Log delivery failed, but no error code received");
                }

                if (logDeliverylistener_) {
                    context_.getExecutorContext().getCallbackExecutor().add([this, bucketInfo] ()
                            {
                                logDeliverylistener_->onLogDeliveryFailure(bucketInfo);
                            });
                }
            }
        }
    }

    processLogUploadDecision(uploadStrategy_->isUploadNeeded(storage_->getStatus()));
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
            channelManager_->onServerFailed(logChannel->getServer(),
                                            KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
            KAA_MUTEX_LOCKING("timeoutsGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(timeoutsGuardLock, timeoutsGuard_);
            KAA_MUTEX_LOCKED("timeoutsGuard_");
            for (const auto &request : timeouts_) {
                storage_->rollbackBucket(request.first);
            }
            timeouts_.clear();
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

void LogCollector::updateBucketInfo(const BucketInfo& bucketInfo, const RecordDeliveryInfo& recordInfo)
{
    KAA_MUTEX_LOCKING("bucketInfoStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(bucketInfoStorageLock, bucketInfoStorageGuard_);
    KAA_MUTEX_LOCKED("bucketInfoStorageGuard_");

    auto& bucket = bucketInfoStorage_[bucketInfo.getBucketId()];

    bucket.bucketInfo_ = bucketInfo;
    bucket.recordDeliveryInfoStorage_.push_back(recordInfo);
}

void LogCollector::notifyDeliveryFuturesOnSuccess(std::int32_t bucketId, std::size_t deliveryTime)
{
    KAA_MUTEX_LOCKING("bucketInfoStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(bucketInfoStorageLock, bucketInfoStorageGuard_);
    KAA_MUTEX_LOCKED("bucketInfoStorageGuard_");

    auto it = bucketInfoStorage_.find(bucketId);
    if (it != bucketInfoStorage_.end()) {
        for (auto& recordFutureInfo : it->second.recordDeliveryInfoStorage_) {
            recordFutureInfo.recordInfo_.setRecordDeliveryTimeMs(deliveryTime - recordFutureInfo.recordInfo_.getRecordAddedTimestampMs());
            recordFutureInfo.recordInfo_.setBucketInfo(it->second.bucketInfo_);
            recordFutureInfo.deliveryFuture_->set_value(recordFutureInfo.recordInfo_);
        }
    }
}

BucketInfo LogCollector::getBucketInfo(std::int32_t id)
{
    KAA_MUTEX_LOCKING("bucketInfoStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(bucketInfoStorageLock, bucketInfoStorageGuard_);
    KAA_MUTEX_LOCKED("bucketInfoStorageGuard_");

    return bucketInfoStorage_[id].bucketInfo_;
}

void LogCollector::removeBucketInfo(std::int32_t id)
{
    KAA_MUTEX_LOCKING("bucketInfoStorageGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(bucketInfoStorageLock, bucketInfoStorageGuard_);
    KAA_MUTEX_LOCKED("bucketInfoStorageGuard_");

    bucketInfoStorage_.erase(id);
}

}  // namespace kaa
