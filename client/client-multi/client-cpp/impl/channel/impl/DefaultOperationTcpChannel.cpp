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
const std::uint16_t DefaultOperationTcpChannel::CHANNEL_TIMEOUT;
const std::uint16_t DefaultOperationTcpChannel::PING_TIMEOUT;
const std::uint16_t DefaultOperationTcpChannel::CONN_ACK_TIMEOUT;

const std::uint32_t DefaultOperationTcpChannel::KAA_PLATFORM_PROTOCOL_AVRO_ID;

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


DefaultOperationTcpChannel::DefaultOperationTcpChannel(IKaaChannelManager& channelManager,
                                                       const KeyPair& clientKeys,
                                                       IKaaClientContext& context)
    : context_(context)
    , channelManager_(channelManager)
    , clientKeys_(clientKeys)
    , work_(io_)
    /*, sock_(io_) */
    , pingTimer_(io_)
    , connAckTimer_(io_)
    , responseProcessor(context)
{
    startThreads();

    responseProcessor.registerConnackReceiver(std::bind(&DefaultOperationTcpChannel::onConnack, this, std::placeholders::_1));
    responseProcessor.registerKaaSyncReceiver(std::bind(&DefaultOperationTcpChannel::onKaaSync, this, std::placeholders::_1));
    responseProcessor.registerPingResponseReceiver(std::bind(&DefaultOperationTcpChannel::onPingResponse, this));
    responseProcessor.registerDisconnectReceiver(std::bind(&DefaultOperationTcpChannel::onDisconnect, this, std::placeholders::_1));
}

DefaultOperationTcpChannel::~DefaultOperationTcpChannel()
{
    if (!isShutdown_) {
        doShutdown();
    }
}

void DefaultOperationTcpChannel::onConnack(const ConnackMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Connack: status %2%")
                                                            % getId()
                                                            % message.getMessage());

    switch (message.getReturnCode()) {
        case ConnackReturnCode::ACCEPTED:
            break;
        case ConnackReturnCode::REFUSE_VERIFICATION_FAILED:
        case ConnackReturnCode::REFUSE_BAD_CREDENTIALS:
            KAA_LOG_WARN(boost::format("Channel [%1%] failed server authentication: %2%")
                                            % getId()
                                            % ConnackMessage::returnCodeToString(message.getReturnCode()));

            onServerFailed(KaaFailoverReason::ENDPOINT_NOT_REGISTERED);

            break;
        default:
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to server: %2%")
                                                                    % getId()
                                                                    % message.getMessage());
            onServerFailed(KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);
            break;
    }
}

void DefaultOperationTcpChannel::onDisconnect(const DisconnectMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received Disconnect: %2%")
                                                              % getId()
                                                              % message.getMessage());

    KaaFailoverReason failover = (message.getReason() == DisconnectReason::CREDENTIALS_REVOKED ?
                                                            KaaFailoverReason::CREDENTIALS_REVOKED :
                                                            KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA);

    onServerFailed(failover);
}

