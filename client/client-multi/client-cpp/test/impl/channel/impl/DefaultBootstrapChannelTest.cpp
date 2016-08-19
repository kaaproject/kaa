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

#include <map>
#include <memory>

#include "kaa/channel/impl/DefaultBootstrapChannel.hpp"
#include "kaa/security/KeyUtils.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/KaaTestUtils.hpp"
#include "headers/channel/MockChannelManager.hpp"
#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/channel/MockKaaDataMultiplexer.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(AbstractHttpChannelTestSuite)

BOOST_AUTO_TEST_CASE(DetectConnectionFailureToFakeServer)
{
    DefaultLogger logger("client_id");
    KaaClientProperties properties;
    auto statePtr = std::make_shared<MockKaaClientStateStorage>();
    MockExecutorContext executor;
    KaaClientContext clientContext(properties, logger, executor, statePtr);

    MockChannelManager mockChannelManager;
    MockKaaDataMultiplexer mockMultiplexer;

    auto keyPair = KaaTestUtils::generateKeyPair();

    DefaultBootstrapChannel channel(mockChannelManager, keyPair, clientContext);

    auto connectionInfo = KaaTestUtils::serializeConnectionInfo("fake.server.com",
                                                                80,
                                                                keyPair.getPublicKey());

    channel.setMultiplexer(&mockMultiplexer);

    channel.setServer(KaaTestUtils::createTransportConnectionInfo(ServerType::BOOTSTRAP,
                                                                  std::rand(),
                                                                  TransportProtocolIdConstants::HTTP_TRANSPORT_ID,
                                                                  connectionInfo));

    channel.syncAll();

    BOOST_CHECK_EQUAL(mockChannelManager.onServerFailed_, 1);
}

BOOST_AUTO_TEST_SUITE_END()

}
