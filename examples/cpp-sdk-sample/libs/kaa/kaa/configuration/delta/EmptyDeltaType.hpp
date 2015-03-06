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

#ifndef EMPTYDELTATYPE_HPP_
#define EMPTYDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <list>

#include <avro/Generic.hh>

#include "kaa/configuration/delta/IDeltaType.hpp"

namespace kaa {

class EmptyDeltaType : public IDeltaType {
public:
    /**
     * Tells if the field was set to default value
     * @return true if the field set to default, false otherwise
     */
    virtual bool isDefault() {
        return false;
    }

    /**
     * Tells if the container field was cleared
     * @return true if the container field is cleared, false if not (or field is not array)
     */
    virtual bool isReset() {
        return false;
    }

    /**
     * Retrieves new field value
     * @return DeltaValue with specific type or empty one if there is no new value (or field is array)
     */
    virtual const DeltaValue& getNewValue() {
        static DeltaValue empty;
        return empty;
    }

    /**
     * Retrieves list of removed editable items
     * @return list which contains handlers (\ref DeltaHandlerId) of removed items, list is empty if there is no removed items (or field is not array)
     */
    virtual const RemovedItems& getRemovedItems() {
        static RemovedItems emptyHandlerIdList;
        return emptyHandlerIdList;
    }

    /**
     * Retrieves list of added items
     * @return list of added items {\ref IConfigurationDelta} for complex items, \ref IDeltaType for others), empty list if there is no added items (or field is not array)
     */
    virtual const AddedItems& getAddedItems() {
        static AddedItems emptyAddedItemsList;
        return emptyAddedItemsList;
    }

    /**
     * Retrieves string representation of empty delta type
     * @return String representation
     */
    virtual std::string toString() const {
        return std::string("null");
    }
};

} /* namespace kaa */

#endif

#endif /* EMPTYDELTATYPE_HPP_ */
