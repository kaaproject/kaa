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

#ifdef KAA_DEFAULT_TCP_CHANNEL

#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"

#include <memory>
#include <sstream>
#include <functional>
#include <chrono>
#include <thread>
#include <deque>

#include <boost/bind.hpp>

#include "kaa/IKaaClient.hpp"
#include "kaa/common/exception/TransportRedirectException.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/kaatcp/KaaTcpResponseProcessor.hpp"
#include "kaa/kaatcp/ConnectMessage.hpp"
#include "kaa/kaatcp/KaaSyncRequest.hpp"
#include "kaa/kaatcp/PingRequest.hpp"
#include "kaa/kaatcp/DisconnectMessage.hpp"
#include "kaa/http/HttpUtils.hpp"
#include "kaa/IKaaClientStateStorage.hpp"

namespace kaa {

const std::uint16_t DefaultOperationTcpChannel::THREADPOOL_SIZE;
const std::string DefaultOperationTcpChannel::CHANNEL_ID = "default_operation_kaa_tcp_channel";

const std::map<TransportType, ChannelDirection> DefaultOperationTcpChannel::SUPPORTED_TYPES =
        {
                { TransportType::PROFILE, ChannelDirection::BIDIRECTIONAL },
                { TransportType::CONFIGURATION, ChannelDirection::BIDIRECTIONAL },
                { TransportType::NOTIFICATION, ChannelDirection::BIDIRECTIONAL },
                { TransportType::USER, ChannelDirection::BIDIRECTIONAL },
                { TransportType::EVENT, ChannelDirection::BIDIRECTIONAL },
                { TransportType::LOGGING, ChannelDirection::BIDIRECTIONAL }
        };

class Connection: std::enable_shared_from_this<Connection> {
    std::mutex connectionMutex_;
    std::unique_ptr<boost::asio::ip::tcp::socket> sock_;
    boost::asio::io_service::strand strand_;
    std::unique_ptr<boost::asio::streambuf> responseBuffer_;
    boost::asio::deadline_timer pingTimer_;
    boost::asio::deadline_timer connAckTimer_;
    IKaaDataMultiplexer *multiplexer_ = nullptr;
    IKaaDataDemultiplexer *demultiplexer_ = nullptr;
    KeyPair clientKeys_;
    std::list<TransportType> ackTypes_;
    KaaTcpResponseProcessor responseProcessor_;

    std::deque<std::vector<uint8_t>> requestQueue_;

    // To avoid simultaneously re-creation/access a shared pointer is used.
    std::shared_ptr<RsaEncoderDecoder> encDec_;

    std::shared_ptr<IPTransportInfo> currentServer_;

    enum class State {
        Disconnected, ///< Connection has not been initiaded yet
        Connecting, ///< Connection has been initiated, but channel is not ready for I/O
        Ready, ///< Channel is connected and ready to do I/O
    };

    std::atomic<State> state_;
    bool hasPendingSyncRequest_ = false;

    IKaaClientContext& context_;
    IKaaChannelManager& channelManager_;

    DefaultOperationTcpChannel *channel_;
    std::string channelId_;
    
    static const std::uint32_t KAA_PLATFORM_PROTOCOL_AVRO_ID = 0xf291f2d4;

    static const std::uint16_t CHANNEL_TIMEOUT = 200;
    static const std::uint16_t PING_TIMEOUT = CHANNEL_TIMEOUT / 2;
    static const std::uint16_t CONN_ACK_TIMEOUT = 20;

public:
    Connection(IKaaChannelManager &channelManager,
                const KeyPair &clientKeys,
                IKaaClientContext &context,
                IKaaDataMultiplexer *multiplexer,
                IKaaDataDemultiplexer *demultiplexer,
                DefaultOperationTcpChannel *channel,
                const std::string &channelId,
                std::shared_ptr<IPTransportInfo> currentServer,
                boost::asio::io_service &io);

    ~Connection();

    void sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes);
    void sendConnect();
    void sendDisconnect();
    void sendPingRequest();
    void sendData(const IKaaTcpRequest& request);
    void sendDataImpl();

    void onReadEvent(const boost::system::error_code& err);
    void onWriteEvent(const boost::system::error_code &err, std::size_t bytes_transferred);
    void onPingTimeout(const boost::system::error_code& err);
    void onConnAckTimeout(const boost::system::error_code& err);

    void onConnack(const ConnackMessage& message);
    void onDisconnect(const DisconnectMessage& message);
    void onKaaSync(const KaaSyncResponse& message);
    void onPingResponse();

    void sync(TransportType type);
    void syncAll();
    void syncAck(TransportType type);

    void readFromSocket();
    void setPingTimer();
    void setConnAckTimer();

    void run();
};


Connection::Connection(IKaaChannelManager& channelManager,
                                                   const KeyPair& clientKeys,
                                                   IKaaClientContext& context,
                                                   IKaaDataMultiplexer *multiplexer,
                                                   IKaaDataDemultiplexer *demultiplexer,
                                                   DefaultOperationTcpChannel *channel,
                                                   const std::string &channelId,
                                                   std::shared_ptr<IPTransportInfo> currentServer,
                                                   boost::asio::io_service &io):
    strand_(io),
    context_(context),
    channelManager_(channelManager),
    clientKeys_(clientKeys),
    pingTimer_(io),
    connAckTimer_(io),
    multiplexer_(multiplexer),
    demultiplexer_(demultiplexer),
    responseProcessor_(context),
    channel_(channel),
    state_(State::Disconnected),
    channelId_(channelId),
    currentServer_(currentServer)
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    responseProcessor_.registerConnackReceiver(std::bind(&Connection::onConnack, this, std::placeholders::_1));
    responseProcessor_.registerKaaSyncReceiver(std::bind(&Connection::onKaaSync, this, std::placeholders::_1));
    responseProcessor_.registerPingResponseReceiver(std::bind(&Connection::onPingResponse, this));
    responseProcessor_.registerDisconnectReceiver(std::bind(&Connection::onDisconnect, this, std::placeholders::_1));

    boost::system::error_code errorCode;

    boost::asio::ip::tcp::endpoint ep = HttpUtils::resolveEndpoint(currentServer_->getHost(),
                                                                   currentServer_->getPort(),
                                                                   errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to resolve endpoint: %2%")
                                                                    % channelId_
                                                                    % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    encDec_ = std::make_shared<RsaEncoderDecoder>(clientKeys_.getPublicKey(),
                                                clientKeys_.getPrivateKey(),
                                                currentServer_->getPublicKey(),
                                                context_);

    responseBuffer_.reset(new boost::asio::streambuf());
    sock_.reset(new boost::asio::ip::tcp::socket(io));
    sock_->open(ep.protocol(), errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to open socket: %2%")
                                                            % channelId_
                                                            % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    sock_->connect(ep, errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to %2%:%3%: %4%")
                                                                % channelId_
                                                                % ep.address().to_string()
                                                                % ep.port()
                                                                % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] connected to %2%") % channelId_ % ep.address().to_string());
    channelManager_.onConnected({sock_->local_endpoint().address().to_string(), ep.address().to_string(), channel_->getServerType()});
}

void Connection::run()
{
    sendConnect();
    state_ = State::Connecting;
    setConnAckTimer();
    readFromSocket();
    setPingTimer();
}

Connection::~Connection()
{
    KAA_LOG_INFO("~Connection ");
    pingTimer_.cancel();
    connAckTimer_.cancel();
    sock_->cancel();
    sendDisconnect();
    state_ = State::Disconnected;
    boost::system::error_code errorCode;
    sock_->shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
    sock_->close(errorCode);
    responseProcessor_.flush();
}

void Connection::onConnack(const ConnackMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Connack: status %2%")
                                                            % channelId_
                                                            % message.getMessage());

    switch (message.getReturnCode()) {
        case ConnackReturnCode::ACCEPTED:
            break;
        case ConnackReturnCode::REFUSE_VERIFICATION_FAILED:
        case ConnackReturnCode::REFUSE_BAD_CREDENTIALS:
            KAA_LOG_WARN(boost::format("Channel [%1%] failed server authentication: %2%")
                                            % channelId_
                                            % ConnackMessage::returnCodeToString(message.getReturnCode()));

            channel_->onServerFailed(KaaFailoverReason::ENDPOINT_NOT_REGISTERED);

            break;
        default:
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to server: %2%")
                                                                    % channelId_
                                                                    % message.getMessage());
            channel_->onServerFailed(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
            break;
    }
}

void Connection::onDisconnect(const DisconnectMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Disconnect: %2%")
                                                              % channelId_
                                                              % message.getMessage());

    KaaFailoverReason failover = (message.getReason() == DisconnectReason::CREDENTIALS_REVOKED ?
                                                            KaaFailoverReason::CREDENTIALS_REVOKED :
                                                            KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);

    channel_->onServerFailed(failover);
}

void Connection::onKaaSync(const KaaSyncResponse& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%]. KaaSync response received") % channelId_);
    const auto& encodedResponse = message.getPayload();

    std::string decodedResponse;

    try {
        decodedResponse = encDec_->decodeData(encodedResponse.data(), encodedResponse.size());
    } catch (const std::exception& e) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] unable to decode data: %2%")
                                                                        % channelId_
                                                                        % e.what());

        channel_->onServerFailed();
        return;
    }

    auto returnCode = demultiplexer_->processResponse(
                                            std::vector<std::uint8_t>(reinterpret_cast<const std::uint8_t *>(decodedResponse.data()),
                                                                      reinterpret_cast<const std::uint8_t *>(decodedResponse.data() + decodedResponse.size())));

    if (returnCode == DemultiplexerReturnCode::REDIRECT) {
        throw TransportRedirectException(boost::format("Channel [%1%] received REDIRECT response") % channelId_);
    } else if (returnCode == DemultiplexerReturnCode::FAILURE) {
        channel_->onServerFailed();
        return;
    }

    if (state_ == State::Connecting) {
        KAA_LOG_INFO(boost::format("Channel [%1%] received first response") % channelId_);
        connAckTimer_.cancel();
        state_ = State::Ready;
    }

    if (hasPendingSyncRequest_) {
        hasPendingSyncRequest_ = false;
        ackTypes_.clear();

        KAA_LOG_INFO(boost::format("Channel [%1%] has pending request. Starting SYNC...") % channelId_);

        Connection::syncAll();
    } else if (!ackTypes_.empty()) {
        KAA_LOG_INFO(boost::format("Channel [%1%] has %2% pending ACK requests. Starting SYNC...")
                                                                                    % channelId_
                                                                                    % ackTypes_.size());

        auto ackTypesCopy = ackTypes_;
        ackTypes_.clear();

        if (ackTypesCopy.size() > 1) {
            Connection::syncAll();
        } else {
            Connection::sync(*ackTypesCopy.begin());
        }
    }
}

void Connection::onPingResponse()
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received ping response ") % channelId_);
}

void Connection::sendData(const IKaaTcpRequest& request)
{
    auto data = request.getRawMessage();
    strand_.post([this, data] {
        requestQueue_.push_back(data);
        if (requestQueue_.size() > 1) {
            return;
        }
        sendDataImpl();
    });
}

void Connection::sendDataImpl()
{
    const auto &data = requestQueue_[0];
    KAA_LOG_INFO(boost::format("Channel [%1%] sending data: size %2% %3%") % channelId_ % data.size() % sock_.get());
    boost::asio::async_write(*sock_, boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()),
                                strand_.wrap(boost::bind(&Connection::onWriteEvent, shared_from_this(),
                                            boost::asio::placeholders::error, boost::asio::placeholders::bytes_transferred)));
}

void Connection::onWriteEvent(const boost::system::error_code &err, std::size_t bytes_transferred)
{
    requestQueue_.pop_front();

    if (err) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] write failed") % channelId_);
        channel_->onServerFailed();
    } else {
        KAA_LOG_TRACE(boost::format("Channel [%1%] sent %2% bytes") % channelId_ % bytes_transferred);
        if (!requestQueue_.empty()) {
            this->sendDataImpl();
        }
    }
}

void Connection::sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    KAA_LOG_INFO(boost::format("Channel [%1%] sending KAASYNC") % channelId_);
    const auto& requestBody = multiplexer_->compileRequest(transportTypes);
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    sendData(KaaSyncRequest(false, true, 0, requestEncoded, KaaSyncMessageType::SYNC));
}

