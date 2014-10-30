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

#include "kaa/channel/ServerType.hpp"
#include "kaa/channel/server/HttpServerInfo.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"
#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"

namespace kaa {

class PingServerStorage : public IPingServerStorage {
public:
    PingServerStorage(const std::string& host, const std::uint16_t& port)
        : si_(new HttpServerInfo(ServerType::BOOTSTRAP, host, port, "a2V5")) {}

    virtual IServerInfoPtr getPingServer() {
        return si_;
    }

private:
    const IServerInfoPtr si_;

};

BOOST_AUTO_TEST_SUITE(PinConnectivityTestSuite)

BOOST_AUTO_TEST_CASE(UnreachableServerTest)
{
    PingServerStorage pss("www.fake.server", 90);
    PingConnectivityChecker checker(pss);
    BOOST_CHECK(!checker.checkConnectivity());
}

BOOST_AUTO_TEST_CASE(SuccessPingTest)
{
    PingServerStorage pss("www.google.com", 80);
    PingConnectivityChecker checker(pss);
    BOOST_CHECK(checker.checkConnectivity());
}

BOOST_AUTO_TEST_SUITE_END()

}


