/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef LOGCOLLECTOR_HPP_
#define LOGCOLLECTOR_HPP_


#include <chrono>
#include <memory>
#include <unordered_map>

#include "kaa/KaaThread.hpp"
#include "kaa/log/ILogStorage.hpp"
#include "kaa/log/ILogCollector.hpp"
#include "kaa/log/ILogProcessor.hpp"
#include "kaa/log/ILogUploadStrategy.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/log/ILogFailoverCommand.hpp"
#include "kaa/utils/KaaTimer.hpp"

namespace kaa {

class IExecutorContext;
class LoggingTransport;
class KaaClientProperties;

typedef std::chrono::system_clock clock_t;

class TimeoutInfo {

public:
    TimeoutInfo(const std::int32_t& transportAccessPointId, const std::chrono::time_point<clock_t>& timeoutTime)
        : transportAccessPointId_(transportAccessPointId), timeoutTime_(timeoutTime) {}

    std::int32_t getTransportAccessPointId() const {
        return transportAccessPointId_;
    }

    std::chrono::time_point<clock_t> getTimeoutTime() const {
        return timeoutTime_;
    }

private:
    std::int32_t transportAccessPointId_;
    std::chrono::time_point<clock_t> timeoutTime_;
};

/**
 * Default @c ILogCollector implementation.
 */
class LogCollector : public ILogCollector, public ILogProcessor, public ILogFailoverCommand {
public:
    LogCollector(IKaaChannelManagerPtr manager, IExecutorContext& executorContext, const KaaClientProperties& clientProperties);

    virtual void addLogRecord(const KaaUserLogRecord& record);

    virtual void setStorage(ILogStoragePtr storage);
    virtual void setUploadStrategy(ILogUploadStrategyPtr strategy);

    virtual std::shared_ptr<LogSyncRequest> getLogUploadRequest();
    virtual void onLogUploadResponse(const LogSyncResponse& response);

    void setTransport(LoggingTransport* transport);

private:
    virtual void retryLogUpload();
    virtual void retryLogUpload(std::size_t delay);

    virtual void switchAccessPoint();

    void doSync();
    void processLogUploadDecision(LogUploadStrategyDecision decision);

    bool isDeliveryTimeout();
    void addDeliveryTimeout(std::int32_t requestId);
    bool removeDeliveryTimeout(std::int32_t requestId);

    void startTimeoutTimer();
    void startLogUploadCheckTimer();

    void processTimeout();

    void rescheduleTimers();

    bool isUploadAllowed();

private:
    ILogStoragePtr    storage_;
    KAA_MUTEX_DECLARE(storageGuard_);

    ILogUploadStrategyPtr uploadStrategy_;

    LoggingTransport* transport_;
    KAA_MUTEX_DECLARE(transportGuard_);

    IKaaChannelManagerPtr    channelManager_;

    std::unordered_map<std::int32_t, TimeoutInfo> timeouts_;
    std::int32_t timeoutAccessPointId_;
    KAA_MUTEX_DECLARE(timeoutsGuard_);

    KaaTimer<void ()>        logUploadCheckTimer_;
    KaaTimer<void ()>        scheduledUploadTimer_;
    KaaTimer<void ()>        timeoutTimer_;

    IExecutorContext& executorContext_;
};

}  // namespace kaa

#endif /* LOGCOLLECTOR_HPP_ */
