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

#ifndef IEVENTDATAPROCESSOR_HPP_
#define IEVENTDATAPROCESSOR_HPP_

#include "kaa/gen/EndpointGen.hpp"
#include <map>
#include <list>

namespace kaa {

class IEventDataProcessor {
public:
    virtual std::list<Event> getPendingEvents() = 0;
    virtual std::map<std::string, std::list<std::string> > getPendingListenerRequests() = 0;

    virtual void onEventsReceived(const EventSyncResponse::events_t& events) = 0;
    virtual void onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listeners) = 0;

    virtual ~IEventDataProcessor() {}
};

} /* namespace kaa */

#endif /* IEVENTDATAPROCESSOR_HPP_ */
