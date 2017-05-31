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

/**
 * Represents a single connection to the Kaa Operation server via TCP.
 */
class ChannelConnection: public std::enable_shared_from_this<ChannelConnection> {
public:

    /**
     * Initializes RSA encoding/decoding and opens a TCP connection to the @c currentServer.
     *
     * Throws @c KaaFailoverReason on failure.
     */
    ChannelConnection(IKaaChannelManager &channelManager, const KeyPair &clientKeys,
                      IKaaClientContext &context, IKaaDataMultiplexer *multiplexer,
                      IKaaDataDemultiplexer *demultiplexer, DefaultOperationTcpChannel *channel,
                      const std::string &channelId, const IPTransportInfo &currentServer,
                      boost::asio::io_service &io);

    ~ChannelConnection();

    void sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes);
    void sendConnect();
    void sendDisconnect();
    void sendPingRequest();
    void sync(TransportType type);
    void syncAll();
    void syncAck(TransportType type);

    /**
     * Initiates connection sequence.
     */
    void run();

    /**
     * Cancels all pending I/O operations and disconnecte from the server.
     */
    void shutdown();

private:
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


    void readFromSocket();
    void setPingTimer();
    void setConnAckTimer();

private:
    boost::asio::ip::tcp::socket sock_;
    boost::asio::streambuf responseBuffer_;
    boost::asio::deadline_timer pingTimer_;
    boost::asio::deadline_timer connAckTimer_;
    IKaaDataMultiplexer *const multiplexer_;
    IKaaDataDemultiplexer *const demultiplexer_;
    std::list<TransportType> ackTypes_;
    KaaTcpResponseProcessor responseProcessor_;
    std::recursive_mutex connectionMutex_;
    boost::asio::io_service::strand strand_;
    EndpointConnectionInfo currentConnection_;

    std::deque<std::vector<uint8_t>> requestQueue_;

    RsaEncoderDecoder encDec_;

    enum class State {
        Disconnected, ///< Connection has not been initiated yet
        Connecting, ///< Connection has been initiated, but channel is not ready for I/O
        Ready, ///< Channel is connected and ready to do I/O
    };

    std::atomic<State> state_;
    bool hasPendingSyncRequest_ = false;

    IKaaClientContext &context_;
    IKaaChannelManager &channelManager_;

    DefaultOperationTcpChannel *const channel_;
    const std::string channelId_;

    static const std::uint32_t KAA_PLATFORM_PROTOCOL_AVRO_ID = 0xf291f2d4;

    static const int CHANNEL_TIMEOUT = 200;
    static const int PING_TIMEOUT = CHANNEL_TIMEOUT / 2;
    static const int CONN_ACK_TIMEOUT = 20;
    static const int DISCONNECT_TIMEOUT = 3;
};

const std::uint32_t ChannelConnection::KAA_PLATFORM_PROTOCOL_AVRO_ID;
const int ChannelConnection::CHANNEL_TIMEOUT;
const int ChannelConnection::PING_TIMEOUT;
const int ChannelConnection::CONN_ACK_TIMEOUT;
const int ChannelConnection::DISCONNECT_TIMEOUT;

