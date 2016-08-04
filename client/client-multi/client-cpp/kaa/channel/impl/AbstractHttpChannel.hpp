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



#ifndef ABSTRACTHTTPCHANNEL_HPP_
#define ABSTRACTHTTPCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/channel/ImpermanentDataChannel.hpp"

#include <cstdint>

#include "kaa/KaaThread.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/http/HttpClient.hpp"
#include "kaa/http/IHttpRequest.hpp"
#include "kaa/http/IHttpResponse.hpp"
#include "kaa/channel/impl/AbstractHttpChannel.hpp"
#include "kaa/security/RsaEncoderDecoder.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/http/MultipartPostHttpRequest.hpp"
#include "kaa/transport/HttpDataProcessor.hpp"
#include "kaa/transport/TransportException.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/IPTransportInfo.hpp"
#include "kaa/channel/ITransportConnectionInfo.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class AbstractHttpChannel : public ImpermanentDataChannel {
public:
    AbstractHttpChannel(IKaaChannelManager& channelManager,
                        const KeyPair& clientKeys,
                        IKaaClientContext& context);

    virtual void sync(TransportType type);
    virtual void syncAll();
    virtual void syncAck(TransportType type);
    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer);
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer);

    virtual void setServer(ITransportConnectionInfoPtr server);

    virtual ITransportConnectionInfoPtr getServer() {
        return std::dynamic_pointer_cast<ITransportConnectionInfo, IPTransportInfo>(currentServer_);
    }

    virtual TransportProtocolId getTransportProtocolId() const {
        return TransportProtocolIdConstants::HTTP_TRANSPORT_ID;
    }

    virtual void setFailoverStrategy(IFailoverStrategyPtr strategy) {}
    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {
        connectivityChecker_ = checker;
    }

protected:
    typedef std::shared_ptr<IPTransportInfo> IPTransportInfoPtr;

    HttpDataProcessor* getHttpDataProcessor() { return &httpDataProcessor_; }

    virtual void processTypes(const std::map<TransportType, ChannelDirection>& types
#ifdef KAA_THREADSAFE
                            , KAA_MUTEX_UNIQUE& lock
#endif
                            );

    virtual std::string getURLSuffix() = 0;

private:
    virtual std::shared_ptr<IHttpRequest> createRequest(IPTransportInfoPtr server, const std::vector<std::uint8_t>& body) = 0;
    virtual std::string retrieveResponse(const IHttpResponse& response) = 0;

    void onServerFailed(KaaFailoverReason reason);

private:
    IKaaChannelManager&      channelManager_;
    IKaaClientContext&       context_;
    KeyPair                  clientKeys_;
    IPTransportInfoPtr       currentServer_;
    HttpDataProcessor        httpDataProcessor_;
    HttpClient               httpClient_;

    KAA_MUTEX_DECLARE(channelGuard_);

    IKaaDataMultiplexer      *multiplexer_   = nullptr;
    IKaaDataDemultiplexer    *demultiplexer_ = nullptr;
    ConnectivityCheckerPtr   connectivityChecker_;
};

}

#endif /* ABSTRACTHTTPCHANNEL_HPP_ */

