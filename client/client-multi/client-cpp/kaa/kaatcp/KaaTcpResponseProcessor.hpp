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

#ifndef KAATCPRESPONSEPROCESSOR_HPP_
#define KAATCPRESPONSEPROCESSOR_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/KaaSyncResponse.hpp"
#include "kaa/kaatcp/ConnackMessage.hpp"
#include "kaa/kaatcp/DisconnectMessage.hpp"
#include "kaa/kaatcp/KaaTcpParser.hpp"
#include "kaa/IKaaClientContext.hpp"
#include <functional>

namespace kaa
{

class KaaTcpResponseProcessor
{
public:
    KaaTcpResponseProcessor(IKaaClientContext &context): parser_(context), context_(context) { }
    ~KaaTcpResponseProcessor() { }

    void processResponseBuffer(const char *buf, std::uint32_t size);

    void registerConnackReceiver(std::function<void (const ConnackMessage&)> onConnack) { onConnack_ = onConnack; }
    void registerKaaSyncReceiver(std::function<void (const KaaSyncResponse&)> onKaaSync) { onKaaSyncResponse_ = onKaaSync; }
    void registerDisconnectReceiver(std::function<void (const DisconnectMessage&)> onDisconnect) { onDisconnect_ = onDisconnect; }
    void registerPingResponseReceiver(std::function<void ()> onPing) { onPingResp_ = onPing; }

    void flush() { parser_.resetParser(); }

private:
    std::function<void (const ConnackMessage&)> onConnack_;
    std::function<void (const KaaSyncResponse&)> onKaaSyncResponse_;
    std::function<void (const DisconnectMessage&)> onDisconnect_;
    std::function<void ()> onPingResp_;

    KaaTcpParser parser_;
    IKaaClientContext &context_;
};

}




#endif /* KAATCPRESPONSEPROCESSOR_HPP_ */
