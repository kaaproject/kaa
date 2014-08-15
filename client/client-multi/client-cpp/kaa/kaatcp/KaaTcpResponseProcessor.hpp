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

#ifndef KAATCPRESPONSEPROCESSOR_HPP_
#define KAATCPRESPONSEPROCESSOR_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/KaaSyncResponse.hpp"
#include "kaa/kaatcp/ConnackMessage.hpp"
#include "kaa/kaatcp/DisconnectMessage.hpp"
#include "kaa/kaatcp/KaaTcpParser.hpp"
#include <boost/function.hpp>

namespace kaa
{

class KaaTcpResponseProcessor
{
public:
    KaaTcpResponseProcessor() { }
    ~KaaTcpResponseProcessor() { }

    void processResponseBuffer(const char *buf, boost::uint32_t size);

    void registerConnackReceiver(boost::function<void (const ConnackMessage&)> onConnack) { onConnack_ = onConnack; }
    void registerKaaSyncReceiver(boost::function<void (const KaaSyncResponse&)> onKaaSync) { onKaaSyncResponse_ = onKaaSync; }
    void registerDisconnectReceiver(boost::function<void (const DisconnectMessage&)> onDisconnect) { onDisconnect_ = onDisconnect; }
    void registerPingResponseReceiver(boost::function<void ()> onPing) { onPingResp_ = onPing; }

    void flush() { parser_.resetParser(); }

private:
    boost::function<void (const ConnackMessage&)> onConnack_;
    boost::function<void (const KaaSyncResponse&)> onKaaSyncResponse_;
    boost::function<void (const DisconnectMessage&)> onDisconnect_;
    boost::function<void ()> onPingResp_;

    KaaTcpParser parser_;
};

}




#endif /* KAATCPRESPONSEPROCESSOR_HPP_ */