void Connection::sendConnect()
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    KAA_LOG_INFO(boost::format("Channel [%1%] sending CONNECT") % channelId_ );
    const auto& requestBody = multiplexer_->compileRequest(channel_->getSupportedTransportTypes());
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    const auto& sessionKey = encDec_->getEncodedSessionKey();
    const auto& signature = encDec_->signData(sessionKey.data(), sessionKey.size());
    sendData(ConnectMessage(CHANNEL_TIMEOUT, KAA_PLATFORM_PROTOCOL_AVRO_ID, signature, sessionKey, requestEncoded));
}

void Connection::sendDisconnect()
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    KAA_LOG_INFO(boost::format("Channel [%1%] sending DISCONNECT") % channelId_);
    sendData(DisconnectMessage(DisconnectReason::NONE));
}

void Connection::sendPingRequest()
{
    KAA_LOG_INFO(boost::format("Channel [%1%] sending PING") % channelId_);
    sendData(PingRequest());
}

void Connection::readFromSocket()
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    KAA_LOG_INFO(boost::format("readFromSocket() %1%") % this);
    boost::asio::async_read(*sock_,
                            *responseBuffer_,
                            boost::asio::transfer_at_least(1),
                            boost::bind(&Connection::onReadEvent,
                                        shared_from_this(),
                                        boost::asio::placeholders::error));
}

void Connection::setPingTimer()
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    pingTimer_.expires_from_now(boost::posix_time::seconds(PING_TIMEOUT));
    pingTimer_.async_wait(std::bind(&Connection::onPingTimeout, shared_from_this(), std::placeholders::_1));
}

void Connection::setConnAckTimer()
{
    std::lock_guard<std::mutex> lock(connectionMutex_);
    connAckTimer_.expires_from_now(boost::posix_time::seconds(CONN_ACK_TIMEOUT));
    connAckTimer_.async_wait(std::bind(&Connection::onConnAckTimeout, shared_from_this(), std::placeholders::_1));
}

void Connection::syncAck(TransportType type)
{
    ackTypes_.push_back(type);
}

void Connection::onReadEvent(const boost::system::error_code& err)
{
    KAA_LOG_INFO(boost::format("onReadEvent connection id %1%") % this);
    if (!err) {
        std::ostringstream responseStream;
        responseStream << responseBuffer_.get();
        const auto& responseStr = responseStream.str();
        try {
            if (responseStr.empty()) {
                 KAA_LOG_ERROR(boost::format("Channel [%1%] no data read from socket") % channelId_);
            } else {
                responseProcessor_.processResponseBuffer(responseStr.data(), responseStr.size());
            }
        } catch (const TransportRedirectException& exception) {
            KAA_LOG_INFO(boost::format("Channel [%1%] received REDIRECT response") % channelId_);
            return;
        } catch (const KaaException& exception) {
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process data: %2%") % channelId_ % exception.what());
            channel_->onServerFailed();
        }
    } else {
        KAA_LOG_WARN(boost::format("Channel [%1%] socket error: %2%") % channelId_ % err.message());

        if (err != boost::asio::error::operation_aborted && state_ != State::Disconnected) {
            channel_->onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] socket operations aborted") % channelId_);
            return;
        }
    }

    if (state_ != State::Disconnected) {
        readFromSocket();
    }
}

void Connection::onPingTimeout(const boost::system::error_code& err)
{
    if (!err) {
        sendPingRequest();
    } else {
        if (err != boost::asio::error::operation_aborted && state_ != State::Disconnected) {
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process PING: %2%") % channelId_ % err.message());
            channel_->onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] PING timer aborted") % channelId_);
            return;
        }
    }

    if (state_ != State::Disconnected) {
        setPingTimer();
    }
}

void Connection::onConnAckTimeout(const boost::system::error_code& err)
{
    if (!err) {
        KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timeout") % channelId_);
        channel_->onServerFailed();
        return;
    } else {
        if (err != boost::asio::error::operation_aborted){
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process CONNACK timeout: %2%") % channelId_ % err.message());
        } else {
            if (state_ != State::Disconnected) {
                KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK processed") % channelId_);
            } else {
                KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timer aborted") % channelId_);
            }
        }
    }
}

