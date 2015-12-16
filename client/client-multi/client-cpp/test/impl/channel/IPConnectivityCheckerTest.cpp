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

#include <boost/test/unit_test.hpp>

#include <string>
#include <cstdint>

#include <boost/asio/detail/socket_ops.hpp>

#include "kaa/channel/ServerType.hpp"
#include "kaa/channel/connectivity/IPConnectivityChecker.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"
#include "kaa/channel/GenericTransportInfo.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"

namespace kaa {

std::vector<uint8_t> serializeConnectionInfo(const std::string& publicKey
                                           , const std::string& host
                                           , const std::int32_t& port)
{
    std::vector<uint8_t> serializedData(3 * sizeof(std::int32_t) + publicKey.length() + host.length());

    auto *data = serializedData.data();

    std::int32_t networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(publicKey.length());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, publicKey.data(), publicKey.length());
    data += publicKey.length();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(host.length());
    memcpy(data, &networkOrder32, sizeof(std::int32_t));
    data += sizeof(std::int32_t);

    memcpy(data, host.data(), host.length());
    data += host.length();

    networkOrder32 = boost::asio::detail::socket_ops::host_to_network_long(port);
    memcpy(data, &networkOrder32, sizeof(std::int32_t));

    return serializedData;
}

ITransportConnectionInfoPtr createTransportConnectionInfo(ServerType type
                                                        , const std::int32_t& accessPointId
                                                        , TransportProtocolId protocolId
                                                        , const std::vector<uint8_t>& connectionData)
{
    ProtocolMetaData metaData;
    metaData.accessPointId = accessPointId;
    metaData.protocolVersionInfo.id = protocolId.getId();
    metaData.protocolVersionInfo.version = protocolId.getVersion();
    metaData.connectionInfo = connectionData;

    ITransportConnectionInfoPtr info(new GenericTransportInfo(type, metaData));
    return info;
}

class PingServerStorage : public IPingServerStorage {
public:
    PingServerStorage(const std::string& host, const std::uint16_t& port) {
        server_ = createTransportConnectionInfo(ServerType::BOOTSTRAP
                                          , 0x111
                                          , TransportProtocolIdConstants::HTTP_TRANSPORT_ID
                                          , serializeConnectionInfo("key"
                                                                  , host
                                                                  , port));
    }

    virtual ITransportConnectionInfoPtr getPingServer() {
        return server_;
    }

private:
    ITransportConnectionInfoPtr server_;

};

BOOST_AUTO_TEST_SUITE(IPConnectivityTestSuite)

BOOST_AUTO_TEST_CASE(UnreachableServerTest)
{
    PingServerStorage pss("www.fake.server", 90);
    IPConnectivityChecker checker(pss);
    BOOST_CHECK(!checker.checkConnectivity());
}

BOOST_AUTO_TEST_CASE(SuccessPingTest)
{
    PingServerStorage pss("www.google.com", 80);
    IPConnectivityChecker checker(pss);
    BOOST_CHECK(checker.checkConnectivity());
}

BOOST_AUTO_TEST_SUITE_END()

}


