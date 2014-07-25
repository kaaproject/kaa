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

#include <boost/test/unit_test.hpp>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/EventManager.hpp"
#include "kaa/event/gen/BasicEventFamily.hpp"
#include "kaa/event/gen/EventFamilyFactory.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"

#include "headers/update/UpdateManagerMock.hpp"

namespace kaa {

class Listener : public BasicEventFamily::BasicEventFamilyListener {
public:
    Listener() : callCounter_(0) {}

    virtual void onEvent(const Topic& event, const std::string& source) {
        ++callCounter_;
    }

    int callCounter_;
};

BOOST_AUTO_TEST_SUITE(EventManagerTestSuite)

BOOST_AUTO_TEST_CASE(Test)
{
    UpdateManagerMock updateManager;
    EventManager eventManager(updateManager);
    EventFamilyFactory eventFamilyFactory(eventManager);

    auto& factory = eventFamilyFactory.getBasicEventFamily();

    Listener listener;
    factory.addEventFamilyListener(&listener);

    BOOST_CHECK_EQUAL(factory.getSupportedEventClassFQNs().size(), 1);

    Topic pseudoEvent;
    pseudoEvent.id.assign("testId");
    pseudoEvent.name.assign("testName");

    factory.sendEvent(pseudoEvent, "target1");
    factory.sendEvent(pseudoEvent, "target2");

    SyncRequest request;
    eventManager.onUpdate(request);

    BOOST_CHECK_EQUAL(request.events.get_array().size(), 2);

    SyncResponse response;
    Event event1;
    event1.eventClassFQN = "test1";
    event1.eventData = {1, 2, 3};

    AvroByteArrayConverter<Topic> converter;
    auto buffer = converter.toByteArray(pseudoEvent);

    Event event2;
    event2.eventClassFQN = "{event.event_name}";
    event2.eventData.assign(buffer.first.get(), buffer.first.get() + buffer.second);

    Event event3;

    SyncResponse::events_t events;
    events.set_array({event1, event2, event3});

    eventManager.onEventsReceived(events);

    BOOST_CHECK_EQUAL(listener.callCounter_, 1);

    factory.removeEventFamilyListener(&listener);
}

BOOST_AUTO_TEST_SUITE_END()

}
