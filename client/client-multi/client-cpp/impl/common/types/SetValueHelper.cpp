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

#include "kaa/common/types/SetValueHelper.hpp"
#include <boost/cstdint.hpp>
#include <vector>

namespace kaa {

template<avro::Type T>
struct avro_traits
{
    static bool copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field);
};

template <avro::Type T>
bool avro_traits<T>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    return false;
}

template <>
bool avro_traits<avro::AVRO_RECORD>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<avro::GenericRecord>() = field.value<avro::GenericRecord>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_ARRAY>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<avro::GenericArray>() = field.value<avro::GenericArray>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_ENUM>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<avro::GenericEnum>() = field.value<avro::GenericEnum>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_FIXED>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<avro::GenericFixed>() = field.value<avro::GenericFixed>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_BOOL>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<bool>() = field.value<bool>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_INT>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<int32_t>() = field.value<int32_t>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_LONG>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<int64_t>() = field.value<int64_t>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_FLOAT>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<float>() = field.value<float>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_DOUBLE>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<double>() = field.value<double>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_STRING>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<std::string>() = field.value<std::string>();
    return true;
}

template <>
bool avro_traits<avro::AVRO_BYTES>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    indatum.value<std::vector<boost::uint8_t> >() = field.value<std::vector<boost::uint8_t> >();
    return true;
}

template <>
bool avro_traits<avro::AVRO_NULL>::copyValueToDatum(avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    return true;
}

bool SetAvroValueHelper::setValue(avro::Type t, avro::GenericDatum &indatum, const avro::GenericDatum &field)
{
    bool result = false;
    switch (t) {
    case avro::AVRO_RECORD:
        result = avro_traits<avro::AVRO_RECORD>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_ARRAY:
        result = avro_traits<avro::AVRO_ARRAY>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_ENUM:
        result = avro_traits<avro::AVRO_ENUM>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_FIXED:
        result = avro_traits<avro::AVRO_FIXED>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_BOOL:
        result = avro_traits<avro::AVRO_BOOL>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_BYTES:
        result = avro_traits<avro::AVRO_BYTES>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_STRING:
        result = avro_traits<avro::AVRO_STRING>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_INT:
        result = avro_traits<avro::AVRO_INT>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_LONG:
        result = avro_traits<avro::AVRO_LONG>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_FLOAT:
        result = avro_traits<avro::AVRO_FLOAT>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_DOUBLE:
        result = avro_traits<avro::AVRO_DOUBLE>::copyValueToDatum(indatum, field);
        break;
    case avro::AVRO_NULL:
        result = avro_traits<avro::AVRO_NULL>::copyValueToDatum(indatum, field);
        break;
    default:
        break;
    }
    return result;
}

}  // namespace kaa

