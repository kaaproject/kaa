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

#include <fstream>

#include "kaa/common/types/CommonRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <kaa/configuration/ConfigurationProcessor.hpp>
#include <kaa/configuration/manager/ConfigurationManager.hpp>
#include <kaa/configuration/manager/IConfigurationReceiver.hpp>
#include <kaa/configuration/storage/ConfigurationPersistenceManager.hpp>

#include "headers/configuration/ConfigurationTests.hpp"

#include <avro/Compiler.hh>
#include <avro/Specific.hh>
#include <avro/Stream.hh>
#include <avro/Encoder.hh>
#include <avro/Decoder.hh>

#include <boost/test/unit_test.hpp>

namespace kaa {

class ConfigurationReceiverStub : public IConfigurationReceiver
{
public:
    void onConfigurationUpdated(const ICommonRecord &configuration){
        if (checker_ != NULL) {
            checker_->checkLoadedConfiguration(configuration);
        }
    }

    void setConfigurationChecker(ICheckConfiguration *checker)
    {
        checker_ = checker;
    }
private:
    ICheckConfiguration *checker_;
};


#ifdef RESOURCE_DIR
const char * const schema_path = RESOURCE_DIR"/configuration_schema.json";
#else
#error "No path to resources defined!"
#endif

ConfigurationManager manager;

BOOST_AUTO_TEST_SUITE(ProcessConfigurationSuite)

BOOST_AUTO_TEST_CASE(checkRootConfigurationLoad)
{
    std::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);
    ConfigurationProcessor processor(schema);
    ConfigurationPersistenceManager cpm;
    cpm.onSchemaUpdated(schema);
    manager.subscribeForConfigurationChanges(cpm);

    ConfigurationReceiverStub receiver;
    manager.subscribeForConfigurationChanges(receiver);

    processor.addOnProcessedObserver(manager);
    processor.subscribeForUpdates(manager);

    std::shared_ptr<AbstractProcessConfigurationTest> frtest(new FullResyncTest);

    std::uint8_t *data;
    std::size_t len;
    frtest->createDelta(data, len);
    receiver.setConfigurationChecker(frtest.get());
    processor.processConfigurationData(data, len, true);
    delete[] data;
    manager.unsubscribeFromConfigurationChanges(cpm);
    manager.unsubscribeFromConfigurationChanges(receiver);
}

BOOST_AUTO_TEST_CASE(checkOverrideByUuid)
{
    std::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);
    ConfigurationProcessor processor(schema);
    ConfigurationPersistenceManager cpm;
    cpm.onSchemaUpdated(schema);
    manager.subscribeForConfigurationChanges(cpm);

    ConfigurationReceiverStub receiver;

    manager.subscribeForConfigurationChanges(receiver);
    processor.addOnProcessedObserver(manager);
    processor.subscribeForUpdates(manager);

    std::shared_ptr<AbstractProcessConfigurationTest> frtest(new OverrideItemByUuidTest);

    std::uint8_t *data;
    std::size_t len;
    frtest->createDelta(data, len);
    receiver.setConfigurationChecker(frtest.get());
    processor.processConfigurationData(data, len, false);
    delete[] data;
    manager.unsubscribeFromConfigurationChanges(cpm);
    manager.unsubscribeFromConfigurationChanges(receiver);
}

BOOST_AUTO_TEST_CASE(removeTwoArrayItemsAndAddNewItemsToArray)
{
    std::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);
    ConfigurationProcessor processor(schema);
    ConfigurationPersistenceManager cpm;
    cpm.onSchemaUpdated(schema);
    manager.subscribeForConfigurationChanges(cpm);

    ConfigurationReceiverStub receiver;

    manager.subscribeForConfigurationChanges(receiver);
    processor.addOnProcessedObserver(manager);
    processor.subscribeForUpdates(manager);

    std::shared_ptr<AbstractProcessConfigurationTest> frtest(new RemoveTwoArrayItemsAndAddNewItemsToArrayTest);

    std::uint8_t *data;
    std::size_t len;
    frtest->createDelta(data, len);
    receiver.setConfigurationChecker(frtest.get());
    processor.processConfigurationData(data, len, false);
    delete[] data;
    manager.unsubscribeFromConfigurationChanges(cpm);
    manager.unsubscribeFromConfigurationChanges(receiver);
}

BOOST_AUTO_TEST_CASE(resetArrayAddMoreItemsOfDiffTypeToArray)
{
    std::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);
    ConfigurationProcessor processor(schema);
    ConfigurationPersistenceManager cpm;
    cpm.onSchemaUpdated(schema);
    manager.subscribeForConfigurationChanges(cpm);

    ConfigurationReceiverStub receiver;

    manager.subscribeForConfigurationChanges(receiver);
    processor.addOnProcessedObserver(manager);
    processor.subscribeForUpdates(manager);

    std::shared_ptr<AbstractProcessConfigurationTest> frtest(new ResetArrayAddMoreItemsOfDiffTypeToArrayTest);

    std::uint8_t *data;
    std::size_t len;
    frtest->createDelta(data, len);
    receiver.setConfigurationChecker(frtest.get());
    processor.processConfigurationData(data, len, false);
    delete[] data;
    manager.unsubscribeFromConfigurationChanges(cpm);
    manager.unsubscribeFromConfigurationChanges(receiver);
}

BOOST_AUTO_TEST_CASE(resetArrayOfDiff)
{
    std::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);
    ConfigurationProcessor processor(schema);
    ConfigurationPersistenceManager cpm;
    cpm.onSchemaUpdated(schema);
    manager.subscribeForConfigurationChanges(cpm);

    ConfigurationReceiverStub receiver;

    manager.subscribeForConfigurationChanges(receiver);
    processor.addOnProcessedObserver(manager);
    processor.subscribeForUpdates(manager);

    std::shared_ptr<AbstractProcessConfigurationTest> frtest(new ResetArrayOfDiffTest);

    std::uint8_t *data;
    std::size_t len;
    frtest->createDelta(data, len);
    receiver.setConfigurationChecker(frtest.get());
    processor.processConfigurationData(data, len, false);
    delete[] data;
    manager.unsubscribeFromConfigurationChanges(cpm);
    manager.unsubscribeFromConfigurationChanges(receiver);
}

BOOST_AUTO_TEST_CASE(getConfiguration)
{
    ICommonRecord& config = manager.getConfiguration();

    // TODO: Need to do smth with this, not just calling. E.g. test json for validness.
    std::string json_config = config.toString();
}

BOOST_AUTO_TEST_CASE(getExistingField)
{
    ICommonRecord& config = manager.getConfiguration();
    BOOST_REQUIRE_NO_THROW(config.getField("testField1"));
}

BOOST_AUTO_TEST_CASE(getNonExistingField)
{
    ICommonRecord& config = manager.getConfiguration();
    BOOST_REQUIRE_THROW(config.getField("testField100"), KaaException);
}

BOOST_AUTO_TEST_CASE(removeExistingField)
{
    ICommonRecord& config = manager.getConfiguration();
    BOOST_REQUIRE_NO_THROW(config.removeField("testField1"));
}

BOOST_AUTO_TEST_CASE(removeNonExistingField)
{
    ICommonRecord& config = manager.getConfiguration();
    BOOST_REQUIRE_THROW(config.removeField("testField100"), KaaException);
}

BOOST_AUTO_TEST_SUITE_END()

} // namespace kaa

