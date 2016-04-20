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

#ifndef IDATACHANNEL_HPP_
#define IDATACHANNEL_HPP_

#include <vector>
#include <map>

#include "kaa/failover/IFailoverStrategy.hpp"
#include "kaa/channel/ServerType.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/ChannelDirection.hpp"
#include "kaa/channel/IKaaDataMultiplexer.hpp"
#include "kaa/channel/IKaaDataDemultiplexer.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"
#include "kaa/channel/connectivity/IConnectivityChecker.hpp"

namespace kaa {

class IPingServerStorage;

/**
 * Channel is responsible for sending/receiving data to/from the endpoint server.
 */
class IDataChannel {
public:


    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) = 0;

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
     * Retrieves the @link TransportProtocolId @endlink.
     *
     * @return the transport protocol id.
     * @see TransportProtocolId
     *
     */
    virtual TransportProtocolId getTransportProtocolId() const  = 0;

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
     * Sets the connection data for the current channel.
     *
     * @param connectionInfo The server's connection data.
     * @see ITransportConnectionInfo
     *
     */
    virtual void setServer(ITransportConnectionInfoPtr connectionInfo) = 0;

    /**
     * Retrieves current used server.
     *
     * @return Server info.
     * @see IServerInfo
     *
     */
    virtual ITransportConnectionInfoPtr getServer() = 0;

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

    /**
     * Shuts down the channel instance. All connections and threads should be terminated. The instance can no longer be used.
     *
     */
    virtual void shutdown() = 0;

    /**
     * Pauses the channel's workflow. The channel should stop all network activity.
     *
     */
    virtual void pause() = 0;

    /**
     * Resumes the channel's workflow. The channel should restore the previous connection.
     *
     */
    virtual void resume() = 0;

    virtual ~IDataChannel() {}

};

typedef IDataChannel* IDataChannelPtr;

}  // namespace kaa


#endif /* IDATACHANNEL_HPP_ */
