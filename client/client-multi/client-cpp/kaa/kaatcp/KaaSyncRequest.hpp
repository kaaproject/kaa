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

#ifndef KAASYNCREQUEST_HPP_
#define KAASYNCREQUEST_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/IKaaTcpRequest.hpp"
#ifdef _WIN32
#include <Winsock2.h>
#else
#include <arpa/inet.h>
#endif


namespace kaa {

enum class KaaSyncMessageType : std::uint8_t {
    UNUSED      = 0x0,
    SYNC        = 0x1,
    BOOTSTRAP   = 0x2
};

class KaaSyncRequest : public IKaaTcpRequest
{
public:
    template<class T>
    KaaSyncRequest(bool zipped, bool encrypted, std::uint16_t messageId, const T& payload, KaaSyncMessageType messageType) : message_(0)
    {
        char header[6];
        std::uint8_t size = KaaTcpCommon::createBasicHeader(
                (std::uint8_t) KaaTcpMessageType::MESSAGE_KAASYNC,
                payload.size() + KaaTcpCommon::KAA_SYNC_HEADER_LENGTH, header);

        message_.resize(payload.size() + KaaTcpCommon::KAA_SYNC_HEADER_LENGTH + size);

        std::copy(reinterpret_cast<const std::uint8_t *>(header),
                reinterpret_cast<const std::uint8_t *>(header + size),
                message_.begin());

        auto messageIt = message_.begin() + size;

        std::uint16_t nameLengthNetworkOrder = htons(KaaTcpCommon::KAA_TCP_NAME_LENGTH);
        std::copy(reinterpret_cast<std::uint8_t *>(&nameLengthNetworkOrder), reinterpret_cast<std::uint8_t *>(&nameLengthNetworkOrder) + 2, messageIt);
        messageIt += 2;

        std::copy((const std::uint8_t * const ) KaaTcpCommon::KAA_TCP_NAME,
                (const std::uint8_t * const ) (KaaTcpCommon::KAA_TCP_NAME + KaaTcpCommon::KAA_TCP_NAME_LENGTH), messageIt);
        messageIt += KaaTcpCommon::KAA_TCP_NAME_LENGTH;

        *(messageIt++) = KaaTcpCommon::PROTOCOL_VERSION;

        std::uint16_t messageIdNetworkOrder = htons(messageId);
        std::copy(reinterpret_cast<std::uint8_t *>(&messageIdNetworkOrder), reinterpret_cast<std::uint8_t *>(&messageIdNetworkOrder) + 2, messageIt);
        messageIt += 2;

        *messageIt |= (((std::uint8_t)messageType) << 4);

        *messageIt |= KaaTcpCommon::KAA_SYNC_REQUEST_BIT;
        if (zipped) {
            *messageIt |= KaaTcpCommon::KAA_SYNC_ZIPPED_BIT;
        }
        if (encrypted) {
            *messageIt |= KaaTcpCommon::KAA_SYNC_ENCRYPTED_BIT;
        }
        ++messageIt;

        std::copy(payload.begin(), payload.end(), messageIt);
    }

    ~KaaSyncRequest() { }

    const std::vector<std::uint8_t>& getRawMessage() const { return message_; }

private:
    std::vector<std::uint8_t> message_;

    static const std::uint8_t KAA_SYNC_MESSAGE_TYPE_SYNC      = 0x01;
    static const std::uint8_t KAA_SYNC_MESSAGE_TYPE_BOOTSTRAP = 0x02;
};

}


#endif /* KAASYNCREQUEST_HPP_ */
