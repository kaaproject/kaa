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

#include "kaa/channel/impl/DefaultOperationTcpChannel.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/kaatcp/KaaTcpResponseProcessor.hpp"
#include "kaa/kaatcp/ConnectMessage.hpp"
#include "kaa/kaatcp/KaaSyncRequest.hpp"
#include "kaa/kaatcp/PingRequest.hpp"
#include "kaa/kaatcp/DisconnectMessage.hpp"
#include "kaa/http/HttpUtils.hpp"
#include <sstream>

namespace kaa {

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


DefaultOperationTcpChannel::DefaultOperationTcpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys)
    : clientKeys_(clientKeys), work_(io_), sock_(io_), pingTimer_(io_)
    , firstStart_(true), isConnected_(false), isFirstResponseReceived_(false), isPendingSyncRequest_(false)
    , multiplexer_(nullptr), demultiplexer_(nullptr), channelManager_(channelManager)
{
    responsePorcessor.registerConnackReceiver(boost::bind(&DefaultOperationTcpChannel::onConnack, this, _1));
    responsePorcessor.registerKaaSyncReceiver(boost::bind(&DefaultOperationTcpChannel::onKaaSync, this, _1));
    responsePorcessor.registerPingResponseReceiver(boost::bind(&DefaultOperationTcpChannel::onPingResponse, this));
    responsePorcessor.registerDisconnectReceiver(boost::bind(&DefaultOperationTcpChannel::onDisconnect, this, _1));
}

DefaultOperationTcpChannel::~DefaultOperationTcpChannel()
{
    closeConnection();
    io_.stop();
    channelThreads_.join_all();
}

void DefaultOperationTcpChannel::onConnack(const ConnackMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Connack (result=%2%) response received") % getId() % message.getMessage());
    if (message.getReturnCode() != ConnackReturnCode::SUCCESS) {
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Connack result failed: %2%. Closing connection") % getId() % message.getMessage());
        onServerFailed();
    }
}

void DefaultOperationTcpChannel::onDisconnect(const DisconnectMessage& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Disconnect (result=%2%) response received") % getId() % message.getMessage());
    if (message.getReason() != DisconnectReason::NONE) {
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Disconnect result failed: %2%.") % getId() % message.getMessage());
        onServerFailed();
    } else {
        closeConnection();
    }
}

void DefaultOperationTcpChannel::onKaaSync(const KaaSyncResponse& message)
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". KaaSync response received") % getId());
    const auto& encodedResponse = message.getPayload();

    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    const auto& decodedResposne = encDec_->decodeData(encodedResponse.data(), encodedResponse.size());
    KAA_MUTEX_LOCKING("channelGuard_");
    lock.unlock();
    KAA_MUTEX_LOCKED("channelGuard_");

    demultiplexer_->processResponse(
            std::vector<boost::uint8_t>(reinterpret_cast<const boost::uint8_t *>(decodedResposne.data()),
                                        reinterpret_cast<const boost::uint8_t *>(decodedResposne.data() + decodedResposne.size())));

    KAA_MUTEX_LOCKING("channelGuard_");
    lock.lock();
    KAA_MUTEX_LOCKED("channelGuard_");
    if (!isFirstResponseReceived_) {
        KAA_LOG_INFO(boost::format("Channel \"%1%\". First response received") % getId());
        isFirstResponseReceived_ = true;
        if (isPendingSyncRequest_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            lock.unlock();
            KAA_MUTEX_UNLOCKED("channelGuard_");
            KAA_LOG_INFO(boost::format("Channel \"%1%\". Pending request detected. Starting SYNC...") % getId());
            syncAll();
        } else if (!ackTypes_.empty()) {
            KAA_LOG_INFO(boost::format("Channel \"%1%\". Have %2% acknowledgment requests. Starting SYNC...")
                    % getId() % ackTypes_.size());
            if (ackTypes_.size() > 1) {
                KAA_MUTEX_UNLOCKING("channelGuard_");
                lock.unlock();
                KAA_MUTEX_UNLOCKED("channelGuard_");
                syncAll();
            } else {
                KAA_MUTEX_UNLOCKING("channelGuard_");
                lock.unlock();
                KAA_MUTEX_UNLOCKED("channelGuard_");
                sync(*ackTypes_.begin());
            }
            ackTypes_.clear();
        }
    }
}

