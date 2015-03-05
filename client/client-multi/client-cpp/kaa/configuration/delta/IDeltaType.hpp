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

#ifndef IDELTATYPE_HPP_
#define IDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <list>
#include <memory>
#include <boost/any.hpp>

#include "kaa/configuration/delta/DeltaHandlerId.hpp"

namespace kaa {

class IDeltaType;
typedef std::shared_ptr<IDeltaType> DeltaTypePtr;

/**
 * Interface of delta type object
 */
class IDeltaType {
public:
    typedef boost::any DeltaValue;
    typedef std::list<DeltaValue> AddedItems;
    typedef std::list<DeltaHandlerId> RemovedItems;

    /**
     * Checks if the field was set to default value
     *
     * @return true if the field set to default, false otherwise
     */
    virtual bool isDefault() = 0;

    /**
     * Checks if the container field was cleared
     *
     * @return true if the container field is cleared, false if
     *         not (or field is not array)
     */
    virtual bool isReset() = 0;

    /**
     * Retrieves new field value
     *
     * @return @link IDeltaValue @endlink with specific type or empty one
     *         if there is no new value (or field is array)
     */
    virtual const DeltaValue& getNewValue() = 0;

    /**
     * Retrieves list of removed editable items
     *
     * @return list which contains handlers @link DeltaHandlerId @endlink
     *         of removed items, list is empty if there is no removed
     *         items (or field is not array)
     */
    virtual const RemovedItems& getRemovedItems() = 0;

    /**
     * Retrieves list of added items
     *
     * @return list of added items (@link IConfigurationDelta @endlink
     *         for complex items, @link IDeltaType @endlink for others),
     *         empty list if there is no added items (or field is not array)
     */
    virtual const AddedItems& getAddedItems() = 0;

    /**
     * Retrieves JSON representation of the inner data
     *
     * @return String representation
     */
    virtual std::string toString() const = 0;

    virtual ~IDeltaType()
    {
    }
};

} /* namespace kaa */

#endif

#endif /* IDELTATYPE_HPP_ */
