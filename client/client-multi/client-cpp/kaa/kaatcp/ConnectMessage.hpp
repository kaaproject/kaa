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

#ifndef CONNECTMESSAGE_HPP_
#define CONNECTMESSAGE_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/IKaaTcpRequest.hpp"
#include "kaa/common/EndpointObjectHash.hpp"
#include <botan/botan.h>
#ifdef _WIN32
#include <Winsock2.h>
#else
#include <arpa/inet.h>
#endif

namespace kaa {

class ConnectMessage : public IKaaTcpRequest
{
public:
    template<class T, class U, class V>
    ConnectMessage(std::uint16_t timer,
            std::uint32_t nextProtocolId,
            const T& signature,
            const U& sessionKey,
            const V& payload) : message_(0)
    {
        char header[6];
        std::uint8_t size = KaaTcpCommon::createBasicHeader(
                (std::uint8_t) KaaTcpMessageType::MESSAGE_CONNECT,
                payload.size() + KaaTcpCommon::KAA_CONNECT_HEADER_LENGTH + sessionKey.size() + signature.size(), header);

        message_.resize(payload.size() + KaaTcpCommon::KAA_CONNECT_HEADER_LENGTH + sessionKey.size() + signature.size() + size);

        std::copy(reinterpret_cast<const std::uint8_t *>(header),
                reinterpret_cast<const std::uint8_t *>(header + size),
                message_.begin());

        auto messageIt = message_.begin() + size;

        std::uint16_t nameLengthNetworkOrder = htons(KaaTcpCommon::KAA_TCP_NAME_LENGTH);
        std::copy(reinterpret_cast<std::uint8_t *>(&nameLengthNetworkOrder), reinterpret_cast<std::uint8_t *>(&nameLengthNetworkOrder) + 2, messageIt);
        messageIt += sizeof(std::uint16_t);

        std::copy((const std::uint8_t * const ) KaaTcpCommon::KAA_TCP_NAME,
                (const std::uint8_t * const ) (KaaTcpCommon::KAA_TCP_NAME + KaaTcpCommon::KAA_TCP_NAME_LENGTH), messageIt);
        messageIt += KaaTcpCommon::KAA_TCP_NAME_LENGTH;

        *(messageIt++) = KaaTcpCommon::PROTOCOL_VERSION;
        *(messageIt++) = 0x02;

        std::uint32_t nextProtocolIdNetworkOrder = htonl(nextProtocolId);
        std::copy(reinterpret_cast<std::uint8_t *>(&nextProtocolIdNetworkOrder), reinterpret_cast<std::uint8_t *>(&nextProtocolIdNetworkOrder) + 4, messageIt);
        messageIt += sizeof(std::uint32_t);

        *(messageIt++) = sessionKey.size() > 0 ? KaaTcpCommon::KAA_CONNECT_SESSION_KEY_FLAGS : 0;
        *(messageIt++) = signature.size() > 0 ? KaaTcpCommon::KAA_CONNECT_SIGNATURE_FLAGS : 0;

        std::uint16_t timerNetworkOrder = htons(timer);
        std::copy(reinterpret_cast<std::uint8_t *>(&timerNetworkOrder), reinterpret_cast<std::uint8_t *>(&timerNetworkOrder) + 2, messageIt);
        messageIt += sizeof(std::uint16_t);

        std::copy(sessionKey.begin(), sessionKey.end(), messageIt);
        messageIt += sessionKey.size();

        std::copy(signature.begin(), signature.end(), messageIt);
        messageIt += signature.size();

        std::copy(payload.begin(), payload.end(), messageIt);
    }
    ~ConnectMessage() { }

    const std::vector<std::uint8_t>& getRawMessage() const { return message_; }

private:
    std::vector<std::uint8_t> message_;
};

}



#endif /* CONNECTMESSAGE_HPP_ */
