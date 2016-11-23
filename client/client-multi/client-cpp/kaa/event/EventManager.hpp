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

#ifndef EVENTMANAGER_HPP_
#define EVENTMANAGER_HPP_


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
#include "kaa/IKaaClientContext.hpp"

namespace kaa {

class IExecutorContext;

class EventManager : public IEventManager
                   , public IEventListenersResolver
                   , public IEventDataProcessor
                   , public AbstractTransactable<std::list<Event> >
{
public:
    EventManager(IKaaClientContext &context)
        : context_(context), currentEventIndex_(0),eventTransport_(nullptr)
    {
    }

    virtual void registerEventFamily(IEventFamily* eventFamily);

    virtual void produceEvent(const std::string& fqn
                            , const std::vector<std::uint8_t>& data
                            , const std::string& target
                            , TransactionIdPtr trxId);

    virtual void onEventsReceived(const EventSyncResponse::events_t& events);
    virtual void onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listeners);

    virtual std::map<std::int32_t, Event> releasePendingEvents();
    virtual bool hasPendingEvents() const;

    virtual std::map<std::int32_t, std::list<std::string> > getPendingListenerRequests();
    virtual bool hasPendingListenerRequests() const;

    virtual std::int32_t findEventListeners(const std::list<std::string>& eventFQNs, IFetchEventListenersPtr listener);

    virtual void setTransport(EventTransport *transport);

    using AbstractTransactable::beginTransaction;
    virtual TransactionIdPtr beginTransaction()
    {
        return AbstractTransactable::beginTransaction(context_);
    }

    virtual void commit(TransactionIdPtr trxId, IKaaClientContext &context_);

    using AbstractTransactable::rollback;
    virtual void rollback(TransactionIdPtr trxId)
    {
        AbstractTransactable::rollback(trxId, context_);
    }
private:
    struct EventListenersInfo {
        std::list<std::string> eventFQNs_;
        IFetchEventListenersPtr listener_;
    };

    void onEventFromServer(const std::string& eventClassFQN
                         , const std::vector<std::uint8_t>& data
                         , const std::string& source);

    void generateUniqueRequestId(std::string& requstId);

    void doSync();

private:
    IKaaClientContext &context_;

    std::set<IEventFamily*>   eventFamilies_;
    std::map<std::int32_t, Event>          pendingEvents_;
    KAA_MUTEX_MUTABLE_DECLARE(pendingEventsGuard_);

    std::int32_t currentEventIndex_;

    EventTransport *          eventTransport_;

    std::map<std::int32_t/*request id*/, std::shared_ptr<EventListenersInfo> > eventListenersRequests_;
    KAA_MUTEX_MUTABLE_DECLARE(eventListenersGuard_);
};

} /* namespace kaa */

#endif /* EVENTMANAGER_HPP_ */
