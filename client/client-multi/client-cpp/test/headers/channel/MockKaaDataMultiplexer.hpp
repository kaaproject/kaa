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

#ifndef MOCKKAADATAMULTIPLEXER_HPP_
#define MOCKKAADATAMULTIPLEXER_HPP_

#include <cstddef>
#include <cstdint>
#include <vector>

#include "kaa/channel/IKaaDataMultiplexer.hpp"

namespace kaa {

class MockKaaDataMultiplexer : public IKaaDataMultiplexer {
public:
    virtual std::vector<std::uint8_t> compileRequest(const std::map<TransportType, ChannelDirection>& transportTypes) {
        ++onCompileRequest_;
        return compiledData_;
    }

public:
    std::size_t onCompileRequest_ = 0;
    std::vector<std::uint8_t> compiledData_;

};

} /* namespace kaa */

#endif /* MOCKKAADATAMULTIPLEXER_HPP_ */
