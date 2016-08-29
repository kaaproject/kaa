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

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || defined (KAA_DEFAULT_OPERATION_HTTP_CHANNEL)

#include "kaa/channel/impl/AbstractHttpChannel.hpp"
#include "kaa/common/exception/HttpTransportException.hpp"

namespace kaa {

static KaaFailoverReason getNotAccessibleFailoverReason(ServerType type)
{
    return type == ServerType::BOOTSTRAP ?
                KaaFailoverReason::CURRENT_BOOTSTRAP_SERVER_NA :
                KaaFailoverReason::CURRENT_OPERATIONS_SERVER_NA;
}

AbstractHttpChannel::AbstractHttpChannel(IKaaChannelManager& channelManager,
                                         const KeyPair& clientKeys,
                                         IKaaClientContext& context)
    : channelManager_(channelManager)
    , context_(context)
    , clientKeys_(clientKeys)
    , httpDataProcessor_(context)
    , httpClient_(context)
{

}

void AbstractHttpChannel::processTypes(const std::map<TransportType, ChannelDirection>& types
#ifdef KAA_THREADSAFE
                                     , KAA_MUTEX_UNIQUE& lock
#endif
                                       )
{
    auto postRequest = createRequest(currentServer_, multiplexer_->compileRequest(types));

    KAA_MUTEX_UNLOCKING("channelGuard_");
    KAA_UNLOCK(lock);
    KAA_MUTEX_UNLOCKED("channelGuard_");

    try {
        // Sending http request
        EndpointConnectionInfo connection("", "", getServerType());
        auto response = httpClient_.sendRequest(*postRequest, &connection);
        channelManager_.onConnected(connection);

        KAA_MUTEX_LOCKING("channelGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(lockInternal, channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        // Retrieving the avro data from the HTTP response
        const std::string& processedResponse = retrieveResponse(*response);

        KAA_MUTEX_UNLOCKING("channelGuard_");
        KAA_UNLOCK(lockInternal);
        KAA_MUTEX_UNLOCKED("channelGuard_");

        if (!processedResponse.empty()) {
            demultiplexer_->processResponse(
                    std::vector<std::uint8_t>(reinterpret_cast<const std::uint8_t *>(processedResponse.data()),
                                              reinterpret_cast<const std::uint8_t *>(processedResponse.data() + processedResponse.size())));
        }
    } catch (HttpTransportException& e) {
        KAA_LOG_WARN(boost::format("Channel [%1%] failed to connect %2%:%3%: %4%")
                                                                         % getId()
                                                                         % currentServer_->getHost()
                                                                         % currentServer_->getPort()
                                                                         % e.getHttpStatusCode());

        KaaFailoverReason reason;
        switch (e.getHttpStatusCode()) {
        case HttpStatusCode::UNAUTHORIZED:
            reason = KaaFailoverReason::ENDPOINT_NOT_REGISTERED;
            break;
        case HttpStatusCode::FORBIDDEN:
            reason = KaaFailoverReason::CREDENTIALS_REVOKED;
            break;
        default:
            reason = getNotAccessibleFailoverReason(getServerType());
            break;
        }

        onServerFailed(reason);
    } catch (TransportException& e) {
        KAA_LOG_WARN(boost::format("Channel [%1%] failed to connect %2%:%3%: %4%")
                                                                     % getId()
                                                                     % currentServer_->getHost()
                                                                     % currentServer_->getPort()
                                                                     % e.getErrorCode().message());

        KaaFailoverReason reason = getNotAccessibleFailoverReason(getServerType());
        if (connectivityChecker_ && !connectivityChecker_->checkConnectivity()) {
            KAA_LOG_WARN(boost::format("Channel [%1%] detected loss of connectivity")
                                                                          % getId());
            reason = KaaFailoverReason::NO_CONNECTIVITY;
        }

        onServerFailed(reason);
    }
}


void AbstractHttpChannel::onServerFailed(KaaFailoverReason reason)
{
    auto server = std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);

    KAA_LOG_WARN(boost::format("Channel [%1%] detected '%2%' failover for %3%")
                                                         % getId()
                                                         % LoggingUtils::toString(reason)
                                                         % LoggingUtils::toString(*server));

    channelManager_.onServerFailed(server, reason);
}

void AbstractHttpChannel::sync(TransportType type)
{
    const auto& supportedTypes = getSupportedTransportTypes();
    auto it = supportedTypes.find(type);
    if (it == supportedTypes.end() || it->second == ChannelDirection::DOWN) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] unsupported transport type '%2%'")
                                                        % getId()
                                                        % LoggingUtils::toString(type));
        return;
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (!currentServer_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: server is null") % getId());
        return;
    }

    processTypes(std::map<TransportType, ChannelDirection>({ { type, it->second } })
#ifdef KAA_THREADSAFE
               , lock
#endif
                );

}


void AbstractHttpChannel::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    if (!currentServer_) {
        KAA_LOG_WARN(boost::format("Channel [%1%] can't sync: server is null") % getId());
        return;
    }

    processTypes(getSupportedTransportTypes()
#ifdef KAA_THREADSAFE
               , lock
#endif
                );
}

void AbstractHttpChannel::syncAck(TransportType type)
{
    KAA_LOG_WARN(boost::format("Channel [%1%] not support sync ACK operation") % getId());
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
    if (server->getTransportId() != getTransportProtocolId()) {
        KAA_LOG_ERROR(boost::format("Channel [%1%] ignored invalid server info") % getId());
        return;
    }

    KAA_MUTEX_LOCKING("channelGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(lock, channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");

    KAA_LOG_TRACE(boost::format("Channel [%1%] preparing to use new server %2%")
                                                            % getId()
                                                            % LoggingUtils::toString(*server));

    currentServer_ = std::make_shared<IPTransportInfo>(server);

    httpDataProcessor_.setEncoderDecoder(
            std::make_shared<RsaEncoderDecoder>(clientKeys_.getPublicKey(),
                                                clientKeys_.getPrivateKey(),
                                                currentServer_->getPublicKey(),
                                                context_));
}

}

#endif
