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

#ifndef REMOVEDITEMSDELTATYPE_HPP_
#define REMOVEDITEMSDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <list>
#include <sstream>

#include "kaa/configuration/delta/DeltaHandlerId.hpp"
#include "kaa/configuration/delta/EmptyDeltaType.hpp"

namespace kaa {

class RemovedItemsDeltaType: public EmptyDeltaType {
public:
    /**
     * Default constructor
     */
    RemovedItemsDeltaType()
    {
    }

    /**
     * Copy constructor
     */
    RemovedItemsDeltaType(const RemovedItemsDeltaType& type)
            : removedItemsStorage_(type.removedItemsStorage_)
    {
    }

    /**
     * Specific constructor
     * @param removedItemsList list of removed editable items
     */
    RemovedItemsDeltaType(const RemovedItems& items)
            : removedItemsStorage_(items)
    {
    }

    /**
     * Retrieves list of removed editable items
     * @return list which contains handlers (@link DeltaHandlerId @endlink) of
     *         removed items, list is empty if there is no removed items
     *         (or field is not array)
     */
    virtual const RemovedItems& getRemovedItems()
    {
        return removedItemsStorage_;
    }

    /**
     * Retrieves JSON representation of the handler (@link DeltaHandlerId @endlink) array
     * @return String representation
     */
    virtual std::string toString() const;

    /**
     * Add new handler (@link DeltaHandlerId @endlink) to array
     */
    void addHandlerId(const DeltaHandlerId& item)
    {
        removedItemsStorage_.push_back(item);
    }

private:
    RemovedItems removedItemsStorage_;
};

inline std::string RemovedItemsDeltaType::toString() const
{
    std::stringstream ss;
    ss << "[ ";

    for (auto it = removedItemsStorage_.begin(); it != removedItemsStorage_.end();) {
        ss << it->getHandlerId();

        if (++it != removedItemsStorage_.end()) {
            ss << ", ";
        }
    }

    ss << " ]";
    return ss.str();
}

} /* namespace kaa */

#endif

#endif /* REMOVEDITEMSDELTATYPE_HPP_ */
