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

#ifndef ADDEDITEMSDELTATYPE_HPP_
#define ADDEDITEMSDELTATYPE_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <list>

#include <boost/any.hpp>

#include "kaa/configuration/delta/EmptyDeltaType.hpp"

namespace kaa {

/**
 * Field with this delta type adds new items (@link IDeltaType @endlink implementation)
 */
class AddedItemsDeltaType: public EmptyDeltaType {
public:
    /**
     * Default constructor
     */
    AddedItemsDeltaType()
    {
    }

    /**
     * Copy constructor
     */
    AddedItemsDeltaType(const AddedItemsDeltaType& type)
            : addedItemsStorage_(type.addedItemsStorage_)
    {
    }

    /**
     * Specific constructor
     * @param items list of removed editable items
     */
    AddedItemsDeltaType(const AddedItems& items)
            : addedItemsStorage_(items)
    {
    }

    /**
     * Retrieves list of added items
     * @return list of added items {\ref IConfigurationDelta} for complex items, \ref IDeltaType for others), empty list if there is no added items (or field is not array)
     */
    virtual const AddedItems& getAddedItems()
    {
        return addedItemsStorage_;
    }

    /**
     * Retrieves JSON representation of the inner data
     * @return String representation
     */
    virtual std::string toString() const;

    /**
     * Add new item (\ref DeltaValue) to array
     */
    void addItem(const IDeltaType::DeltaValue& item)
    {
        addedItemsStorage_.push_back(item);
    }

private:
    std::list<IDeltaType::DeltaValue> addedItemsStorage_;
};

inline std::string AddedItemsDeltaType::toString() const
{
    std::stringstream ss;
    bool toRecord = false;
    bool isKnownType = true;

    ss << "[ ";

    if (!addedItemsStorage_.empty()) {
        try {
            boost::any_cast<ConfigurationDeltaPtr>(addedItemsStorage_.front());
            toRecord = true;
        } catch (...) {
            try {
                boost::any_cast<DeltaTypePtr>(addedItemsStorage_.front());
            } catch (...) {
                isKnownType = false;
            }
        }

        if (isKnownType) {
            for (auto it = addedItemsStorage_.begin(); it != addedItemsStorage_.end();) {
                if (toRecord) {
                    ConfigurationDeltaPtr configurationDelta = boost::any_cast<ConfigurationDeltaPtr>(*it);
                    ss << configurationDelta->toString();
                } else {
                    DeltaTypePtr deltaType = boost::any_cast<DeltaTypePtr>(*it);
                    ss << deltaType->toString();
                }

                if (++it != addedItemsStorage_.end()) {
                    ss << ", ";
                }
            }
        } else {
            ss << "unknown type";
        }
    }

    ss << " ]";
    return ss.str();
}

} /* namespace kaa */

#endif

#endif /* ADDEDITEMSDELTATYPE_HPP_ */
