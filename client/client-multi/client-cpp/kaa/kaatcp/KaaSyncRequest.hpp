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

#ifndef KAASYNCREQUEST_HPP_
#define KAASYNCREQUEST_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/IKaaTcpRequest.hpp"


namespace kaa {

class KaaSyncRequest : public IKaaTcpRequest
{
public:
    template<class T>
    KaaSyncRequest(bool zipped, bool encrypted, boost::uint16_t messageId, const T& payload) : message_(0)
    {
        char header[6];
        boost::uint8_t size = KaaTcpCommon::createBasicHeader(
                (boost::uint8_t) KaaTcpMessageType::MESSAGE_KAASYNC,
                payload.size() + KaaTcpCommon::KAA_SYNC_HEADER_LENGTH, header);

        message_.resize(payload.size() + KaaTcpCommon::KAA_SYNC_HEADER_LENGTH + size);

        std::copy(reinterpret_cast<const boost::uint8_t *>(header),
                reinterpret_cast<const boost::uint8_t *>(header + size),
                message_.begin());

        auto messageIt = message_.begin() + size;

        *(messageIt++) = (boost::uint8_t) (KaaTcpCommon::KAA_TCP_NAME_LENGTH >> 8);
        *(messageIt++) = (boost::uint8_t) (KaaTcpCommon::KAA_TCP_NAME_LENGTH & 0xFF);

        std::copy((const boost::uint8_t * const ) KaaTcpCommon::KAA_TCP_NAME,
                (const boost::uint8_t * const ) (KaaTcpCommon::KAA_TCP_NAME + KaaTcpCommon::KAA_TCP_NAME_LENGTH), messageIt);
        messageIt += KaaTcpCommon::KAA_TCP_NAME_LENGTH;

        *(messageIt++) = KaaTcpCommon::PROTOCOL_VERSION;
        *(messageIt++) = (boost::uint8_t) (messageId >> 8);
        *(messageIt++) = (boost::uint8_t) (messageId & 0xFF);

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

    const std::vector<boost::uint8_t>& getRawMessage() const { return message_; }

private:
    std::vector<boost::uint8_t> message_;
};

}


#endif /* KAASYNCREQUEST_HPP_ */
