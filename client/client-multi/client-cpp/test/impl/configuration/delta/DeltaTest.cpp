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

#include <fstream>
#include <algorithm>

#include <boost/any.hpp>
#include <boost/uuid/uuid.hpp>

#include <avro/Compiler.hh>

#include "resources/AvroAutoGen.hpp"
#include "headers/configuration/ConfigurationTests.hpp"

#include "kaa/configuration/ConfigurationProcessor.hpp"
#include "kaa/configuration/delta/IDeltaType.hpp"
#include "kaa/configuration/delta/EmptyDeltaType.hpp"
#include "kaa/configuration/delta/DefaultDeltaType.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/configuration/delta/ValueDeltaType.hpp"
#include "kaa/configuration/delta/ResetDeltaType.hpp"
#include "kaa/configuration/delta/IConfigurationDelta.hpp"
#include "kaa/configuration/delta/RemovedItemsDeltaType.hpp"
#include "kaa/configuration/delta/AddedItemsDeltaType.hpp"
#include "kaa/configuration/delta/manager/DefaultDeltaManager.hpp"

#ifdef RESOURCE_DIR
const char * const schema_path = RESOURCE_DIR"/configuration_schema.json";
#else
#error "No path to resources defined!"
#endif

namespace kaa {

class FullResyncReceiver : public IDeltaReceiver {
public:
    void loadDelta(ConfigurationDeltaPtr delta) {
        BOOST_CHECK_MESSAGE(delta,"Delta is empty");

        std::string fullDeltaToJson = delta->toString();

        BOOST_CHECK_MESSAGE(!fullDeltaToJson.empty(),"Delta representation in JSON is empty");

        DeltaTypePtr testField1 = delta->getDeltaType(std::string("testField1"));
        DeltaTypePtr testField2 = delta->getDeltaType(std::string("testField2"));
        DeltaTypePtr testArrayRecord1 = delta->getDeltaType(std::string("testArrayRecord1"));
        DeltaTypePtr testArrayRecord2 = delta->getDeltaType(std::string("testArrayRecord2"));

        BOOST_CHECK_MESSAGE(testField1,"Field 'testField1' is absent");
        BOOST_CHECK_MESSAGE(testField2,"Record 'testField2' is absent");
        BOOST_CHECK_MESSAGE(testArrayRecord1,"Record 'testArrayRecord1' is absent");
        BOOST_CHECK_MESSAGE(testArrayRecord2,"Record 'testArrayRecord2' is absent");

        DeltaTypePtr unknownField = delta->getDeltaType(std::string("unknown"));

        BOOST_CHECK_MESSAGE(!unknownField,"Strange! Field 'unknown' is present");

        DeltaTypePtr testField3 = (boost::any_cast<ConfigurationDeltaPtr>(testField2->getNewValue()))->
                getDeltaType(std::string("testField3"));

        std::string testField3toJson = testField3->toString();

        BOOST_CHECK_MESSAGE(!testField3toJson.empty(),"Delta representation of 'testField3' in JSON is empty");

        BOOST_CHECK_MESSAGE(100500 == boost::any_cast<boost::int32_t>(testField3->getNewValue())
                ,"Field 'testField3' isn't equal to 100500");

        DeltaTypePtr testArray1 = (boost::any_cast<ConfigurationDeltaPtr>(testArrayRecord1->getNewValue()))->
                getDeltaType(std::string("testArray1"));

        BOOST_CHECK_MESSAGE(testArray1,"Array 'testArray1' is absent");

        IDeltaType::AddedItems addedItems = testArray1->getAddedItems();

        BOOST_CHECK_MESSAGE(addedItems.size() == 3, "Array has wrong size. Expected 3");

        ConfigurationDeltaPtr lastElem = boost::any_cast<ConfigurationDeltaPtr>(addedItems.back());

        BOOST_CHECK_MESSAGE(!lastElem->hasChanged(std::string("enabled"))
                , "Strange! Field 'enabled' has changed for last element");

        BOOST_CHECK_MESSAGE(lastElem->getDeltaType(std::string("strategy"))->isDefault()
                , "Field 'strategy' isn't default");
    }
};

class OverrideItemByUuidReceiver : public IDeltaReceiver {
public:
    void loadDelta(ConfigurationDeltaPtr delta) {
        DeltaTypePtr enabled = delta->getDeltaType(std::string("enabled"));
        DeltaTypePtr strategy = delta->getDeltaType(std::string("strategy"));
        DeltaTypePtr uuid = delta->getDeltaType(std::string("__uuid"));

        BOOST_CHECK_MESSAGE(enabled,"Field 'enabled' is absent");
        BOOST_CHECK_MESSAGE(strategy,"Field 'strategy' is absent");
        BOOST_CHECK_MESSAGE(uuid,"Field '__uuid' is absent");

        BOOST_CHECK_MESSAGE(std::string("CONCRETE1") == boost::any_cast<std::string>(strategy->getNewValue())
                ,"Field 'strategy' has unexpected value");

        BOOST_CHECK_MESSAGE(!enabled->isReset(),"Field 'enabled' is reseted");

        BOOST_CHECK_MESSAGE(uuid->getRemovedItems().empty(),"Field '__uuid' has removed items");
    }
};

class RemoveTwoArrayItemsAndAddNewItemsToArrayReceiver : public IDeltaReceiver {
public:
    void loadDelta(ConfigurationDeltaPtr delta) {
        DeltaTypePtr testArray1 = delta->getDeltaType(std::string("testArray1"));

        BOOST_CHECK_MESSAGE(testArray1,"Array 'testArray1' is absent");

        IDeltaType::RemovedItems removedItems = testArray1->getRemovedItems();

        BOOST_CHECK_MESSAGE(removedItems.size() == 2,"Wrong size of 'testArray1'. Expected 2");

        boost::uuids::uuid uuid1({{1,2,3,4,5,6}});
        boost::uuids::uuid uuid2({{1,2,3,4,5,7}});

        DeltaHandlerId deltaId1(uuid1);
        DeltaHandlerId deltaId2(uuid2);

        BOOST_CHECK_MESSAGE(deltaId1 == removedItems.front(),"Unexpected removed delta id ");
        BOOST_CHECK_MESSAGE(deltaId2 == removedItems.back(),"Unexpected removed delta id ");

        std::string json = static_cast<RemovedItemsDeltaType*>(testArray1.get())->toString();
        BOOST_CHECK_MESSAGE(!json.empty(),"Delta representation of an array of removed items in JSON is empty");
    }
};


class ResetArrayOfDiffReceiver : public IDeltaReceiver {
public:
    void loadDelta(ConfigurationDeltaPtr delta) {
        boost::uuids::uuid uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};
        DeltaHandlerId expectedDeltaId(uuid);

