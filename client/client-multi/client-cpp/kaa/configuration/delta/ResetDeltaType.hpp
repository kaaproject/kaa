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

#ifndef RESETDELTATYPE_HPP_
#define RESETDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/configuration/delta/EmptyDeltaType.hpp"

namespace kaa {

class ResetDeltaType: public EmptyDeltaType {
public:
    /**
     * Tells if the container field was cleared
     * @return true if the container field is cleared, false if not (or field is not array)
     */
    virtual bool isReset()
    {
        return true;
    }

    /**
     * Retrieves string representation of delta reset type
     * @return String representation
     */
    virtual std::string toString() const
    {
        return std::string("reset");
    }
};

} /* namespace kaa */

#endif

#endif /* RESETDELTATYPE_HPP_ */
