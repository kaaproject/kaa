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

#ifndef KAASYNCRESPONSE_HPP_
#define KAASYNCRESPONSE_HPP_

#include "kaa/kaatcp/KaaTcpCommon.hpp"
#include <vector>

namespace kaa {


class KaaSyncResponse
{
public:
    KaaSyncResponse(const char * payload, std::uint32_t size);

    const std::vector<std::uint8_t>& getPayload() const { return payload_; }
    bool isZipped() const { return isZipped_; }
    bool isEncrypted() const { return isEncrypted_; }
    std::uint16_t getMessageId() const { return messageId_; }

private:
    void parseMessage(const char * payload, std::uint32_t size);

private:
    bool isZipped_;
    bool isEncrypted_;
    std::uint16_t messageId_;

    std::vector<std::uint8_t> payload_;
};

}



#endif /* KAASYNCRESPONSE_HPP_ */
