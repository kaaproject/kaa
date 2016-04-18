/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#ifndef IEVENTMANAGER_HPP_
#define IEVENTMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#include <string>
#include <vector>
#include <cstdint>
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/transact/TransactionId.hpp"

namespace kaa {

class IEventFamily;

/**
 * Interface for event management.
 */
class IEventManager {
public:
    /**
     * Add event family object which can handle specified event.
     *
     * @param eventFamily event family instance
     * @see IEventFamily
     */
    virtual void registerEventFamily(IEventFamily* eventFamily) = 0;

    /**
     * Creates an Event and passes it to OPS
     *
     * @param eventFqn  Fully qualified name of the Event
     * @param data      Event data
     * @param target    Event target, null for event broadcasting.
     */
    virtual void produceEvent(const std::string& fqn
                            , const std::vector<std::uint8_t>& data
                            , const std::string& target
                            , TransactionIdPtr trxId) = 0;

    virtual ~IEventManager() {}
};

} /* namespace kaa */

#endif /* IEVENTMANAGER_HPP_ */
