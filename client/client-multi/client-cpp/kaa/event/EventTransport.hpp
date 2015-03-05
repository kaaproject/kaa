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

#ifndef EVENTTRANSPORT_HPP_
#define EVENTTRANSPORT_HPP_

#ifdef KAA_USE_EVENTS

#include "kaa/KaaDefaults.hpp"
#include "kaa/KaaThread.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/channel/transport/IEventTransport.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"

namespace kaa {

class IEventDataProcessor;
class IKaaChannelManager;

class EventTransport: public AbstractKaaTransport<TransportType::EVENT>, public IEventTransport {
public:
    EventTransport(IEventDataProcessor& eventManager, IKaaChannelManager& channelManager,
                   IKaaClientStateStoragePtr state);

    std::shared_ptr<EventSyncRequest> createEventRequest(std::int32_t requestId);

    void onEventResponse(const EventSyncResponse& response);
    void onSyncResponseId(std::int32_t requestId);

    void sync();

private:
    KAA_MUTEX_DECLARE(eventsGuard_);

    IEventDataProcessor& eventDataProcessor_;
    std::map<std::uint32_t, std::list<Event> > events_;

    std::int32_t startEventSN_;
    bool isEventSNSynchronized_;;
};

}
  // namespace kaa

#endif

#endif /* EVENTTRANSPORT_HPP_ */
