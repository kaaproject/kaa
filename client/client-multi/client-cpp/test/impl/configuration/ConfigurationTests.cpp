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

#include <vector>

#include "resources/AvroAutoGen.hpp"
#include "headers/configuration/ConfigurationTests.hpp"

#include "kaa/common/CommonValueTools.hpp"

namespace kaa {

std::vector<deltaT> ResetArrayOfDiffTest::fillDelta()
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

void ResetArrayOfDiffTest::checkLoadedConfiguration(const ICommonRecord &configuration)
{
    BOOST_CHECK(configuration.hasField("testField1"));
    BOOST_CHECK(CommonValueTools::isString(configuration.getField("testField1")));
    BOOST_CHECK(configuration.hasField("testField2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testField2")));
    BOOST_CHECK(configuration.hasField("testArrayRecord1"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord1")));
    BOOST_CHECK(configuration.hasField("testArrayRecord2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord2")));

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord1")
            ).getField("testArray1")
        ).size(), 0);

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord2")
            ).getField("testArray2")
        ).size(), 0);
}

std::vector<deltaT> ResetArrayAddMoreItemsOfDiffTypeToArrayTest::fillDelta()
{
    testArrayRecord1T testArrayRecord1;
    testArrayRecord1.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,7}};
    testArrayRecord1.testArray1.set_resetT(resetT::reset);

    deltaT::delta_t delta_1;
    delta_1.set_testArrayRecord1T(testArrayRecord1);

    deltaT delta1;
    delta1.delta  = delta_1;

    recordArrayItem2T array2RecordItem1;
    array2RecordItem1.role.set_userRoleT(userRoleT::ADMIN);
    array2RecordItem1.user = "user6";
    array2RecordItem1.__uuid = {{5,6,7,8,9,0}};

    recordArrayItem2T array2RecordItem2;
    array2RecordItem2.role.set_userRoleT(userRoleT::GUEST);
    array2RecordItem2.user = "user7";
    array2RecordItem2.__uuid = {{5,6,7,8,9,1}};

    recordArrayItem2T array2RecordItem3;
    array2RecordItem3.role.set_userRoleT(userRoleT::MODERATOR);
    array2RecordItem3.user = "user8";
    array2RecordItem3.__uuid = {{5,6,7,8,9,2}};

    recordArrayItem2T array2RecordItem4;
    array2RecordItem4.role.set_userRoleT(userRoleT::USER);
    array2RecordItem4.user = "user9";
    array2RecordItem4.__uuid = {{5,6,7,8,9,3}};


    std::vector<_configuration_schema_Union__8__ > testArray2;

    _configuration_schema_Union__8__ user1;
    user1.set_recordArrayItem2T(array2RecordItem1);
    testArray2.push_back(user1);

    _configuration_schema_Union__8__ user2;
    user2.set_recordArrayItem2T(array2RecordItem2);
    testArray2.push_back(user2);

    _configuration_schema_Union__8__ user3;
    user3.set_recordArrayItem2T(array2RecordItem3);
    testArray2.push_back(user3);

    _configuration_schema_Union__8__ user4;
    user4.set_recordArrayItem2T(array2RecordItem4);
    testArray2.push_back(user4);

    testArrayRecord2T testArrayRecord2;
    testArrayRecord2.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};
    testArrayRecord2.testArray2.set_array(testArray2);

    deltaT::delta_t delta_2;
    delta_2.set_testArrayRecord2T(testArrayRecord2);

    deltaT delta2;
    delta2.delta  = delta_2;

    std::vector<deltaT > deltas;
    deltas.push_back(delta1);
    deltas.push_back(delta2);

    return deltas;
}

void ResetArrayAddMoreItemsOfDiffTypeToArrayTest::checkLoadedConfiguration(const ICommonRecord & configuration)
{
    BOOST_CHECK(configuration.hasField("testField1"));
    BOOST_CHECK(CommonValueTools::isString(configuration.getField("testField1")));
    BOOST_CHECK(configuration.hasField("testField2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testField2")));
    BOOST_CHECK(configuration.hasField("testArrayRecord1"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord1")));
    BOOST_CHECK(configuration.hasField("testArrayRecord2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord2")));

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord1")
            ).getField("testArray1")
        ).size(), 0);

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord2")
            ).getField("testArray2")
        ).size(), 9);
}

