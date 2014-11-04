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

#ifndef AVRODATUMSCOMPARATOR_HPP_
#define AVRODATUMSCOMPARATOR_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Generic.hh>
#include <cstdint>

#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

struct avro_comparator {

bool operator ()(const avro::GenericDatum &left, const avro::GenericDatum &right)
{
    return compareDatumsLess(left, right);
}

private:

bool compareDatumsLess(const avro::GenericDatum &left, const avro::GenericDatum &right)
{
    int ltype = left.type(), rtype = right.type();
    if (ltype != rtype) {
        if (avro::Type::AVRO_RECORD > avro::Type::AVRO_STRING) {
            if (ltype < avro::Type::AVRO_RECORD) {
                ltype += avro::Type::AVRO_RECORD;
            } else {
                ltype -= avro::Type::AVRO_RECORD;
            }

            if (rtype < avro::Type::AVRO_RECORD) {
                rtype += avro::Type::AVRO_RECORD;
            } else {
                rtype -= avro::Type::AVRO_RECORD;
            }
        }

        return ltype < rtype;
    }
    avro::Type type = left.type();
    switch (type) {
    case avro::Type::AVRO_STRING:
        return std::less<std::string>()(left.value<std::string>(), right.value<std::string>());
    case avro::Type::AVRO_INT:
        return std::less<std::int32_t>()(left.value<std::int32_t>(), right.value<std::int32_t>());
    case avro::Type::AVRO_LONG:
        return std::less<std::int64_t>()(left.value<std::int64_t>(), right.value<std::int64_t>());
    case avro::Type::AVRO_FLOAT:
        return std::less<float>()(left.value<float>(), right.value<float>());
    case avro::Type::AVRO_DOUBLE:
        return std::less<double>()(left.value<double>(), right.value<double>());
    case avro::Type::AVRO_BOOL:
        return std::less<bool>()(left.value<bool>(), right.value<bool>());
    case avro::Type::AVRO_BYTES: {
        std::vector<std::uint8_t> l_vec = left.value<std::vector<std::uint8_t> >();
        std::vector<std::uint8_t> r_vec = right.value<std::vector<std::uint8_t> >();
        return std::less<std::vector<std::uint8_t> >()(l_vec, r_vec);
    }

    case avro::Type::AVRO_RECORD: {
        const avro::GenericRecord &leftRecord = left.value<avro::GenericRecord>();
        const avro::GenericRecord &rightRecord = right.value<avro::GenericRecord>();

        size_t leftFieldCount = leftRecord.schema()->names();
        size_t rightFieldCount = rightRecord.schema()->names();
        if (leftFieldCount != rightFieldCount) {
            throw KaaException("Can not compare records with different count of fields");
        }

        for (size_t l = 0; l < leftFieldCount; ++l) {
            std::string fieldName = leftRecord.schema()->nameAt(l);
            bool isLess = compareDatumsLess(leftRecord.field(fieldName), rightRecord.field(fieldName));
            if ( !(!isLess && !compareDatumsLess(rightRecord.field(fieldName), leftRecord.field(fieldName)))) {
                return isLess;
            }
        }
        return false;
    }
    case avro::Type::AVRO_ENUM: {
        return left.value<avro::GenericEnum>().value() < right.value<avro::GenericEnum>().value();
    }
    case avro::Type::AVRO_ARRAY: {
        const avro::GenericArray &leftArray = left.value<avro::GenericArray>();
        const avro::GenericArray &rightArray = right.value<avro::GenericArray>();
        const avro::GenericArray::Value l_value = leftArray.value();
        const avro::GenericArray::Value r_value = rightArray.value();
        for (size_t l = 0, r = 0; l < l_value.size() && r < r_value.size(); ++l, ++r) {
            bool isLess = compareDatumsLess(l_value[l], r_value[r]);
            if (!(!isLess && !compareDatumsLess(r_value[r], l_value[l]))) {
                return isLess;
            }
        }
        return l_value.size() > r_value.size();
    }

    case avro::Type::AVRO_FIXED: {
        std::vector<std::uint8_t> l_vec = left.value<avro::GenericFixed>().value();
        std::vector<std::uint8_t> r_vec = right.value<avro::GenericFixed>().value();
        return std::less<std::vector<std::uint8_t> >()(l_vec, r_vec);
    }

    case avro::Type::AVRO_NULL:
        return false;

    case avro::Type::AVRO_MAP:
    case avro::Type::AVRO_UNION:
    default:
        throw KaaException(boost::format("Can not compare datums of \"%1%\" type") % avro::toString(type));
    }
}

};

}  // namespace kaa

#endif

#endif /* AVRODATUMSCOMPARATOR_HPP_ */
