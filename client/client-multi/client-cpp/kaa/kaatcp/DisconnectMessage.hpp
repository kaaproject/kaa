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

#ifndef DISCONNECTMESSAGE_HPP_
#define DISCONNECTMESSAGE_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/IKaaTcpRequest.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <boost/format.hpp>

namespace kaa
{

enum class DisconnectReason : std::uint8_t
{
    NONE                = 0x00,
    BAD_REQUEST         = 0x01,
    INTERNAL_ERROR      = 0x02,
    CREDENTIALS_REVOKED = 0x03,
};

class DisconnectMessage : public IKaaTcpRequest
{
public:
    static std::string reasonToString(DisconnectReason reason)
    {
        switch (reason) {
            case DisconnectReason::NONE:
                return "No error";
            case DisconnectReason::BAD_REQUEST:
                return "Bad request";
            case DisconnectReason::INTERNAL_ERROR:
                return "Internal error has been occurred";
            case DisconnectReason::CREDENTIALS_REVOKED:
                return "Credentials have been revoked";
            default:
                return (boost::format("Invalid Disconnect reason %1%") % (std::uint8_t) reason).str();
        }
    }

    DisconnectMessage(DisconnectReason reason) : message_(4), reason_(reason)
    {
        char header[2];
        KaaTcpCommon::createBasicHeader((std::uint8_t) KaaTcpMessageType::MESSAGE_DISCONNECT, 2, header);
        std::copy(reinterpret_cast<const std::uint8_t *>(header),
                reinterpret_cast<const std::uint8_t *>(header + 2),
                message_.begin());
        message_[2] = 0;
        message_[3] = (std::uint8_t) reason_;
    }

    DisconnectMessage(const char *payload, std::uint16_t size)
    {
        parseMessage(payload, size);
    }

    ~DisconnectMessage() { }

    DisconnectReason getReason() const { return reason_; }
    std::string getMessage() const { return reasonToString(reason_); }

    const std::vector<std::uint8_t>& getRawMessage() const { return message_; }

private:
    void parseMessage(const char *payload, std::uint16_t size)
    {
        if (!payload || !size) {
            throw KaaException("Bad Disconnect payload data");
        }

        int code = *(payload + 1);
        if (code < (int)DisconnectReason::NONE || code > (int)DisconnectReason::CREDENTIALS_REVOKED) {
            throw KaaException(boost::format("Bad Disconnect return code: %1%") % code);
        }
        reason_ = (DisconnectReason) code;
    }

private:
    std::vector<std::uint8_t> message_;
    DisconnectReason reason_;

};

}



#endif /* DISCONNECTREQUEST_HPP_ */
