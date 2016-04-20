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

#include "kaa/channel/connectivity/PingConnectivityChecker.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(PingConnectivityTestSuite)

BOOST_AUTO_TEST_CASE(DefaultHostTest)
{
    PingConnectivityChecker checker;
    BOOST_CHECK(checker.checkConnectivity());
}

BOOST_AUTO_TEST_CASE(SpecificHostTest)
{
    PingConnectivityChecker checker("www.test.com", 443);
    BOOST_CHECK(checker.checkConnectivity());
}

BOOST_AUTO_TEST_CASE(UnreachableServerTest)
{
    PingConnectivityChecker checker("www.fake.server", 90);
    BOOST_CHECK(!checker.checkConnectivity());
}

BOOST_AUTO_TEST_SUITE_END()

}
