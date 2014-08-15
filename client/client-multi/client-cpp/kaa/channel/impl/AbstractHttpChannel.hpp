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

#ifndef ABSTRACTHTTPCHANNEL_HPP_
#define ABSTRACTHTTPCHANNEL_HPP_


#include "kaa/channel/IDataChannel.hpp"
#include "kaa/channel/server/AbstractServerInfo.hpp"
#include "kaa/http/HttpClient.hpp"

#include "kaa/channel/impl/AbstractHttpChannel.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/http/IHttpResponse.hpp"
#include "kaa/http/IHttpRequest.hpp"
#include "kaa/http/MultipartPostHttpRequest.hpp"
#include <boost/bind.hpp>
#include <boost/cstdint.hpp>
#include <boost/thread/mutex.hpp>
#include "kaa/transport/HttpDataProcessor.hpp"
#include "kaa/transport/TransportException.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"

namespace kaa {

template <ChannelType Type>
class AbstractHttpChannel : public IDataChannel {
public:
    AbstractHttpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys);
    virtual ~AbstractHttpChannel() { }

    virtual void sync(TransportType type);
    virtual void syncAll();
    virtual void syncAck(TransportType type);
    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer);
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer);
    virtual void setServer(IServerInfoPtr server);

    virtual ChannelType getType() const  { return Type; }

protected:
    typedef boost::shared_ptr<AbstractServerInfo<Type> > AbstractServerInfoPtr;

    HttpDataProcessor* getHttpDataProcessor() { return &httpDataProcessor_; }
    virtual void processTypes(const std::map<TransportType, ChannelDirection>& types, boost::unique_lock<boost::mutex>& lock);

private:
    virtual boost::shared_ptr<IHttpRequest> createRequest(AbstractServerInfoPtr server, const std::vector<boost::uint8_t>& body) = 0;
    virtual std::string retrieveResponse(const IHttpResponse& response) = 0;

private:
    KeyPair clientKeys_;

    bool lastConnectionFailed_;

    IKaaDataMultiplexer *multiplexer_;
    IKaaDataDemultiplexer *demultiplexer_;
    IKaaChannelManager *channelManager_;
    AbstractServerInfoPtr currentServer_;
    HttpDataProcessor httpDataProcessor_;
    HttpClient httpClient_;
    boost::mutex channelGuard_;
};

template <ChannelType Type>
AbstractHttpChannel<Type>::AbstractHttpChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys)
    : clientKeys_(clientKeys), lastConnectionFailed_(false)
    , multiplexer_(nullptr), demultiplexer_(nullptr), channelManager_(channelManager) {}

template <ChannelType Type>
void AbstractHttpChannel<Type>::processTypes(const std::map<TransportType, ChannelDirection>& types, boost::unique_lock<boost::mutex>& lock)
{
    AbstractServerInfoPtr server = currentServer_;

    const auto& bodyRaw = multiplexer_->compileRequest(types);
    // Creating HTTP request using the given data
    boost::shared_ptr<IHttpRequest> postRequest = createRequest(server, bodyRaw);

    KAA_MUTEX_UNLOCKING("channelGuard_");
    lock.unlock();
    KAA_MUTEX_UNLOCKED("channelGuard_");
    try {
        // Sending http request
        auto response = httpClient_.sendRequest(*postRequest);

        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lockInternal(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        // Retrieving the avro data from the HTTP response
        const std::string& processedResponse = retrieveResponse(*response);
        lastConnectionFailed_ = false;

        KAA_MUTEX_UNLOCKING("channelGuard_");
        lockInternal.unlock();
        KAA_MUTEX_UNLOCKED("channelGuard_");

        if (!processedResponse.empty()) {
            demultiplexer_->processResponse(
                    std::vector<boost::uint8_t>(reinterpret_cast<const boost::uint8_t *>(processedResponse.data()),
                                                reinterpret_cast<const boost::uint8_t *>(processedResponse.data() + processedResponse.size())));
        }
    } catch (std::exception& e) {
        KAA_LOG_ERROR(boost::format("Connection failed, server %1%:%2%: %3%") % server->getHost() % server->getPort() % e.what());

        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lockInternal(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");

        lastConnectionFailed_ = true;

        KAA_MUTEX_UNLOCKING("channelGuard_");
        lockInternal.unlock();
        KAA_MUTEX_UNLOCKED("channelGuard_");

        channelManager_->onServerFailed(server);
    }
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::sync(TransportType type)
{
    const auto& supportedTypes = getSupportedTransportTypes();
    auto it = supportedTypes.find(type);
    if (it != supportedTypes.end() && (it->second == ChannelDirection::UP || it->second == ChannelDirection::BIDIRECTIONAL)) {
        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        if (currentServer_) {
            processTypes(std::map<TransportType, ChannelDirection>({ { type, it->second } }), lock);
        } else {
            lastConnectionFailed_ = true;
            KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
        }
    } else {
        KAA_LOG_ERROR(boost::format("Unsupported transport type for channel %1%") % getId());
    }
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::syncAll()
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    if (currentServer_) {
        processTypes(getSupportedTransportTypes(), lock);
    } else {
        lastConnectionFailed_ = true;
        KAA_LOG_WARN(boost::format("Can't sync channel %1%. Server is null") % getId());
    }
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::syncAck(TransportType type)
{
    KAA_LOG_DEBUG(boost::format("Sync ack operation is not supported by channel %1%.") % getId());
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::setMultiplexer(IKaaDataMultiplexer *multiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    multiplexer_ = multiplexer;
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::setDemultiplexer(IKaaDataDemultiplexer *demultiplexer)
{
    KAA_MUTEX_LOCKING("channelGuard_");
    boost::unique_lock<boost::mutex> lock(channelGuard_);
    KAA_MUTEX_LOCKED("channelGuard_");
    demultiplexer_ = demultiplexer;
}

template <ChannelType Type>
void AbstractHttpChannel<Type>::setServer(IServerInfoPtr server)
{
    if (server->getType() == getType()) {
        KAA_MUTEX_LOCKING("channelGuard_");
        boost::unique_lock<boost::mutex> lock(channelGuard_);
        KAA_MUTEX_LOCKED("channelGuard_");
        currentServer_ = boost::dynamic_pointer_cast<AbstractServerInfo<Type>, IServerInfo>(server);
        boost::shared_ptr<IEncoderDecoder> encDec(new RsaEncoderDecoder(clientKeys_.first, clientKeys_.second, currentServer_->getPublicKey()));
        httpDataProcessor_.setEncoderDecoder(encDec);
        if (lastConnectionFailed_) {
            lastConnectionFailed_ = false;
            processTypes(getSupportedTransportTypes(), lock);
        }
    } else {
        KAA_LOG_ERROR(boost::format("Invalid server info for channel %1%") % getId());
    }
}

}

#endif /* ABSTRACTHTTPCHANNEL_HPP_ */
