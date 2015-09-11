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

#include "kaa/configuration/storage/ConfigurationPersistenceManager.hpp"
#include "kaa/configuration/IConfigurationProcessor.hpp"
#include "kaa/configuration/IConfigurationProcessedObservable.hpp"
#include "kaa/configuration/IDecodedDeltaObservable.hpp"
#include "kaa/configuration/IGenericDeltaReceiver.hpp"
#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/ClientStatus.hpp"

#include <avro/Compiler.hh>

#include <boost/test/unit_test.hpp>

#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

class ConfigurationProcessorStub : public IConfigurationProcessor,
                                   public IDecodedDeltaObservable,
                                   public IConfigurationProcessedObservable
{
public:
    ConfigurationProcessorStub() : processConfigurationCalled_(false) {}
    void processConfigurationData(const std::uint8_t *data, std::size_t dataLength, bool fullResync)
    {
        processConfigurationCalled_ = true;
    }

    void subscribeForUpdates(IGenericDeltaReceiver &receiver) {}
    void unsubscribeFromUpdates(IGenericDeltaReceiver &receiver) {}
    void addOnProcessedObserver(IConfigurationProcessedObserver &observer) {}
    void removeOnProcessedObserver(IConfigurationProcessedObserver &observer) {}

    bool isProcessConfigurationCalled() { return processConfigurationCalled_; }

private:
    bool processConfigurationCalled_;
};

class CoonfigurationStorageStub : public IConfigurationStorage
{
public:
    CoonfigurationStorageStub() : configurationSaveCalled_(false), configurationLoadCalled_(false), configuration_(getDefaultConfigData().begin(), getDefaultConfigData().end())
    {

    }

    void saveConfiguration(std::vector<std::uint8_t> &&bytes)
    {
        configurationSaveCalled_ = true;
        configuration_ = std::move(bytes);
    }

    std::vector<std::uint8_t> loadConfiguration()
    {
        configurationLoadCalled_ = true;
        return configuration_;
    }

    virtual void clearConfiguration()
    {

    }

    bool isSaveCalled() { return configurationSaveCalled_; }
    bool isLoadCalled() { return configurationLoadCalled_; }

private:
    bool configurationSaveCalled_;
    bool configurationLoadCalled_;
    std::vector<std::uint8_t> configuration_;
};


BOOST_AUTO_TEST_SUITE(ConfigurationPersistenceSuite)


BOOST_AUTO_TEST_CASE(checkConfigurationPersistence)
{
    AvroByteArrayConverter<KaaRootConfiguration> converter;
    KaaRootConfiguration configuration = converter.fromByteArray(getDefaultConfigData().begin(), getDefaultConfigData().size());
    EndpointObjectHash checkHash(getDefaultConfigData().begin(), getDefaultConfigData().size());

    ConfigurationPersistenceManager cpm(IKaaClientStateStoragePtr(new MockKaaClientStateStorage));

    try {
        ConfigurationProcessorStub cpstub;

        std::shared_ptr<CoonfigurationStorageStub> csstub = std::make_shared<CoonfigurationStorageStub>();

        cpm.setConfigurationProcessor(&cpstub);
        cpm.setConfigurationStorage(csstub);

        BOOST_CHECK(csstub->isLoadCalled());
        BOOST_CHECK(!csstub->isSaveCalled());
        BOOST_CHECK(cpstub.isProcessConfigurationCalled());

        cpm.onConfigurationUpdated(configuration);
        BOOST_CHECK(!csstub->isSaveCalled());

        cpm.onConfigurationUpdated(configuration);
        BOOST_CHECK(csstub->isSaveCalled());

        BOOST_CHECK(cpm.getConfigurationHash() == checkHash);

    } catch (...) {
        BOOST_CHECK(false);
    }
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
