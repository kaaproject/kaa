/*
 * Copyright 2014 CyberVision, Inc.
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

#ifndef KAA_LOG_LOGUPLOADFAILOVERSTRATEGY_HPP_
#define KAA_LOG_LOGUPLOADFAILOVERSTRATEGY_HPP_

#ifdef KAA_USE_LOGGING

#include <chrono>
#include <memory>

#include "kaa/channel/IDataChannel.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/log/ILogUploadFailoverStrategy.hpp"

namespace kaa {

class LogUploadFailoverStrategy: public ILogUploadFailoverStrategy {
public:
    LogUploadFailoverStrategy(IKaaChannelManagerPtr manager)
            : channelManager_(manager), isUploadApproved_(true), RETRY_PERIOD_SEC_(300)
    {
    }

    virtual bool isUploadApproved();

    virtual void onTimeout();
    virtual void onFailure(LogDeliveryErrorCode code);

    void setRetryPeriod(std::uint32_t period)
    {
        RETRY_PERIOD_SEC_ = period;
    }

private:
    IKaaChannelManagerPtr channelManager_;

    bool isUploadApproved_;
    std::uint32_t RETRY_PERIOD_SEC_;
    std::chrono::time_point<std::chrono::system_clock> nextUploadAttemptTS_;
};

} /* namespace kaa */

#endif /* KAA_USE_LOGGING */
#endif /* KAA_LOG_LOGUPLOADFAILOVERSTRATEGY_HPP_ */