void DefaultOperationTcpChannel::onPingResponse()
{
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Ping response received") % getId());
}

void DefaultOperationTcpChannel::openConnection()
{
    const auto& ep = HttpUtils::getEndpoint(currentServer_->getHost(), currentServer_->getPort());
    boost::system::error_code errorCode;
    sock_.open(ep.protocol(), errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format("Channel \"%1%\". Failed to open socket: %2%") % getId() % errorCode.message());
        onServerFailed();
        return;
    }
    sock_.connect(ep, errorCode);
    if (errorCode) {
        KAA_LOG_ERROR(boost::format(
                        "Channel \"%1%\". Failed to connect to %2%:%3% socket: %4%")
                        % getId() % ep.address().to_string() % ep.port()
                        % errorCode.message());
        onServerFailed();
        return;
    }
    KAA_MUTEX_LOCKING("channelGuard_");
    channelGuard_.lock();
    KAA_MUTEX_LOCKED("channelGuard_");
    isConnected_ = true;
    KAA_MUTEX_UNLOCKING("channelGuard_");
    channelGuard_.unlock();
    KAA_MUTEX_UNLOCKED("channelGuard_");

    sendConnect();
    readFromSocket();
    setTimer();
}

void DefaultOperationTcpChannel::closeConnection()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    channelGuard_.lock();
    KAA_MUTEX_LOCKED("channelGuard_");
    isFirstResponseReceived_ = false;
    isConnected_ = false;
    isPendingSyncRequest_ = false;
    KAA_MUTEX_UNLOCKING("channelGuard_");
    channelGuard_.unlock();
    KAA_MUTEX_UNLOCKED("channelGuard_");

    pingTimer_.cancel();
    sendDisconnect();
    boost::system::error_code errorCode;
    sock_.shutdown(boost::asio::ip::tcp::socket::shutdown_both, errorCode);
    sock_.close(errorCode);
    responsePorcessor.flush();
}

void DefaultOperationTcpChannel::onServerFailed()
{
    closeConnection();
    channelManager_->onServerFailed(currentServer_);
}

boost::system::error_code DefaultOperationTcpChannel::sendData(const IKaaTcpRequest& request)
{
    boost::system::error_code errorCode;
    const auto& data = request.getRawMessage();
    KAA_LOG_TRACE(boost::format("Channel \"%1%\". Sending message size=%2%") % getId() % data.size());
    boost::asio::write(sock_, boost::asio::buffer(reinterpret_cast<const char *>(data.data()), data.size()), errorCode);
    return errorCode;
}

boost::system::error_code DefaultOperationTcpChannel::sendKaaSync(const std::map<TransportType, ChannelDirection>& transportTypes)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending KAASYNC message") % getId());
    const auto& requestBody = multiplexer_->compileRequest(transportTypes);
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    return sendData(KaaSyncRequest(false, true, 0, requestEncoded));
}

boost::system::error_code DefaultOperationTcpChannel::sendConnect()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Channel \"%1%\". Sending CONNECT message") % getId());
    const auto& requestBody = multiplexer_->compileRequest(SUPPORTED_TYPES);
    const auto& requestEncoded = encDec_->encodeData(requestBody.data(), requestBody.size());
    const auto& sessionKey = encDec_->getEncodedSessionKey();
    const auto& signature = encDec_->signData(sessionKey.begin(), sessionKey.size());
    return sendData(ConnectMessage(PING_TIMEOUT, signature, sessionKey, requestEncoded));
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
    boost::asio::async_read(sock_, responseBuffer_,
              boost::asio::transfer_at_least(1),
              boost::bind(&DefaultOperationTcpChannel::onReadEvent, this,
                      boost::asio::placeholders::error));
}

void DefaultOperationTcpChannel::setTimer()
{
    pingTimer_.expires_from_now(boost::posix_time::seconds(PING_TIMEOUT));
    pingTimer_.async_wait(boost::bind(&DefaultOperationTcpChannel::onPingTimeout, this, _1));
}

void DefaultOperationTcpChannel::createThreads()
{
    channelThreads_.create_thread(boost::bind(&boost::asio::io_service::run, &io_));
    channelThreads_.create_thread(boost::bind(&boost::asio::io_service::run, &io_));
}

void DefaultOperationTcpChannel::onReadEvent(const boost::system::error_code& err)
{
    if (!err) {
        std::ostringstream responseStream;
        responseStream << &responseBuffer_;
        const auto& responseStr = responseStream.str();
        responsePorcessor.processResponseBuffer(responseStr.data(), responseStr.size());
    } else if (err != boost::asio::error::eof) {
        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> channelLock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (err != boost::asio::error::operation_aborted && isConnected_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            channelLock.unlock();
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
    boost::unique_lock<boost::mutex> channelLock(channelGuard_);
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
        boost::unique_lock<boost::mutex> channelLock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (err != boost::asio::error::operation_aborted && isConnected_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            channelLock.unlock();
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
    boost::unique_lock<boost::mutex> channelLock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (isConnected_) {
        setTimer();
    }
}

void DefaultOperationTcpChannel::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    multiplexer_ = multiplexer;
}

void DefaultOperationTcpChannel::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    demultiplexer_ = demultiplexer;
}

void DefaultOperationTcpChannel::setServer(IServerInfoPtr server)
{
    if (server->getType() == ChannelType::KAATCP) {
        closeConnection();

        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        if (firstStart_) {
            createThreads();
            firstStart_ = false;
        }
        currentServer_ = boost::dynamic_pointer_cast<OperationServerKaaTcpInfo, IServerInfo>(server);
        encDec_.reset(new RsaEncoderDecoder(clientKeys_.first, clientKeys_.second, currentServer_->getPublicKey()));

        KAA_MUTEX_UNLOCKING("channelGuard_");
        lock.unlock();
        KAA_MUTEX_UNLOCKED("channelGuard_");

        io_.post(boost::bind(&DefaultOperationTcpChannel::openConnection, this));
    } else {
        KAA_LOG_ERROR(boost::format("Invalid server info for channel %1%") % getId());
    }
}

void DefaultOperationTcpChannel::sync(TransportType type)
{
    auto it = SUPPORTED_TYPES.find(type);
    if (it != SUPPORTED_TYPES.end() && (it->second == ChannelDirection::UP || it->second == ChannelDirection::BIDIRECTIONAL)) {
        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (currentServer_) {
            if (isFirstResponseReceived_) {
                KAA_MUTEX_UNLOCKING("channelGuard_");
                lock.unlock();
                KAA_MUTEX_UNLOCKED("channelGuard_");

                std::map<TransportType, ChannelDirection> types;
                types.insert(std::make_pair(type, it->second));
                for (auto typeIt = SUPPORTED_TYPES.begin(); typeIt != SUPPORTED_TYPES.end(); ++typeIt) {
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
            KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
        }
    } else {
        KAA_LOG_ERROR(boost::format("Unsupported transport type for channel %1%") % getId());
    }
}

void DefaultOperationTcpChannel::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (currentServer_) {
        if (isFirstResponseReceived_) {
            KAA_MUTEX_UNLOCKING("channelGuard_");
            lock.unlock();
            KAA_MUTEX_UNLOCKED("channelGuard_");

            boost::system::error_code errorCode = sendKaaSync(SUPPORTED_TYPES);
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
    boost::unique_lock<boost::mutex> channelLock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    KAA_LOG_DEBUG(boost::format("Going to add acknowledgment message for transport type %1% for channel %2%")
            % LoggingUtils::TransportTypeToString(type)
            % getId());
    ackTypes_.push_back(type);
}

}
