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

#ifndef DEFAULTDELTATYPE_HPP_
#define DEFAULTDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/delta/EmptyDeltaType.hpp"

namespace kaa {

/**
 * Field with this delta type uses the default value (@link IDeltaType @endlink implementation)
 */
class DefaultDeltaType: public EmptyDeltaType {
public:
    /**
     * Tells if the field was set to default value
     * @return true if the field set to default, false otherwise
     */
    virtual bool isDefault()
    {
        return true;
    }

    /**
     * Retrieves string representation of delta default type
     * @return String representation
     */
    virtual std::string toString() const
    {
        return std::string("default");
    }
};

} /* namespace kaa */

#endif

#endif /* DEFAULTDELTATYPE_HPP_ */
