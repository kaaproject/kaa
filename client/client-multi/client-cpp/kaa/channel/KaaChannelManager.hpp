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

#include "kaa/KaaDefaults.hpp"
#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"

namespace kaa {

class IBootstrapManager;

class KaaChannelManager: public IKaaChannelManager {
public:
    KaaChannelManager(IBootstrapManager& manager)
        : bootstrapManager_(manager), bootstrapServers_(getServerInfoList())
    {
        bootstrapServerIt_ = bootstrapServers_.begin();
    }

    virtual void addChannel(IDataChannelPtr channel);
    virtual void removeChannel(IDataChannelPtr channel);

    virtual std::list<IDataChannelPtr> getChannels();
    virtual std::list<IDataChannelPtr> getChannelsByType(ChannelType type);

    virtual IDataChannelPtr getChannelByTransportType(TransportType type);
    virtual IDataChannelPtr getChannel(const std::string& channelId);

    virtual void onServerUpdated(IServerInfoPtr server);
    virtual void onServerFailed(IServerInfoPtr server);

    virtual void clearChannelList();

private:
    void useNewChannel(IDataChannelPtr channel);
    void useNewChannelForType(TransportType type);

private:
    IBootstrapManager&   bootstrapManager_;

    const BootstrapServers&             bootstrapServers_;
    BootstrapServers::const_iterator    bootstrapServerIt_;

    std::map<ChannelType, IServerInfoPtr>    lastServers_;

    std::set<IDataChannelPtr>                    channels_;
    std::map<TransportType, IDataChannelPtr>    mappedChannels_;
};

} /* namespace kaa */

#endif /* KAACHANNELMANAGER_HPP_ */
