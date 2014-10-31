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

#include <map>
#include <list>
#include <vector>
#include <memory>
#include <utility>

#include "kaa/ClientStatus.hpp"
#include "headers/gen/EndpointGen.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/exception/UnavailableTopicException.hpp"
#include "kaa/notification/AbstractNotificationListener.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/notification/INotificationTopicListListener.hpp"

#include "headers/channel/MockChannelManager.hpp"
#include "headers/channel/MockDataChannel.hpp"

namespace kaa {

class SystemNotificationListener : public AbstractNotificationListener<BasicSystemNotification>
{
public:
    SystemNotificationListener(const std::string& topicId) : topicId_(topicId) {}

    virtual void onNotification(const std::string& id, const BasicSystemNotification& notification)
    {
        BOOST_CHECK_MESSAGE(topicId_ == id, "Mandatory: received notification for a foreign topic");

        BOOST_CHECK_MESSAGE(notification.notificationBody == "system"
                , "Mandatory: unexpected notification body. Expected 'system'!!! ");

        BOOST_CHECK_MESSAGE((notification.systemNotificationParam1 == 1 && notification.systemNotificationParam2 == 2)
                , "Mandatory: unexpected notification param values. Expected 1 and 2!!! ");
    }

private:
    const std::string topicId_;
};

class UserNotificationListener : public AbstractNotificationListener<BasicUserNotification>
{
public:
    UserNotificationListener(const std::string& topicId) : topicId_(topicId) {}

    virtual void onNotification(const std::string& id, const BasicUserNotification& notification)
    {
        BOOST_CHECK_MESSAGE(topicId_ == id, "Voluntary: received notification for a foreign topic");

        BOOST_CHECK_MESSAGE(notification.notificationBody == "user"
                , "Voluntary: unexpected notification body. Expected 'system'!!! ");

        BOOST_CHECK_MESSAGE(notification.userNotificationParam == 3
                , "Voluntary: unexpected notification param value. Expected 3!!! ");
    }

private:
    const std::string topicId_;
};

class TopicListener : public INotificationTopicListListener {
public:
    TopicListener(INotificationManager& manager)
        : notificationManager_(manager){}

    virtual void onListUpdated(const Topics& newList)
    {
        BOOST_CHECK_MESSAGE(newList.size() == 2, "Wring topics number. Expected 2!!!");

        BOOST_CHECK_MESSAGE(newList.front().subscriptionType == SubscriptionType::MANDATORY
                , "Expected MANDATORY topic!!!");
        BOOST_CHECK_MESSAGE(newList.back().subscriptionType == SubscriptionType::VOLUNTARY
                , "Expected VOLUNTARY topic!!!");

        topics_ = newList;
        voluntaryListener_.reset(new UserNotificationListener(newList.back().id));

        notificationManager_.addNotificationListener(newList.back().id, voluntaryListener_);
        notificationManager_.subscribeOnTopic(newList.back().id, false);
    }

    ~TopicListener() {
        if (voluntaryListener_) {
            notificationManager_.removeNotificationListener(topics_.back().id, voluntaryListener_);
            notificationManager_.unsubscribeFromTopic(topics_.back().id, false);
        }
    }

private:
    Topics topics_;
    std::shared_ptr<UserNotificationListener> voluntaryListener_;
    INotificationManager& notificationManager_;
};

BOOST_AUTO_TEST_SUITE(NotificationTestSuite)

BOOST_AUTO_TEST_CASE(BadSubscriber)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fakePath"));
    MockChannelManager channelManager;
    std::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager( status);
    notificationManager.setTransport(transport);

    BOOST_CHECK_THROW(notificationManager.addNotificationListener("someId", INotificationListenerPtr()), KaaException);

