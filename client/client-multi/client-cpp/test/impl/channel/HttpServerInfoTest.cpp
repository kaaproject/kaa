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

#include "kaa/gen/BootstrapGen.hpp"
#include "kaa/channel/server/HttpServerInfo.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(HttpServerInfoTestSuite)

BOOST_AUTO_TEST_CASE(GetURLTest)
{
    HttpServerInfo hsi1(ServerType::OPERATIONS, "test.com", 80, "a2V5");
    BOOST_CHECK(hsi1.getUrl().getUri() == "/EP/Sync");

    HttpServerInfo hsi2(ServerType::BOOTSTRAP, "test.com", 80, "a2V5");
    BOOST_CHECK(hsi2.getUrl().getUri() == "/BS/Resolve");
}

BOOST_AUTO_TEST_SUITE_END()

}
