/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include "kaa/log/LogUploadFailoverStrategy.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"

namespace kaa {

bool LogUploadFailoverStrategy::isUploadApproved()
{
    if (!isUploadApproved_ && (std::chrono::system_clock::now() >= nextUploadAttemptTS_)) {
        isUploadApproved_ = true;
    }

    return isUploadApproved_;
}

void LogUploadFailoverStrategy::onTimeout()
{
    KAA_LOG_INFO("Log upload timeout occurred. Switching to next Operation server");

    IDataChannelPtr channel = channelManager_->getChannelByTransportType(TransportType::LOGGING);

    if (channel != nullptr) {
        channelManager_->onServerFailed(channel->getServer());
    } else {
        KAA_LOG_ERROR("Failed to switch Operation server. No channel is used for logging transport");
    }
}

void LogUploadFailoverStrategy::onFailure(LogDeliveryErrorCode code)
{
    switch (code) {
        case LogDeliveryErrorCode::NO_APPENDERS_CONFIGURED:
        case LogDeliveryErrorCode::APPENDER_INTERNAL_ERROR:
        case LogDeliveryErrorCode::REMOTE_CONNECTION_ERROR:
        case LogDeliveryErrorCode::REMOTE_INTERNAL_ERROR:
            isUploadApproved_ = false;
            nextUploadAttemptTS_ = std::chrono::system_clock::now() +
                                       std::chrono::seconds(RETRY_PERIOD_SEC_);
            break;
        default:
            break;
    }
}

} /* namespace kaa */