ChannelConnection::ChannelConnection(IKaaChannelManager& channelManager,
                                     const KeyPair& clientKeys,
                                     IKaaClientContext& context,
                                     IKaaDataMultiplexer *multiplexer,
                                     IKaaDataDemultiplexer *demultiplexer,
                                     DefaultOperationTcpChannel *channel,
                                     const std::string &channelId,
                                     const IPTransportInfo &currentServer,
                                     boost::asio::io_service &io):
    sock_(io),
    strand_(io),
    context_(context),
    channelManager_(channelManager),
    pingTimer_(io),
    connAckTimer_(io),
    multiplexer_(multiplexer),
    demultiplexer_(demultiplexer),
    responseProcessor_(context),
    channel_(channel),
    encDec_(clientKeys.getPublicKey(),
           clientKeys.getPrivateKey(),
           currentServer.getPublicKey(),
           context_),
    state_(State::Disconnected),
    channelId_(channelId)
{
    responseProcessor_.registerConnackReceiver(std::bind(&ChannelConnection::onConnack, this, std::placeholders::_1));
    responseProcessor_.registerKaaSyncReceiver(std::bind(&ChannelConnection::onKaaSync, this, std::placeholders::_1));
    responseProcessor_.registerPingResponseReceiver(std::bind(&ChannelConnection::onPingResponse, this));
    responseProcessor_.registerDisconnectReceiver(std::bind(&ChannelConnection::onDisconnect, this, std::placeholders::_1));

    boost::system::error_code errorCode;

    boost::asio::ip::tcp::endpoint ep = HttpUtils::resolveEndpoint(currentServer.getHost(),
                                                                   currentServer.getPort(),
                                                                   errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to resolve endpoint: %2%")
                                                                    % channelId_
                                                                    % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    sock_.open(ep.protocol(), errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to open socket: %2%")
                                                            % channelId_
                                                            % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    sock_.connect(ep, errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to %2%:%3%: %4%")
                                                                % channelId_
                                                                % ep.address().to_string()
                                                                % ep.port()
                                                                % errorCode.message());
        throw(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] connected to %2%") % channelId_ % ep.address().to_string());

    currentConnection_.endpointIp_ = sock_.local_endpoint().address().to_string();
    currentConnection_.serverIp_ = ep.address().to_string();
    currentConnection_.serverType_ = channel_->getServerType();
    currentConnection_.connectionAccepted_ = false;

    channelManager_.onConnected(currentConnection_);
}

void ChannelConnection::run()
{
    sendConnect();
    state_ = State::Connecting;
    setConnAckTimer();
    readFromSocket();
    setPingTimer();
}

void ChannelConnection::shutdown()
{
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    if (state_ == State::Disconnected) {
        return;
    }
    pingTimer_.cancel();
    connAckTimer_.cancel();
    sock_.cancel();
    sendDisconnect();
    state_ = State::Disconnected;
    boost::system::error_code errorCode;
    sock_.shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
    sock_.close(errorCode);
    responseProcessor_.flush();
}

ChannelConnection::~ChannelConnection()
{
    shutdown();
}

void ChannelConnection::onConnack(const ConnackMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Connack: status %2%")
                                                            % channelId_
                                                            % message.getMessage());

    switch (message.getReturnCode()) {
        case ConnackReturnCode::ACCEPTED:
            currentConnection_.connectionAccepted_ = true;
            channelManager_.onConnected(currentConnection_);
            break;
        case ConnackReturnCode::REFUSE_VERIFICATION_FAILED:
        case ConnackReturnCode::REFUSE_BAD_CREDENTIALS:
            KAA_LOG_WARN(boost::format("Channel [%1%] failed server authentication: %2%")
                         % channelId_ % ConnackMessage::returnCodeToString(message.getReturnCode()));

            channel_->onServerFailed(KaaFailoverReason::ENDPOINT_NOT_REGISTERED);
            break;
        default:
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to server: %2%")
                          % channelId_ % message.getMessage());
            channel_->onServerFailed(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
            break;
    }
}

void ChannelConnection::onDisconnect(const DisconnectMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Disconnect: %2%")
                  % channelId_ % message.getMessage());

    KaaFailoverReason failover = (message.getReason() == DisconnectReason::CREDENTIALS_REVOKED ?
                                                            KaaFailoverReason::CREDENTIALS_REVOKED :
                                                            KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);

    channel_->onServerFailed(failover);
}

void ChannelConnection::onKaaSync(const KaaSyncResponse& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%]. KaaSync response received") % channelId_);
    const auto& encodedResponse = message.getPayload();

    std::string decodedResponse;

    try {
        decodedResponse = encDec_.decodeData(encodedResponse.data(), encodedResponse.size());
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
        throw TransportRedirectException(boost::format("Channel [%1%] received REDIRECT response")
                                         % channelId_);
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
        syncAll();
    } else if (!ackTypes_.empty()) {
        KAA_LOG_INFO(boost::format("Channel [%1%] has %2% pending ACK requests. Starting SYNC...")
                     % channelId_ % ackTypes_.size());
        auto ackTypesCopy = ackTypes_;
        ackTypes_.clear();

        if (ackTypesCopy.size() > 1) {
            syncAll();
        } else {
            sync(*ackTypesCopy.begin());
        }
    }
}

void ChannelConnection::onPingResponse()
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received ping response ") % channelId_);
}

void ChannelConnection::sendData(const IKaaTcpRequest& request)
{
    auto data = request.getRawMessage();
    strand_.post([this, data] {
        requestQueue_.push_back(data);
        if (requestQueue_.size() == 1) {
            sendDataImpl();
        }
    });
}

void ChannelConnection::sendDataImpl()
{
    if (state_ == State::Disconnected) {
        return;
    }
    const auto &data = requestQueue_[0];
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending data: size %2%") % channelId_ % data.size());
    boost::asio::async_write(sock_,
            boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()),
            strand_.wrap(boost::bind(&ChannelConnection::onWriteEvent, shared_from_this(),
                                    boost::asio::placeholders::error,
                                    boost::asio::placeholders::bytes_transferred)));
}

