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

#include <boost/test/unit_test.hpp>

#include <map>
#include <list>
#include <vector>
#include <memory>
#include <utility>
#include <thread>

#include "kaa/ClientStatus.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/UnavailableTopicException.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"
#include "kaa/notification/NotificationManager.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"

#include "headers/MockKaaClientStateStorage.hpp"
#include "headers/channel/MockChannelManager.hpp"
#include "headers/notification/MockNotificationListener.hpp"
#include "headers/notification/MockNotificationTopicListListener.hpp"
#include "headers/context/MockExecutorContext.hpp"

namespace kaa {

#define UNKNOWNTOPIC 65000

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static MockExecutorContext tmpExecContext;

void testSleep(std::size_t seconds)
{
    std::this_thread::sleep_for(std::chrono::seconds(seconds));
}

class TestNotificationTransport : public NotificationTransport
{
public:
    TestNotificationTransport(MockChannelManager &channelManager, IKaaClientContext &context)
        : NotificationTransport(channelManager, context) {}

    virtual void setNotificationProcessor(INotificationProcessor* processor) {
        ++onSetNotificationProcessor_;
        notificationProcessor_ = processor;
    }

public:
    std::size_t onSetNotificationProcessor_ = 0;
    INotificationProcessor* notificationProcessor_ = NULL;

};

BOOST_AUTO_TEST_SUITE(NotificationTestSuite)

BOOST_AUTO_TEST_CASE(SyncWithoutTransportTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    BOOST_CHECK_THROW(notificationManager.sync(), TransportNotFoundException);
}

BOOST_AUTO_TEST_CASE(SyncWithTransportTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    MockChannelManager manager;
    std::shared_ptr<TestNotificationTransport> notificationTransport(new TestNotificationTransport(manager, clientContext));
    notificationManager.setTransport(notificationTransport);

    BOOST_CHECK_NO_THROW(notificationManager.sync());
    BOOST_CHECK_EQUAL(notificationTransport->onSetNotificationProcessor_, 1);
    BOOST_CHECK_EQUAL(notificationTransport->notificationProcessor_, &notificationManager);
}

BOOST_AUTO_TEST_CASE(GetEmptyTopicListTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    BOOST_CHECK(notificationManager.getTopics().empty());
}

BOOST_AUTO_TEST_CASE(GetTopicsTest)
{
    const std::string STATUS_FILE_PATH = "test_status.txt";

    std::srand(std::time(NULL));
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext1(properties, tmp_logger, executorContext, status);
    IKaaClientStateStoragePtr clientStatus1(new ClientStatus(clientContext1));

    Topics topicList;
    std::size_t topicCount = 1 + rand() % 10;
    std::ostringstream ss;
    std::int64_t topicId = 1;
    for (std::size_t i = 0; i < topicCount; ++i) {
        Topic topic;

        topic.id = topicId++;

        ss.str("");
        ss << "topic_" << i << "_name";
        topic.name = ss.str();
        topic.subscriptionType = SubscriptionType::OPTIONAL_SUBSCRIPTION;
        topicList.push_back(topic);
    }

    clientStatus1->setTopicList(topicList);
    clientStatus1->save();
    clientStatus1.reset();

    KaaClientContext clientContext(properties, tmp_logger, tmpExecContext);
    IKaaClientStateStoragePtr clientStatus2(new ClientStatus(clientContext));
    clientContext.setStatus(clientStatus2);
    NotificationManager notificationManager(clientContext);

    auto topics = notificationManager.getTopics();

    BOOST_CHECK_EQUAL(topics.size(), topicList.size());

    std::remove(STATUS_FILE_PATH.c_str());
}

static Topics createTopics(std::size_t topicCount, bool isOptional = true)
{
    std::vector<Topic> topics;
    topics.reserve(topicCount);

    std::ostringstream ss;
    for (std::size_t i = 0; i < topicCount; ++i) {
        Topic topic;
        topic.id = i;
        ss.str("");
        ss << "topic_" << i << "_name";
        topic.name = ss.str();

        topic.subscriptionType = isOptional ? SubscriptionType::OPTIONAL_SUBSCRIPTION : SubscriptionType::MANDATORY_SUBSCRIPTION;

        topics.push_back(topic);
    }
    return topics;
}

static bool isTopicsEqual(const Topics& actualTopics, const Topics& expectedTopics)
{
    if (actualTopics.size() != expectedTopics.size()) {
        return false;
    }

    for (const auto& actualTopic : actualTopics) {
        bool found = false;
        for (const auto& expectedTopic : expectedTopics) {
            if (actualTopic.id == expectedTopic.id && actualTopic.name == expectedTopic.name &&
                    actualTopic.subscriptionType == expectedTopic.subscriptionType)
            {
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }
    }

   return true;
}

BOOST_AUTO_TEST_CASE(AddRemoveTopicListListenerTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    SimpleExecutorContext executorContext;
    executorContext.init();
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    MockNotificationTopicListListener topicListListener1;
    MockNotificationTopicListListener topicListListener2;

    notificationManager.addTopicListListener(topicListListener1);
    notificationManager.addTopicListListener(topicListListener2);

    std::size_t topicCount1 = 1 + rand() % 10;
    auto topics1 = createTopics(topicCount1);

    notificationManager.topicsListUpdated(topics1);
    testSleep(1);

    BOOST_CHECK_EQUAL(topicListListener1.onListUpdated_, 1);
    BOOST_CHECK_EQUAL(topicListListener2.onListUpdated_, 1);

    BOOST_CHECK(isTopicsEqual(topicListListener1.topics_, topics1));
    BOOST_CHECK(isTopicsEqual(topicListListener2.topics_, topics1));

    notificationManager.removeTopicListListener(topicListListener2);

    std::size_t topicCount2 = 1 + rand() % 10;
    auto topics2 = createTopics(topicCount2);

    notificationManager.topicsListUpdated(topics2);
    testSleep(1);

    BOOST_CHECK_EQUAL(topicListListener1.onListUpdated_, 2);
    BOOST_CHECK_EQUAL(topicListListener2.onListUpdated_, 1);

    BOOST_CHECK(isTopicsEqual(topicListListener1.topics_, topics2));
    BOOST_CHECK(isTopicsEqual(topicListListener2.topics_, topics1));
}

static Notification createNotification(const std::int64_t topicId)
{
    KaaNotification originalNotification;
    AvroByteArrayConverter<KaaNotification> serializer;

    Notification notification;
    notification.topicId = topicId;
    notification.seqNumber.set_int(rand());
    serializer.toByteArray(originalNotification, notification.body);
    notification.type = NotificationType::CUSTOM;
    notification.uid.set_null();

    return notification;
}

static std::vector<Notification> createNotifications(std::size_t notificationCount, const std::int64_t topicId)
{
    std::vector<Notification> notifications;
    notifications.reserve(notificationCount);

    for (size_t i = 0; i < notificationCount; ++i) {
        notifications.push_back(createNotification(topicId));
    }

    return notifications;
}

BOOST_AUTO_TEST_CASE(AddRemoveGlobalNotificationListenerTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    SimpleExecutorContext executorContext;
    executorContext.init();
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    MockNotificationListener notificationListener1;
    MockNotificationListener notificationListener2;

    notificationManager.addNotificationListener(notificationListener1);
    notificationManager.addNotificationListener(notificationListener2);

    std::size_t topicCount = 1;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    std::size_t notificationCount1 = rand() % 10;
    auto notifications1 = createNotifications(notificationCount1, topics.front().id);

    notificationManager.notificationReceived(notifications1);
    testSleep(1);

    BOOST_CHECK_EQUAL(notificationListener1.onNotification_, notificationCount1);
    BOOST_CHECK_EQUAL(notificationListener2.onNotification_, notificationCount1);

    std::size_t notificationCount2 = rand() % 10;
    auto notifications2 = createNotifications(notificationCount2, topics.front().id);

    notificationManager.removeNotificationListener(notificationListener2);
    notificationManager.notificationReceived(notifications2);
    testSleep(1);

    BOOST_CHECK_EQUAL(notificationListener1.onNotification_, notificationCount2 + notificationCount1);
    BOOST_CHECK_EQUAL(notificationListener2.onNotification_, notificationCount1);
}

BOOST_AUTO_TEST_CASE(NotificationListenerForUnknownTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;

    std::size_t topicCount = 1 + rand() % 10;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_THROW(notificationManager.addNotificationListener(UNKNOWNTOPIC, topicSpecificNotificationListener)
                    , UnavailableTopicException);

    BOOST_CHECK_THROW(notificationManager.removeNotificationListener(UNKNOWNTOPIC, topicSpecificNotificationListener)
                    , UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(AddRemoveTopicSpecificNotificationListenerTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    SimpleExecutorContext executorContext;
    executorContext.init();
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);

    std::size_t topicCount = 2;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    MockNotificationListener globalNotificationListener;
    MockNotificationListener topicSpecificNotificationListener;

    notificationManager.addNotificationListener(globalNotificationListener);
    notificationManager.addNotificationListener(topics[1].id, topicSpecificNotificationListener);

    std::size_t topic1NotificationCount1 = rand() % 10;
    auto topic1Notification1 = createNotifications(topic1NotificationCount1, topics[0].id);
    notificationManager.notificationReceived(topic1Notification1);
    testSleep(1);

    std::size_t topic2NotificationCount1 = rand() % 10;
    auto topic2Notification1 = createNotifications(topic2NotificationCount1, topics[1].id);
    notificationManager.notificationReceived(topic2Notification1);
    testSleep(1);

    BOOST_CHECK_EQUAL(globalNotificationListener.onNotification_, topic1NotificationCount1);
    BOOST_CHECK_EQUAL(topicSpecificNotificationListener.onNotification_, topic2NotificationCount1);

    notificationManager.removeNotificationListener(topics[1].id, topicSpecificNotificationListener);

    std::size_t topic1NotificationCount2 = rand() % 10;
    auto topic1Notification2 = createNotifications(topic1NotificationCount2, topics[0].id);
    notificationManager.notificationReceived(topic1Notification2);
    testSleep(1);

    std::size_t topic2NotificationCount2 = rand() % 10;
    auto topic2Notification2 = createNotifications(topic2NotificationCount2, topics[1].id);
    notificationManager.notificationReceived(topic2Notification2);
    testSleep(1);

    BOOST_CHECK_EQUAL(globalNotificationListener.onNotification_, topic1NotificationCount1 + topic1NotificationCount2 + topic2NotificationCount2);
    BOOST_CHECK_EQUAL(topicSpecificNotificationListener.onNotification_, topic2NotificationCount1);
}

BOOST_AUTO_TEST_CASE(SubscribeToUnknownTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;

    std::size_t topicCount = 1 + rand() % 10;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_THROW(notificationManager.subscribeToTopic(UNKNOWNTOPIC), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.subscribeToTopic(UNKNOWNTOPIC, false), UnavailableTopicException);

    BOOST_CHECK_THROW(notificationManager.subscribeToTopics({ topics.front().id, UNKNOWNTOPIC }), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.subscribeToTopics({ topics.front().id, UNKNOWNTOPIC }, false), UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(UnsubscribeToUnknownTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;

    std::size_t topicCount = 1 + rand() % 10;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopic(UNKNOWNTOPIC), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopic(UNKNOWNTOPIC, false), UnavailableTopicException);

    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopics({ topics.front().id, UNKNOWNTOPIC }), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopics({ topics.front().id, UNKNOWNTOPIC }, false), UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(SubscribeToMandatoryTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;

    std::size_t topicCount = 1 + rand() % 10;
    /*
     * All topics are MANDATORY_SUBSCRIPTION.
     */
    auto topics = createTopics(topicCount, false);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_THROW(notificationManager.subscribeToTopic(topics.front().id), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.subscribeToTopic(topics.front().id, false), UnavailableTopicException);

    std::list<std::int64_t> topicIds;
    for (const auto& topic : topics) {
        topicIds.push_back(topic.id);
    }

    BOOST_CHECK_THROW(notificationManager.subscribeToTopics(topicIds), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.subscribeToTopics(topicIds, false), UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(UnsubscribeFromMandatoryTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;

    std::size_t topicCount = 1 + rand() % 10;
    /*
     * All topics are MANDATORY_SUBSCRIPTION.
     */
    auto topics = createTopics(topicCount, false);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopic(topics.front().id), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopic(topics.front().id, false), UnavailableTopicException);

    std::list<std::int64_t> topicIds;
    for (const auto& topic : topics) {
        topicIds.push_back(topic.id);
    }

    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopics(topicIds), UnavailableTopicException);
    BOOST_CHECK_THROW(notificationManager.unsubscribeFromTopics(topicIds, false), UnavailableTopicException);
}

BOOST_AUTO_TEST_CASE(SubscribeToOptionalTopicTest)
{
    IKaaClientStateStoragePtr status(new MockKaaClientStateStorage);
    MockExecutorContext executorContext;
    KaaClientContext clientContext(properties, tmp_logger, executorContext, status);
    NotificationManager notificationManager(clientContext);
    MockNotificationListener topicSpecificNotificationListener;
    MockChannelManager manager;
    std::shared_ptr<TestNotificationTransport> notificationTransport(new TestNotificationTransport(manager, clientContext));
    notificationManager.setTransport(notificationTransport);

    std::size_t topicCount = 1;
    auto topics = createTopics(topicCount);

    notificationManager.topicsListUpdated(topics);

    BOOST_CHECK_NO_THROW(notificationManager.subscribeToTopic(topics.front().id, false));
    BOOST_CHECK_NO_THROW(notificationManager.subscribeToTopic(topics.front().id));
}

BOOST_AUTO_TEST_SUITE_END()

}
