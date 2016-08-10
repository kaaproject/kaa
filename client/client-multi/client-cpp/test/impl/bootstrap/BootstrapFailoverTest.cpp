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
#include <cstdlib>
#include <cstdint>

#include "kaa/KaaClient.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/bootstrap/BootstrapManager.hpp"
#include "kaa/failover/DefaultFailoverStrategy.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "test/headers/channel/MockChannelManager.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientProperties.hpp"

#include "test/headers/MockKaaClientStateStorage.hpp"


namespace kaa {

BOOST_AUTO_TEST_SUITE(BootstrapFailoverTest)

BOOST_AUTO_TEST_CASE(BootstrapEmptyOperationalServersListTest)
{
    KaaClientProperties properties;
    DefaultLogger tmp_logger(properties.getClientId());

    SimpleExecutorContext exeContext;
    IKaaClientStateStoragePtr status (new MockKaaClientStateStorage);
    KaaClientContext context(properties, tmp_logger, exeContext, status);
    BootstrapManager bootstrapManager(context, nullptr);

    MockChannelManager channelManager;
    std::vector<ProtocolMetaData> operationServers;

    bootstrapManager.setChannelManager(&channelManager);
    bootstrapManager.setFailoverStrategy(std::make_shared<DefaultFailoverStrategy>(context));

    bootstrapManager.onServerListUpdated(operationServers);

    BOOST_CHECK(channelManager.onServerFailed_);
}

BOOST_AUTO_TEST_SUITE_END()

}
