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

#ifndef I_COMMON_VALUE_HPP_
#define I_COMMON_VALUE_HPP_

#include <avro/Generic.hh>
#include <boost/any.hpp>

#include "kaa/common/types/CommonValueTypes.hpp"

namespace kaa {

/**
 * Common interface for configuration objects
 */
class ICommonValue {
public:
    ICommonValue(CommonValueType type) : type_(type) {}
    virtual ~ICommonValue() {};

    /**
     * Retrieves nested object
     */
    virtual const boost::any    getValue()  const   = 0;

    /**
     * Retrieves Avro representation of the nested object
     */
    virtual avro::GenericDatum  toAvro() const = 0;

    /**
     * Retrieves string representation of the nested object
     */
    virtual std::string toString() const = 0;

    /**
     * Retrieves type of the nested object
     *
     * @see CommonValueType
     */
    CommonValueType getCommonType() { return type_; }
protected:
    CommonValueType     type_;
};

}  // namespace kaa


#endif /* I_COMMON_VALUE_HPP_ */
