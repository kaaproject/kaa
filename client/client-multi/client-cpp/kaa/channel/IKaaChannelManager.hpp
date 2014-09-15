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

#ifndef IKAACHANNELMANAGER_HPP_
#define IKAACHANNELMANAGER_HPP_

#include <list>

#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/channel/IDataChannel.hpp"
#include "kaa/channel/server/IServerInfo.hpp"
#include "kaa/channel/connectivity/IConnectivityChecker.hpp"

namespace kaa {

class IIServerInfo;

/**
 * Channel manager establishes/removes channels' links between client and server.
 */
class IKaaChannelManager {
public:

    /**
     * Updates the manager by adding the channel.
     *
     * @param channel channel to be added.
     * @see IDataChannel
     *
     */
    virtual void addChannel(IDataChannelPtr channel) = 0;

    /**
     * Updates the manager by removing the channel from the manager.
     *
     * @param channel channel to be removed.
     * @see IDataChannel
     *
     */
    virtual void removeChannel(IDataChannelPtr channel) = 0;

    /**
     * Retrieves the list of current channels.
     *
     * @return the channels' list.
     * @see IDataChannel
     *
     */
    virtual std::list<IDataChannelPtr> getChannels() = 0;

    /**
     * Retrieves the list of channels by the specific type (HTTP, HTTP_LP,
     * BOOTSTRAP and etc.).
     *
     * @param type type of the channel.
     * @return the channels' list.
     *
     * @see ChannelType
     * @see IDataChannel
     *
     */
    virtual std::list<IDataChannelPtr> getChannelsByType(ChannelType type) = 0;

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
     * @see IServerInfo
     *
     */
    virtual void onServerFailed(IServerInfoPtr server) = 0;

    /**
     * Reports to Channel Manager about the new server.
     *
     * @param newServer the parameters of the new server.
     * @see IServerInfo
     *
     */
    virtual void onServerUpdated(IServerInfoPtr newServer) = 0;

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

    virtual ~IKaaChannelManager() {}
};

}  // namespace kaa


#endif /* IKAACHANNELMANAGER_HPP_ */
