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

#ifdef KAA_USE_EVENTS


#include <set>
#include <list>

#include <cstdint>
#include <memory>

#include "kaa/KaaDefaults.hpp"
#include "kaa/KaaThread.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/IEventManager.hpp"
#include "kaa/event/IEventListenersResolver.hpp"
#include "kaa/event/EventTransport.hpp"
#include "kaa/event/IEventDataProcessor.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/transact/AbstractTransactable.hpp"

namespace kaa {

class EventManager : public IEventManager
                   , public IEventListenersResolver
                   , public IEventDataProcessor
                   , public AbstractTransactable<std::list<Event> >
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
                            , const std::string& target
                            , TransactionIdPtr trxId);

    virtual void onEventsReceived(const EventSyncResponse::events_t& events);
    virtual void onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listeners);

    virtual std::list<Event> releasePendingEvents();
    virtual bool hasPendingEvents() const;

    virtual std::map<std::int32_t, std::list<std::string> > getPendingListenerRequests();

    virtual std::int32_t findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListeners* listener);

    virtual void setTransport(EventTransport *transport);

    virtual TransactionIdPtr beginTransaction()
    {
        return AbstractTransactable::beginTransaction();
    }

    virtual void commit(TransactionIdPtr trxId);

    virtual void rollback(TransactionIdPtr trxId)
    {
        AbstractTransactable::rollback(trxId);
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
    KAA_MUTEX_MUTABLE_DECLARE(pendingEventsGuard_);

    std::int32_t            eventSequenceNumber_;
    KAA_MUTEX_DECLARE(sequenceGuard_);

    EventTransport *          eventTransport_;
    IKaaClientStateStoragePtr status_;

    std::map<std::int32_t/*request id*/, std::shared_ptr<EventListenersInfo> > eventListenersRequests_;
    KAA_MUTEX_DECLARE(eventListenersGuard_);
};

} /* namespace kaa */

#endif

#endif /* EVENTMANAGER_HPP_ */
