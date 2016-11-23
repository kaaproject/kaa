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

class DefaultOperationTcpChannel : public IDataChannel {
public:
    DefaultOperationTcpChannel(IKaaChannelManager& channelManager,
                               const KeyPair& clientKeys,
                               IKaaClientContext& context);

    ~DefaultOperationTcpChannel();

    virtual void sync(TransportType type);
    virtual void syncAll();
    virtual void syncAck(TransportType type);

    virtual const std::string& getId() const { return CHANNEL_ID; }

    virtual TransportProtocolId getTransportProtocolId() const {
        return TransportProtocolIdConstants::TCP_TRANSPORT_ID;
    }

    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer);
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer);
    virtual void setServer(ITransportConnectionInfoPtr server);

    virtual ITransportConnectionInfoPtr getServer() {
        return std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);
    }

    virtual void shutdown();
    virtual void pause();
    virtual void resume();

    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const {
        return SUPPORTED_TYPES;
    }

    virtual ServerType getServerType() const {
        return ServerType::OPERATIONS;
    }

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) {
        failoverStrategy_ = strategy;
    }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {
        connectivityChecker_= checker;
    }

    void onReadEvent(const boost::system::error_code& err);
    void onPingTimeout(const boost::system::error_code& err);
    void onConnAckTimeout(const boost::system::error_code& err);

    void onConnack(const ConnackMessage& message);
    void onDisconnect(const DisconnectMessage& message);
    void onKaaSync(const KaaSyncResponse& message);
    void onPingResponse();

    void openConnection();
    void closeConnection();
    void onServerFailed(KaaFailoverReason failoverReason = KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);

private:
    boost::system::error_code sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes);
    boost::system::error_code sendConnect();
    boost::system::error_code sendDisconnect();
    boost::system::error_code sendPingRequest();
    boost::system::error_code sendData(const IKaaTcpRequest& request);

    void readFromSocket();
    void setPingTimer();
    void setConnAckTimer();

    void startThreads();
    void stopThreads();

    void doShutdown();

private:
    static const std::string CHANNEL_ID;

    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;

    static const std::uint16_t THREADPOOL_SIZE = 2;
    static const std::uint32_t KAA_PLATFORM_PROTOCOL_AVRO_ID = 0xf291f2d4;

    static const std::uint16_t CHANNEL_TIMEOUT = 200;
    static const std::uint16_t PING_TIMEOUT = CHANNEL_TIMEOUT / 2;
    static const std::uint16_t CONN_ACK_TIMEOUT = 20;

private:
    IKaaClientContext& context_;
    IKaaChannelManager& channelManager_;
    KeyPair clientKeys_;

    std::list<TransportType> ackTypes_;

    boost::asio::io_service                          io_;
    boost::asio::io_service::work                    work_;
    std::unique_ptr<boost::asio::ip::tcp::socket>    sock_;
    boost::asio::deadline_timer                      pingTimer_;
    boost::asio::deadline_timer                      connAckTimer_;
    std::vector<std::thread>                         ioThreads_;
    std::unique_ptr<boost::asio::streambuf>          responseBuffer_;

    // TODO: http://jira.kaaproject.org/browse/KAA-1321
    // Use states and present them as enum
    bool isConnected_ = false;
    bool isFirstResponseReceived_ = false;
    bool isPendingSyncRequest_ = false;
    bool isShutdown_ = false;
    bool isPaused_ = false;
    bool isFailoverInProgress_ = false;

    IKaaDataMultiplexer *multiplexer_ = nullptr;
    IKaaDataDemultiplexer *demultiplexer_ = nullptr;

    KaaTcpResponseProcessor responseProcessor;

    // To avoid simultaneously re-creation/access a shared pointer is used.
    std::shared_ptr<RsaEncoderDecoder> encDec_;

    KAA_MUTEX_DECLARE(channelGuard_);

    std::shared_ptr<IPTransportInfo> currentServer_;
    ConnectivityCheckerPtr connectivityChecker_;
    IFailoverStrategyPtr failoverStrategy_;
};

}

#endif /* DEFAULTOPERATIONTCPCHANNEL_HPP_ */
