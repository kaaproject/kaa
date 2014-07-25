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

#include "kaa/common/CommonTypesFactory.hpp"
#include "kaa/common/CommonValueTools.hpp"
#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/CommonRecord.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <avro/Compiler.hh>
#include <vector>

namespace kaa {

template <typename CType, avro::Type AType>
boost::shared_ptr<ICommonValue> check(CType exp)
{
    try {
        boost::shared_ptr<ICommonValue> cvp;
        const avro::GenericDatum &d = avro::GenericDatum(exp);

        cvp = CommonTypesFactory::createCommon<AType>(d);
        BOOST_CHECK(cvp.get() != nullptr);

        const CType actual = boost::any_cast<CType>(cvp->getValue());
        BOOST_CHECK_EQUAL(actual, exp);
        return cvp;
    } catch (std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
        throw e;
    }
}

bool false_p(const kaa::KaaException &ex)
{
    return false;
}

BOOST_AUTO_TEST_SUITE(CommonTypesSuite)

BOOST_AUTO_TEST_CASE(checkPrimitiveCommonValue)
{
    boost::shared_ptr<ICommonValue> cvp;
    cvp = check<int32_t, avro::AVRO_INT>(100500);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<int32_t, avro::AVRO_INT>(-100500);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<int64_t, avro::AVRO_LONG>(100500L);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<int64_t, avro::AVRO_LONG>(-100500L);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<float, avro::AVRO_FLOAT>(0.0005374);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<float, avro::AVRO_FLOAT>(-0.0005374);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<double, avro::AVRO_DOUBLE>(0.3423241234L);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = cvp = check<double, avro::AVRO_DOUBLE>(-0.3423241234L);
    BOOST_CHECK_EQUAL(CommonValueTools::isNumeric(cvp), true);
    cvp = check<bool, avro::AVRO_BOOL>(true);
    BOOST_CHECK_EQUAL(CommonValueTools::isBool(cvp), true);
    cvp = check<bool, avro::AVRO_BOOL>(false);
    BOOST_CHECK_EQUAL(CommonValueTools::isBool(cvp), true);
    cvp = check<const std::string&, avro::AVRO_STRING>("Hello World!!!");
    BOOST_CHECK_EQUAL(CommonValueTools::isString(cvp), true);
    BOOST_REQUIRE_THROW(CommonValueTools::getEnumValue(cvp), KaaException);
    BOOST_REQUIRE_THROW(CommonValueTools::getByteArray(cvp), KaaException);
}

BOOST_AUTO_TEST_CASE(checkBytesCommonValue)
{
    try {
        boost::shared_ptr<ICommonValue> cvp;
        std::vector<boost::uint8_t> exp = {0,1,2,3,4,5,6};
        const avro::GenericDatum &d = avro::GenericDatum(exp);

        cvp = CommonTypesFactory::createCommon<avro::AVRO_BYTES>(d);
        BOOST_CHECK(cvp.get() != nullptr);

        BOOST_CHECK_EQUAL(CommonValueTools::isByteArray(cvp), true);
        BOOST_CHECK_EQUAL(CommonValueTools::isFixed(cvp), false);
        BOOST_CHECK_NO_THROW(CommonValueTools::getByteArray(cvp));

        const std::vector<boost::uint8_t> & actual = CommonValueTools::getByteArray(cvp);
        BOOST_CHECK_EQUAL(actual.size(), exp.size());
        BOOST_CHECK_EQUAL_COLLECTIONS(exp.begin(), exp.end(), actual.begin(), actual.end());
    } catch (std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(checkNullCommonValue)
{
    try {
        boost::shared_ptr<ICommonValue> cvp;
        avro::GenericDatum d;
        cvp = CommonTypesFactory::createCommon<avro::AVRO_NULL>(d);
        BOOST_CHECK(cvp.get() != nullptr);
        BOOST_CHECK_EQUAL(CommonValueTools::isNull(cvp), true);
        BOOST_REQUIRE_THROW(cvp->getValue(), KaaException);
    } catch (std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(checkEnumCommonValue)
{
    const std::string enum_schema = "{\"name\":\"foo\", \"namespace\":\"bar\",\"type\":\"enum\", \"symbols\":[\"SYMBOL\"]}";
    try {
        boost::shared_ptr<ICommonValue> cvp;
        std::string exp = "SYMBOL";
        avro::ValidSchema vs = avro::compileJsonSchemaFromMemory(
                        reinterpret_cast<const boost::uint8_t *>(
                                enum_schema.c_str()
                        ),
                        enum_schema.size()
        );

        avro::GenericDatum d(vs.root());
        avro::GenericEnum &f = d.value<avro::GenericEnum>();
        f.set("SYMBOL");
        cvp = CommonTypesFactory::createCommon<avro::AVRO_ENUM>(d);
        BOOST_CHECK(cvp.get() != nullptr);
        BOOST_CHECK_EQUAL(CommonValueTools::isEnum(cvp), true);
        const std::string & actual = CommonValueTools::getEnumValue(cvp);
        BOOST_CHECK_EQUAL(actual, exp);

    } catch (const std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(checkFixedCommonValue)
{
    const std::string fixed_schema = "{\"name\":\"foo\", \"namespace\":\"bar\",\"type\":\"fixed\", \"size\":5}";
    try {
        boost::shared_ptr<ICommonValue> cvp;
        std::vector<boost::uint8_t> exp = {0,1,2,3,4};
        avro::ValidSchema vs = avro::compileJsonSchemaFromMemory(reinterpret_cast<const boost::uint8_t *>(fixed_schema.c_str()), fixed_schema.size());

        avro::GenericDatum d(vs.root());
        avro::GenericFixed &f = d.value<avro::GenericFixed>();
        f.value() = exp;
        cvp = CommonTypesFactory::createCommon<avro::AVRO_FIXED>(d);
        BOOST_CHECK(cvp.get() != nullptr);
        BOOST_CHECK_EQUAL(CommonValueTools::isByteArray(cvp), true);
        BOOST_CHECK_EQUAL(CommonValueTools::isFixed(cvp), true);
        std::vector<boost::uint8_t> actual = CommonValueTools::getByteArray(cvp);
        BOOST_CHECK_EQUAL(actual.size(), exp.size());
        BOOST_CHECK_EQUAL_COLLECTIONS(exp.begin(), exp.end(), actual.begin(), actual.end());
    } catch (const std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(checkRecordCommonValue)
{
    const std::string record_schema = "{\
            \"name\": \"foo\",\
            \"namespace\": \"bar\",\
            \"type\": \"record\",\
            \"fields\": [\
                {\
                    \"type\": \"string\",\
                    \"name\": \"stringfield\"\
                }\
            ]\
        }";
    try {
        avro::ValidSchema vs = avro::compileJsonSchemaFromMemory(
                        reinterpret_cast<const boost::uint8_t *>(
                                record_schema.c_str()
                        ),
                        record_schema.size()
        );
        uuid_t empty_uuid;
        boost::shared_ptr<ICommonValue> cvp = CommonTypesFactory::createCommonRecord(empty_uuid, vs.root());

        BOOST_CHECK(cvp.get() != nullptr);
        BOOST_CHECK(cvp->getCommonType() == CommonValueType::COMMON_RECORD);
        BOOST_CHECK(CommonValueTools::isRecord(cvp));
        const ICommonRecord &r = CommonValueTools::getRecord(cvp);
        BOOST_REQUIRE_THROW(CommonValueTools::getList(cvp), KaaException);
        BOOST_CHECK_EQUAL(r.getFields().size(), 0);
    } catch (const std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_CASE(checkArrayCommonValue)
{
    const std::string array_schema = "{\"name\":\"foo\", \"namespace\":\"bar\",\"type\":\"array\", \"items\":\"int\"}";
    try {
        avro::ValidSchema vs = avro::compileJsonSchemaFromMemory(
                        reinterpret_cast<const boost::uint8_t *>(
                                array_schema.c_str()
                        ),
                        array_schema.size()
        );

        boost::shared_ptr<ICommonValue> cvp = CommonTypesFactory::createCommonArray(vs.root());
        BOOST_CHECK(cvp.get() != nullptr);
        BOOST_CHECK(CommonValueTools::isArray(cvp));
        BOOST_REQUIRE_THROW(CommonValueTools::getRecord(cvp), KaaException);
        auto l = CommonValueTools::getList(cvp);
        BOOST_CHECK_EQUAL(l.size(), 0);
    } catch (const std::exception &e) {
        BOOST_CHECK_MESSAGE(false, e.what());
    }
}

BOOST_AUTO_TEST_SUITE_END();

}  // namespace kaa
