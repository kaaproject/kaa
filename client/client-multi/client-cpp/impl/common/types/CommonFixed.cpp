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

#include "kaa/common/types/CommonFixed.hpp"

namespace kaa {

CommonFixed::CommonFixed(const avro::NodePtr &schema)
    : ICommonValue(CommonValueType::COMMON_FIXED)
    , schema_(schema)
{

}

void CommonFixed::setValue(const std::vector<boost::uint8_t> &value)
{
    value_ = value;
}

avro::GenericDatum CommonFixed::toAvro() const
{
    avro::GenericDatum datum(getSchema());
    avro::GenericFixed &fixed = datum.value<avro::GenericFixed>();
    fixed.value().assign(value_.begin(), value_.end());
    return datum;
}

std::string CommonFixed::toString() const
{
    std::stringstream ss;
    for (auto it = value_.begin(); it != value_.end();) {
        ss << std::setw(2) << std::setfill('0') << std::hex << (int)*it << std::dec;
        if (++it != value_.end()) {
            ss << "-";
        }
    }
    return ss.str();
}

}  // namespace kaa
