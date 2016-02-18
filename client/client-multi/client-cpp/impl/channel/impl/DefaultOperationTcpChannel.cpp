/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifdef KAA_DEFAULT_TCP_CHANNEL

#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"

#include <memory>
#include <sstream>
#include <functional>
#include <chrono>
#include <thread>

#include <boost/bind.hpp>

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

const std::uint16_t DefaultOperationTcpChannel::CHANNEL_TIMEOUT = 200;
const std::uint16_t DefaultOperationTcpChannel::PING_TIMEOUT = CHANNEL_TIMEOUT / 2;
const std::uint16_t DefaultOperationTcpChannel::CONN_ACK_TIMEOUT = 20;
const std::uint16_t DefaultOperationTcpChannel::RECONNECT_TIMEOUT = 5; // sec

const std::uint32_t DefaultOperationTcpChannel::KAA_PLATFORM_PROTOCOL_AVRO_ID = 0xf291f2d4;

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


DefaultOperationTcpChannel::DefaultOperationTcpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys, IKaaClientContext &context)
    : clientKeys_(clientKeys), work_(io_), socketWork_(socketIo_),/*sock_(io_), */pingTimer_(io_), connAckTimer_(io_)/*, reconnectTimer_(io_)*/
    , retryTimer_("DefaultOperationTcpChannel retryTimer")
    , firstStart_(true), isConnected_(false), isFirstResponseReceived_(false), isPendingSyncRequest_(false)
    , isShutdown_(false), isPaused_(false), isFailoverInProgress_(false), multiplexer_(nullptr), demultiplexer_(nullptr)
    , channelManager_(channelManager), responsePorcessor(context), context_(context)
{
    responsePorcessor.registerConnackReceiver(std::bind(&DefaultOperationTcpChannel::onConnack, this, std::placeholders::_1));
    responsePorcessor.registerKaaSyncReceiver(std::bind(&DefaultOperationTcpChannel::onKaaSync, this, std::placeholders::_1));
    responsePorcessor.registerPingResponseReceiver(std::bind(&DefaultOperationTcpChannel::onPingResponse, this));
    responsePorcessor.registerDisconnectReceiver(std::bind(&DefaultOperationTcpChannel::onDisconnect, this, std::placeholders::_1));
}

DefaultOperationTcpChannel::~DefaultOperationTcpChannel()
{
    if (!isShutdown_) {
        doShutdown();
    }
}

void DefaultOperationTcpChannel::onConnack(const ConnackMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Connack (result=%2%) response received") % getId() % message.getMessage());

    switch (message.getReturnCode()) {
    case ConnackReturnCode::ACCEPTED:
        break;
    case ConnackReturnCode::REFUSE_BAD_CREDENTIALS:
        KAA_LOG_WARN(boost::format("Channel \"%1%\". Connack result: bad credentials. Going to re-register... ") % getId());
        context_.getStatus().setRegistered(false);
        context_.getStatus().save();
        setServer(currentServer_);
        break;
    default:
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Connack result failed: %2%. Closing connection") % getId() % message.getMessage());
        onServerFailed();
        break;
    }
}

void DefaultOperationTcpChannel::onDisconnect(const DisconnectMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Disconnect (result=%2%) response received") % getId() % message.getMessage());
    onServerFailed();
}

