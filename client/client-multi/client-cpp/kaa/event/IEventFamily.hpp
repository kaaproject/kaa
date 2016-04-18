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

#ifndef IEVENTFAMILY_HPP_
#define IEVENTFAMILY_HPP_

#include "kaa/KaaDefaults.hpp"

#include <list>
#include <string>
#include <vector>

#include <cstdint>

namespace kaa {

typedef std::list<std::string> FQNList;

/**
 * Interface for Event Family.
 * Each event family should be accessed through @link EventFamilyFactory @endlink
 */
class IEventFamily {
public:
    /**
     * Returns set of supported incoming events in event family
     *
     * @return set of supported events presented as set event fully qualified names
     */
    virtual const FQNList& getSupportedEventClassFQNs() = 0;

    /**
     * Generic handler of event received from server.
     *
     * @param eventFQN  Fully qualified name of an event
     * @param data      Event data
     * @param source    Event source
     */
    virtual void onGenericEvent(const std::string& fqn
                              , const std::vector<std::uint8_t>& data
                              , const std::string& source) = 0;

    virtual ~IEventFamily() {}
};

} /* namespace kaa */

#endif /* IEVENTFAMILY_HPP_ */