        BOOST_CHECK_MESSAGE(delta->getHandlerId() == expectedDeltaId
                ,"Received delta with unexpected id");

        DeltaTypePtr testArray2 = delta->getDeltaType(std::string("testArray2"));

        BOOST_CHECK_MESSAGE(testArray2,"Array 'testArray2' is absent");
        BOOST_CHECK_MESSAGE(testArray2->isReset(),"Array 'testArray2' is not reseted");
    }
};

BOOST_AUTO_TEST_SUITE(DeltaTestSuite)

BOOST_AUTO_TEST_CASE(FullResyncDelta)
{
    boost::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);

    ConfigurationProcessor processor(schema);
    DefaultDeltaManager deltaManager;
    FullResyncReceiver deltaReceiver;

    deltaManager.registerRootReceiver(&deltaReceiver);
    processor.subscribeForUpdates(deltaManager);

    FullResyncTest frt;
    boost::uint8_t *data;
    size_t len;

    frt.createDelta(data, len);
    processor.processConfigurationData(data, len, true);
}

BOOST_AUTO_TEST_CASE(OverrideItemByUuid)
{
    boost::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);

    ConfigurationProcessor processor(schema);
    DefaultDeltaManager deltaManager;
    FullResyncReceiver deltaReceiver;
    OverrideItemByUuidReceiver specificReceiver;

    boost::uuids::uuid uuid = {{1,2,3,4,5,8}};
    DeltaHandlerId deltaId(uuid);

    deltaManager.registerRootReceiver(&deltaReceiver);
    deltaManager.subscribeForDeltaUpdates(deltaId, &specificReceiver);
    processor.subscribeForUpdates(deltaManager);

    OverrideItemByUuidTest frt;
    boost::uint8_t *data;
    size_t len;

    frt.createDelta(data, len);
    processor.processConfigurationData(data, len, false);

    deltaManager.unsubscribeFromDeltaUpdates(deltaId);
}

BOOST_AUTO_TEST_CASE(RemoveTwoArrayItemsAndAddNewItemsToArray)
{
    boost::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);

    ConfigurationProcessor processor(schema);
    DefaultDeltaManager deltaManager;
    FullResyncReceiver deltaReceiver;
    RemoveTwoArrayItemsAndAddNewItemsToArrayReceiver specificReceiver;

    boost::uuids::uuid uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,7}};
    DeltaHandlerId deltaId(uuid);

    deltaManager.registerRootReceiver(&deltaReceiver);
    deltaManager.subscribeForDeltaUpdates(deltaId, &specificReceiver);
    processor.subscribeForUpdates(deltaManager);

    RemoveTwoArrayItemsAndAddNewItemsToArrayTest frt;
    boost::uint8_t *data;
    size_t len;

    frt.createDelta(data, len);
    processor.processConfigurationData(data, len, false);

    deltaManager.unsubscribeFromDeltaUpdates(deltaId);
}