void DefaultOperationTcpChannel::onKaaSync(const KaaSyncResponse& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". KaaSync response received") % getId());
    const auto& encodedResponse = message.getPayload();

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    std::string decodedResposne;
    try {
        decodedResposne = encDec_->decodeData(encodedResponse.data(), encodedResponse.size());
    } catch (const std::exception& e) {
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");
        KAA_LOG_ERROR(boost::format("Unable to decode data: %s") % e.what());
        onServerFailed();
        return;
    }

    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(lock);
    KAA_MUTEX_UNLOCKED("channelGuard_");

    auto returnCode = demultiplexer_->processResponse(
                                            std::vector<std::uint8_t>(reinterpret_cast<const std::uint8_t *>(decodedResposne.data()),
                                                    reinterpret_cast<const std::uint8_t *>(decodedResposne.data() + decodedResposne.size())));

    if (returnCode == DemultiplexerReturnCode::REDIRECT) {
        throw TransportRedirectException(boost::format("Channel \"%1%\". Redirect response received") % getId());
    } else if (returnCode == DemultiplexerReturnCode::FAILURE) {
        onServerFailed();
        return;
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_LOCK(lock);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (!isFirstResponseReceived_) {
        KAA_LOG_INFO(boost::format("Channel \"%1%\". First response received") % getId());
        connAckTimer_.cancel();
        isFirstResponseReceived_ = true;
    }
    if (isPendingSyncRequest_) {
        isPendingSyncRequest_ = false;
        ackTypes_.clear();
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");
        KAA_LOG_INFO(boost::format("Channel \"%1%\". Pending request detected. Starting SYNC...") % getId());
        syncAll();
    } else if (!ackTypes_.empty()) {
        KAA_LOG_INFO(boost::format("Channel \"%1%\". Have %2% acknowledgment requests. Starting SYNC...")
                % getId() % ackTypes_.size());

        auto ackTypesCopy = ackTypes_;
        ackTypes_.clear();

        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        if (ackTypesCopy.size() > 1) {
            syncAll();
        } else {
            sync(*ackTypesCopy.begin());
        }
    }
}

void DefaultOperationTcpChannel::onPingResponse()
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Ping response received") % getId());
}

void DefaultOperationTcpChannel::openConnection()
{
    if (isConnected_) {
        KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Connection is already opened. Ignoring.") % getId());
        return;
    }
    boost::asio::ip::tcp::endpoint ep;
    try {
        ep = HttpUtils::getEndpoint(currentServer_->getHost(), currentServer_->getPort());
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Connection to endpoint failed: %2%") % getId() % e.what());
        onServerFailed();
    }
    boost::system::error_code errorCode;
    responseBuffer_.reset(new boost::asio::streambuf());
    sock_.reset(new boost::asio::ip::tcp::socket(socketIo_));
    sock_->open(ep.protocol(), errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to open socket: %2%") % getId() % errorCode.message());
        onServerFailed();
        return;
    }
    sock_->connect(ep, errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format(
                        "Channel \"%1%\". Failed to connect to %2%:%3% socket: %4%")
                        % getId() % ep.address().to_string() % ep.port()
                        % errorCode.message());
        onServerFailed();
        return;
    }
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_LOCK(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    isConnected_ = true;
    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(channelGuard_);
    KAA_MUTEX_UNLOCKED("channelGuard_");

    sendConnect();
    setConnAckTimer();
    readFromSocket();
    setTimer();
}

void DefaultOperationTcpChannel::closeConnection()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    bool wasConnected = isConnected_;
    isFirstResponseReceived_ = false;
    isConnected_ = false;
    isPendingSyncRequest_ = false;
    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(lock);
    KAA_MUTEX_UNLOCKED("channelGuard_");
    if (wasConnected) {
        pingTimer_.cancel();
        connAckTimer_.cancel();
        sendDisconnect();
        boost::system::error_code errorCode;
        sock_->shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
        sock_->close(errorCode);
        responsePorcessor.flush();
    }
}

