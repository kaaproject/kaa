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

#include "kaa/kaatcp/ConnackMessage.hpp"
#include <boost/format.hpp>

namespace kaa {

ConnackMessage::ConnackMessage(const char *payload, std::uint16_t size)
        : returnCode_(ConnackReturnCode::UNKNOWN)
{
    parseMessage(payload, size);
}

std::string ConnackMessage::returnCodeToString(ConnackReturnCode code)
{
    switch (code) {
    case ConnackReturnCode::UNKNOWN:
        return "Connack Unknown";
    case ConnackReturnCode::SUCCESS:
        return "Connection Accepted";
    case ConnackReturnCode::UNACCEPTABLE_VERSION:
        return "Connection Refused: unacceptable protocol version";
    case ConnackReturnCode::IDENTIFIER_REJECTED:
        return "Connection Refused: identifier rejected";
    case ConnackReturnCode::SERVER_UNAVAILABLE:
        return "Connection Refused: server unavailable";
    case ConnackReturnCode::BAD_USER_PASSWORD:
        return "Connection Refused: bad user name or password";
    case ConnackReturnCode::NOT_AUTHORIZED:
        return "Connection Refused: not authorized";
    default:
        return (boost::format("Invalid response code %1%") % (std::uint8_t) code).str();
    }
}

std::string ConnackMessage::getMessage() const
{
    return returnCodeToString(returnCode_);
}

void ConnackMessage::parseMessage(const char *payload, std::uint16_t size)
{
    returnCode_ = (ConnackReturnCode) *(payload + 1);
}

}

