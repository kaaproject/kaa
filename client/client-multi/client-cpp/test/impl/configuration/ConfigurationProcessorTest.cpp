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

#include "headers/configuration/AbstractConfigurationDeltaCreator.hpp"

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

    void onDeltaRecevied(int index, const avro::GenericDatum &data, bool full_resync)
    {
        deltasCount_++;
    }

    bool isConfigurationProcessedCalled() { return configurationProcessedCalled_; }
    int32_t receivedDeltasCount() { return deltasCount_; }
    void reset() { configurationProcessedCalled_ = false; deltasCount_ = 0; }
private:
    bool configurationProcessedCalled_;
    int32_t deltasCount_;
};

class DeltaCreator : public AbstractConfigurationDeltaCreator
{
public:
    DeltaCreator() {}
    std::vector<deltaT> fillDelta()
    {
        testArrayRecord2T testArrayRecord2;
        testArrayRecord2.testArray2.set_resetT(resetT::reset);
        testArrayRecord2.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};

        deltaT::delta_t delta;
        delta.set_testArrayRecord2T(testArrayRecord2);
        deltaT delta2;
        delta2.delta  = delta;

        std::vector<deltaT > deltas;
        deltas.push_back(delta2);

        return deltas;
    }
};

#ifdef RESOURCE_DIR
const char * const schema_path = RESOURCE_DIR"/configuration_schema.json";
#else
#error "No path to resources defined!"
#endif

BOOST_AUTO_TEST_SUITE(ConfigurationProcessorSuite)

BOOST_AUTO_TEST_CASE(processConfiguration)
{
    ConfigurationProcessor cp;
    boost::shared_ptr<avro::ValidSchema> schema;
    BOOST_REQUIRE_THROW(cp.onSchemaUpdated(schema), KaaException);

    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);
    avro::compileJsonSchema(is, *schema);

    BOOST_REQUIRE_NO_THROW(cp.onSchemaUpdated(schema));

    ConfigurationProcessorSubscriber cps;
    cp.subscribeForUpdates(cps);
    cp.addOnProcessedObserver(cps);

    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);

    boost::uint8_t *delta;
    size_t          delta_len;

    DeltaCreator dc;
    dc.createDelta(delta, delta_len);
    cp.processConfigurationData(delta, delta_len, true);

    BOOST_CHECK(cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 1);

    cps.reset();
    cp.removeOnProcessedObserver(cps);
    cp.processConfigurationData(delta, delta_len, true);
    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 1);

    cps.reset();
    cp.unsubscribeFromUpdates(cps);
    cp.processConfigurationData(delta, delta_len, true);
    BOOST_CHECK(!cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);

    cps.reset();
    cp.addOnProcessedObserver(cps);
    cp.processConfigurationData(delta, delta_len, true);
    BOOST_CHECK(cps.isConfigurationProcessedCalled());
    BOOST_CHECK(cps.receivedDeltasCount() == 0);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa

