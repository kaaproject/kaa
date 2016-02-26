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

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || defined (KAA_DEFAULT_OPERATION_HTTP_CHANNEL)

#include "kaa/channel/impl/AbstractHttpChannel.hpp"
#include "kaa/common/exception/HttpTransportException.hpp"

namespace kaa {

AbstractHttpChannel::AbstractHttpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys, IKaaClientContext &context)
    : clientKeys_(clientKeys), lastConnectionFailed_(false)
    , multiplexer_(nullptr), demultiplexer_(nullptr), channelManager_(channelManager)
    , httpDataProcessor_(context), httpClient_(context), context_(context) {}


void AbstractHttpChannel::processTypes(const std::map<TransportType, ChannelDirection>& types
#ifdef KAA_THREADSAFE
                                     , KAA_MUTEX_UNIQUE& lock
#endif
                                       )
{
    const auto& bodyRaw = multiplexer_->compileRequest(types);

    // Creating HTTP request using the given data
    std::shared_ptr<IHttpRequest> postRequest = createRequest(currentServer_, bodyRaw);

    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(lock);
    KAA_MUTEX_UNLOCKED("channelGuard_");
    try {
        // Sending http request
        auto response = httpClient_.sendRequest(*postRequest);

        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lockInternal, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        // Retrieving the avro data from the HTTP response
        const std::string& processedResponse = retrieveResponse(*response);
        lastConnectionFailed_ = false;

        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lockInternal);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        if (!processedResponse.empty()) {
            demultiplexer_->processResponse(
                    std::vector<std::uint8_t>(reinterpret_cast<const std::uint8_t *>(processedResponse.data()),
                                              reinterpret_cast<const std::uint8_t *>(processedResponse.data() + processedResponse.size())));
        }
    } catch (HttpTransportException& e) {
        switch (e.getHttpStatusCode()) {
        case HttpStatusCode::UNAUTHORIZED:
            KAA_LOG_WARN(boost::format("Connection failed, server %1%:%2%: bad credentials. Going to re-register...")
                                                            % currentServer_->getHost() % currentServer_->getPort());
            context_.getStatus().setRegistered(false);
            context_.getStatus().save();
            setServer(currentServer_);
            break;
        default:
            KAA_LOG_ERROR(boost::format("Connection failed, server %1%:%2%: %3%")
                    % currentServer_->getHost() % currentServer_->getPort() % e.what());
            onServerFailed();
            break;
        }
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Connection failed, server %1%:%2%: %3%")
            % currentServer_->getHost() % currentServer_->getPort() % e.what());
        onServerFailed();
    }
}


void AbstractHttpChannel::onServerFailed()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lockInternal, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    lastConnectionFailed_ = true;

    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(lockInternal);
    KAA_MUTEX_UNLOCKED("channelGuard_");

    channelManager_->onServerFailed(std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_));
}

void AbstractHttpChannel::sync(TransportType type)
{
    const auto& supportedTypes = getSupportedTransportTypes();
    auto it = supportedTypes.find(type);
    if (it != supportedTypes.end() && (it->second == ChannelDirection::UP || it->second == ChannelDirection::BIDIRECTIONAL)) {
        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (currentServer_) {
            processTypes(std::map<TransportType, ChannelDirection>({ { type, it->second } })
#ifdef KAA_THREADSAFE
                       , lock
#endif
                        );
        } else {
            lastConnectionFailed_ = true;
            KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
        }
    } else {
        KAA_LOG_ERROR(boost::format("Unsupported transport type for channel %1%") % getId());
    }
}


void AbstractHttpChannel::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (currentServer_) {
        processTypes(getSupportedTransportTypes()
#ifdef KAA_THREADSAFE
                   , lock
#endif
                    );
    } else {
        lastConnectionFailed_ = true;
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
    }
}


void AbstractHttpChannel::syncAck(TransportType type)
{
    KAA_LOG_DEBUG(boost::format("Sync ack operation is not supported by channel %1%.") % getId());
}


void AbstractHttpChannel::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    multiplexer_ = multiplexer;
}


void AbstractHttpChannel::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    demultiplexer_ = demultiplexer;
}


void AbstractHttpChannel::setServer(ITransportConnectionInfoPtr server)
{
    if (server->getTransportId() == getTransportProtocolId()) {
        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        currentServer_.reset(new IPTransportInfo(server));
        std::shared_ptr<IEncoderDecoder> encDec(new RsaEncoderDecoder(clientKeys_.getPublicKey(), clientKeys_.getPrivateKey(), currentServer_->getPublicKey(), context_));
        httpDataProcessor_.setEncoderDecoder(encDec);
        if (lastConnectionFailed_) {
            lastConnectionFailed_ = false;
            processTypes(getSupportedTransportTypes()
#ifdef KAA_THREADSAFE
                        , lock
#endif
                        );
        }
    } else {
        KAA_LOG_ERROR(boost::format("Invalid server info for channel %1%") % getId());
    }
}

}

#endif
