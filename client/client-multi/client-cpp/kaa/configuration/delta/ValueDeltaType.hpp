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

#ifndef VALUEDELTATYPE_HPP_
#define VALUEDELTATYPE_HPP_

#include <iomanip>
#include <sstream>
#include <vector>
#include <cstdint>

#include "kaa/configuration/delta/EmptyDeltaType.hpp"
#include "kaa/configuration/delta/IConfigurationDelta.hpp"

namespace kaa {

/**
 * This delta type may consist of either compound item (\ref IConfigurationDelta) or
 * primitive avro's type (BOOL, ENUM, INT, FLOAT, DOUBLE, STRING, BYTE or FIXED).
 */
class ValueDeltaType: public EmptyDeltaType {
public:
    /**
     * Constructor
     * @param data data to be stored
     * @param type avro type of data
     */
    ValueDeltaType(const IDeltaType::DeltaValue& data, const avro::Type& type)
        : value_(data), type_(type) {}

    /**
     * Retrieves new field value
     * @return DeltaValue consist of new value
     */
    inline virtual const DeltaValue& getNewValue() {
        return value_;
    }

    /**
     * Retrieves JSON representation for compounds or value for the primitive type
     * @return String representation
     */
    virtual std::string toString() const;

private:
    IDeltaType::DeltaValue value_;
    avro::Type             type_;
};

inline std::string ValueDeltaType::toString() const
{
    std::stringstream ss;

    try {
        switch (type_) {
        case avro::AVRO_RECORD: {
            ConfigurationDeltaPtr configurationDelta = boost::any_cast<ConfigurationDeltaPtr>(value_);
            ss << configurationDelta->toString();
            break;
        }
        case avro::AVRO_BOOL: {
            ss << std::boolalpha << boost::any_cast<bool>(value_);
            break;
        }
        case avro::AVRO_INT: {
            ss << boost::any_cast<std::int32_t>(value_);
            break;
        }
        case avro::AVRO_LONG: {
            ss << boost::any_cast<std::int64_t>(value_);
            break;
        }
        case avro::AVRO_FLOAT: {
            ss << boost::any_cast<float>(value_);
            break;
        }
        case avro::AVRO_DOUBLE: {
            ss << boost::any_cast<double>(value_);
            break;
        }
        case avro::AVRO_STRING: {
            ss << "\"" << boost::any_cast<std::string>(value_) << "\"";
            break;
        }
        case avro::AVRO_ENUM: {
            ss << boost::any_cast<std::string>(value_);
            break;
        }
        case avro::AVRO_BYTES:
        case avro::AVRO_FIXED: {
            const std::vector<std::uint8_t> buffer =
                    boost::any_cast<std::vector<std::uint8_t> >(value_);

            for (auto it = buffer.begin(); it != buffer.end();) {
                ss << std::setw(2) << std::setfill('0') << std::hex << (int)*it << std::dec;
                if (++it != buffer.end()) {
                    ss << "-";
                }
            }
            break;
        }
        default: throw KaaException("Unknown avro type");
        }
    } catch (...) {
        ss << "unknown";
    }

    return ss.str();
}

} /* namespace kaa */

#endif /* VALUEDELTATYPE_HPP_ */
