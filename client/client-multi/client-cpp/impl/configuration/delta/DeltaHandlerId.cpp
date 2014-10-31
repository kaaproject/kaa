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

#include "kaa/configuration/delta/DeltaHandlerId.hpp"

#ifdef KAA_USE_CONFIGURATION

namespace kaa {

DeltaHandlerId::DeltaHandlerId(const uuid_t& uuid)
{
    /*
     * For more details about UUID format, please, see RFC 4122
     */
    if (uuid.version() == boost::uuids::uuid::version_time_based) {
        std::uint16_t node = 0;
        std::uint64_t timestamp = 0;

        for (int i = 0; i < 6; ++i) {
            /* time_low + time_mid*/
            timestamp = (timestamp << 8) | (uuid.data[i] & 0xFF);
        }

        for (int i = 14; i < 16; ++i) {
            /* Last two byte of node */
            node = (node << 8) | (uuid.data[i] & 0xFF);
        }

        handlerId_ = (timestamp << 16) | node;
    } else {
        std::uint64_t high = 0, low = 0;

        for (int i = 0; i < 8; ++i) {
            high = (high << 8) | (uuid.data[i] & 0xFF);
            low = (low << 8) | (uuid.data[i + 8] & 0xFF);
        }

        handlerId_ = (high << 32) | (low & 0xFFFFFFFF);
    }
}

}

#endif