void ChannelConnection::onWriteEvent(const boost::system::error_code &err, std::size_t bytes_transferred)
{
    requestQueue_.pop_front();

    if (err && err != boost::asio::error::operation_aborted) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] write failed: %2%") % channelId_ % err.message());
        channel_->onServerFailed();
    } else {
        KAA_LOG_TRACE(boost::format("Channel [%1%] sent %2% bytes") % channelId_ % bytes_transferred);
        if (!requestQueue_.empty()) {
            this->sendDataImpl();
        }
    }
}

void ChannelConnection::sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending KAASYNC") % channelId_);
    const auto& requestBody = multiplexer_->compileRequest(transportTypes);
    const auto& requestEncoded = encDec_.encodeData(requestBody.data(), requestBody.size());
    sendData(KaaSyncRequest(false, true, 0, requestEncoded, KaaSyncMessageType::SYNC));
}

void ChannelConnection::sendConnect()
{
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending CONNECT") % channelId_ );
    const auto& requestBody = multiplexer_->compileRequest(channel_->getSupportedTransportTypes());
    const auto& requestEncoded = encDec_.encodeData(requestBody.data(), requestBody.size());
    const auto& sessionKey = encDec_.getEncodedSessionKey();
    const auto& signature = encDec_.signData(sessionKey.data(), sessionKey.size());
    sendData(ConnectMessage(CHANNEL_TIMEOUT, KAA_PLATFORM_PROTOCOL_AVRO_ID, signature, sessionKey, requestEncoded));
}

void ChannelConnection::sendDisconnect()
{
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending DISCONNECT") % channelId_);
    // The DISCONNECT message is sent synchronously with timeout equal to DISCONNECT_TIMEOUT.
    // This is done in order to keep the ChannelConnection alive until the DISCONNECT is delivered
    // or timeout occurs.
    std::condition_variable cv;
    std::mutex cvMutex;
    auto msg = DisconnectMessage(DisconnectReason::NONE);
    const auto data = msg.getRawMessage();
    boost::asio::async_write(sock_,
            boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()),
            [this, &cv] (const boost::system::error_code &ec, std::size_t bytes_transferred)
            {
                cv.notify_one();
            });
    {
        std::unique_lock<std::mutex> lock(cvMutex);
        cv.wait_for(lock, std::chrono::seconds(DISCONNECT_TIMEOUT));
    }
    sock_.cancel();
}

void ChannelConnection::sendPingRequest()
{
    if (state_ == State::Disconnected) {
        return;
    }
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending PING") % channelId_);
    sendData(PingRequest());
}

void ChannelConnection::readFromSocket()
{
    if (state_ == State::Disconnected) {
        return;
    }
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    boost::asio::async_read(sock_, responseBuffer_, boost::asio::transfer_at_least(1),
                            boost::bind(&ChannelConnection::onReadEvent, shared_from_this(),
                                        boost::asio::placeholders::error));
}

void ChannelConnection::setPingTimer()
{
    if (state_ == State::Disconnected) {
        return;
    }
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    pingTimer_.expires_from_now(boost::posix_time::seconds(PING_TIMEOUT));
    pingTimer_.async_wait(std::bind(&ChannelConnection::onPingTimeout, shared_from_this(), std::placeholders::_1));
}

void ChannelConnection::setConnAckTimer()
{
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    connAckTimer_.expires_from_now(boost::posix_time::seconds(CONN_ACK_TIMEOUT));
    connAckTimer_.async_wait(std::bind(&ChannelConnection::onConnAckTimeout, shared_from_this(), std::placeholders::_1));
}

void ChannelConnection::syncAck(TransportType type)
{
    if (state_ == State::Disconnected) {
        return;
    }
    std::lock_guard<std::recursive_mutex> lock(connectionMutex_);
    ackTypes_.push_back(type);
}

void ChannelConnection::sync(TransportType type)
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
                      % channelId_ % LoggingUtils::toString(type));
        return;
    }

    sendKaaSync({{type, it->second}});
}

void ChannelConnection::syncAll()
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

