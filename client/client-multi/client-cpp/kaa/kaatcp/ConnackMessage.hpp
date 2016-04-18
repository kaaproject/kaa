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

#ifndef CONNACKMESSAGE_HPP_
#define CONNACKMESSAGE_HPP_

#include <cstdint>
#include <string>

namespace kaa {

enum class ConnackReturnCode : std::uint8_t
{
    UNKNOWN                    = 0x00,
    ACCEPTED                   = 0x01,
    REFUSE_BAD_PROTOCOL        = 0x02,
    REFUSE_ID_REJECT           = 0x03,
    REFUSE_SERVER_UNAVAILABLE  = 0x04,
    REFUSE_BAD_CREDENTIALS     = 0x05,
    REFUSE_NO_AUTH             = 0x06,
    REFUSE_VERIFICATION_FAILED = 0x10,
};

class ConnackMessage
{
public:
    ConnackMessage(const char *payload, std::uint16_t size);
    ~ConnackMessage() { }

    static std::string returnCodeToString(ConnackReturnCode code);

    std::string getMessage() const;
    ConnackReturnCode getReturnCode() const { return returnCode_; }

private:
    void parseMessage(const char *payload, std::uint16_t size);

private:
    ConnackReturnCode returnCode_;
};

}



#endif /* CONNACKMESSAGE_HPP_ */