void DefaultOperationTcpChannel::onServerFailed()
{
    if (isFailoverInProgress_) {
        KAA_LOG_TRACE("Failover in progress. On server failed skipped.");
        return;
    } else {
        isFailoverInProgress_ = true;
    }

    closeConnection();

    if (connectivityChecker_ && !connectivityChecker_->checkConnectivity()) {
        KAA_LOG_TRACE("Loss of connectivity detected.");
        FailoverStrategyDecision decision = failoverStrategy_->onFailover(Failover::NO_CONNECTIVITY);
        switch (decision.getAction()) {
            case FailoverStrategyAction::NOOP:
                KAA_LOG_WARN("No operation is performed according to failover strategy decision.");
                isFailoverInProgress_ = false;
                return;
            case FailoverStrategyAction::RETRY:
            {
                std::size_t period = decision.getRetryPeriod();
                KAA_LOG_WARN(boost::format("Attempt to reconnect will be made in %1% secs "
                        "according to failover strategy decision.") % period);
                retryTimer_.stop();
                retryTimer_.start(period, [&] { isFailoverInProgress_ = false; openConnection(); });
                break;
            }
            case FailoverStrategyAction::STOP_APP:
                KAA_LOG_WARN("Stopping application according to failover strategy decision!");
                exit(EXIT_FAILURE);
                break;
            default:
                break;
        }

        //KAA_LOG_TRACE(boost::format("Loss of connectivity. Attempt to reconnect will be made in {} sec") % RECONNECT_TIMEOUT);
        //reconnectTimer_.expires_from_now(boost::posix_time::seconds(RECONNECT_TIMEOUT));
        //reconnectTimer_.async_wait(std::bind(&DefaultOperationTcpChannel::openConnection, this));
        return;
    }

    channelManager_->onServerFailed(std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_));
}

boost::system::error_code DefaultOperationTcpChannel::sendData(const IKaaTcpRequest& request)
{
    boost::system::error_code errorCode;
    const auto& data = request.getRawMessage();
    KAA_LOG_TRACE(boost::format("Channel \"%1%\". Sending message size=%2%") % getId() % data.size());
    boost::asio::write(*sock_, boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()), errorCode);
    return errorCode;
}

boost::system::error_code DefaultOperationTcpChannel::sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending KAASYNC message") % getId());
    const auto& requestBody = multiplexer_->compileRequest(transportTypes);
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    return sendData(KaaSyncRequest(false, true, 0, requestEncoded, KaaSyncMessageType::SYNC));
}

boost::system::error_code DefaultOperationTcpChannel::sendConnect()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending CONNECT message") % getId());
    const auto& requestBody = multiplexer_->compileRequest(getSupportedTransportTypes());
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    const auto& sessionKey = encDec_->getEncodedSessionKey();
    const auto& signature = encDec_->signData(sessionKey.data(), sessionKey.size());
    return sendData(ConnectMessage(CHANNEL_TIMEOUT, KAA_PLATFORM_PROTOCOL_AVRO_ID, signature, sessionKey, requestEncoded));
}

boost::system::error_code DefaultOperationTcpChannel::sendDisconnect()
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending DISCONNECT message") % getId());
    return sendData(DisconnectMessage(DisconnectReason::NONE));
}

boost::system::error_code DefaultOperationTcpChannel::sendPingRequest()
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending PING message") % getId());
    return sendData(PingRequest());
}

void DefaultOperationTcpChannel::readFromSocket()
{
    boost::asio::async_read(*sock_, *responseBuffer_,
              boost::asio::transfer_at_least(1),
              boost::bind(&DefaultOperationTcpChannel::onReadEvent, this,
                      boost::asio::placeholders::error));
}

void DefaultOperationTcpChannel::setTimer()
{
    pingTimer_.expires_from_now(boost::posix_time::seconds(PING_TIMEOUT));
    pingTimer_.async_wait(std::bind(&DefaultOperationTcpChannel::onPingTimeout, this, std::placeholders::_1));
}

void DefaultOperationTcpChannel::setConnAckTimer()
{
    connAckTimer_.expires_from_now(boost::posix_time::seconds(CONN_ACK_TIMEOUT));
    connAckTimer_.async_wait(std::bind(&DefaultOperationTcpChannel::onConnAckTimeout, this, std::placeholders::_1));
}

void DefaultOperationTcpChannel::createThreads()
{
    for (std::size_t i = 0; i < TIMER_THREADPOOL_SIZE; ++i) {
        timerThreads_[i] = std::thread([this](){ io_.run(); });
    }
    for (std::size_t i = 0; i < SOCKET_THREADPOOL_SIZE; ++i) {
        channelThreads_[i] = std::thread([this](){ socketIo_.run(); });
    }
}

