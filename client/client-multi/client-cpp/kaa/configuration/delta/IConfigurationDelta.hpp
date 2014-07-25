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

#ifndef ICONFIGURATIONDELTA_HPP_
#define ICONFIGURATIONDELTA_HPP_

#include <string>

#include <boost/shared_ptr.hpp>

#include "kaa/configuration/delta/IDeltaType.hpp"
#include "kaa/configuration/delta/DeltaHandlerId.hpp"

namespace kaa {

class IConfigurationDelta;
typedef boost::shared_ptr<IConfigurationDelta> ConfigurationDeltaPtr;

class IConfigurationDelta {
public:
    /**
     * Retrieves handler id of the current delta
     *
     * @return handler id of this delta, id would be set to 0
     * if delta hasn't delta id
     *
     * @see DeltaHandlerId
     */
    virtual DeltaHandlerId getHandlerId() = 0;

    /**
     * Checks if the field was changed
     *
     * @param field the name of the field
     * @return true if the field was changed, false otherwise
     */
    virtual bool hasChanged(const std::string& field) = 0;

    /**
     * Retrieves the delta type of the given field
     *
     * @param field the name of the field
     * @return delta type of the field, or null if the field was not changed
     * @see IDeltaType
     */
    virtual boost::shared_ptr<IDeltaType> getDeltaType(const std::string& field) = 0;

    /**
     * Retrieves JSON representation of the inner data
     *
     * @return String representation
     */
    virtual std::string toString() const = 0;

    virtual ~IConfigurationDelta() {}
};

} /* namespace kaa */

#endif /* ICONFIGURATIONDELTA_HPP_ */
