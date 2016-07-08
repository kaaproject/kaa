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

#include <boost/test/unit_test.hpp>

#include <string>
#include <cstdint>

#include "headers/KaaTestUtils.hpp"

#include "kaa/channel/connectivity/IPConnectivityChecker.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"
#include "kaa/channel/TransportProtocolIdConstants.hpp"

namespace kaa {

class PingServerStorage : public IPingServerStorage {
public:
    PingServerStorage(const std::string& host, const std::uint16_t& port) {
        server_ = KaaTestUtils::createTransportConnectionInfo(ServerType::BOOTSTRAP,
                                                              0x111,
                                                              TransportProtocolIdConstants::HTTP_TRANSPORT_ID,
                                                              KaaTestUtils::serializeConnectionInfo(host,
                                                                                                    port,
                                                                                                    KaaTestUtils::generateKeyPair().getPublicKey()));
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


