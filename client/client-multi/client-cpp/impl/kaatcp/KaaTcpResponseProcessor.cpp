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

#include "kaa/kaatcp/KaaTcpResponseProcessor.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"

namespace kaa
{

void KaaTcpResponseProcessor::processResponseBuffer(const char *buf, std::uint32_t size)
{
    parser_.parseBuffer(buf, size);
    const auto& messages_ = parser_.releaseMessages();
    for (auto it = messages_.begin(); it != messages_.end(); ++it) {
        switch (it->first) {
            case KaaTcpMessageType::MESSAGE_CONNACK:
                KAA_LOG_DEBUG("KaaTcp: CONNACK message received");
                if (onConnack_) {
                    onConnack_(ConnackMessage(it->second.first.get(), it->second.second));
                }
                break;
            case KaaTcpMessageType::MESSAGE_KAASYNC:
                KAA_LOG_DEBUG("KaaTcp: KAASYNC message received");
                if (onKaaSyncResponse_) {
                    onKaaSyncResponse_(KaaSyncResponse(it->second.first.get(), it->second.second));
                }
                break;
            case KaaTcpMessageType::MESSAGE_PINGRESP:
                KAA_LOG_DEBUG("KaaTcp: PINGRESP message received");
                if (onPingResp_) {
                    onPingResp_();
                }
                break;
            case KaaTcpMessageType::MESSAGE_DISCONNECT:
                KAA_LOG_DEBUG("KaaTcp: DISCONNECT message received");
                if (onDisconnect_) {
                    onDisconnect_(DisconnectMessage(it->second.first.get(), it->second.second));
                }
                break;
            default:
                KAA_LOG_ERROR(boost::format("KaaTcp: unexpected message type %1%") % (int) it->first);
                throw KaaException(boost::format("KaaTcp: unexpected message type: %1%") % (int) it->first);
        }
    }
}

}


