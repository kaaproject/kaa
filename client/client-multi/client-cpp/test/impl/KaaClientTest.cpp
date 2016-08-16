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

#include "kaa/ClientStatus.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClient.hpp"
#include "kaa/Kaa.hpp"

#include <string>

namespace kaa {

BOOST_AUTO_TEST_SUITE(KaaClientSuite)

BOOST_AUTO_TEST_CASE(endpointKeyHash)
{
    auto kaaClient = Kaa::newClient();

    auto &status = kaaClient->getKaaClientContext().getStatus();

    BOOST_CHECK_EQUAL(status.getEndpointKeyHash(), kaaClient->getEndpointKeyHash());
}

BOOST_AUTO_TEST_SUITE_END()

}
