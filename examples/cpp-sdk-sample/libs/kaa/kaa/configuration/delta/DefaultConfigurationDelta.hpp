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

#ifndef DEFAULTCONFIGURATIONDELTA_HPP_
#define DEFAULTCONFIGURATIONDELTA_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <map>
#include <string>
#include <memory>

#include "kaa/configuration/delta/IDeltaType.hpp"
#include "kaa/configuration/delta/IConfigurationDelta.hpp"
#include "kaa/configuration/delta/DeltaHandlerId.hpp"

namespace kaa {

class DefaultConfigurationDelta: public IConfigurationDelta {
public:
    /**
     * Default constructor
     * Set delta handler id to default value, i.e. 0
     */
    DefaultConfigurationDelta() : handlerId_(0) {}

    /**
     * Constructor
     * @param handlerId delta handler id
     */
    DefaultConfigurationDelta(const DeltaHandlerId& handlerId)
        : handlerId_(handlerId) {}

    /**
     * Retrieves handler id of the current delta
     * @return handler id of this delta, id would be set to 0 if delta hasn't delta id
     */
    virtual DeltaHandlerId getHandlerId() {
        return handlerId_;
    }

    /**
     * Tells if the field was changed
     * @param field the name of the field
     * @return true if the field was changed, false otherwise
     */
    virtual bool hasChanged(const std::string& field) {
        return (deltaTypesStorage_.find(field) != deltaTypesStorage_.end());
    }

    /**
     * Retrieves the delta type of the given field
     * @param field the name of the field
     * @return delta type of the field, or null if the field was not changed
     */
    virtual DeltaTypePtr getDeltaType(const std::string& field);

    /**
     * Retrieves JSON representation of the inner data
     * @return String representation
     */
    virtual std::string toString() const;

    /**
     * Add new delta pair (field name-delta type) to an inner storage
     */
    void updateFieldDeltaType(const std::string& field, DeltaTypePtr type) {
        deltaTypesStorage_[field] = type;
    }

private:
    DeltaHandlerId                        handlerId_;
    std::map<std::string, DeltaTypePtr>   deltaTypesStorage_;
};

inline DeltaTypePtr DefaultConfigurationDelta::getDeltaType(const std::string& field)
{
    auto it = deltaTypesStorage_.find(field);
    if (it != deltaTypesStorage_.end()) {
        return it->second;
    }

    DeltaTypePtr deltaPtr;
    return deltaPtr;
}

inline std::string DefaultConfigurationDelta::toString() const
{
    std::stringstream ss;
    ss << "[ ";
    for (auto it = deltaTypesStorage_.begin(); it != deltaTypesStorage_.end();) {
        ss << "{ \"" << it->first << "\": " << it->second->toString() <<" }";
        if (++it != deltaTypesStorage_.end()) {
            ss << ", ";
        }
    }
    ss << " ]";
    return ss.str();
}

} /* namespace kaa */

#endif

#endif /* DEFAULTCONFIGURATIONDELTA_HPP_ */
