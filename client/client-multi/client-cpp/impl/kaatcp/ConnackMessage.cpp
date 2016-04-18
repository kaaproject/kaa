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

#include "kaa/kaatcp/ConnackMessage.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <boost/format.hpp>

namespace kaa {

ConnackMessage::ConnackMessage(const char *payload, std::uint16_t size):
                               returnCode_(ConnackReturnCode::UNKNOWN)
{
    parseMessage(payload, size);
}

std::string ConnackMessage::returnCodeToString(ConnackReturnCode code)
{
    switch (code) {
        case ConnackReturnCode::UNKNOWN:
            return "Connack Unknown";
        case ConnackReturnCode::ACCEPTED:
            return "Connection Accepted";
        case ConnackReturnCode::REFUSE_BAD_PROTOCOL:
            return "Connection Refused: unacceptable protocol version";
        case ConnackReturnCode::REFUSE_ID_REJECT:
            return "Connection Refused: identifier rejected";
        case ConnackReturnCode::REFUSE_SERVER_UNAVAILABLE:
            return "Connection Refused: server unavailable";
        case ConnackReturnCode::REFUSE_BAD_CREDENTIALS:
            return "Connection Refused: invalid authentication parameters";
        case ConnackReturnCode::REFUSE_NO_AUTH:
            return "Connection Refused: not authorized";
        case ConnackReturnCode::REFUSE_VERIFICATION_FAILED:
            return "Connection Refused: verification failed";

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
    if (!payload || !size) {
        throw KaaException("Bad Connack payload data");
    }

    int code = *(payload + 1);
    if (code < (int)ConnackReturnCode::UNKNOWN || code > (int)ConnackReturnCode::REFUSE_VERIFICATION_FAILED) {
        throw KaaException(boost::format("Bad Connack return code: %1%") % code);
    }
    returnCode_ = (ConnackReturnCode) code;
}

}


