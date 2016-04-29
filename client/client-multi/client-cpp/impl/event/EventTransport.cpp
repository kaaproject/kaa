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

#include "kaa/event/EventTransport.hpp"

#ifdef KAA_USE_EVENTS

#include "kaa/event/IEventDataProcessor.hpp"
#include "kaa/logging/Log.hpp"

#include <algorithm>

namespace kaa {

EventTransport::EventTransport(IEventDataProcessor& processor, IKaaChannelManager& channelManager,
                               IKaaClientContext &context)
        : AbstractKaaTransport(channelManager,context), eventDataProcessor_(processor), startEventSN_(0),
          isEventSNSynchronized_(false)
{
    startEventSN_ = context.getStatus().getEventSequenceNumber();
}
std::shared_ptr<EventSyncRequest> EventTransport::createEventRequest(std::int32_t requestId)
{
    auto resolveRequests = eventDataProcessor_.getPendingListenerRequests();
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
    if (isEventSNSynchronized_) {
        auto releasedEvents(eventDataProcessor_.releasePendingEvents());

        KAA_MUTEX_LOCKING("eventsGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(eventsGuardLock, eventsGuard_);
        KAA_MUTEX_LOCKED("eventsGuard_");

        if (releasedEvents.size() != 0) {
            auto sNum = context_.getStatus().getEventSequenceNumber();
            for (auto& pair : releasedEvents) {
                pair.second.seqNum = sNum++;
                events_[requestId].push_back(std::move(pair.second));
            }
            context_.getStatus().setEventSequenceNumber(sNum);
        }
        std::vector<Event> eventsForSending;
        for (auto& pair : events_) {
            for (auto& curEvent : pair.second) {
                eventsForSending.push_back(curEvent);
            }
        }
        request->events.set_array(std::move(eventsForSending));
        request->eventSequenceNumberRequest.set_null();
    } else {
        request->events.set_null();
        request->eventSequenceNumberRequest.set_EventSequenceNumberRequest(EventSequenceNumberRequest());
        KAA_LOG_TRACE(boost::format("Sending event sequence number request: restored_sn = %li")% startEventSN_);
    }
    return request;
}

void EventTransport::onEventResponse(const EventSyncResponse& response)
{
    bool needResync = false;
    if (!isEventSNSynchronized_ && !response.eventSequenceNumberResponse.is_null()) {
        std::int32_t lastEventSN = response.eventSequenceNumberResponse.get_EventSequenceNumberResponse().seqNum;
        std::int32_t expectedEventSN = (lastEventSN > 0 ? lastEventSN + 1 : lastEventSN);

        if (startEventSN_ != expectedEventSN) {
            startEventSN_ = expectedEventSN;
            context_.getStatus().setEventSequenceNumber(startEventSN_);
            KAA_LOG_INFO(boost::format("Event sequence number is unsynchronized. Set to %li") % startEventSN_);
        } else {
            KAA_LOG_INFO(boost::format("Event sequence number is up to date: %li") % startEventSN_);
        }

        isEventSNSynchronized_ = true;
        needResync = eventDataProcessor_.hasPendingEvents() || eventDataProcessor_.hasPendingListenerRequests();
    }
    if (!response.events.is_null()) {
        eventDataProcessor_.onEventsReceived(response.events);
    }

    if (!response.eventListenersResponses.is_null()) {
        eventDataProcessor_.onEventListenersReceived(response.eventListenersResponses);
    }
    if (needResync) {
        KAA_LOG_DEBUG("Need to send pending events after sequence number synchronization");
        sync();
    }
}

void EventTransport::onSyncResponseId(std::int32_t requestId)
{
    KAA_MUTEX_LOCKING("eventsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(eventsGuardLock, eventsGuard_);
    KAA_MUTEX_LOCKED("eventsGuard_");

    events_.erase(requestId);
}

void EventTransport::sync()
{
    syncByType();
}

}  // namespace kaa

#endif

