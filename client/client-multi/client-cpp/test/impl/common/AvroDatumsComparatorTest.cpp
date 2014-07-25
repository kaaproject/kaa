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

#include <avro/Compiler.hh>
#include <avro/Generic.hh>
#include "kaa/common/AvroDatumsComparator.hpp"

namespace kaa {

BOOST_AUTO_TEST_SUITE(AvroDatumsComparatorSuite)

BOOST_AUTO_TEST_CASE(comparePrimitives)
{
    avro::GenericDatum intDatum1((boost::int32_t) 10);
    avro::GenericDatum intDatum2((boost::int32_t) 11);
    BOOST_CHECK_EQUAL(avro_comparator()(intDatum1, intDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(intDatum1, intDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(intDatum2, intDatum1), false);

    avro::GenericDatum longDatum1((boost::int64_t) 10);
    avro::GenericDatum longDatum2((boost::int64_t) 11);
    BOOST_CHECK_EQUAL(avro_comparator()(longDatum1, longDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(longDatum1, longDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(longDatum2, longDatum1), false);

    avro::GenericDatum floatDatum1((float) 10.0);
    avro::GenericDatum floatDatum2((float) 11.0);
    BOOST_CHECK_EQUAL(avro_comparator()(floatDatum1, floatDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(floatDatum1, floatDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(floatDatum2, floatDatum1), false);

    avro::GenericDatum doubleDatum1((double) 10.0);
    avro::GenericDatum doubleDatum2((double) 11.0);
    BOOST_CHECK_EQUAL(avro_comparator()(doubleDatum1, doubleDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(doubleDatum1, doubleDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(doubleDatum2, doubleDatum1), false);

    avro::GenericDatum boolDatum1((bool) false);
    avro::GenericDatum boolDatum2((bool) true);
    BOOST_CHECK_EQUAL(avro_comparator()(boolDatum1, boolDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(boolDatum1, boolDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(boolDatum2, boolDatum1), false);

    avro::GenericDatum stringDatum1(std::string("foobar1"));
    avro::GenericDatum stringDatum2(std::string("foobar2"));
    BOOST_CHECK_EQUAL(avro_comparator()(stringDatum1, stringDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(stringDatum1, stringDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(stringDatum2, stringDatum1), false);

    avro::GenericDatum bytesDatum1((std::vector<boost::uint8_t>) {1,2,3,4,5});
    avro::GenericDatum bytesDatum2((std::vector<boost::uint8_t>) {1,2,3,4,6});
    avro::GenericDatum bytesDatum3((std::vector<boost::uint8_t>) {1,2,3,4});
    BOOST_CHECK_EQUAL(avro_comparator()(bytesDatum1, bytesDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(bytesDatum1, bytesDatum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(bytesDatum2, bytesDatum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(bytesDatum1, bytesDatum3), false);
    BOOST_CHECK_EQUAL(avro_comparator()(bytesDatum3, bytesDatum1), true);
}

BOOST_AUTO_TEST_CASE(compareRecords)
{
    std::string schemaString = "{\"name\": \"recordT\", \"namespace\": \"com.cxx.test\", \"type\": \"record\", \"fields\":[{\"name\":\"f1\", \"type\": \"int\"}, {\"name\":\"f2\", \"type\": \"string\"}]}";
    avro::ValidSchema recordSchema = avro::compileJsonSchemaFromMemory(reinterpret_cast<const unsigned char *>(schemaString.c_str()), schemaString.length());

    avro::GenericDatum record1(recordSchema);
    avro::GenericDatum record2(recordSchema);
    avro::GenericDatum record3(recordSchema);

    record1.value<avro::GenericRecord>().field("f1").value<boost::int32_t>() = 10;
    record1.value<avro::GenericRecord>().field("f2").value<std::string>() = "foobar1";

    record2.value<avro::GenericRecord>().field("f1").value<boost::int32_t>() = 10;
    record2.value<avro::GenericRecord>().field("f2").value<std::string>() = "foobar2";

    record3.value<avro::GenericRecord>().field("f1").value<boost::int32_t>() = 11;
    record3.value<avro::GenericRecord>().field("f2").value<std::string>() = "foobar2";

    BOOST_CHECK_EQUAL(avro_comparator()(record1, record1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(record1, record2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(record2, record1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(record1, record3), true);
    BOOST_CHECK_EQUAL(avro_comparator()(record3, record1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(record2, record3), true);
    BOOST_CHECK_EQUAL(avro_comparator()(record3, record2), false);
}

BOOST_AUTO_TEST_CASE(compareArrays)
{
    std::string schemaString = "{\"type\": \"array\", \"items\": \"int\"}";
    avro::ValidSchema arraySchema = avro::compileJsonSchemaFromMemory(reinterpret_cast<const unsigned char *>(schemaString.c_str()), schemaString.length());

    avro::GenericDatum array1(arraySchema);
    avro::GenericDatum array2(arraySchema);

    array1.value<avro::GenericArray>().value().push_back(avro::GenericDatum(10));
    array1.value<avro::GenericArray>().value().push_back(avro::GenericDatum(11));

    array2.value<avro::GenericArray>().value().push_back(avro::GenericDatum(10));
    array2.value<avro::GenericArray>().value().push_back(avro::GenericDatum(11));
    array2.value<avro::GenericArray>().value().push_back(avro::GenericDatum(12));
    array2.value<avro::GenericArray>().value().push_back(avro::GenericDatum(13));

    BOOST_CHECK_EQUAL(avro_comparator()(array1, array1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(array1, array2), false);
    BOOST_CHECK_EQUAL(avro_comparator()(array2, array1), true);
}

BOOST_AUTO_TEST_CASE(compareEnums)
{
    std::string schemaString = "{\"name\": \"recordT\", \"namespace\": \"com.cxx.test\", \"type\": \"enum\", \"symbols\": [\"VAL1\", \"VAL2\"]}";
    avro::ValidSchema enumSchema = avro::compileJsonSchemaFromMemory(reinterpret_cast<const unsigned char *>(schemaString.c_str()), schemaString.length());

    avro::GenericDatum enum1(enumSchema);
    avro::GenericDatum enum2(enumSchema);

    enum1.value<avro::GenericEnum>().set("VAL1");
    enum2.value<avro::GenericEnum>().set("VAL2");
    BOOST_CHECK_EQUAL(avro_comparator()(enum1, enum1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(enum1, enum2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(enum2, enum1), false);

}

BOOST_AUTO_TEST_CASE(compareFixed)
{
    std::string schemaString = "{\"name\": \"recordT\", \"namespace\": \"com.cxx.test\", \"type\": \"fixed\", \"size\": 2}";
    avro::ValidSchema fixedSchema = avro::compileJsonSchemaFromMemory(reinterpret_cast<const unsigned char *>(schemaString.c_str()), schemaString.length());

    avro::GenericDatum fixed1(fixedSchema);
    avro::GenericDatum fixed2(fixedSchema);

    fixed1.value<avro::GenericFixed>().value() = {0, 1};
    fixed2.value<avro::GenericFixed>().value() = {1, 2};
    BOOST_CHECK_EQUAL(avro_comparator()(fixed1, fixed1), false);
    BOOST_CHECK_EQUAL(avro_comparator()(fixed1, fixed2), true);
    BOOST_CHECK_EQUAL(avro_comparator()(fixed2, fixed1), false);
}

BOOST_AUTO_TEST_SUITE_END()

}  // namespace kaa