void DefaultOperationTcpChannel::onReadEvent(const boost::system::error_code& err)
{
    if (err) {
        KAA_LOG_TRACE(boost::format("Channel \"%1%\". onReadEvent err %2%") % getId() % err);
    }
    if (!err) {
        std::ostringstream responseStream;
        responseStream << responseBuffer_.get();
        const auto& responseStr = responseStream.str();
        try {
            if (responseStr.empty()) {
                 KAA_LOG_ERROR(boost::format("Channel \"%1%\". No data read from socket.") % getId());
                 std::this_thread::sleep_for(std::chrono::milliseconds(50));
                 //onServerFailed();
            } else {
                responsePorcessor.processResponseBuffer(responseStr.data(), responseStr.size());
            }
        } catch (const TransportRedirectException& exception) {
            KAA_LOG_INFO(boost::format("Channel \"%1%\". Redirect response received.") % getId());
            return;
        } catch (const KaaException& exception) {
            KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to process response buffer, reason: %2%") % getId() % exception.what());
            onServerFailed();
        }
    } else if (err != boost::asio::error::eof) {
        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (err != boost::asio::error::operation_aborted && isConnected_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            KAA_UNLOCK(channelLock);
            KAA_MUTEX_UNLOCKED("channelGuard_")

            KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to read from the socket: %2%") % getId() % err.message());
            onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Reading was aborted") % getId());
            return;
        }
    }
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isConnected_) {
        readFromSocket();
    }
}

void DefaultOperationTcpChannel::onPingTimeout(const boost::system::error_code& err)
{
    if (!err) {
        sendPingRequest();
    } else {
        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (err != boost::asio::error::operation_aborted && isConnected_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            KAA_UNLOCK(channelLock);
            KAA_MUTEX_UNLOCKED("channelGuard_")

            KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to process ping request: %2%") % getId() % err.message());
            onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Ping timer was aborted ") % getId());
            return;
        }
    }
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isConnected_) {
        setTimer();
    }
}

void DefaultOperationTcpChannel::onConnAckTimeout(const boost::system::error_code& err)
{
    if (!err) {
        KAA_LOG_DEBUG(boost::format("Channel \"%1%\". ConnAck message timeout") % getId());
        onServerFailed();
        return;
    } else {
        if (err != boost::asio::error::operation_aborted){
            KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to process ConnAck timeout: %2%") % getId() % err.message());
        }else{
            if (isConnected_) {
                KAA_LOG_DEBUG(boost::format("Channel \"%1%\". ConnAck message processed") % getId());
            } else {
                KAA_LOG_DEBUG(boost::format("Channel \"%1%\". ConnAck timer was aborted") % getId());
            }
        }
    }
}

void DefaultOperationTcpChannel::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    multiplexer_ = multiplexer;
}

void DefaultOperationTcpChannel::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    demultiplexer_ = demultiplexer;
}

void DefaultOperationTcpChannel::setServer(ITransportConnectionInfoPtr server)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Can't set server for channel %1%. Channel is down") % getId());
        return;
    }
    if (server->getTransportId() == TransportProtocolIdConstants::TCP_TRANSPORT_ID) {
        if (firstStart_ && !isPaused_) {
            createThreads();
            firstStart_ = false;
        }

        currentServer_.reset(new IPTransportInfo(server));
        encDec_.reset(new RsaEncoderDecoder(clientKeys_.getPublicKey(), clientKeys_.getPrivateKey(), currentServer_->getPublicKey(), context_));

        if (!isPaused_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            KAA_UNLOCK(lock);
            KAA_MUTEX_UNLOCKED("channelGuard_");
            closeConnection();
            std::this_thread::sleep_for(std::chrono::seconds(1));
            isFailoverInProgress_ = false;
            io_.post(std::bind(&DefaultOperationTcpChannel::openConnection, this));
        } else {
            isFailoverInProgress_ = false;
        }
    } else {
        KAA_LOG_ERROR(boost::format("Invalid server info for channel %1%") % getId());
        isFailoverInProgress_ = false;
    }
}

