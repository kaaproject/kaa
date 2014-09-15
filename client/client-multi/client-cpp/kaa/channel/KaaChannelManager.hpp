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

#ifndef KAACHANNELMANAGER_HPP_
#define KAACHANNELMANAGER_HPP_

#include <map>
#include <set>
#include <list>

#include "kaa/KaaDefaults.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"

namespace kaa {

class IBootstrapManager;

class KaaChannelManager: public IKaaChannelManager, public IPingServerStorage {
public:
    KaaChannelManager(IBootstrapManager& manager, const BootstrapServers& servers);

    virtual void addChannel(IDataChannelPtr channel);
    virtual void removeChannel(IDataChannelPtr channel);

    virtual std::list<IDataChannelPtr> getChannels();
    virtual std::list<IDataChannelPtr> getChannelsByType(ChannelType type);

    virtual IDataChannelPtr getChannelByTransportType(TransportType type);
    virtual IDataChannelPtr getChannel(const std::string& channelId);

    virtual void onServerUpdated(IServerInfoPtr server);
    virtual void onServerFailed(IServerInfoPtr server);

    virtual void clearChannelList();

    virtual IServerInfoPtr getPingServer() {
        //FIXME: propose more proper way to get current ping-able server
        return (*lastServers_.begin()).second;
    }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker);

private:
    void useNewChannel(IDataChannelPtr channel);
    void useNewChannelForType(TransportType type);

    IServerInfoPtr getCurrentBootstrapServer(ChannelType type);
    IServerInfoPtr getNextBootstrapServer(IServerInfoPtr currentServer);

private:
    IBootstrapManager&   bootstrapManager_;

    std::map<ChannelType, std::list<IServerInfoPtr>> bootstrapServers_;

    std::map<ChannelType, IServerInfoPtr>    lastServers_;
    std::map<ChannelType, IServerInfoPtr>    lastBSServers_;

    std::set<IDataChannelPtr>                   channels_;
    std::map<TransportType, IDataChannelPtr>    mappedChannels_;

    ConnectivityCheckerPtr connectivityChecker_;
};

} /* namespace kaa */

#endif /* KAACHANNELMANAGER_HPP_ */
