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

#ifndef COMMONNULL_HPP_
#define COMMONNULL_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include <sstream>

namespace kaa {

class CommonNull: public ICommonValue {
public:
    CommonNull()
            : ICommonValue(CommonValueType::COMMON_NULL)
    {
    }

    const boost::any getValue() const
    {
        throw KaaException("Null does not have any value");
    }
    avro::GenericDatum toAvro() const
    {
        avro::GenericDatum datum;
        return datum;
    }

    std::string toString() const
    {
        std::stringstream ss;
        ss << "null";
        return ss.str();
    }
};

}

#endif

#endif /* COMMONNULL_HPP_ */
