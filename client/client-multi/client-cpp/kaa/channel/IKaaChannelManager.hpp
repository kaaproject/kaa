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

#ifndef IKAACHANNELMANAGER_HPP_
#define IKAACHANNELMANAGER_HPP_

#include <list>
#include <string>

#include "kaa/channel/IDataChannel.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"
#include "kaa/channel/connectivity/IConnectivityChecker.hpp"
#include "kaa/EndpointConnectionInfo.hpp"

namespace kaa {

class IIServerInfo;

/**
 * Channel manager establishes/removes channels' links between client and server.
 */
class IKaaChannelManager
{
public:

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) = 0;

    /**
     * Updates the manager by setting the channel to the specified transport type.
     *
     * @param type the type of the transport which is going to receive updates using the specified channel.
     * @param channel the channel to be added.
     * @see IDataChannel
     *
     */
    virtual void setChannel(TransportType type, IDataChannelPtr channel) = 0;

    /**
     * Updates the manager by adding the channel.
     *
     * @param channel the channel to be added.
     * @see IDataChannel
     *
     */
    virtual void addChannel(IDataChannelPtr channel) = 0;

    /**
     * Updates the manager by removing the channel from the manager.
     *
     * @param id the channel's id.
     * @see KaaDataChannel
     *
     */
    virtual void removeChannel(const std::string& id) = 0;

    /**
     * Updates the manager by removing the channel from the manager.
     *
     * @param channel the channel to be removed.
     * @see IDataChannel
     *
     */
    virtual void removeChannel(IDataChannelPtr channel) = 0;

    /**
     * Retrieves a list of current channels.
     *
     * @return a list of channels.
     * @see IDataChannel
     *
     */
    virtual std::list<IDataChannelPtr> getChannels() = 0;

    /**
     * Retrieves the list of channels by the specific transport type.
     *
     * @param type the transport's type.
     * @return the channels' list.
     *
     * @see TransportType
     * @see IDataChannel
     *
     */
    virtual IDataChannelPtr getChannelByTransportType(TransportType type) = 0;

    /**
     * Retrieves channel by the unique channel id.
     *
     * @param id the channel's id.
     * @return channel object.
     *
     * @see IDataChannel
     *
     */
    virtual IDataChannelPtr getChannel(const std::string& channelId) = 0;

    /**
     * Reports to Channel Manager in case link with server was not established.
     *
     * @param server the parameters of server that was not connected.
     * @see ITransportConnectionInfo
     *
     * @param reason The reason which caused failure.
     * @see KaaFailoverReason
     *
     */
    virtual void onServerFailed(ITransportConnectionInfoPtr connectionInfo, KaaFailoverReason reason) = 0;

    /**
     * Reports to Channel Manager about successful connection.
     *
     * @param connection connection metadata.
     */
    virtual void onConnected(const EndpointConnectionInfo& connection) = 0;

    /**
     * Reports to Channel Manager about the new server.
     *
     * @param newServer the parameters of the new server.
     * @see ITransportConnectionInfo
     *
     */
    virtual void onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr connectionInfo) = 0;

    /**
     * Clears the list of channels.
     */
    virtual void clearChannelList() = 0;

    /**
     * Sets connectivity checker to the current channel.
     *
     * @param checker platform-dependent connectivity checker.
     * @see IConnectivityChecker
     *
     */
    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) = 0;

    /**
     * Shuts down the manager and all registered channels. The instance can no longer be used.
     *
     */
    virtual void shutdown() = 0;

    /**
     * Pauses all active channels.
     *
     */
    virtual void pause() = 0;

    /**
     * Restores channels' activity.
     *
     */
    virtual void resume() = 0;

    virtual ~IKaaChannelManager() {}
};

typedef IKaaChannelManager* IKaaChannelManagerPtr;

}  // namespace kaa


#endif /* IKAACHANNELMANAGER_HPP_ */
