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

#ifndef COMMONVALUETOOLS_HPP_
#define COMMONVALUETOOLS_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <memory>
#include <cstdint>
#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/ICommonArray.hpp"
#include "kaa/common/types/CommonRecord.hpp"
#include "kaa/common/types/CommonFixed.hpp"

#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

class CommonValueTools {
public:
    static bool isRecord(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? v->getCommonType() == CommonValueType::COMMON_RECORD : false);
    }

    static bool isArray(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? v->getCommonType() == CommonValueType::COMMON_ARRAY : false);
    }

    static bool isFixed(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? v->getCommonType() == CommonValueType::COMMON_FIXED : false);
    }

    static bool isEnum(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? v->getCommonType() == CommonValueType::COMMON_ENUM : false);
    }

    static bool isString(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? v->getCommonType() == CommonValueType::COMMON_STRING : false);
    }

    static bool isNumeric(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ?
                (v->getCommonType() >= CommonValueType::COMMON_INT32 && v->getCommonType()
                        <= CommonValueType::COMMON_DOUBLE) :
                false);
    }

    static bool isBool(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? (v->getCommonType() == CommonValueType::COMMON_BOOL) : false);
    }

    static bool isByteArray(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ?
                (v->getCommonType() == CommonValueType::COMMON_BYTES || v->getCommonType()
                        == CommonValueType::COMMON_FIXED) :
                false);
    }

    static bool isNull(std::shared_ptr<ICommonValue> v)
    {
        return (v.get() ? (v->getCommonType() == CommonValueType::COMMON_NULL) : false);
    }

    static const CommonRecord getRecord(std::shared_ptr<ICommonValue> v)
    {
        if (isRecord(v)) {
            return boost::any_cast<const CommonRecord &>(v->getValue());
        }
        throw KaaException("Can not cast to record");
    }

    static ICommonArray::container_type getList(std::shared_ptr<ICommonValue> v)
    {
        if (isArray(v)) {
            return boost::any_cast<const ICommonArray::container_type &>(v->getValue());
        }
        throw KaaException("Can not cast to array");
    }

    static const std::string &getEnumValue(std::shared_ptr<ICommonValue> v)
    {
        if (isEnum(v)) {
            return boost::any_cast<const std::string &>(v->getValue());
        }
        throw KaaException("Can not cast to enum");
    }

    static std::vector<std::uint8_t> getByteArray(std::shared_ptr<ICommonValue> v)
    {
        if (isByteArray(v)) {
            return boost::any_cast<const std::vector<std::uint8_t> &>(v->getValue());
        }
        throw KaaException("Can not cast to byte array");
    }
};

}  // namespace kaa

#endif

#endif /* COMMONVALUETOOLS_HPP_ */
