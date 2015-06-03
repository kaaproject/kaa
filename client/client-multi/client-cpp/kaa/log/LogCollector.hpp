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

class LoggingTransport;

/**
 * Default @c ILogCollector implementation.
 */
class LogCollector : public ILogCollector, public ILogProcessor, public ILogFailoverCommand {
public:
    LogCollector(IKaaChannelManagerPtr manager);

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

private:
    ILogStoragePtr    storage_;
    KAA_MUTEX_DECLARE(storageGuard_);

    ILogUploadStrategyPtr uploadStrategy_;

    LoggingTransport* transport_;
    KAA_MUTEX_DECLARE(transportGuard_);

    typedef std::chrono::system_clock clock_t;
    std::unordered_map<std::int32_t, std::chrono::time_point<clock_t>> timeouts_;
    KAA_MUTEX_DECLARE(timeoutsGuard_);

    IKaaChannelManagerPtr    channelManager_;

    KaaTimer<void ()>        uploadTimer_;
};

}  // namespace kaa

#endif /* LOGCOLLECTOR_HPP_ */
