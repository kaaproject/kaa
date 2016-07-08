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
