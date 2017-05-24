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

#ifndef DEFAULTOPERATIONTCPCHANNEL_HPP_
#define DEFAULTOPERATIONTCPCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#include <cstdint>
#include <thread>
#include <array>
#include <memory>

#include <boost/asio.hpp>

#include "kaa/KaaThread.hpp"
#include "kaa/security/KeyUtils.hpp"
#include "kaa/channel/IDataChannel.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/kaatcp/KaaTcpResponseProcessor.hpp"
#include "kaa/channel/IPTransportInfo.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"
#include "kaa/utils/KaaTimer.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/IKaaClientContext.hpp"


namespace kaa {

class IKaaTcpRequest;
class KeyPair;
class ChannelConnection;

class DefaultOperationTcpChannel : public IDataChannel {
public:
    DefaultOperationTcpChannel(IKaaChannelManager& channelManager,
                               const KeyPair& clientKeys,
                               IKaaClientContext& context);

    ~DefaultOperationTcpChannel();

    virtual void sync(TransportType type);
    virtual void syncAll();
    virtual void syncAck(TransportType type);

    virtual const std::string& getId() const
    {
        return CHANNEL_ID;
    }

    virtual TransportProtocolId getTransportProtocolId() const
    {
        return TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    }

    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer);
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer);
    virtual void setServer(ITransportConnectionInfoPtr server);

    virtual ITransportConnectionInfoPtr getServer()
    {
        return std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);
    }

    virtual void shutdown();
    virtual void pause();
    virtual void resume();

    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const
    {
        return SUPPORTED_TYPES;
    }

    virtual ServerType getServerType() const {
        return ServerType::OPERATIONS;
    }

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy)
    {
        static_cast<void>(strategy);
    }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker)
    {
        connectivityChecker_= checker;
    }

    void openConnection();
    void closeConnection();
    void onServerFailed(KaaFailoverReason failoverReason = KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);

private:
    void startThreads();
    void stopThreads();

private:
    static const std::string CHANNEL_ID;
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;
    static const std::uint16_t THREADPOOL_SIZE = 2;

    IKaaClientContext& context_;
    IKaaChannelManager& channelManager_;

    std::shared_ptr<ChannelConnection> connection_;
    std::shared_ptr<IPTransportInfo> currentServer_;

    boost::asio::io_service io_;
    boost::asio::io_service::work work_;
    std::vector<std::thread> ioThreads_;
    KeyPair clientKeys_;

    IKaaDataMultiplexer *multiplexer_ = nullptr;
    IKaaDataDemultiplexer *demultiplexer_ = nullptr;

    bool isFailoverInProgress_ = false;
    bool isShutdown_ = false;

    std::recursive_mutex channelGuard_;

    ConnectivityCheckerPtr connectivityChecker_;
};

}

#endif /* DEFAULTOPERATIONTCPCHANNEL_HPP_ */