void DefaultOperationTcpChannel::onKaaSync(const KaaSyncResponse& message)
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%]. KaaSync response received") % getId());
    const auto& encodedResponse = message.getPayload();

    std::string decodedResponse;

    try {
        decodedResponse = encDec_->decodeData(encodedResponse.data(), encodedResponse.size());
    } catch (const std::exception& e) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] unable to decode data: %2%")
                                                                        % getId()
                                                                        % e.what());

        onServerFailed();
        return;
    }

    auto returnCode = demultiplexer_->processResponse(
                                            std::vector<std::uint8_t>(reinterpret_cast<const std::uint8_t *>(decodedResponse.data()),
                                                                      reinterpret_cast<const std::uint8_t *>(decodedResponse.data() + decodedResponse.size())));

    if (returnCode == DemultiplexerReturnCode::REDIRECT) {
        throw TransportRedirectException(boost::format("Channel [%1%] received REDIRECT response") % getId());
    } else if (returnCode == DemultiplexerReturnCode::FAILURE) {
        onServerFailed();
        return;
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (!isFirstResponseReceived_) {
        KAA_LOG_INFO(boost::format("Channel [%1%] received first response") % getId());
        connAckTimer_.cancel();
        isFirstResponseReceived_ = true;
    }

    if (isPendingSyncRequest_) {
        isPendingSyncRequest_ = false;
        ackTypes_.clear();

        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        KAA_LOG_INFO(boost::format("Channel [%1%] has pending request. Starting SYNC...") % getId());

        syncAll();
    } else if (!ackTypes_.empty()) {
        KAA_LOG_INFO(boost::format("Channel [%1%] has %2% pending ACK requests. Starting SYNC...")
                                                                                    % getId()
                                                                                    % ackTypes_.size());

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
    KAA_LOG_DEBUG(boost::format("Channel [%1%] received ping response ") % getId());
}

void DefaultOperationTcpChannel::openConnection()
{
    if (isConnected_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] connection is already opened") % getId());
        return;
    }

    KAA_LOG_TRACE(boost::format("Channel [%1%] opening connection to %2%:%3%")
                                                        % getId()
                                                        % currentServer_->getHost()
                                                        % currentServer_->getPort());

    boost::system::error_code errorCode;

    boost::asio::ip::tcp::endpoint ep = HttpUtils::resolveEndpoint(currentServer_->getHost(),
                                                                   currentServer_->getPort(),
                                                                   errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to resolve endpoint: %2%")
                                                                    % getId()
                                                                    % errorCode.message());
        onServerFailed();
        return;
    }

    responseBuffer_.reset(new boost::asio::streambuf());
    sock_.reset(new boost::asio::ip::tcp::socket(io_));
    sock_->open(ep.protocol(), errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to open socket: %2%")
                                                            % getId()
                                                            % errorCode.message());
        onServerFailed();
        return;
    }

    sock_->connect(ep, errorCode);

    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] failed to connect to %2%:%3%: %4%")
                                                                % getId()
                                                                % ep.address().to_string()
                                                                % ep.port()
                                                                % errorCode.message());
        onServerFailed();
        return;
    }

    channelManager_.onConnected({sock_->local_endpoint().address().to_string(), ep.address().to_string(), getServerType()});

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
    setPingTimer();
}

void DefaultOperationTcpChannel::closeConnection()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    bool wasConnected = isConnected_;

    KAA_LOG_TRACE(boost::format("Channel [%1%] closing connection: isConnected '%2%'")
                                                        % getId()
                                                        % boost::io::group(std::boolalpha, wasConnected));

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
        responseProcessor.flush();
    }
}

void DefaultOperationTcpChannel::onServerFailed(KaaFailoverReason failoverReason)
{
    if (isFailoverInProgress_) {
        KAA_LOG_TRACE(boost::format("Channel [%1%] failover processing already in progress. "
                                    "Ignore '%2%' failover")
                                                    % getId()
                                                    % LoggingUtils::toString(failoverReason));
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

    auto server = std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);

    KAA_LOG_WARN(boost::format("Channel [%1%] detected '%2%' failover for %3%")
                                                         % getId()
                                                         % LoggingUtils::toString(finalFailoverReason)
                                                         % LoggingUtils::toString(*server));

    channelManager_.onServerFailed(server, finalFailoverReason);
}

boost::system::error_code DefaultOperationTcpChannel::sendData(const IKaaTcpRequest& request)
{
    boost::system::error_code errorCode;
    const auto& data = request.getRawMessage();
    KAA_LOG_TRACE(boost::format("Channel [%1%] sending data: size %2%") % getId() % data.size());
    boost::asio::write(*sock_, boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()), errorCode);
    return errorCode;
}

boost::system::error_code DefaultOperationTcpChannel::sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel [%1%] sending KAASYNC") % getId());
    const auto& requestBody = multiplexer_->compileRequest(transportTypes);
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    return sendData(KaaSyncRequest(false, true, 0, requestEncoded, KaaSyncMessageType::SYNC));
}

