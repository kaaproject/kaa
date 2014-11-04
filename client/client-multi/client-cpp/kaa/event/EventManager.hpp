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

#ifndef EVENTMANAGER_HPP_
#define EVENTMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include <set>
#include <list>

#include <cstdint>
#include <memory>

#include "kaa/KaaThread.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/IEventManager.hpp"
#include "kaa/event/IEventListenersResolver.hpp"
#include "kaa/event/EventTransport.hpp"
#include "kaa/event/IEventDataProcessor.hpp"
#include "kaa/IKaaClientStateStorage.hpp"

namespace kaa {

class IUpdateManager;

class EventManager : public IEventManager
                   , public IEventListenersResolver
                   , public IEventDataProcessor
{
public:
    EventManager(IKaaClientStateStoragePtr status)
        : eventTransport_(nullptr)
        , status_(status)
    {
        eventSequenceNumber_ = status_->getEventSequenceNumber();
    }

    virtual void registerEventFamily(IEventFamily* eventFamily);

    virtual void produceEvent(const std::string& fqn
                            , const std::vector<std::uint8_t>& data
                            , const std::string& target);

    virtual void onEventsReceived(const EventSyncResponse::events_t& events);
    virtual void onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listeners);

    virtual std::list<Event> getPendingEvents();

    virtual std::map<std::string, std::list<std::string> > getPendingListenerRequests() {
        std::map<std::string, std::list<std::string> > result;
        for (const auto& idToFqnList : eventListenersRequests_) {
            result.insert(std::make_pair(idToFqnList.first, idToFqnList.second->eventFQNs_));
        }
        return result;
    }

    virtual std::string findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListeners* listener);

    virtual void setTransport(EventTransport *transport) {
        eventTransport_ = transport;
        if (eventTransport_ != nullptr && (!pendingEvents_.empty() || !eventListenersRequests_.empty())) {
            eventTransport_->sync();
        }
    }
private:
    struct EventListenersInfo {
        std::list<std::string> eventFQNs_;
        IFetchEventListeners* listener_;
    };

    void onEventFromServer(const std::string& eventClassFQN
                         , const std::vector<std::uint8_t>& data
                         , const std::string& source);


    void generateUniqueRequestId(std::string& requstId);
private:
    std::set<IEventFamily*>   eventFamilies_;
    std::list<Event>          pendingEvents_;
    KAA_MUTEX_DECLARE(pendingEventsGuard_);

    std::int32_t            eventSequenceNumber_;
    KAA_MUTEX_DECLARE(sequenceGuard_);

    EventTransport *          eventTransport_;
    IKaaClientStateStoragePtr status_;

    std::map<std::string/*request id*/, std::shared_ptr<EventListenersInfo> > eventListenersRequests_;
};

} /* namespace kaa */

#endif

#endif /* EVENTMANAGER_HPP_ */
