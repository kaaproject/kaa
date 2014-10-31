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

#include "kaa/common/types/CommonEnum.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <sstream>

namespace kaa {

CommonEnum::CommonEnum(const avro::NodePtr &schema)
    : ICommonValue(CommonValueType::COMMON_ENUM)
    , schema_(schema)
{

}

avro::GenericDatum CommonEnum::toAvro() const
{
    avro::GenericDatum datum(getSchema());
    avro::GenericEnum &enum_ = datum.value<avro::GenericEnum>();
    enum_.set(symbol_);
    return datum;
}

std::string CommonEnum::toString() const
{
    std::stringstream ss;
    ss << symbol_;
    return ss.str();
}
void CommonEnum::setValue(const std::string &value)
{
    symbol_ = value;
}

}  // namespace kaa

#endif