    INotificationListenerPtr mandatoryListener(new SystemNotificationListener("fake"));
    BOOST_CHECK_THROW(notificationManager.removeNotificationListener("unknownId", mandatoryListener), UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(VoluntarySubscription)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fakePath"));
    MockChannelManager channelManager;
    std::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager(status);
    notificationManager.setTransport(transport);

    const std::string topicId1("id1");
    const std::string topicId2("id2");

    Topic topic1;
    topic1.id = topicId1;
    topic1.subscriptionType = MANDATORY;

    Topic topic2;
    topic2.id = topicId2;
    topic2.subscriptionType = VOLUNTARY;

    std::vector<Topic> topics = {topic1, topic2};
    NotificationSyncResponse response;
    response.availableTopics.set_array(topics);

    transport->onNotificationResponse(response);

    INotificationListenerPtr notificationListener(new UserNotificationListener(topic2.id));

    notificationManager.addNotificationListener(topic2.id, notificationListener);
    notificationManager.subscribeOnTopic(topic2.id, true);

    auto request = transport->createNotificationRequest();

    BOOST_CHECK(!request->subscriptionCommands.is_null());
    BOOST_CHECK(request->subscriptionCommands.get_array().size() == 1);
    BOOST_CHECK(request->subscriptionCommands.get_array().front().topicId == topic2.id);
}

BOOST_AUTO_TEST_CASE(NotificationReceiving)
{
    /* TEST DATA */
    Topic manadatoryTopic;
    manadatoryTopic.id = "aa11";
    manadatoryTopic.subscriptionType = SubscriptionType::MANDATORY;

    Topic voluntaryTopic;
    voluntaryTopic.id = "bb22";
    voluntaryTopic.subscriptionType = SubscriptionType::VOLUNTARY;

    Topics topics{manadatoryTopic, voluntaryTopic};

    SharedDataBuffer encodedData;
    AvroByteArrayConverter<BasicSystemNotification> systemNotificationContainer;
    AvroByteArrayConverter<BasicUserNotification> userNotificationContainer;

    BasicSystemNotification sn;
    sn.notificationBody = "system";
    sn.systemNotificationParam1 = 1;
    sn.systemNotificationParam2 = 2;

    encodedData = systemNotificationContainer.toByteArray(sn);

    Notification systemNotification1;
    systemNotification1.uid.set_null();
    systemNotification1.topicId = manadatoryTopic.id;
    systemNotification1.type = NotificationType::SYSTEM;
    systemNotification1.body = std::vector<uint8_t>(encodedData.first.get()
                                , encodedData.first.get() + encodedData.second);

    BasicUserNotification un;
    un.notificationBody = "user";
    un.userNotificationParam = 3;

    encodedData = userNotificationContainer.toByteArray(un);

    Notification personalNotification1;
    personalNotification1.uid.set_null();
    personalNotification1.topicId = voluntaryTopic.id;
    personalNotification1.type = NotificationType::CUSTOM;
    personalNotification1.body = std::vector<uint8_t>(encodedData.first.get()
            , encodedData.first.get() + encodedData.second);

    Notifications notifications{systemNotification1, personalNotification1};

    NotificationSyncResponse response;
    response.availableTopics.set_array(topics);
    response.notifications.set_array(notifications);

    /* TEST */
    IKaaClientStateStoragePtr status(new ClientStatus("fakePath"));
    MockChannelManager channelManager;
    std::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager(status);
    notificationManager.setTransport(transport);
    INotificationListenerPtr mandatoryListener(new SystemNotificationListener(manadatoryTopic.id));
    INotificationTopicListListenerPtr topicsListener(new TopicListener(notificationManager));

    notificationManager.addNotificationListener(mandatoryListener);
    notificationManager.addTopicListListener(topicsListener);

    transport->onNotificationResponse(response);

    notificationManager.removeNotificationListener(mandatoryListener);
    notificationManager.removeTopicListListener(topicsListener);
}

BOOST_AUTO_TEST_CASE(TopicsPersistence)
{
    IKaaClientStateStoragePtr status(new ClientStatus("test_status.txt"));
    MockChannelManager channelManager;
    boost::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager(status);
    notificationManager.setTransport(transport);

    const std::string topicId1("id1");
    const std::string topicId2("id2");

    Topic topic1;
    topic1.id = topicId1;
    topic1.subscriptionType = MANDATORY;

    Topic topic2;
    topic2.id = topicId2;
    topic2.subscriptionType = VOLUNTARY;

    std::vector<Topic> topics = {topic1, topic2};
    NotificationSyncResponse response;
    response.availableTopics.set_array(topics);

    transport->onNotificationResponse(response);

    status->save();

    IKaaClientStateStoragePtr newStatus(new ClientStatus("test_status.txt"));
    boost::shared_ptr<NotificationTransport> newTransport(new NotificationTransport(newStatus, channelManager));
    NotificationManager newNotificationManager(newStatus);
    newNotificationManager.setTransport(newTransport);

    const Topics loadedTopics = newNotificationManager.getTopics();

//    for (const auto& expectedTopic : topics) {
//        if (std::find_if(loadedTopics.begin(), loadedTopics.end(),
//                [&expectedTopic] (const Topic& topic) { return (expectedTopic.id == topic.id
//                        && expectedTopic.subscriptionType == topic.subscriptionType); }) == loadedTopics.end())
//        {
//            BOOST_CHECK_MESSAGE(false, "Couldn't find topic " + expectedTopic.id);
//        }
//    }
}

BOOST_AUTO_TEST_SUITE_END()

}
