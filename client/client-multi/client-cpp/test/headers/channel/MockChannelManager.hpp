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
    virtual void setChannel(TransportType type, IDataChannelPtr channel) override { ++onSetChannel_; }
    virtual void addChannel(IDataChannelPtr channel) override { ++onAddChannel_; }
    virtual void removeChannel(IDataChannelPtr channel) override { ++onRemoveChannel_; }
    virtual void removeChannel(const std::string& id) override { ++onRemoveChannelById_; }

    virtual std::list<IDataChannelPtr> getChannels() override {
        static std::list<IDataChannelPtr> channels;
        ++onGetChannels_;
        return channels;
    }

    virtual IDataChannelPtr getChannelByTransportType(TransportType type) override {
        static IDataChannelPtr channel(new MockDataChannel);
        ++onGetChannelByTransportType_;
        return channel;
    }

    virtual IDataChannelPtr getChannel(const std::string& channelId) override {
        static IDataChannelPtr channel(new MockDataChannel);
        ++onGetChannel_;
        return channel;
    }

    virtual void onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr server) override { ++onGetChannelByTransportType_; }
    virtual void onServerFailed(ITransportConnectionInfoPtr server,
                                KaaFailoverReason reason = KaaFailoverReason::NO_CONNECTIVITY)
    {
        ++onServerFailed_;
    }
    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) override { ++onFailOverStrategyChange_;}
    virtual void clearChannelList() override { ++onClearChannelList_; }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) override { ++onSetConnectivityChecker_; }

    virtual void onConnected(const EndpointConnectionInfo& connection)  override {}
    virtual void shutdown() override { ++onShutdown_; }
    virtual void pause() override { ++onPause_; }
    virtual void resume() override { ++onResume; }

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
