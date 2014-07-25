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
#include "kaa/notification/AbstractNotificationListener.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/notification/INotificationTopicsListener.hpp"

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

class TopicListener : public INotificationTopicsListener {
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

        TopicSubscribers subscriptions;
        TopicSubscriberInfo info;
        info.action_ = TopicSubscriberInfo::ADD;
        info.lisnener_ = voluntaryListener_.get();
        subscriptions.insert(std::make_pair(newList.back().id, info));
        notificationManager_.updateTopicSubscriptions(subscriptions);
    }

    ~TopicListener() {
        if (voluntaryListener_) {
            TopicSubscribers subscriptions;
            TopicSubscriberInfo info;
            info.action_ = TopicSubscriberInfo::REMOVE;
            info.lisnener_ = voluntaryListener_.get();
            subscriptions.insert(std::make_pair(topics_.back().id, info));
            notificationManager_.updateTopicSubscriptions(subscriptions);
        }
    }

private:
    Topics topics_;
    boost::shared_ptr<UserNotificationListener> voluntaryListener_;
    INotificationManager& notificationManager_;
};

BOOST_AUTO_TEST_SUITE(NotificationTestSuite)

BOOST_AUTO_TEST_CASE(BadSubscriber)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fakePath"));
    MockChannelManager channelManager;
    boost::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager( status);
    notificationManager.setTransport(transport);

    TopicSubscribers subscriptions;
    TopicSubscriberInfo info;
    info.action_ = TopicSubscriberInfo::ADD;
    info.lisnener_ = NULL;
    subscriptions.insert(std::make_pair("someId", info));

    BOOST_CHECK_THROW(notificationManager.updateTopicSubscriptions(subscriptions), KaaException);

    SystemNotificationListener mandatoryListener("fake");
    subscriptions.clear();
    info.action_ = TopicSubscriberInfo::REMOVE;
    info.lisnener_ = &mandatoryListener;
    subscriptions.insert(std::make_pair("unknownId", info));

    BOOST_CHECK_THROW(notificationManager.updateTopicSubscriptions(subscriptions), KaaException);
}

BOOST_AUTO_TEST_CASE(VoluntarySubscription)
{
    IKaaClientStateStoragePtr status(new ClientStatus("fakePath"));
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

    UserNotificationListener notificationListener(topic2.id);
    TopicSubscribers subscriptions;
    TopicSubscriberInfo info;
    info.action_ = TopicSubscriberInfo::ADD;
    info.lisnener_ = &notificationListener;
    subscriptions.insert(std::make_pair(topic2.id, info));

    notificationManager.updateTopicSubscriptions(subscriptions);

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
    boost::shared_ptr<NotificationTransport> transport(new NotificationTransport(status, channelManager));
    NotificationManager notificationManager(status);
    notificationManager.setTransport(transport);
    SystemNotificationListener mandatoryListener(manadatoryTopic.id);
    TopicListener topicsListener(notificationManager);

    notificationManager.addMandatoryTopicsListener(&mandatoryListener);
    notificationManager.addTopicsListener(&topicsListener);

    transport->onNotificationResponse(response);

    notificationManager.removeMandatoryTopicsListener(&mandatoryListener);
    notificationManager.removeTopicsListener(&topicsListener);
}

//BOOST_AUTO_TEST_CASE(RestorePersistedTopicState)
//{
//    DetailedTopicStates dts;
//    DetailedTopicState topicState1;
//    topicState1.topicId = "[top1]";
//    topicState1.topicName = "[Mandatory], Mandatory Topic";
//    topicState1.sequenceNumber = 1;
//    topicState1.subscriptionType = SubscriptionType::MANDATORY;
//
//    DetailedTopicState topicState2;
//    topicState2.topicId = "top2";
//    topicState2.topicName = "Voluntary Topic";
//    topicState2.sequenceNumber = 10;
//    topicState2.subscriptionType = SubscriptionType::VOLUNTARY;
//
//    dts.insert(std::make_pair(topicState1.topicId, topicState1));
//    dts.insert(std::make_pair(topicState2.topicId, topicState2));
//
//    {
//        ClientStatus cs("fake.txt");
//        cs.setTopicStates(dts);
//        cs.save();
//    }
//
//    ClientStatus restored("fake.txt");
//    MockChannelManager channelManager;
//    boost::shared_ptr<NotificationTransport> transport(new NotificationTransport(restored, channelManager));
//    NotificationManager notificationManager(transport, restored);
//
//    const auto& topics = notificationManager.getTopics();
//
//    auto topic1 = topics.find("[top1]");
//    auto topic2 = topics.find("top2");
//    BOOST_CHECK_MESSAGE(topic1 != topics.end(), "Failed to restore topic");
//    BOOST_CHECK_MESSAGE(topic2 != topics.end(), "Failed to restore topic");
//
//    BOOST_CHECK_EQUAL(topic1->second.id, topicState1.topicId);
//    BOOST_CHECK_EQUAL(topic1->second.name, topicState1.topicName);
//    BOOST_CHECK_EQUAL(topic1->second.subscriptionType, topicState1.subscriptionType);
//
//    BOOST_CHECK_EQUAL(topic2->second.id, topicState2.topicId);
//    BOOST_CHECK_EQUAL(topic2->second.name, topicState2.topicName);
//    BOOST_CHECK_EQUAL(topic2->second.subscriptionType, topicState2.subscriptionType);
//}

BOOST_AUTO_TEST_SUITE_END()

}
