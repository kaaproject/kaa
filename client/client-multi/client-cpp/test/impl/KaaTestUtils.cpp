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


#include <headers/KaaTestUtils.hpp>

#include <cstring>

#include <boost/asio/detail/socket_ops.hpp>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/channel/GenericTransportInfo.hpp"
#include "kaa/security/KeyUtils.hpp"

namespace kaa {

std::vector<uint8_t> KaaTestUtils::serializeConnectionInfo(const std::string& host,
                                                           std::int32_t port,
                                                           const PublicKey& publicKey)
{
    std::vector<uint8_t> serializedData(3 * sizeof(std::int32_t) + publicKey.size() + host.length());

    auto *data = serializedData.data();

    std::int32_t networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(publicKey.size());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, publicKey.data(), publicKey.size());
    data += publicKey.size();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(host.length());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, host.data(), host.length());
    data += host.length();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(port);
    memcpy(data, &networkOrder32, sizeof(std::int32_t));

    return serializedData;
}

ITransportConnectionInfoPtr KaaTestUtils::createTransportConnectionInfo(ServerType type,
                                                                        std::int32_t accessPointId,
                                                                        TransportProtocolId protocolId,
                                                                        const std::vector<uint8_t>& connectionData)
{
    ProtocolMetaData metaData;
    metaData.accessPointId = accessPointId;
    metaData.protocolVersionInfo.id = protocolId.getId();
    metaData.protocolVersionInfo.version = protocolId.getVersion();
    metaData.connectionInfo = connectionData;

    ITransportConnectionInfoPtr info(new GenericTransportInfo(type, metaData));
    return info;
}

KeyPair KaaTestUtils::generateKeyPair(std::size_t size)
{
    static KeyUtils keyUtils;
    return keyUtils.generateKeyPair(size);
}

} /* namespace kaa */