boost::system::error_code DefaultOperationTcpChannel::sendConnect()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel [%1%] sending CONNECT") % getId());
    const auto& requestBody = multiplexer_->compileRequest(getSupportedTransportTypes());
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    const auto& sessionKey = encDec_->getEncodedSessionKey();
    const auto& signature = encDec_->signData(sessionKey.data(), sessionKey.size());
    return sendData(ConnectMessage(CHANNEL_TIMEOUT, KAA_PLATFORM_PROTOCOL_AVRO_ID, signature, sessionKey, requestEncoded));
}

boost::system::error_code DefaultOperationTcpChannel::sendDisconnect()
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] sending DISCONNECT") % getId());
    return sendData(DisconnectMessage(DisconnectReason::NONE));
}

boost::system::error_code DefaultOperationTcpChannel::sendPingRequest()
{
    KAA_LOG_DEBUG(boost::format("Channel [%1%] sending PING") % getId());
    return sendData(PingRequest());
}

void DefaultOperationTcpChannel::readFromSocket()
{
    boost::asio::async_read(*sock_,
                            *responseBuffer_,
                            boost::asio::transfer_at_least(1),
                            boost::bind(&DefaultOperationTcpChannel::onReadEvent,
                                        this,
                                        boost::asio::placeholders::error));
}

void DefaultOperationTcpChannel::setPingTimer()
{
    pingTimer_.expires_from_now(boost::posix_time::seconds(PING_TIMEOUT));
    pingTimer_.async_wait(std::bind(&DefaultOperationTcpChannel::onPingTimeout, this, std::placeholders::_1));
}

void DefaultOperationTcpChannel::setConnAckTimer()
{
    connAckTimer_.expires_from_now(boost::posix_time::seconds(CONN_ACK_TIMEOUT));
    connAckTimer_.async_wait(std::bind(&DefaultOperationTcpChannel::onConnAckTimeout, this, std::placeholders::_1));
}

void DefaultOperationTcpChannel::startThreads()
{
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

void DefaultOperationTcpChannel::onReadEvent(const boost::system::error_code& err)
{
    if (!err) {
        std::ostringstream responseStream;
        responseStream << responseBuffer_.get();
        const auto& responseStr = responseStream.str();
        try {
            if (responseStr.empty()) {
                 KAA_LOG_ERROR(boost::format("Channel [%1%] no data read from socket") % getId());
            } else {
                responseProcessor.processResponseBuffer(responseStr.data(), responseStr.size());
            }
        } catch (const TransportRedirectException& exception) {
            KAA_LOG_INFO(boost::format("Channel [%1%] received REDIRECT response") % getId());
            return;
        } catch (const KaaException& exception) {
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process data: %2%") % getId() % exception.what());
            onServerFailed();
        }
    } else {
        KAA_LOG_WARN(boost::format("Channel [%1%] socket error: %2%") % getId() % err.message());

        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        if (err != boost::asio::error::operation_aborted && isConnected_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            KAA_UNLOCK(channelLock);
            KAA_MUTEX_UNLOCKED("channelGuard_")

            onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] socket operations aborted") % getId());
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

            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process PING: %2%") % getId() % err.message());
            onServerFailed();
            return;
        } else {
            KAA_LOG_DEBUG(boost::format("Channel [%1%] PING timer aborted") % getId());
            return;
        }
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(channelLock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (isConnected_) {
        setPingTimer();
    }
}

void DefaultOperationTcpChannel::onConnAckTimeout(const boost::system::error_code& err)
{
    if (!err) {
        KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timeout") % getId());
        onServerFailed();
        return;
    } else {
        if (err != boost::asio::error::operation_aborted){
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to process CONNACK timeout: %2%") % getId() % err.message());
        } else {
            if (isConnected_) {
                KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK processed") % getId());
            } else {
                KAA_LOG_DEBUG(boost::format("Channel [%1%] CONNACK timer aborted") % getId());
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
        KAA_LOG_WARN(boost::format("Channel [%1%] ignore new server: channel is shut down") % getId());
        return;
    }

    if (server->getTransportId() != getTransportProtocolId()) {
        KAA_LOG_WARN(boost::format("Channel [%1%] ignore new server: unsupported transport %2%")
                                                                        % getId()
                                                                        % LoggingUtils::toString(server->getTransportId()));
        return;
    }

    KAA_LOG_TRACE(boost::format("Channel [%1%] preparing to use new server %2%")
                                                            % getId()
                                                            % LoggingUtils::toString(*server));

    currentServer_ = std::make_shared<IPTransportInfo>(server);
    encDec_ = std::make_shared<RsaEncoderDecoder>(clientKeys_.getPublicKey(),
                                                  clientKeys_.getPrivateKey(),
                                                  currentServer_->getPublicKey(),
                                                  context_);

    isFailoverInProgress_ = false;

    if (!isPaused_) {
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        closeConnection();

        KAA_LOG_TRACE(boost::format("Channel [%1%] scheduling open connection")
                                                                        % getId());

        io_.post(std::bind(&DefaultOperationTcpChannel::openConnection, this));
    }
}

void DefaultOperationTcpChannel::sync(TransportType type)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: channel is shut down") % getId());
        return;
    }

    if (isPaused_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: channel is paused") % getId());
        return;
    }

    const auto& suppportedTypes = getSupportedTransportTypes();
    auto it = suppportedTypes.find(type);
    if (it == suppportedTypes.end() || it->second == ChannelDirection::DOWN) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] ignore sync: unsupported transport type %2%")
                                                                        % getId()
                                                                        % LoggingUtils::toString(type));
        return;
    }

    if (!currentServer_) {
        KAA_LOG_DEBUG(boost::format("Channel [%1%] can't sync: server is null") % getId());
        return;
    }

    if (isFirstResponseReceived_) {
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        std::map<TransportType, ChannelDirection> syncTypes;
        syncTypes.insert(std::make_pair(type, it->second));
        for (const auto& typeIt : suppportedTypes) {
            if (typeIt.first != type) {
                syncTypes.insert(std::make_pair(typeIt.first, ChannelDirection::DOWN));
            }
        }

        boost::system::error_code errorCode = sendKaaSync(syncTypes);
        if (errorCode) {
            KAA_LOG_ERROR(boost::format("Channel [%1%] failed to sync: %2%")
                                                                % getId()
                                                                % errorCode.message());
            onServerFailed();
        }
    } else {
        KAA_LOG_DEBUG(boost::format("Channel [%1%] can't sync: waiting for CONNACK + KAASYNC") % getId());
        isPendingSyncRequest_ = true;
    }
}

