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

#ifndef DEFAULTOPERATIONTCPCHANNEL_HPP_
#define DEFAULTOPERATIONTCPCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#include <cstdint>
#include <thread>
#include <array>

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


namespace kaa {

class IKaaTcpRequest;
class KeyPair;

class DefaultOperationTcpChannel : public IDataChannel {
public:
    DefaultOperationTcpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys);
    virtual ~DefaultOperationTcpChannel();

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
    
    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {
        connectivityChecker_= checker;
    }

    void onReadEvent(const boost::system::error_code& err);
    void onPingTimeout(const boost::system::error_code& err);

    void onConnack(const ConnackMessage& message);
    void onDisconnect(const DisconnectMessage& message);
    void onKaaSync(const KaaSyncResponse& message);
    void onPingResponse();

    void openConnection();
    void closeConnection();
    void onServerFailed();

private:
    static const std::uint16_t PING_TIMEOUT;
    static const std::uint16_t RECONNECT_TIMEOUT;

    boost::system::error_code sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes);
    boost::system::error_code sendConnect();
    boost::system::error_code sendDisconnect();
    boost::system::error_code sendPingRequest();
    boost::system::error_code sendData(const IKaaTcpRequest& request);

    void readFromSocket();
    void setTimer();

    void createThreads();

    void doShutdown();

private:
    static const std::string CHANNEL_ID;
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;
    static const std::uint16_t THREADPOOL_SIZE = 2;
    static const std::uint32_t KAA_PLATFORM_PROTOCOL_AVRO_ID;

    std::list<TransportType> ackTypes_;
    KeyPair clientKeys_;

    boost::asio::io_service io_;
    boost::asio::io_service::work work_;
    boost::asio::ip::tcp::socket sock_;
    boost::asio::deadline_timer pingTimer_;
    boost::asio::deadline_timer reconnectTimer_;
    boost::asio::streambuf responseBuffer_;
    std::array<std::thread, THREADPOOL_SIZE> channelThreads_;

    bool firstStart_;
    bool isConnected_;
    bool isFirstResponseReceived_;
    bool isPendingSyncRequest_;
    bool isShutdown_;
    bool isPaused_;

    IKaaDataMultiplexer *multiplexer_;
    IKaaDataDemultiplexer *demultiplexer_;
    IKaaChannelManager *channelManager_;
    std::shared_ptr<IPTransportInfo> currentServer_;
    KaaTcpResponseProcessor responsePorcessor;
    std::unique_ptr<RsaEncoderDecoder> encDec_;

    KAA_MUTEX_DECLARE(channelGuard_);

    ConnectivityCheckerPtr connectivityChecker_;
};

}

#endif /* DEFAULTOPERATIONTCPCHANNEL_HPP_ */
