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

#ifndef DELTAHANDLERID_HPP_
#define DELTAHANDLERID_HPP_

#include <cstdint>

#include "kaa/common/types/ICommonRecord.hpp"

namespace kaa {

/**
 * Id which is used to identify delta objects and subscribe for their updates
 */
class DeltaHandlerId {
public:

    /**
     * Constructor
     * @param uuid UUID object
     */
    DeltaHandlerId(const uuid_t& uuid);

    /**
     * Copy constructor
     */
    DeltaHandlerId(const DeltaHandlerId& id)
        : handlerId_(id.handlerId_) {}

    DeltaHandlerId& operator=(const DeltaHandlerId& id) {
        handlerId_ = id.handlerId_;
        return *this;
    }

    /**
     * Constructor
     * @param handlerId inner representation of \ref DeltaHandlerId
     */
    DeltaHandlerId(const std::uint64_t& handlerId)
        : handlerId_(handlerId) {}

    /**
     * Equal operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is equal than id, false otherwise
     */
    inline bool operator==(const DeltaHandlerId& id) const {
        return (handlerId_ == id.handlerId_);
    }

    /**
     * Not equal operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is not equal than id, false otherwise
     */
    inline bool operator!=(const DeltaHandlerId& id) const {
        return (handlerId_ != id.handlerId_);
    }

    /**
     * Less operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is less than id, false otherwise
     */
    inline bool operator<(const DeltaHandlerId& id) const {
        return (handlerId_ < id.handlerId_);
    }

    /**
     * Greater operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is greater than id, false otherwise
     */
    inline bool operator>(const DeltaHandlerId& id) const {
        return (handlerId_ > id.handlerId_);
    }

    /**
     * Not greater operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is not less than id, false otherwise
     */
    inline bool operator<=(const DeltaHandlerId& id) const {
        return (handlerId_ <= id.handlerId_);
    }

    /**
     * Not less operator
     * @param id instance of \ref DeltaHandlerId to be compared
     * @return true if this is not greater than id, false otherwise
     */
    inline bool operator>=(const DeltaHandlerId& id) const {
        return (handlerId_ >= id.handlerId_);
    }

    /**
     * Retrieves inner representation value of \ref DeltaHandlerId
     */
    inline std::uint64_t getHandlerId() const {
        return handlerId_ ;
    }

private:
    std::uint64_t handlerId_;
};

} /* namespace kaa */

#endif /* DELTAHANDLERID_HPP_ */
