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

#ifndef PINGREQUEST_HPP_
#define PINGREQUEST_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include "kaa/kaatcp/IKaaTcpRequest.hpp"

namespace kaa
{

class PingRequest : public IKaaTcpRequest
{
public:
    PingRequest() : message_(2)
    {
        char header[2];
        KaaTcpCommon::createBasicHeader((std::uint8_t) KaaTcpMessageType::MESSAGE_PINGREQ, 0, header);
        std::copy(reinterpret_cast<const std::uint8_t *>(header),
                reinterpret_cast<const std::uint8_t *>(header + 2),
                message_.begin());
    }
    ~PingRequest() { }

    const std::vector<std::uint8_t>& getRawMessage() const { return message_; }

private:
    std::vector<std::uint8_t> message_;

};

}


#endif /* PINGREQUEST_HPP_ */
