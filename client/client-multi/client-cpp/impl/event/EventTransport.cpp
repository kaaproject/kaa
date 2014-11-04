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

#include "kaa/event/EventTransport.hpp"

#ifdef KAA_USE_EVENTS

#include "kaa/event/EventManager.hpp"
#include "kaa/logging/Log.hpp"

#include <algorithm>

namespace kaa {

EventTransport::EventTransport(EventManager& eventManager, IKaaChannelManager& channelManager)
    : AbstractKaaTransport(channelManager)
    , eventManager_(eventManager)
{

}

std::shared_ptr<EventSyncRequest> EventTransport::createEventRequest(std::int32_t requestId)
{
    std::map<std::string, std::list<std::string> > resolveRequests = eventManager_.getPendingListenerRequests();
    std::list<Event> pendingEvents = eventManager_.getPendingEvents();
    std::shared_ptr<EventSyncRequest> request(new EventSyncRequest);

    if (resolveRequests.empty()) {
        request->eventListenersRequests.set_null();
    } else {
        std::vector<EventListenersRequest> requests;
        for (auto& it : resolveRequests) {
            EventListenersRequest req;
            req.requestId = it.first;
            req.eventClassFQNs.assign(it.second.begin(), it.second.end());
            requests.push_back(req);
        }
        request->eventListenersRequests.set_array(requests);
    }

    KAA_MUTEX_UNIQUE_DECLARE(lock, eventsGuard_);
    for (auto it = events_.begin(); it != events_.end(); ++it) {
        pendingEvents.insert(pendingEvents.end(), it->second.begin(), it->second.end());
    }
    events_.clear();

    if (pendingEvents.empty()) {
        request->events.set_null();
    } else {
        std::vector<Event> eventsCopy;
        for (const Event& e : pendingEvents) {
            eventsCopy.push_back(e);
        }
        std::sort(eventsCopy.begin(), eventsCopy.end(), [&](const Event& l, const Event& r) -> bool { return l.seqNum < r.seqNum; });
        request->events.set_array(eventsCopy);
        events_.insert(std::make_pair(requestId, pendingEvents));
    }

    return request;
}

void EventTransport::onEventResponse(const EventSyncResponse& response)
{
    if (!response.events.is_null()) {
        eventManager_.onEventsReceived(response.events);
    }

    if (!response.eventListenersResponses.is_null()) {
        eventManager_.onEventListenersReceived(response.eventListenersResponses);
    }
}

void EventTransport::onSyncResponseId(std::int32_t requestId)
{
    KAA_MUTEX_UNIQUE_DECLARE(lock, eventsGuard_);

    auto it = events_.find(requestId);
    if (it != events_.end()) {
        events_.erase(it);
    }
}

void EventTransport::sync()
{
    syncByType();
}

}  // namespace kaa

#endif



