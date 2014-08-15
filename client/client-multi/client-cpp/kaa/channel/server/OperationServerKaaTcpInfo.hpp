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

#ifndef OPERATIONSERVERKAATCPINFO_HPP_
#define OPERATIONSERVERKAATCPINFO_HPP_

#include "kaa/channel/server/AbstractServerInfo.hpp"

namespace kaa {

class OperationServerKaaTcpInfo: public AbstractServerInfo<ChannelType::KAATCP> {
public:
    OperationServerKaaTcpInfo(const std::string& hostPort, const std::string& publicKey) :
        AbstractServerInfo(hostPort, publicKey) {}

    OperationServerKaaTcpInfo(const std::string& host, const boost::int32_t& port, const std::string& publicKey)
        : AbstractServerInfo(host, port, publicKey) {}

    OperationServerKaaTcpInfo(const std::string& host, const boost::int32_t& port
            , const Botan::MemoryVector<boost::uint8_t>& publicKey)
        : AbstractServerInfo(host, port, publicKey) {}

    virtual HttpUrl getUrl() const {
        return "";
    }
};

typedef boost::shared_ptr<OperationServerKaaTcpInfo> OperationServerKaaTcpInfoPtr;

}



#endif /* OPERATIONSERVERKAATCPINFO_HPP_ */
