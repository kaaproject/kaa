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

#ifndef IBOOTSTRAPMANAGER_HPP_
#define IBOOTSTRAPMANAGER_HPP_

#include <vector>
#include <string>
#include <cstdint>

#include "kaa/failover/IFailoverStrategy.hpp"

namespace kaa {

class IKaaChannelManager;
class IBootstrapTransport;
class TransportProtocolId;

/**
 * Bootstrap manager manages the list of available operation servers.
 */
class IBootstrapManager {
public:

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) = 0;

    /**
     * Receives the latest list of servers from the bootstrap service.
     */
    virtual void receiveOperationsServerList() = 0;

    /**
     * Notifies Channel manager about new server meets given parameters.
     *
     * @param type the channel's type (i.e. HTTP channel, HTTP long poll channel, etc.).
     * @see ChannelType
     *
     * @param failoverReason The reason why failover occured.
     */
    virtual void onOperationsServerFailed(const TransportProtocolId& protocolId, KaaFailoverReason failoverReason) = 0;

    /**
     * Update the Channel Manager with endpoint's properties retrieved by its DNS.
     *
     * @param name endpoint's DNS.
     *
     */
    virtual void useNextOperationsServerByAccessPointId(std::int32_t id) = 0;

    /**
     * Sets bootstrap transport object.
     *
     * @param transport object to be set.
     * @see IBootstrapTransport
     *
     */
    virtual void setTransport(IBootstrapTransport* transport) = 0;

    /**
     * Sets Channel manager.
     *
     * @param manager the channel manager to be set.
     * @see IKaaChannelManager
     *
     */
    virtual void setChannelManager(IKaaChannelManager* manager) = 0;

    /**
     * Updates the operation server list.
     *
     * @param list the operation server list.
     * @see OperationsServerList
     *
     */
    virtual void onServerListUpdated(const std::vector<ProtocolMetaData>& operationsServers) = 0;


    virtual ~IBootstrapManager() { }
};

}



#endif /* IBOOTSTRAPMANAGER_HPP_ */