DefaultOperationTcpChannel::DefaultOperationTcpChannel(IKaaChannelManager& channelManager,
                                                       const KeyPair& clientKeys,
                                                       IKaaClientContext& context)
    : context_(context),
      channelManager_(channelManager),
      clientKeys_(clientKeys),
      work_(io_)
{
    startThreads();
}

DefaultOperationTcpChannel::~DefaultOperationTcpChannel()
{
    if (!isShutdown_) {
        doShutdown();
    }
}

void DefaultOperationTcpChannel::openConnection()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (connection_ != nullptr) {
        KAA_LOG_WARN(boost::format("Channel [%1%] connection is already opened") % getId());
        return;
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] opening connection to %2%:%3%")
                                                        % getId()
                                                        % currentServer_->getHost()
                                                        % currentServer_->getPort());
    try {
        connection_ = std::make_shared<Connection>(channelManager_, clientKeys_, context_,
                                         multiplexer_, demultiplexer_, this,
                                         getId(), currentServer_, io_);
        KAA_LOG_INFO(boost::format("Opened connection id %1%") % connection_.get());
        connection_->run();
    } catch (KaaFailoverReason r) {
        KAA_LOG_INFO("connection_.reset() in openConnection()");
        connection_.reset();
        onServerFailed(r);
    }
}

void DefaultOperationTcpChannel::closeConnection()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!connection_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't close connection: connection is null") % getId());
        return;
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] closing connection") % getId())
    connection_.reset();
}

void DefaultOperationTcpChannel::onServerFailed(KaaFailoverReason failoverReason)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isFailoverInProgress_) {
        KAA_LOG_TRACE(boost::format("Channel [%1%] failover processing already in progress. "
                                    "Ignore '%2%' failover")
                                                    % getId()
                                                    % LoggingUtils::toString(failoverReason));
        return;
    }

    isFailoverInProgress_ = true;

    KAA_LOG_INFO("closing connection onServerFailed");
    closeConnection();

    KaaFailoverReason finalFailoverReason = failoverReason;
    if (failoverReason == KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA) {
        if (connectivityChecker_ && !connectivityChecker_->checkConnectivity()) {
            KAA_LOG_INFO(boost::format("Channel [%1%] detected loss of connectivity") % getId());
            finalFailoverReason = KaaFailoverReason::NO_CONNECTIVITY;
        }
    }

    auto server = std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);

    KAA_LOG_WARN(boost::format("Channel [%1%] detected '%2%' failover for %3%")
                                                         % getId()
                                                         % LoggingUtils::toString(finalFailoverReason)
                                                         % LoggingUtils::toString(*server));

    channelManager_.onServerFailed(server, finalFailoverReason);
}

void DefaultOperationTcpChannel::startThreads()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!ioThreads_.empty()) {
        return;
    }

    KAA_LOG_TRACE(boost::format("Channel [%1%] starting %2% IO service threads...")
                                                                    % getId()
                                                                    % THREADPOOL_SIZE);

    ioThreads_.reserve(THREADPOOL_SIZE);

    for (std::size_t i = 0; i < THREADPOOL_SIZE; ++i) {
        ioThreads_.emplace_back(
                [this]()
                {
                    try {
                        KAA_LOG_TRACE(boost::format("Channel [%1%] running IO service") % getId());

                        // Blocking call.
                        io_.run();

                        KAA_LOG_TRACE(boost::format("Channel [%1%] IO service stopped") % getId());
                    } catch (std::exception& e) {
                        KAA_LOG_ERROR(boost::format("Channel [%1%] unexpected stop of IO service: %2%")
                                                                                                % getId()
                                                                                                % e.what());

                        //TODO: http://jira.kaaproject.org/browse/KAA-1321
                        // Reset IO service and, perhaps, notify
                        // the channel manager about a transport failover.
                    }
                });
    }
}

void DefaultOperationTcpChannel::stopThreads()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    for (std::size_t i = 0; i < THREADPOOL_SIZE; ++i) {
        if (ioThreads_[i].joinable()) {
            ioThreads_[i].join();
        }
    }

    ioThreads_.clear();

    KAA_LOG_TRACE(boost::format("Channel [%1%] %2% IO service threads stopped")
                                                                    % getId()
                                                                    % THREADPOOL_SIZE);
}

