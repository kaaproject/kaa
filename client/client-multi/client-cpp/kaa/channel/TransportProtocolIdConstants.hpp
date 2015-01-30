/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef TRANSPORT_PROTOCOL_ID_CONSTANTS_HPP_
#define TRANSPORT_PROTOCOL_ID_CONSTANTS_HPP_

#include <cstdint>

#include "kaa/channel/TransportProtocolId.hpp"

namespace kaa {

/*
 * Represents transport protocol specific constants.
 */
class TransportProtocolIdConstants {
public:
   static const TransportProtocolId HTTP_TRANSPORT_ID;
   static const TransportProtocolId TCP_TRANSPORT_ID;

private:
   static const std::int32_t HTTP_TRANSPORT_PROTOCOL_ID      = 0xfb9a3cf0;
   static const std::int32_t HTTP_TRANSPORT_PROTOCOL_VERSION = 1;

   static const std::int32_t TCP_TRANSPORT_PROTOCOL_ID       = 0x56c8ff92;
   static const std::int32_t TCP_TRANSPORT_PROTOCOL_VERSION  = 1;
};

} /* namespace kaa */

#endif /* TRANSPORT_PROTOCOL_ID_CONSTANTS_HPP_ */