void ChannelConnection::onReadEvent(const boost::system::error_code& err)
{
    if (!err) {
        std::ostringstream responseStream;
        responseStream << &responseBuffer_;
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
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process data: %2%")
                          % channelId_ % exception.what());
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

void ChannelConnection::onPingTimeout(const boost::system::error_code& err)
{
    if (!err) {
        sendPingRequest();
    } else if (err != boost::asio::error::operation_aborted && state_ != State::Disconnected) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process PING: %2%")
                      % channelId_ % err.message());
        channel_->onServerFailed();
        return;
    } else {
        KAA_LOG_DEBUG(boost::format("Channel [%1%] PING timer aborted") % channelId_);
        return;
    }

    if (state_ != State::Disconnected) {
        setPingTimer();
    }
}

void ChannelConnection::onConnAckTimeout(const boost::system::error_code& err)
{
    switch (err.value()) {
    case boost::system::errc::success:
        KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timeout") % channelId_);
        channel_->onServerFailed();
        break;
    case boost::asio::error::operation_aborted:
        if (state_ != State::Disconnected) {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK processed") % channelId_);
        } else {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timer aborted") % channelId_);
        }
        break;
    default:
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process CONNACK timeout: %2%") % channelId_ % err.message());
        break;
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
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!isShutdown_) {
        shutdown();
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
                 % getId() % currentServer_->getHost() % currentServer_->getPort());
    try {
        connection_ = std::make_shared<ChannelConnection>(channelManager_, clientKeys_,
                                                          context_, multiplexer_, demultiplexer_,
                                                          this, getId(), *currentServer_, io_);
        connection_->run();
    } catch (KaaFailoverReason r) {
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
    connection_->shutdown();
    connection_.reset();
}

void DefaultOperationTcpChannel::onServerFailed(KaaFailoverReason failoverReason)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isFailoverInProgress_) {
        KAA_LOG_TRACE(boost::format("Channel [%1%] failover processing already in progress. Ignore '%2%' failover")
                      % getId() % LoggingUtils::toString(failoverReason));
        return;
    }

    isFailoverInProgress_ = true;

    closeConnection();

    KaaFailoverReason finalFailoverReason = failoverReason;
    if (failoverReason == KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA) {
        if (connectivityChecker_ && !connectivityChecker_->checkConnectivity()) {
            KAA_LOG_INFO(boost::format("Channel [%1%] detected loss of connectivity") % getId());
            finalFailoverReason = KaaFailoverReason::NO_CONNECTIVITY;
        }
    }

    KAA_LOG_WARN(boost::format("Channel [%1%] detected '%2%' failover for %3%") % getId()
                 % LoggingUtils::toString(finalFailoverReason) % LoggingUtils::toString(*currentServer_));

    channelManager_.onServerFailed(currentServer_, finalFailoverReason);
}

void DefaultOperationTcpChannel::startThreads()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!ioThreads_.empty()) {
        return;
    }

    KAA_LOG_TRACE(boost::format("Channel [%1%] starting %2% IO service threads...")
                  % getId() % THREADPOOL_SIZE);

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
                                      % getId() % e.what());

                        //TODO(KAA-1321)
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
                  % getId() % THREADPOOL_SIZE);
}

void DefaultOperationTcpChannel::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    multiplexer_ = multiplexer;
}

void DefaultOperationTcpChannel::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
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
                     % getId() % LoggingUtils::toString(server->getTransportId()));
        return;
    }

    KAA_LOG_INFO(boost::format("Channel [%1%] preparing to use new server %2%")
                 % getId() % LoggingUtils::toString(*server));

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
    if (connection_) {
        connection_->sync(type);
    } else {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: connection is not opened") % getId());
    }
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

void DefaultOperationTcpChannel::syncAck(TransportType type)
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (!connection_) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to add ACK for transport %2%: connection is null")
                      % getId() % LoggingUtils::toString(type));
        return;
    }
    KAA_LOG_DEBUG(boost::format("Channel [%1%] adding ACK for transport '%2%'")
                  % getId() % LoggingUtils::toString(type));
    connection_->syncAck(type);
}

void DefaultOperationTcpChannel::shutdown()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    KAA_LOG_DEBUG(boost::format("Channel [%1%] is shutting down: isShutdown '%2%'")
                  % getId() % boost::io::group(std::boolalpha, isShutdown_));

    if (!isShutdown_) {
        isShutdown_ = true;
        closeConnection();

        KAA_LOG_TRACE(boost::format("Channel [%1%] stopping IO service: isStopped '%2%'")
                      % getId() % boost::io::group(std::boolalpha, io_.stopped()));

        if (!io_.stopped()) {
            io_.stop();
        }

        stopThreads();
    }
}

void DefaultOperationTcpChannel::pause()
{
    std::lock_guard<std::recursive_mutex> lock(channelGuard_);
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't pause: channel is shut down") % getId());
        return;
    }

    closeConnection();
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