void DefaultOperationTcpChannel::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    KAA_LOG_INFO("setting multiplexer");
    multiplexer_ = multiplexer;
}

void DefaultOperationTcpChannel::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    KAA_LOG_INFO("setting demultiplexer");
    demultiplexer_ = demultiplexer;
}

void DefaultOperationTcpChannel::setServer(ITransportConnectionInfoPtr server)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] ignore new server: channel is shut down") % getId());
        return;
    }

    if (server->getTransportId() != getTransportProtocolId()) {
        KAA_LOG_WARN(boost::format("Channel [%1%] ignore new server: unsupported transport %2%")
                                                                        % getId()
                                                                        % LoggingUtils::toString(server->getTransportId()));
        return;
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] preparing to use new server %2%")
                                                            % getId()
                                                            % LoggingUtils::toString(*server));

    KAA_LOG_INFO(boost::format("Channel [%1%] scheduling open connection") % getId());

    currentServer_ = std::make_shared<IPTransportInfo>(server);
    isFailoverInProgress_ = false;

    io_.post([this] {
        closeConnection();
        openConnection();
    });
}

void DefaultOperationTcpChannel::sync(TransportType type)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!connection_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: connection is not opened") % getId());
        return;
    }
    connection_->sync(type);
}

void Connection::sync(TransportType type)
{
    if (state_ == State::Disconnected) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: not connected") % channelId_);
        return;
    }

    if (state_ == State::Connecting) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: connection is not ready") % channelId_);
        hasPendingSyncRequest_ = true;
        return;
    }

    const auto& suppportedTypes = channel_->getSupportedTransportTypes();
    auto it = suppportedTypes.find(type);
    if (it == suppportedTypes.end() || it->second == ChannelDirection::DOWN) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] ignore sync: unsupported transport type %2%")
                                                                        % channelId_
                                                                        % LoggingUtils::toString(type));
        return;
    }

    std::map<TransportType, ChannelDirection> syncTypes;
    syncTypes.insert(std::make_pair(type, it->second));
    sendKaaSync(syncTypes);
}

void DefaultOperationTcpChannel::syncAll()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!connection_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: connection is not opened") % getId());
        return;
    }
    connection_->syncAll();
}

void Connection::syncAll()
{
    if (state_ == State::Disconnected) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: not connected") % channelId_);
        return;
    }

    if (state_ == State::Connecting) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: connection is not ready") % channelId_);
        return;
    }

    sendKaaSync(channel_->getSupportedTransportTypes());
}

void DefaultOperationTcpChannel::syncAck(TransportType type)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!connection_) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to add ACK for transport %2%: connection is null")
                      % getId() % LoggingUtils::toString(type));
        return;
    }
    KAA_LOG_DEBUG(boost::format("Channel [%1%] adding ACK for transport '%2%'")
                                                        % channelId_
                                                        % LoggingUtils::toString(type));
    auto conn = connection_;
    io_.post([conn, type] {
        conn->syncAck(type);
    });
}

void DefaultOperationTcpChannel::doShutdown()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    KAA_LOG_DEBUG(boost::format("Channel [%1%] is shutting down: isShutdown '%2%'")
                                                    % channelId_
                                                    % boost::io::group(std::boolalpha, isShutdown_));

    if (!isShutdown_) {
        isShutdown_ = true;
        KAA_LOG_INFO("closing connection doShutdown()");
        closeConnection();

        KAA_LOG_TRACE(boost::format("Channel [%1%] stopping IO service: isStopped '%2%'")
                                                    % channelId_
                                                    % boost::io::group(std::boolalpha, io_.stopped()));

        if (!io_.stopped()) {
            io_.stop();
        }

        stopThreads();
    }
}

void DefaultOperationTcpChannel::shutdown()
{
    doShutdown();
}

void DefaultOperationTcpChannel::pause()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't pause: channel is shut down") % getId());
        return;
    }

    io_.post(std::bind(&DefaultOperationTcpChannel::closeConnection, this));
}

void DefaultOperationTcpChannel::resume()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't resume: channel is shut down") % getId());
        return;
    }

    io_.post(std::bind(&DefaultOperationTcpChannel::openConnection, this));
}

}

#endif
