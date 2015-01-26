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

#ifndef ABSTRACTSERVERINFO_HPP_
#define ABSTRACTSERVERINFO_HPP_

#include <string>
#include <sstream>
#include <climits>
#include <cstdint>

#include <boost/lexical_cast.hpp>

#include <botan/base64.h>

#include "kaa/channel/server/IServerInfo.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/http/HttpUrl.hpp"
#include "kaa/security/SecurityDefinitions.hpp"

namespace kaa {

template<ChannelType Type>
class AbstractServerInfo : public IServerInfo {
public:
    AbstractServerInfo(ServerType type, const std::string& host, const std::int32_t& port
            , const std::string& encodedPublicKey);

    AbstractServerInfo(ServerType type, const std::string& hostPort, const std::string& encodedPublicKey);

    AbstractServerInfo(ServerType type, const std::string& host, const std::int32_t& port
            , const PublicKey& publicKey);

    virtual const std::string& getHost() const {
        return host_;
    }

    virtual std::uint16_t getPort() const {
        return port_;
    }

    virtual const PublicKey& getPublicKey() const {
        return publicKey_;
    }

    virtual HttpUrl getUrl() const {
        std::stringstream ss;
        ss << host_ << ":" << port_;
        return HttpUrl(ss.str());
    }

    virtual ChannelType getChannelType() const {
        return channelType_;
    }

    virtual ServerType getServerType() const {
        return serverType_;
    }

    virtual ~AbstractServerInfo() {}

private:
    template<typename KeyRepresentation>
    void verify(const std::string& host, const std::int32_t& port
                                , const KeyRepresentation& encodedPublicKey);

    void assign(const std::string& host, const std::int32_t& port
                                , const std::string& encodedPublicKey);
    void assign(const std::string& host, const std::int32_t& port
                    , const PublicKey& decodedPublicKey);

private:
    const ChannelType channelType_;
    const ServerType  serverType_;

    std::string        host_;
    std::uint16_t    port_;

    PublicKey   publicKey_;
};

template<ChannelType Type>
AbstractServerInfo<Type>::AbstractServerInfo(ServerType type, const std::string& host, const std::int32_t& port
        , const std::string& encodedPublicKey) : channelType_(Type), serverType_(type)
{
    verify(host, port, encodedPublicKey);
    assign(host, port, encodedPublicKey);
}

template<ChannelType Type>
AbstractServerInfo<Type>::AbstractServerInfo(ServerType type, const std::string& hostPort, const std::string& encodedPublicKey)
    : channelType_(Type), serverType_(type)
{
    if (!hostPort.empty()) {
        std::size_t delimPos = hostPort.find(':');
        std::string host;
        std::int32_t port = 0;

        if (delimPos != std::string::npos) {
            host = hostPort.substr(0, delimPos);
            try {
                port = boost::lexical_cast<std::int32_t>(hostPort.substr(delimPos + 1, std::string::npos));
            } catch (std::exception& e) {
                throw KaaException(e.what());
            }
        }

        verify(host, port, encodedPublicKey);
        assign(host, port, encodedPublicKey);
    } else {
        throw KaaException("Empty server host/port info");
    }
}

template<ChannelType Type>
AbstractServerInfo<Type>::AbstractServerInfo(ServerType type, const std::string& host, const std::int32_t& port
        , const PublicKey& publicKey) : channelType_(Type), serverType_(type)
{
    verify(host, port, publicKey);
    assign(host, port, publicKey);
}

template<ChannelType Type>
template<typename KeyRepresentation>
void AbstractServerInfo<Type>::verify(const std::string& host, const std::int32_t& port, const KeyRepresentation& encodedPublicKey)
{
    if (host.empty()) {
        throw KaaException("Empty server host");
    }

    if (port < 1 || port > USHRT_MAX) {
        throw KaaException("Server port not in valid range");
    }

    if (encodedPublicKey.empty()) {
        throw KaaException("Empty server public key");
    }
}

template<ChannelType Type>
void AbstractServerInfo<Type>::assign(const std::string& host, const std::int32_t& port, const std::string& encodedPublicKey)
{
    host_ = host;
    port_ = port;
    publicKey_ = Botan::base64_decode(encodedPublicKey);
}

template<ChannelType Type>
void AbstractServerInfo<Type>::assign(const std::string& host, const std::int32_t& port, const PublicKey& decodedPublicKey)
{
    host_ = host;
    port_ = port;
    publicKey_ = decodedPublicKey;
}

} /* namespace kaa */

#endif /* ABSTRACTSERVERINFO_HPP_ */
