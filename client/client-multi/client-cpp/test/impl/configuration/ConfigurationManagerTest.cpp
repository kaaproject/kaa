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

#include "kaa/configuration/manager/ConfigurationManager.hpp"
#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "kaa/context/SimpleExecutorContext.hpp"

#include <fstream>
#include <thread>
#include <chrono>
#include <avro/Compiler.hh>
#include "resources/AvroAutoGen.hpp"

#include <boost/test/unit_test.hpp>

#include "kaa/configuration/manager/IConfigurationReceiver.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/context/MockExecutorContext.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

static void testSleep(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
}

class ConfigurationReceiverMock : public IConfigurationReceiver
{
public:
    ConfigurationReceiverMock() : isConfigurationReceived_(false) { }

    virtual void onConfigurationUpdated(const KaaRootConfiguration &configuration)
    {
        isConfigurationReceived_ = true;
    }

    void reset() { isConfigurationReceived_ = false; }

    bool isConfigurationReceived() const { return isConfigurationReceived_; }

private:
    bool isConfigurationReceived_;
};


BOOST_AUTO_TEST_SUITE(ConfigurationManagerSuite)

BOOST_AUTO_TEST_CASE(configurationUpdated)
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    KaaClientProperties properties;
    DefaultLogger tmp_logger(properties.getClientId());
    SimpleExecutorContext context;
    KaaClientContext clientContext(properties, tmp_logger, context, stateMock);
    context.init();
    ConfigurationManager manager(clientContext);
    ConfigurationReceiverMock receiver;

    manager.addReceiver(receiver);

    AvroByteArrayConverter<KaaRootConfiguration> convert;
    auto rootConfig = std::make_shared<KaaRootConfiguration>();
    convert.fromByteArray(getDefaultConfigData().data(), getDefaultConfigData().size(), *rootConfig);

    manager.processConfigurationData(std::vector<std::uint8_t>(getDefaultConfigData().begin(), getDefaultConfigData().begin() + getDefaultConfigData().size()), true);
    testSleep(1);
    BOOST_CHECK(receiver.isConfigurationReceived());

    manager.removeReceiver(receiver);
    receiver.reset();
    manager.processConfigurationData(std::vector<std::uint8_t>(getDefaultConfigData().begin(), getDefaultConfigData().begin() + getDefaultConfigData().size()), true);
    testSleep(1);
    BOOST_CHECK(!receiver.isConfigurationReceived());

    const auto& checkConfiguration = manager.getConfiguration();

    BOOST_CHECK(checkConfiguration.data == (*rootConfig).data);
}

BOOST_AUTO_TEST_CASE(configurationPartialUpdated)
{
    IKaaClientStateStoragePtr stateMock(new MockKaaClientStateStorage);
    MockExecutorContext context;
    KaaClientProperties properties;
    DefaultLogger logger(properties.getClientId());
    KaaClientContext clientContext(properties, logger, context, stateMock);
    ConfigurationManager manager(clientContext);

    BOOST_CHECK_THROW(manager.processConfigurationData(std::vector<std::uint8_t>(getDefaultConfigData().begin(), getDefaultConfigData().begin() + getDefaultConfigData().size()), false);, KaaException);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa

