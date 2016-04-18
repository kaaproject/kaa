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

#include "kaa/channel/TransportProtocolIdConstants.hpp"

namespace kaa {

const std::int32_t TransportProtocolIdConstants::HTTP_TRANSPORT_PROTOCOL_ID      = 0xfb9a3cf0;
const std::int32_t TransportProtocolIdConstants::HTTP_TRANSPORT_PROTOCOL_VERSION = 1;

const std::int32_t TransportProtocolIdConstants::TCP_TRANSPORT_PROTOCOL_ID       = 0x56c8ff92;
const std::int32_t TransportProtocolIdConstants::TCP_TRANSPORT_PROTOCOL_VERSION  = 1;

const TransportProtocolId TransportProtocolIdConstants::HTTP_TRANSPORT_ID(
                    HTTP_TRANSPORT_PROTOCOL_ID, HTTP_TRANSPORT_PROTOCOL_VERSION);

const TransportProtocolId TransportProtocolIdConstants::TCP_TRANSPORT_ID(
                    TCP_TRANSPORT_PROTOCOL_ID, TCP_TRANSPORT_PROTOCOL_VERSION);


} /* namespace kaa */
