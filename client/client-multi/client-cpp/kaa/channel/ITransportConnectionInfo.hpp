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

#ifndef I_TRANSPORT_CONNECTION_INFO_HPP_
#define I_TRANSPORT_CONNECTION_INFO_HPP_

#include <cstdint>
#include <memory>
#include <vector>

#include "kaa/channel/ServerType.hpp"
#include "kaa/channel/TransportProtocolId.hpp"

namespace kaa {

/**
 * Interface to represent a server connection information.
 *
 * Used by @link IKaaDataChannel @endlink and @link IKaaChannelManager @endlink.
 */
class ITransportConnectionInfo
{
public:

    /**
     * Retrieves the channel's server type (i.e. OPERATIONS or BOOTSTRAP).
     *
     * @return The channel's server type.
     * @see ServerType
     *
     */
    virtual ServerType getServerType() const = 0;

    /**
     * Retrieves the access point id (operations/bootstrap service id).
     *
     * @return The access point id.
     */
    virtual std::int32_t getAccessPointId() const = 0;

    /**
     * Retrieves the @link TransportProtocolId @endlink.
     *
     * @return The transport protocol id.
     * @see TransportProtocolId
     *
     */
    virtual TransportProtocolId getTransportId() const = 0;

    /**
     * Retrieves serialized connection data.
     *
     * Serialization may be specific for each transport protocol implementation.
     *
     * @return The serialized connection data.
     */
    virtual const std::vector<std::uint8_t>& getConnectionInfo() const = 0;

    virtual bool isFailedState() const = 0;

    virtual void setFailedState() = 0;

    virtual void resetFailedState() = 0;

    virtual ~ITransportConnectionInfo() {}
};

typedef std::shared_ptr<ITransportConnectionInfo> ITransportConnectionInfoPtr;

} /* namespace kaa */

#endif /* I_TRANSPORT_CONNECTION_INFO_HPP_ */
