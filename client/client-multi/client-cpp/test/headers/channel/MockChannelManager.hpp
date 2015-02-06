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

#ifndef MOCKCHANNELMANAGER_HPP_
#define MOCKCHANNELMANAGER_HPP_

#include "kaa/channel/IKaaChannelManager.hpp"

#include "headers/channel/MockDataChannel.hpp"

namespace kaa {

class MockChannelManager: public IKaaChannelManager {
public:
    virtual void setChannel(TransportType type, IDataChannelPtr channel) {}
    virtual void addChannel(IDataChannelPtr channel) {}
    virtual void removeChannel(IDataChannelPtr channel) {}
    virtual void removeChannel(const std::string& id) {}

    virtual std::list<IDataChannelPtr> getChannels() {
        static std::list<IDataChannelPtr> channels;
        return channels;
    }

    virtual IDataChannelPtr getChannelByTransportType(TransportType type) {
        static IDataChannelPtr channel(new MockDataChannel);
        return channel;
    }

    virtual IDataChannelPtr getChannel(const std::string& channelId) {
        static IDataChannelPtr channel(new MockDataChannel);
        return channel;
    }

    virtual void onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr server) {}
    virtual void onServerFailed(ITransportConnectionInfoPtr server) {}

    virtual void clearChannelList() {}

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {}

    virtual void shutdown() {}

    virtual void pause() {}

    virtual void resume() {}

    virtual ~MockChannelManager() {}
};

} /* namespace kaa */

#endif /* MOCKCHANNELMANAGER_HPP_ */
