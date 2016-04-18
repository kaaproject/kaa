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

#include "kaa/channel/impl/DefaultOperationHttpChannel.hpp"

#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL

namespace kaa {

const std::string DefaultOperationHttpChannel::CHANNEL_ID = "default_operation_http_channel";
const std::map<TransportType, ChannelDirection> DefaultOperationHttpChannel::SUPPORTED_TYPES =
        {
                { TransportType:: EVENT, ChannelDirection::UP },
                { TransportType:: LOGGING, ChannelDirection::UP },
        };

}

#endif