std::vector<deltaT> RemoveTwoArrayItemsAndAddNewItemsToArrayTest::fillDelta()
{
    std::vector<_configuration_schema_Union__5__ > testArray1Inner;

    _configuration_schema_Union__5__ testArray1uuidItem1;
    testArray1uuidItem1.set_uuidT({{1,2,3,4,5,6}});
    testArray1Inner.push_back(testArray1uuidItem1);

    _configuration_schema_Union__5__ testArray1uuidItem2;
    testArray1uuidItem2.set_uuidT({{1,2,3,4,5,7}});
    testArray1Inner.push_back(testArray1uuidItem2);

    testArrayRecord1T::testArray1_t testArray1;
    testArray1.set_array(testArray1Inner);

    testArrayRecord1T testArrayRecord1;
    testArrayRecord1.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,7}};
    testArrayRecord1.testArray1 = testArray1;

    deltaT::delta_t delta_1;
    delta_1.set_testArrayRecord1T(testArrayRecord1);

    deltaT delta1;
    delta1.delta  = delta_1;

    std::vector<_configuration_schema_Union__8__ > testArray2;

    _configuration_schema_Union__8__ user1;
    user1.set_string("user4");
    testArray2.push_back(user1);

    _configuration_schema_Union__8__ user2;
    user2.set_string("user5");
    testArray2.push_back(user2);

    testArrayRecord2T testArrayRecord2;
    testArrayRecord2.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};
    testArrayRecord2.testArray2.set_array(testArray2);

    deltaT::delta_t delta_2;
    delta_2.set_testArrayRecord2T(testArrayRecord2);

    deltaT delta2;
    delta2.delta  = delta_2;

    std::vector<deltaT > deltas;
    deltas.push_back(delta1);
    deltas.push_back(delta2);

    return deltas;
}

void RemoveTwoArrayItemsAndAddNewItemsToArrayTest::checkLoadedConfiguration(const ICommonRecord & configuration)
{
    BOOST_CHECK(configuration.hasField("testField1"));
    BOOST_CHECK(CommonValueTools::isString(configuration.getField("testField1")));
    BOOST_CHECK(configuration.hasField("testField2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testField2")));
    BOOST_CHECK(configuration.hasField("testArrayRecord1"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord1")));
    BOOST_CHECK(configuration.hasField("testArrayRecord2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord2")));

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord1")
            ).getField("testArray1")
        ).size(), 1);

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord2")
            ).getField("testArray2")
        ).size(), 5);
}

std::vector<deltaT> OverrideItemByUuidTest::fillDelta()
{
    recordArrayItemT arr1item3;
    arr1item3.enabled.set_bool(true);
    arr1item3.strategy.set_strategyT(strategyT::CONCRETE1);
    arr1item3.__uuid = {{1,2,3,4,5,8}};
    deltaT::delta_t delta_;
    delta_.set_recordArrayItemT(arr1item3);

    deltaT delta;
    delta.delta  = delta_;

    std::vector<deltaT > deltas;
    deltas.push_back(delta);
    return deltas;
}

void OverrideItemByUuidTest::checkLoadedConfiguration(const ICommonRecord & configuration)
{
    BOOST_CHECK(configuration.hasField("testField1"));
    BOOST_CHECK(CommonValueTools::isString(configuration.getField("testField1")));
    BOOST_CHECK(configuration.hasField("testField2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testField2")));
    BOOST_CHECK(configuration.hasField("testArrayRecord1"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord1")));
    BOOST_CHECK(configuration.hasField("testArrayRecord2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord2")));

    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord1")
            ).getField("testArray1")
        ).size(), 3);


    BOOST_CHECK_EQUAL(CommonValueTools::getList(
            CommonValueTools::getRecord(
                    configuration.getField("testArrayRecord2")
            ).getField("testArray2")
        ).size(), 3);
}

