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

#include "kaa/common/CommonTypesFactory.hpp"

#include <avro/Generic.hh>
#include "kaa/common/types/CommonValue.hpp"
#include "kaa/common/types/CommonRecord.hpp"
#include "kaa/common/types/CommonArray.hpp"
#include "kaa/common/types/CommonFixed.hpp"
#include "kaa/common/types/CommonEnum.hpp"
#include "kaa/common/types/CommonNull.hpp"

#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

template<avro::Type T>
struct AvroToCommon
{
    static constexpr CommonValueType toCommonType();
};

template<avro::Type T>
constexpr CommonValueType AvroToCommon<T>::toCommonType()
{
    return CommonValueType::COMMON_UNKNOWN;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_ARRAY>::toCommonType()
{
    return CommonValueType::COMMON_ARRAY;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_RECORD>::toCommonType()
{
    return CommonValueType::COMMON_ARRAY;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_ENUM>::toCommonType()
{
    return CommonValueType::COMMON_ENUM;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_FIXED>::toCommonType()
{
    return CommonValueType::COMMON_FIXED;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_BOOL>::toCommonType()
{
    return CommonValueType::COMMON_BOOL;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_STRING>::toCommonType()
{
    return CommonValueType::COMMON_STRING;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_INT>::toCommonType()
{
    return CommonValueType::COMMON_INT32;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_LONG>::toCommonType()
{
    return CommonValueType::COMMON_INT64;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_FLOAT>::toCommonType()
{
    return CommonValueType::COMMON_FLOAT;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_DOUBLE>::toCommonType()
{
    return CommonValueType::COMMON_DOUBLE;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_BYTES>::toCommonType()
{
    return CommonValueType::COMMON_BYTES;
}

template<>
constexpr CommonValueType AvroToCommon<avro::AVRO_NULL>::toCommonType()
{
    return CommonValueType::COMMON_NULL;
}

std::shared_ptr<ICommonRecord>CommonTypesFactory::createCommonRecord(uuid_t uuid, const avro::NodePtr schema)
{
    std::shared_ptr<ICommonRecord> record_ptr(new (std::nothrow) CommonRecord(uuid, schema));
    if (record_ptr.get() == NULL) {
        throw KaaException("Memory allocation failed while constructing new CommonRecord!");
    }
    return record_ptr;
}

std::shared_ptr<ICommonArray> CommonTypesFactory::createCommonArray(const avro::NodePtr &schema)
{
    std::shared_ptr<ICommonArray> array_ptr(new (std::nothrow) CommonArray(schema));
    if (array_ptr.get() == NULL) {
        throw KaaException("Memory allocation failed while constructing new CommonRecord!");
    }
    return array_ptr;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_INT>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<int32_t, AvroToCommon<avro::AVRO_INT>::toCommonType() >(d.value<int32_t>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_LONG>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<int64_t, AvroToCommon<avro::AVRO_LONG>::toCommonType() >(d.value<int64_t>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_FLOAT>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<float, AvroToCommon<avro::AVRO_FLOAT>::toCommonType() >(d.value<float>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_DOUBLE>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<double, AvroToCommon<avro::AVRO_DOUBLE>::toCommonType() >(d.value<double>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_BYTES>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<std::vector<std::uint8_t>, AvroToCommon<avro::AVRO_BYTES>::toCommonType() >(d.value<std::vector<std::uint8_t> >()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_BOOL>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<bool, AvroToCommon<avro::AVRO_BOOL>::toCommonType() >(d.value<bool>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_ENUM>(const avro::GenericDatum & d)
{
    auto a = d.value<avro::GenericEnum>();
    CommonEnum enum_(a.schema());
    enum_.setValue(a.symbol());
    return_type result = return_type(new CommonEnum(enum_));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_STRING>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonValue<std::string, AvroToCommon<avro::AVRO_STRING>::toCommonType() >(d.value<std::string>()));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_FIXED>(const avro::GenericDatum & d)
{
    const avro::GenericFixed fixed = d.value<avro::GenericFixed>();
    CommonFixed commonFixed(fixed.schema());
    commonFixed.setValue(fixed.value());
    return_type result(new CommonFixed(commonFixed));
    return result;
}

template<>
CommonTypesFactory::return_type CommonTypesFactory::createCommon<avro::AVRO_NULL>(const avro::GenericDatum & d)
{
    return_type result = return_type(new CommonNull);
    return result;
}

}  // namespace kaa
