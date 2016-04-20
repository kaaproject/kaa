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

#include "kaa/event/EventManager.hpp"

#ifdef KAA_USE_EVENTS

#include <utility>
#include <algorithm>

#include "kaa/common/UuidGenerator.hpp"
#include "kaa/logging/Log.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/IEventFamily.hpp"
#include "kaa/event/IFetchEventListeners.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/utils/IThreadPool.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"

namespace kaa {

void EventManager::registerEventFamily(IEventFamily* eventFamily)
{
    if (eventFamily) {
        auto it = eventFamilies_.insert(eventFamily);
        if (!it.second) {
            KAA_LOG_WARN("Failed to register event family: already exists");
        }
    } else {
        KAA_LOG_WARN("Failed to register event family: bad input data");
    }
}

void EventManager::produceEvent(const std::string& fqn, const std::vector<std::uint8_t>& data,
                                const std::string& target, TransactionIdPtr trxId)
{
    if (fqn.empty()) {
        KAA_LOG_WARN("Failed to process outgoing event: bad input data");
        return;
    }

    KAA_LOG_DEBUG(boost::format("Going to produce Event [FQN: %1%, target: %2%, data_size = %3%]") % fqn
                  % (target.empty() ? "broadcast" : target) % data.size());

    Event event;
    event.eventClassFQN = fqn;
    event.eventData.assign(data.begin(), data.end());

    if (target.empty()) {
        event.target.set_null();
    } else {
        event.target.set_string(target);
    }

    if (trxId) {
        getContainerByTrxId(trxId, context_).push_back(event);
        return;
    }

    KAA_LOG_TRACE(boost::format("New event %1% is produced for %2%") % fqn % target);

    {
        KAA_MUTEX_LOCKING("pendingEventsGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
        KAA_MUTEX_LOCKED("pendingEventsGuard_");
        pendingEvents_.insert(std::make_pair(currentEventIndex_++, event));
        KAA_MUTEX_UNLOCKED("pendingEventsGuard_");
    }

    doSync();
}

std::map<std::int32_t, Event> EventManager::releasePendingEvents()
{
    KAA_MUTEX_LOCKING("pendingEventsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
    KAA_MUTEX_LOCKED("pendingEventsGuard_");

    std::map<std::int32_t, Event> result(std::move(pendingEvents_));
    pendingEvents_ = std::map<std::int32_t, Event>();
    currentEventIndex_ = 0;
    return result;
}

bool EventManager::hasPendingEvents() const
{
    KAA_MUTEX_LOCKING("pendingEventsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
    KAA_MUTEX_LOCKED("pendingEventsGuard_");
    return !pendingEvents_.empty();
}

std::map<std::int32_t, std::list<std::string> > EventManager::getPendingListenerRequests()
{
    KAA_MUTEX_LOCKING("eventListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(eventListenersLock, eventListenersGuard_);
    KAA_MUTEX_LOCKED("eventListenersGuard_");

    std::map<std::int32_t, std::list<std::string> > result;
    for (const auto& idToFqnList : eventListenersRequests_) {
        result.insert(std::make_pair(idToFqnList.first, idToFqnList.second->eventFQNs_));
    }
    return result;
}

bool EventManager::hasPendingListenerRequests() const
{
    KAA_MUTEX_LOCKING("eventListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(eventListenersLock, eventListenersGuard_);
    KAA_MUTEX_LOCKED("eventListenersGuard_");

    return !eventListenersRequests_.empty();
}

void EventManager::onEventFromServer(const std::string& eventClassFQN, const std::vector<std::uint8_t>& data,
                                     const std::string& source)
{
    if (eventClassFQN.empty()) {
        KAA_LOG_WARN("Failed to process incoming event: bad input data");
        return;
    }

    bool isProcessed = false;

    for (auto* family : eventFamilies_) {
        const auto& list = family->getSupportedEventClassFQNs();
        auto it = std::find(list.begin(), list.end(), eventClassFQN);
        if (it != list.end()) {
            KAA_LOG_TRACE(boost::format("Processing event for %1%") % eventClassFQN);
            family->onGenericEvent(eventClassFQN, data, source);
            isProcessed = true;
        }
    }

    if (!isProcessed) {
        KAA_LOG_WARN(boost::format("Event '%1%' wasn't processed: could not find appropriate family") % eventClassFQN);
    }
}

void EventManager::onEventsReceived(const EventSyncResponse::events_t& eventResponse)
{
    auto events = eventResponse.get_array();
    std::sort(events.begin(), events.end(),
              [&events](const Event& l, const Event& r) -> bool {return l.seqNum < r.seqNum;});
    for (const auto& event : events) {
        std::string source;
        if (!event.source.is_null()) {
            source = event.source.get_string();
        }
        onEventFromServer(event.eventClassFQN, event.eventData, source);
    }
}

void EventManager::onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listenersResponses)
{
    if (!listenersResponses.is_null()) {
        const auto& container = listenersResponses.get_array();

        KAA_MUTEX_LOCKING("eventListenersGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(eventListenersLock, eventListenersGuard_);
        KAA_MUTEX_LOCKED("eventListenersGuard_");

        for (const auto& response : container) {
            auto it = eventListenersRequests_.find(response.requestId);

            if (it != eventListenersRequests_.end()) {
                auto callback = it->second->listener_;
                eventListenersRequests_.erase(it);

                if (response.result == SyncResponseResultType::SUCCESS) {
                    std::vector<std::string> listeners;
                    if (!response.listeners.is_null()) {
                        listeners = response.listeners.get_array();
                    }

                    context_.getExecutorContext().getCallbackExecutor().add([callback, listeners]
                                                                {
                                                                    callback->onEventListenersReceived(listeners);
                                                                });
                } else {
                    context_.getExecutorContext().getCallbackExecutor().add([callback] { callback->onRequestFailed(); });
                }

            } else {
                KAA_LOG_WARN(boost::format("Failed to find requester for event listeners (request id = %1%)")
                             % response.requestId);
            }
        }
    }
}

std::int32_t EventManager::findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListenersPtr listener)
{
    if (eventFQNs.empty() || !listener) {
        KAA_LOG_WARN("Failed to add event listeners request: bad input data");
        throw KaaException("Bad event listeners data");
    }

    std::int32_t requestId = UuidGenerator::generateRandomInt();

    std::shared_ptr<EventListenersInfo> info(new EventListenersInfo);
    info->eventFQNs_ = eventFQNs;
    info->listener_ = listener;

    {
        KAA_MUTEX_LOCKING("eventListenersGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(eventListenersLock, eventListenersGuard_);
        KAA_MUTEX_LOCKED("eventListenersGuard_");

        eventListenersRequests_.insert(std::make_pair(requestId, info));
        KAA_MUTEX_UNLOCKED("eventListenersGuard_");
    }

    KAA_LOG_TRACE("Added event listeners resolving request");

    doSync();

    return requestId;
}

void EventManager::setTransport(EventTransport *transport)
{
    eventTransport_ = transport;

    if (eventTransport_) {
        bool needSync = false;
        {
            KAA_MUTEX_LOCKING("pendingEventsGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
            KAA_MUTEX_LOCKED("pendingEventsGuard_");
            needSync = !pendingEvents_.empty();
            KAA_MUTEX_UNLOCKED("pendingEventsGuard_");
        }

        if (!needSync) {
            KAA_MUTEX_LOCKING("eventListenersGuard_");
            KAA_MUTEX_UNIQUE_DECLARE(eventListenersLock, eventListenersGuard_);
            KAA_MUTEX_LOCKED("eventListenersGuard_");
            needSync = !eventListenersRequests_.empty();
            KAA_MUTEX_UNLOCKED("eventListenersGuard_");
        }

        if (needSync) {
            doSync();
        }
    }
}

void EventManager::commit(TransactionIdPtr trxId, IKaaClientContext &context_)
{
    auto it = transactions_.find(trxId);
    if (it != transactions_.end()) {
        KAA_MUTEX_LOCKING("pendingEventsGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(eventsLock, pendingEventsGuard_);
        KAA_MUTEX_LOCKED("pendingEventsGuard_");

        std::list<Event> & events = it->second;
        for (Event &e : events) {
            pendingEvents_.insert(std::make_pair(currentEventIndex_++, std::move(e)));
        }
        transactions_.erase(it);

        KAA_MUTEX_UNLOCKING("pendingEventsGuard_");
        KAA_UNLOCK(eventsLock);
        KAA_MUTEX_UNLOCKED("pendingEventsGuard_");

        doSync();
    }
}

void EventManager::doSync()
{
    if (eventTransport_) {
        eventTransport_->sync();
    } else {
        KAA_LOG_ERROR("Failed to sync: event transport is not set");
        throw TransportNotFoundException("Event transport not found");
    }
}

} /* namespace kaa */

#endif

