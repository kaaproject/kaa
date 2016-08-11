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

#ifndef DEFAULTBOOTSTRAPCHANNEL_HPP_
#define DEFAULTBOOTSTRAPCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/channel/impl/AbstractHttpChannel.hpp"

namespace kaa {

class DefaultBootstrapChannel : public AbstractHttpChannel {
public:
    DefaultBootstrapChannel(IKaaChannelManager& channelManager,
                            const KeyPair& clientKeys,
                            IKaaClientContext& context)
        : AbstractHttpChannel(channelManager, clientKeys, context)
    {

    }

    virtual const std::string& getId() const { return CHANNEL_ID; }
    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const { return SUPPORTED_TYPES; }

private:
    virtual std::shared_ptr<IHttpRequest> createRequest(IPTransportInfoPtr server, const std::vector<std::uint8_t>& body)
    {
        HttpDataProcessor *processor = getHttpDataProcessor();
        auto request = processor->createBootstrapRequest(server->getURL() + getURLSuffix(), body);
        return request;
    }

    virtual std::string retrieveResponse(const IHttpResponse& response)
    {
        return getHttpDataProcessor()->retrieveBootstrapResponse(response);
    }

    virtual ServerType getServerType() const {
        return ServerType::BOOTSTRAP;
    }

protected:
    virtual std::string getURLSuffix() {
        return "/BS/Sync";
    }

private:
    static const std::string CHANNEL_ID;
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;
};

}

#endif /* DEFAULTBOOTSTRAPCHANNEL_HPP_ */
