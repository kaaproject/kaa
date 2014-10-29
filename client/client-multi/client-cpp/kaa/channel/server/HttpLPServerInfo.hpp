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

#ifndef OPERATIONSERVERLONGPOLLINFO_HPP_
#define OPERATIONSERVERLONGPOLLINFO_HPP_

#include "kaa/channel/server/AbstractServerInfo.hpp"

namespace kaa {

class HttpLPServerInfo: public AbstractServerInfo<ChannelType::HTTP_LP> {
public:
    HttpLPServerInfo(ServerType type, const std::string& hostPort, const std::string& publicKey) :
        AbstractServerInfo(type, hostPort, publicKey) {}

    HttpLPServerInfo(ServerType type, const std::string& host, const std::int32_t& port, const std::string& publicKey)
        : AbstractServerInfo(type, host, port, publicKey) {}

    HttpLPServerInfo(ServerType type, const std::string& host, const std::int32_t& port
            , const Botan::MemoryVector<std::uint8_t>& publicKey)
        : AbstractServerInfo(type, host, port, publicKey) {}

    virtual HttpUrl getUrl() const {
        std::stringstream ss;
        ss << getHost() << ":" << getPort() << "/EP/LongSync";
        return HttpUrl(ss.str());
    }
};

typedef std::shared_ptr<HttpLPServerInfo> OperationServerLongPollInfoPtr;

}



#endif /* OPERATIONSERVERLONGPOLLINFO_HPP_ */
