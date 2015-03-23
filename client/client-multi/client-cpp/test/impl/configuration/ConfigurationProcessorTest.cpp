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

#include "kaa/configuration/IConfigurationProcessedObserver.hpp"
#include "kaa/configuration/IGenericDeltaReceiver.hpp"

#include "kaa/common/exception/KaaException.hpp"
#include "kaa/configuration/ConfigurationProcessor.hpp"

#include <fstream>
#include <avro/Compiler.hh>
#include "resources/AvroAutoGen.hpp"

#include <boost/test/unit_test.hpp>

namespace kaa {

class ConfigurationProcessorSubscriber  : public IConfigurationProcessedObserver
                                        , public IGenericDeltaReceiver
{
public:
    ConfigurationProcessorSubscriber() : configurationProcessedCalled_(false), deltasCount_(0) {}
    void onConfigurationProcessed()
    {
        configurationProcessedCalled_ = true;
    }

    void onDeltaReceived(int index, const KaaRootConfiguration& data, bool full_resync)
    {
        deltasCount_++;
    }

    bool isConfigurationProcessedCalled() { return configurationProcessedCalled_; }
    std::int32_t receivedDeltasCount() { return deltasCount_; }
    void reset() { configurationProcessedCalled_ = false; deltasCount_ = 0; }
private:
    bool configurationProcessedCalled_;
    std::int32_t deltasCount_;
};

BOOST_AUTO_TEST_SUITE(ConfigurationProcessorSuite)

BOOST_AUTO_TEST_CASE(processConfiguration)
{
    ConfigurationProcessor cp;

    ConfigurationProcessorSubscriber cps;
    cp.subscribeForUpdates(cps);
    cp.addOnProcessedObserver(cps);

    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);

    cp.processConfigurationData(getDefaultConfigData().begin(), getDefaultConfigData().size(), true);

    BOOST_CHECK(cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 1);

    cps.reset();
    cp.removeOnProcessedObserver(cps);
    cp.processConfigurationData(getDefaultConfigData().begin(), getDefaultConfigData().size(), true);
    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 1);

    cps.reset();
    cp.unsubscribeFromUpdates(cps);
    cp.processConfigurationData(getDefaultConfigData().begin(), getDefaultConfigData().size(), true);
    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);

    cps.reset();
    cp.addOnProcessedObserver(cps);
    cp.processConfigurationData(getDefaultConfigData().begin(), getDefaultConfigData().size(), true);
    BOOST_CHECK(cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa

