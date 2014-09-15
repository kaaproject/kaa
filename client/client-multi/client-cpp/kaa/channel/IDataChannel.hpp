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

#ifndef IDATACHANNEL_HPP_
#define IDATACHANNEL_HPP_

#include <boost/shared_ptr.hpp>

#include <vector>
#include <map>

#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/ChannelDirection.hpp"
#include "kaa/channel/server/IServerInfo.hpp"
#include "kaa/channel/IKaaDataMultiplexer.hpp"
#include "kaa/channel/IKaaDataDemultiplexer.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/channel/server/IServerInfo.hpp"
#include "kaa/channel/ServerType.hpp"
#include "kaa/channel/connectivity/IConnectivityChecker.hpp"

namespace kaa {

class IPingServerStorage;

/**
 * Channel is responsible for sending/receiving data to/from the endpoint server.
 */
class IDataChannel {
public:

    /**
     * Updates the channel's state of the specific service.
     *
     * @param type transport type of the service.
     * @see TransportType
     *
     */
    virtual void sync(TransportType type) = 0;

    /**
     * Updates the channel's state of all supported services.
     */
    virtual void syncAll() = 0;

    /**
     * Updates the channel's state of all supported services.
     */
    virtual void syncAck(TransportType type) = 0;

    /**
     * Retrieves the channel's id.
     * It should be unique in existing channels scope.
     *
     * @return the channel's id.
     *
     */
    virtual const std::string& getId() const  = 0;

    /**
     * Retrieves the channel's type (i.e. HTTP, TCP, etc.).
     *
     * @return the channel's type.
     * @see ChannelType
     *
     */
    virtual ChannelType getChannelType() const  = 0;

    /**
     * Retrieves the channel's server type (i.e. BOOTSTRAP or OPERATIONS).
     *
     * @return the channel's type.
     * @see ServerType
     *
     */
    virtual ServerType getServerType() const  = 0;

    /**
     * Sets the response demultiplexer for this channel.
     *
     * @param demultiplexer demultiplexer instance to be set.
     * @see IKaaDataDemultiplexer
     *
     */
    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer) = 0;

    /**
     * Sets the request multiplexer for this channel.
     *
     * @param multiplexer multiplexer instance to be set.
     * @see IKaaDataMultiplexer
     *
     */
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer) = 0;

    /**
     * Sets the server's parameters for the current channel.
     *
     * @param server server's parameters.
     * @see IServerInfo
     *
     */
    virtual void setServer(IServerInfoPtr server) = 0;

    /**
     * Retrieves the map of transport types and their directions supported by this channel.
     *
     * @return the map of transport types.
     * @see TransportType
     * @see ChannelDirection
     *
     */
    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const = 0;

    /**
     * Sets connectivity checker to the current channel.
     *
     * @param checker platform-dependent connectivity checker.
     * @see IConnectivityChecker
     *
     */
    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) = 0;

    virtual ~IDataChannel() {}

};

typedef IDataChannel* IDataChannelPtr;

}  // namespace kaa


#endif /* IDATACHANNEL_HPP_ */
