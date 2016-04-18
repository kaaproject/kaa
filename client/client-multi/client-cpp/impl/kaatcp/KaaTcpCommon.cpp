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

#include "kaa/kaatcp/KaaTcpCommon.hpp"

namespace kaa {

const char * const KaaTcpCommon::KAA_TCP_NAME = "Kaatcp";

std::uint8_t KaaTcpCommon::createBasicHeader(std::uint8_t messageType, std::uint32_t length, char *message)
{
    if (length <= MAX_MESSAGE_LENGTH && messageType <= MAX_MESSAGE_TYPE_LENGTH) {
        std::uint8_t size = 1;
        *(message++) = (char) (messageType << 4);
        do {
            std::uint8_t byte = length % FIRST_BIT;
            length /= FIRST_BIT;
            if (length) {
                byte |= FIRST_BIT;
            }
            *(message++) = (char) byte;
            ++size;
        } while (length);

        return size;
    }
    return 0;
}

}



