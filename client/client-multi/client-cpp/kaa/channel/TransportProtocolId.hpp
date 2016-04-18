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

#ifndef TRANSPORT_PROTOCOL_ID_HPP_
#define TRANSPORT_PROTOCOL_ID_HPP_

#include <cstdint>

#include "kaa/gen/EndpointGen.hpp"

namespace kaa {

/*
 * An identifier used to uniquely represent a transport protocol.
 */
class TransportProtocolId
{
public:
    TransportProtocolId(const std::int32_t& id, const std::int32_t& version)
        : id_(id), version_(version) {}

    TransportProtocolId(const ProtocolVersionPair& protocolId)
        : id_(protocolId.id), version_(protocolId.version) {}

    std::int32_t getId() const {
        return id_;
    }

    std::int32_t getVersion() const {
        return version_;
    }

    bool operator==(const TransportProtocolId& protocolId) const {
        return (id_ == protocolId.id_ && version_ == protocolId.version_);
    }

    bool operator!=(const TransportProtocolId& protocolId) const {
        return !(*this == protocolId);
    }

    bool operator<(const TransportProtocolId& protocolId) const {
        return (hashCode() < protocolId.hashCode());
    }

    bool operator>(const TransportProtocolId& protocolId) const {
        return (hashCode() > protocolId.hashCode());
    }

private:
    std::uint32_t hashCode() const {
        const std::uint32_t prime = 31;

        std::uint32_t hashCode = 1;
        hashCode = prime * hashCode + id_;
        hashCode = prime * hashCode + version_;

        return hashCode;
    }

private:
    std::int32_t id_;
    std::int32_t version_;
};

} /* namespace kaa */

#endif /* TRANSPORT_PROTOCOL_ID_HPP_ */
