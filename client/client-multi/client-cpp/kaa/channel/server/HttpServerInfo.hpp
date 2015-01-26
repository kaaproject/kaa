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

#ifndef OPERATIONSERVERHTTPINFO_HPP_
#define OPERATIONSERVERHTTPINFO_HPP_


#include "kaa/channel/server/AbstractServerInfo.hpp"

namespace kaa {

class HttpServerInfo: public AbstractServerInfo<ChannelType::HTTP> {
public:
    HttpServerInfo(ServerType type, const std::string& hostPort, const std::string& publicKey) :
        AbstractServerInfo(type, hostPort, publicKey)
    {
        constructURL(type);
    }

    HttpServerInfo(ServerType type, const std::string& host, const std::int32_t& port, const std::string& publicKey)
        : AbstractServerInfo(type, host, port, publicKey)
    {
        constructURL(type);
    }

    HttpServerInfo(ServerType type, const std::string& host, const std::int32_t& port
            , const PublicKey& publicKey)
        : AbstractServerInfo(type, host, port, publicKey)
    {
        constructURL(type);
    }

    virtual HttpUrl getUrl() const {
        return HttpUrl(url_);
    }

private:
    void constructURL(ServerType type) {
        std::stringstream ss;
        ss << getHost() << ":" << getPort();
        if (type == ServerType::OPERATIONS) {
            ss << "/EP/Sync";
        } else {
            ss << "/BS/Resolve";
        }
        url_ = ss.str();
    }

private:
    std::string url_;
};

typedef std::shared_ptr<HttpServerInfo> OperationServerHttpInfoPtr;

}



#endif /* OPERATIONSERVERHTTPINFO_HPP_ */
