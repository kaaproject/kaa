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

#ifndef MOCKCHANNELMANAGER_HPP_
#define MOCKCHANNELMANAGER_HPP_

#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/failover/IFailoverStrategy.hpp"
#include "headers/channel/MockDataChannel.hpp"

namespace kaa {

class MockChannelManager: public IKaaChannelManager {
public:
    virtual void setChannel(TransportType type, IDataChannelPtr channel) { ++onSetChannel_; }
    virtual void addChannel(IDataChannelPtr channel) { ++onAddChannel_; }
    virtual void removeChannel(IDataChannelPtr channel) { ++onRemoveChannel_; }
    virtual void removeChannel(const std::string& id) { ++onRemoveChannelById_; }

    virtual std::list<IDataChannelPtr> getChannels() {
        static std::list<IDataChannelPtr> channels;
        ++onGetChannels_;
        return channels;
    }

    virtual IDataChannelPtr getChannelByTransportType(TransportType type) {
        static IDataChannelPtr channel(new MockDataChannel);
        ++onGetChannelByTransportType_;
        return channel;
    }

    virtual IDataChannelPtr getChannel(const std::string& channelId) {
        static IDataChannelPtr channel(new MockDataChannel);
        ++onGetChannel_;
        return channel;
    }

    virtual void onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr server) { ++onGetChannelByTransportType_; }
    virtual void onServerFailed(ITransportConnectionInfoPtr server,
                                KaaFailoverReason reason = KaaFailoverReason::NO_CONNECTIVITY)
    {
        ++onServerFailed_;
    }
    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) { ++onFailOverStrategyChange_;}
    virtual void clearChannelList() { ++onClearChannelList_; }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) { ++onSetConnectivityChecker_; }

    virtual void shutdown() { ++onShutdown_; }
    virtual void pause() { ++onPause_; }
    virtual void resume() { ++onResume; }

public:
    std::size_t onSetChannel_ = 0;
    std::size_t onAddChannel_ = 0;
    std::size_t onRemoveChannel_ = 0;
    std::size_t onRemoveChannelById_ = 0;
    std::size_t onGetChannels_ = 0;
    std::size_t onGetChannelByTransportType_ = 0;
    std::size_t onGetChannel_ = 0;
    std::size_t onTransportConnectionInfoUpdated_ = 0;
    std::size_t onServerFailed_ = 0;
    std::size_t onClearChannelList_ = 0;
    std::size_t onSetConnectivityChecker_ = 0;
    std::size_t onShutdown_ = 0;
    std::size_t onPause_ = 0;
    std::size_t onResume = 0;
    std::size_t onFailOverStrategyChange_ = 0;
};

} /* namespace kaa */

#endif /* MOCKCHANNELMANAGER_HPP_ */