void DefaultOperationTcpChannel::sync(TransportType type)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Channel is down") % getId());
        return;
    }
    if (isPaused_) {
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Channel is paused") % getId());
        return;
    }
    const auto& types = getSupportedTransportTypes();
    auto it = types.find(type);
    if (it != types.end() && (it->second == ChannelDirection::UP || it->second == ChannelDirection::BIDIRECTIONAL)) {
        if (currentServer_) {
            if (isFirstResponseReceived_) {
                KAA_MUTEX_UNLOCKING("channelGuard_");
                KAA_UNLOCK(lock);
                KAA_MUTEX_UNLOCKED("channelGuard_");

                std::map<TransportType, ChannelDirection> types;
                types.insert(std::make_pair(type, it->second));
                for (auto typeIt = types.begin(); typeIt != types.end(); ++typeIt) {
                    if (typeIt->first != type) {
                        types.insert(std::make_pair(typeIt->first, ChannelDirection::DOWN));
                    }
                }

                boost::system::error_code errorCode = sendKaaSync(types);
                if (errorCode) {
                    KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to sync: %2%") % getId() % errorCode.message());
                    onServerFailed();
                }
            } else {
                KAA_LOG_DEBUG(boost::format("Can't sync channel %1%. Waiting for CONNACK message + KAASYNC message") % getId());
                isPendingSyncRequest_ = true;
            }
        } else {
            KAA_LOG_DEBUG(boost::format("Can't sync channel %1%. Server is null") % getId());
        }
    } else {
        KAA_LOG_ERROR(boost::format("Unsupported transport type for channel %1%") % getId());
    }
}

void DefaultOperationTcpChannel::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Channel is down") % getId());
        return;
    }
    if (isPaused_) {
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Channel is paused") % getId());
        return;
    }
    if (currentServer_) {
        if (isFirstResponseReceived_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            KAA_UNLOCK(lock);
            KAA_MUTEX_UNLOCKED("channelGuard_");

            boost::system::error_code errorCode = sendKaaSync(getSupportedTransportTypes());
            if (errorCode) {
                KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to sync: %2%") % getId() % errorCode.message());
                onServerFailed();
            }
        } else {
            KAA_LOG_DEBUG(boost::format("Can't sync channel %1%. Waiting for CONNACK message + KAASYNC message") % getId());
            isPendingSyncRequest_ = true;
        }
    } else {
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
    }
}

void DefaultOperationTcpChannel::syncAck(TransportType type)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Going to add acknowledgment message for transport type %1% for channel %2%")
            % LoggingUtils::TransportTypeToString(type)
            % getId());
    ackTypes_.push_back(type);
}

void DefaultOperationTcpChannel::doShutdown()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (!isShutdown_) {
        isShutdown_ = true;
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");
        closeConnection();
        io_.stop();
        socketIo_.stop();
        for (std::size_t i = 0; i < SOCKET_THREADPOOL_SIZE; ++i) {
            if (channelThreads_[i].joinable()) {
                channelThreads_[i].join();
            }
        }
        for (std::size_t i = 0; i < TIMER_THREADPOOL_SIZE; ++i) {
            if (timerThreads_[i].joinable()) {
                timerThreads_[i].join();
            }
        }
    }
}

void DefaultOperationTcpChannel::shutdown()
{
    doShutdown();
}

void DefaultOperationTcpChannel::pause()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Can't pause channel %1%. Channel is down") % getId());
        return;
    }
    if (!isPaused_) {
        isPaused_ = true;
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");
        closeConnection();
    }
}

void DefaultOperationTcpChannel::resume()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Can't resume channel %1%. Channel is down") % getId());
        return;
    }
    if (isPaused_) {
        isPaused_ = false;
        if (firstStart_) {
            createThreads();
            firstStart_ = false;
        }
        io_.post(std::bind(&DefaultOperationTcpChannel::openConnection, this));
    }
}

}

#endif
