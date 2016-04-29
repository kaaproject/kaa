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

#ifndef IKAADATAMULTIPLEXER_HPP_
#define IKAADATAMULTIPLEXER_HPP_

#include <cstdint>
#include <map>
#include <vector>
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/ChannelDirection.hpp"

namespace kaa {

/**
 * Multiplexer collects the info about states from different
 * services and compiles it in one request.
 *
 * Required in user implementation of any kind of data channel.
 *
 */
class IKaaDataMultiplexer {
public:

    /**
     * Compiles request for given transport types.
     *
     * @param types map of types to be polled.
     * @return the serialized request data.
     *
     * @see TransportType
     * @see ChannelDirection
     *
     */
    virtual std::vector<std::uint8_t> compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes) = 0;

    virtual ~IKaaDataMultiplexer() {}
};

}  // namespace kaa


#endif /* IKAADATAMULTIPLEXER_HPP_ */