BOOST_AUTO_TEST_CASE(ResetArrayOfDiff)
{
    boost::shared_ptr<avro::ValidSchema> schema;
    schema.reset(new avro::ValidSchema());
    std::ifstream is(schema_path);

    avro::compileJsonSchema(is, *schema);

    ConfigurationProcessor processor(schema);
    DefaultDeltaManager deltaManager;
    FullResyncReceiver deltaReceiver;
    ResetArrayOfDiffReceiver specificReceiver;

    boost::uuids::uuid uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};
    DeltaHandlerId deltaId(uuid);

    deltaManager.registerRootReceiver(&deltaReceiver);
    deltaManager.subscribeForDeltaUpdates(deltaId, &specificReceiver);
    processor.subscribeForUpdates(deltaManager);

    ResetArrayOfDiffTest frt;
    boost::uint8_t *data;
    size_t len;

    frt.createDelta(data, len);
    processor.processConfigurationData(data, len, false);

    deltaManager.unsubscribeFromDeltaUpdates(deltaId);
}

BOOST_AUTO_TEST_CASE(EmptyDelta)
{
    EmptyDeltaType emptyDelta;

    BOOST_CHECK_MESSAGE(!emptyDelta.isReset(),"Empty delta is reseted!!!");
    BOOST_CHECK_MESSAGE(!emptyDelta.isDefault(),"Empty delta is drop down to default!!!");
    BOOST_CHECK_MESSAGE(emptyDelta.getNewValue().empty(),"Empty delta has non-empty value!!!");
    BOOST_CHECK_MESSAGE(emptyDelta.getAddedItems().empty(),"Empty delta has added items!!!");
    BOOST_CHECK_MESSAGE(emptyDelta.getRemovedItems().empty(),"Empty delta has removed items!!!");
}

BOOST_AUTO_TEST_CASE(DeltaInJSON)
{
    EmptyDeltaType emptyDelta;
    BOOST_CHECK_MESSAGE(std::string("null") == emptyDelta.toString()
                            ,"Wrong JSON representation for empty delta!!!");

    ResetDeltaType resetDelta;
    BOOST_CHECK_MESSAGE(std::string("reset") == resetDelta.toString()
                            ,"Wrong JSON representation for reset delta!!!");

    std::string json;

    ValueDeltaType boolValue(false, avro::AVRO_BOOL);
    json = boolValue.toString();
    BOOST_CHECK_MESSAGE(!json.empty(),"Wrong JSON representation for bool delta!!!");

    ValueDeltaType intValue(0xFF, avro::AVRO_INT);
    json = intValue.toString();
    BOOST_CHECK_MESSAGE(!json.empty(),"Wrong JSON representation for int delta!!!");

    ValueDeltaType longValue(0xFFFFFFL, avro::AVRO_LONG);
    json = longValue.toString();
    BOOST_CHECK_MESSAGE(!json.empty(),"Wrong JSON representation for long delta!!!");

    ValueDeltaType floatValue(3.14, avro::AVRO_FLOAT);
    json = floatValue.toString();
    BOOST_CHECK_MESSAGE(!json.empty(),"Wrong JSON representation for float delta!!!");

    ValueDeltaType doubleValue(3.14, avro::AVRO_DOUBLE);
    json = doubleValue.toString();
    BOOST_CHECK_MESSAGE(!json.empty(),"Wrong JSON representation for double delta!!!");

    ValueDeltaType unexpectedValue(3.14, avro::AVRO_MAP);
    json = unexpectedValue.toString();
    BOOST_CHECK_MESSAGE(std::string("unknown") == json
                        , "Wrong JSON representation for unknown delta type!!!");

    AddedItemsDeltaType addedItems;
    addedItems.addItem(10);
    BOOST_CHECK_MESSAGE(std::string("[ unknown type ]") == addedItems.toString()
                        , "Wrong JSON representation for unknown delta in added items!!!");
}

BOOST_AUTO_TEST_CASE(DefaultDeltaId)
{
    std::string schemaStr = "{\"type\":\"record\",\"name\":\"testRecord\",\"namespace\":\"test.namespace\","
                            "\"fields\":[{\"name\":\"boolValue\",\"type\":\"boolean\"}]}";
    avro::ValidSchema schema = avro::compileJsonSchemaFromString(schemaStr);

    avro::GenericDatum datum(schema);
    avro::GenericRecord& record = datum.value<avro::GenericRecord>();
    record.setField("boolValue", avro::GenericDatum(true));

    DeltaHandlerId expectedId(0);
    DefaultConfigurationDeltaFactory factory;
    auto delta = factory.createDelta(datum);

    BOOST_CHECK_MESSAGE(delta->getHandlerId() == expectedId, "Wrong default delta id. Expected 0!!!");
}

BOOST_AUTO_TEST_SUITE_END()

}