std::vector<deltaT> FullResyncTest::fillDelta()
{
    testT::testField1_t testField1;
    testField1.set_string("abcd");

    testRecordT::testField3_t testField3;
    testField3.set_int(100500);

    testRecordT testRecord;
    testRecord.testField3 = testField3;
    testRecord.__uuid = {{0,1,2,3,4}};

    testT::testField2_t testField2;
    testField2.set_testRecordT(testRecord);

    std::vector<_configuration_schema_Union__5__ > testArray1Inner;

    recordArrayItemT arr1item1;
    arr1item1.enabled.set_bool(false);
    arr1item1.strategy.set_strategyT(strategyT::CONCRETE1);
    arr1item1.__uuid = {{1,2,3,4,5,6}};
    _configuration_schema_Union__5__ testArray1RecordItem1;
    testArray1RecordItem1.set_recordArrayItemT(arr1item1);
    testArray1Inner.push_back(testArray1RecordItem1);

    recordArrayItemT arr1item2;
    arr1item2.enabled.set_bool(true);
    arr1item2.strategy.set_strategyT(strategyT::CONCRETE2);
    arr1item2.__uuid = {{1,2,3,4,5,7}};

    _configuration_schema_Union__5__ testArray1RecordItem2;
    testArray1RecordItem2.set_recordArrayItemT(arr1item2);
    testArray1Inner.push_back(testArray1RecordItem2);


    recordArrayItemT arr1item3;
    arr1item3.enabled.set_unchangedT(unchangedT::unchanged);
    arr1item3.strategy.set_null();
    arr1item3.__uuid = {{1,2,3,4,5,8}};

    _configuration_schema_Union__5__ testArray1RecordItem3;
    testArray1RecordItem3.set_recordArrayItemT(arr1item3);
    testArray1Inner.push_back(testArray1RecordItem3);

    std::vector<_configuration_schema_Union__8__ > testArray2;

    _configuration_schema_Union__8__ user1;
    user1.set_string("user1");
    testArray2.push_back(user1);

    _configuration_schema_Union__8__ user2;
    user2.set_string("user2");
    testArray2.push_back(user2);

    _configuration_schema_Union__8__ user3;
    user3.set_string("user3");
    testArray2.push_back(user3);

    testArrayRecord1T::testArray1_t testArray1;
    testArray1.set_array(testArray1Inner);

    testArrayRecord1T testArrayRecord1;
    testArrayRecord1.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,7}};
    testArrayRecord1.testArray1 = testArray1;

    testArrayRecord2T testArrayRecord2;
    testArrayRecord2.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,6}};
    testArrayRecord2.testArray2.set_array(testArray2);

    testT test;
    test.testField1 = testField1;
    test.testField2 = testField2;
    test.testArrayRecord1 = testArrayRecord1;
    test.testArrayRecord2 = testArrayRecord2;
    test.__uuid = {{0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,8}};

    deltaT::delta_t delta_;
    delta_.set_testT(test);

    deltaT delta;
    delta.delta  = delta_;

    std::vector<deltaT > deltas;
    deltas.push_back(delta);

    return deltas;
}

void FullResyncTest::checkLoadedConfiguration(const ICommonRecord &configuration)
{
    BOOST_CHECK(configuration.hasField("testField1"));
    BOOST_CHECK(CommonValueTools::isString(configuration.getField("testField1")));
    BOOST_CHECK_EQUAL(boost::any_cast<const std::string &>(
            configuration.getField("testField1")->getValue()
            ), "abcd");

    BOOST_CHECK(configuration.hasField("testField2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testField2")));
    const ICommonRecord &testField2 = CommonValueTools::getRecord(configuration.getField("testField2"));

    BOOST_CHECK(testField2.hasField("__uuid"));
    BOOST_CHECK(CommonValueTools::isFixed(testField2.getField("__uuid")));

    BOOST_CHECK(testField2.hasField("testField3"));
    BOOST_CHECK(CommonValueTools::isNumeric(testField2.getField("testField3")));
    BOOST_CHECK_EQUAL(boost::any_cast<int32_t>(testField2.getField("testField3")->getValue()), 100500);

    BOOST_CHECK(configuration.hasField("testArrayRecord1"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord1")));
    const ICommonRecord &testArrayRecord1 = CommonValueTools::getRecord(configuration.getField("testArrayRecord1"));

    BOOST_CHECK(testArrayRecord1.hasField("__uuid"));
    BOOST_CHECK(CommonValueTools::isFixed(testArrayRecord1.getField("__uuid")));
    BOOST_CHECK(testArrayRecord1.hasField("testArray1"));
    BOOST_CHECK(CommonValueTools::isArray(testArrayRecord1.getField("testArray1")));

    const ICommonArray::container_type &testArray1 = CommonValueTools::getList(testArrayRecord1.getField("testArray1"));
    BOOST_CHECK_EQUAL(testArray1.size(), 3);

    for (ICommonArray::container_type::const_iterator it = testArray1.begin(); it != testArray1.end(); ++it) {
        BOOST_CHECK(CommonValueTools::isRecord(*it));
        BOOST_CHECK(CommonValueTools::getRecord(*it).hasField("__uuid"));
        BOOST_CHECK(CommonValueTools::isFixed(CommonValueTools::getRecord(*it).getField("__uuid")));
    }

    BOOST_CHECK(configuration.hasField("testArrayRecord2"));
    BOOST_CHECK(CommonValueTools::isRecord(configuration.getField("testArrayRecord2")));
    const ICommonRecord &testArrayRecord2 = CommonValueTools::getRecord(configuration.getField("testArrayRecord2"));

    BOOST_CHECK(testArrayRecord2.hasField("__uuid"));
    BOOST_CHECK(CommonValueTools::isFixed(testArrayRecord2.getField("__uuid")));
    BOOST_CHECK(testArrayRecord2.hasField("testArray2"));
    BOOST_CHECK(CommonValueTools::isArray(testArrayRecord2.getField("testArray2")));

    const ICommonArray::container_type &testArray2 = CommonValueTools::getList(testArrayRecord2.getField("testArray2"));
    BOOST_CHECK_EQUAL(testArray2.size(), 3);

    for (ICommonArray::container_type::const_iterator it = testArray2.begin(); it != testArray2.end(); ++it) {
        BOOST_CHECK(CommonValueTools::isString(*it));
    }
}

} /* namespace kaa */
