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

#include "headers/update/UpdateManagerMock.hpp"
#include <kaa/update/IUpdateListener.hpp>
#include <kaa/update/UpdateManager.hpp>
#include <kaa/KaaDefaults.hpp>
#include <boost/test/unit_test.hpp>
#include <boost/cstdint.hpp>

namespace kaa {

class UpdateListenerFake : public IUpdateListener {
public:
    UpdateListenerFake() : updateCounter(0) { }
    ~UpdateListenerFake() { }

    virtual void onUpdate(const SyncResponse& response)
    {
        ++updateCounter;
    }

    boost::uint16_t getUpdateCounter() const { return updateCounter; }

private:
    boost::uint16_t updateCounter;
};

BOOST_AUTO_TEST_SUITE(UpdateManagerTestSuite)

BOOST_AUTO_TEST_CASE(testSubscriptions)
{
    boost::shared_ptr<UpdateListenerFake> listener(new UpdateListenerFake);
    ClientStatus status(CLIENT_STATUS_FILE_LOCATION);
    UpdateManager manager(status);
    manager.addUpdateListener(IUpdateListenerPtr());
    manager.addUpdateListener(listener);
    manager.addUpdateListener(listener);
    manager.onSyncResponse(SyncResponse());

    manager.removeUpdateListener(listener);
    manager.removeUpdateListener(listener);
    manager.removeUpdateListener(IUpdateListenerPtr());
    manager.onSyncResponse(SyncResponse());

    BOOST_CHECK_EQUAL(listener->getUpdateCounter(), 1);
}

BOOST_AUTO_TEST_CASE(testSubscriptionCommands)
{
    ClientStatus status(CLIENT_STATUS_FILE_LOCATION);
    UpdateManager manager(status);

    SubscriptionCommands subsciptions;
    SubscriptionCommand cmd;
    cmd.command = SubscriptionCommandType::ADD;
    cmd.topicId = "topicId";
    subsciptions.push_back(cmd);
    manager.updateSubscriptionCommands(subsciptions);

    const auto& commands = manager.getSubscriptionCommands();
    SubscriptionCommand result = *commands.begin();
    BOOST_CHECK_EQUAL(result.topicId, cmd.topicId);
    BOOST_CHECK_EQUAL(result.command, cmd.command);
}

BOOST_AUTO_TEST_CASE(testNotifications)
{
    SyncResponse response;
    NotificationSyncResponse nfResponse;
    Topic topic;
    topic.id = "topicId";
    topic.name = "topicName";
    topic.subscriptionType = SubscriptionType::MANDATORY;
    nfResponse.availableTopics.set_array(std::vector<Topic>({ topic }));
    Notification nf;
    nf.body = { 1, 2, 3 };
    nf.seqNumber.set_int(5);
    nf.topicId = "topicId";
    nf.type = NotificationType::CUSTOM;
    nf.uid.set_null();
    nfResponse.notifications.set_array(std::vector<Notification>({ nf }));
    response.notificationSyncResponse.set_NotificationSyncResponse(nfResponse);

    ClientStatus status(CLIENT_STATUS_FILE_LOCATION);
    UpdateManager manager(status);
    manager.onSyncResponse(response);

    AcceptedNotificationIds ids;
    manager.getAcceptedNotificationIds(ids);
    BOOST_CHECK(ids.empty());

    TopicStates states;
    manager.getTopicStates(states);
    BOOST_CHECK_EQUAL(states.size(), 1);
    TopicState state = *states.begin();
    BOOST_CHECK_EQUAL(state.topicId, "topicId");
    BOOST_CHECK_EQUAL(state.seqNumber, 5);

    nf.uid.set_string("nfUid");
    nfResponse.notifications.set_array(std::vector<Notification>({ nf }));
    response.notificationSyncResponse.set_NotificationSyncResponse(nfResponse);

    manager.onSyncResponse(response);
    manager.getAcceptedNotificationIds(ids);
    BOOST_CHECK(!ids.empty());
    BOOST_CHECK_EQUAL(*ids.begin(), "nfUid");
}

BOOST_AUTO_TEST_SUITE_END()

}