void DefaultOperationTcpChannel::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: channel is shut down") % getId());
        return;
    }

    if (isPaused_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: channel is on oause") % getId());
        return;
    }

    if (!currentServer_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: server is null") % getId());
        return;
    }

    if (isFirstResponseReceived_) {
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        boost::system::error_code errorCode = sendKaaSync(getSupportedTransportTypes());
        if (errorCode) {
            KAA_LOG_ERROR(boost::format("Channel [%1%]. Failed to sync: %2%") % getId() % errorCode.message());
            onServerFailed();
        }
    } else {
        KAA_LOG_DEBUG(boost::format("Can't sync channel [%1%]. Waiting for CONNACK message + KAASYNC message") % getId());
        isPendingSyncRequest_ = true;
    }
}

void DefaultOperationTcpChannel::syncAck(TransportType type)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    KAA_LOG_DEBUG(boost::format("Channel [%1%] adding ACK for transport '%2%'")
                                                        % getId()
                                                        % LoggingUtils::toString(type));
    ackTypes_.push_back(type);
}

void DefaultOperationTcpChannel::doShutdown()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    KAA_LOG_DEBUG(boost::format("Channel [%1%] is shutting down: isShutdown '%2%'")
                                                    % getId()
                                                    % boost::io::group(std::boolalpha, isShutdown_));

    if (!isShutdown_) {
        isShutdown_ = true;
        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lock);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        closeConnection();

        KAA_LOG_TRACE(boost::format("Channel [%1%] stopping IO service: isStopped '%2%'")
                                                    % getId()
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
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (isShutdown_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't pause: channel is shut down") % getId());
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
        KAA_LOG_WARN(boost::format("Channel [%1%] can't resume: channel is shut down") % getId());
        return;
    }

    if (isPaused_) {
        isPaused_ = false;
        io_.post(std::bind(&DefaultOperationTcpChannel::openConnection, this));
    }
}

}

#endif
