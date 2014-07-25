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

#include <list>
#include <string>
#include <algorithm>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/event/EventManager.hpp"
#include "kaa/event/IFetchEventListeners.hpp"
#include "kaa/update/EventUpdateListener.hpp"
#include "kaa/common/exception/KaaException.hpp"

#include "headers/update/UpdateManagerMock.hpp"

namespace kaa {

class FetchEventListeners : public IFetchEventListeners
{
public:
    FetchEventListeners() : isReceived_(false), isFailed_(false) {}

    virtual void onEventListenersReceived(const std::vector<std::string>& eventListeners)
    {
        BOOST_CHECK(eventListeners.empty());

        isReceived_ = true;
    }

    virtual void onRequestFailed()
    {
        isFailed_ = true;
    }

public:
    bool isReceived_;
    bool isFailed_;
};

BOOST_AUTO_TEST_SUITE(EventListenersResolverTestSuite)

BOOST_AUTO_TEST_CASE(EventListenersResolverTest)
{
    UpdateManagerMock updateManager;
    EventManager eventManager(updateManager);
    EventUpdateListener updateListener(eventManager);

    FetchEventListeners listener;
    std::list<std::string> fqnList = {"fqn1", "fqn2", "fqn3"};

    std::string reqId1 = eventManager.findEventListeners(fqnList, &listener);
    std::string reqId2 = eventManager.findEventListeners(fqnList, &listener);

    BOOST_CHECK_THROW(eventManager.findEventListeners(fqnList, nullptr), KaaException);

    SyncRequest request;
    eventManager.onUpdate(request);

    const auto& requestContainer = request.eventListenersRequests.get_array();
    BOOST_CHECK(requestContainer.size() == 2);
    BOOST_CHECK(std::equal(requestContainer.begin()->eventClassFQNs.begin(), requestContainer.begin()->eventClassFQNs.end(),
            fqnList.begin()));

    EventListenersResponse elr1;
    elr1.requestId = reqId1;
    elr1.result = SUCCESS;
    elr1.listeners.is_null();

    EventListenersResponse elr2;
    elr2.requestId = reqId2;
    elr2.result = FAILURE;

    SyncResponse response;
    response.eventListenersResponses.set_array({elr1, elr2});

    updateListener.onUpdate(response);

    BOOST_CHECK(listener.isReceived_);
    BOOST_CHECK(listener.isFailed_);
}

BOOST_AUTO_TEST_SUITE_END()

}
