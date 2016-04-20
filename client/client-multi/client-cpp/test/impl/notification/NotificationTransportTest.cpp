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

#include "kaa/ClientStatus.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/KaaClientContext.hpp"
#include "kaa/KaaClientProperties.hpp"
#include "kaa/logging/DefaultLogger.hpp"
#include "kaa/context/SimpleExecutorContext.hpp"

#include "headers/channel/MockChannelManager.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

static KaaClientProperties properties;
static DefaultLogger tmp_logger(properties.getClientId());
static SimpleExecutorContext context;
static MockKaaClientStateStorage tmp_state;
//static KaaClientContext clientContext(properties, tmp_logger, tmp_state, context);

BOOST_AUTO_TEST_SUITE(NotificationTransportTestSuite)

BOOST_AUTO_TEST_CASE(EmptyRequestTest)
{
    properties.setStateFileName("fakePath");
    KaaClientContext clientContext(properties, tmp_logger, context);
    IKaaClientStateStoragePtr status(new ClientStatus(clientContext));
    clientContext.setStatus(status);
    MockChannelManager channelManager;

    NotificationTransport transport(channelManager, clientContext);
    auto request = transport.createNotificationRequest();
    BOOST_CHECK(request->acceptedUnicastNotifications.is_null());
    BOOST_CHECK(request->subscriptionCommands.is_null());
    BOOST_CHECK(!request->topicListHash);
    BOOST_CHECK(request->topicStates.is_null());
}

BOOST_AUTO_TEST_CASE(SubscriptionInfoTest)
{
    properties.setStateFileName("fakePath");
    KaaClientContext clientContext(properties, tmp_logger, context);
    IKaaClientStateStoragePtr status(new ClientStatus(clientContext));
    clientContext.setStatus(status);

    MockChannelManager channelManager;
    NotificationTransport transport(channelManager, clientContext);

    SubscriptionCommand cmd1;

    cmd1.topicId = 1;
    cmd1.command = SubscriptionCommandType::ADD;

    SubscriptionCommand cmd2;
    cmd2.topicId = 2;
    cmd2.command = SubscriptionCommandType::REMOVE;

    SubscriptionCommands expectedCmds = {cmd1, cmd2};

    transport.onSubscriptionChanged(SubscriptionCommands(expectedCmds));

    auto request = transport.createNotificationRequest();
    BOOST_CHECK(!request->subscriptionCommands.is_null());

    const auto& actualCmds = request->subscriptionCommands.get_array();
    BOOST_CHECK(actualCmds.size() == expectedCmds.size());

    for (const auto& cmd : actualCmds) {
        bool found = false;
        for (auto it = expectedCmds.begin(); it != expectedCmds.end() && !found; ++it) {
            if (cmd.topicId == it->topicId) {
                found = true;
            }
        }

        BOOST_CHECK(found);
    }

    NotificationSyncResponse response;
    transport.onNotificationResponse(response);

    request = transport.createNotificationRequest();

    BOOST_CHECK(request->subscriptionCommands.is_null());
}

BOOST_AUTO_TEST_CASE(AcceptedUnicastNotificationsTest)
{
    properties.setStateFileName("fakePath");
    KaaClientContext clientContext(properties, tmp_logger, context);
    IKaaClientStateStoragePtr status(new ClientStatus(clientContext));
    clientContext.setStatus(status);

    MockChannelManager channelManager;
    NotificationTransport transport(channelManager, clientContext);

    std::string unicastNfUid("uid1");
    Notification nf1;
    nf1.topicId = 1;
    nf1.uid.set_string(unicastNfUid);

    Notification nf2;
    nf2.topicId = 2;
    nf2.uid.set_null();

    NotificationSyncResponse response1;

    response1.notifications.set_array(std::vector<Notification>({nf2, nf1}));
    transport.onNotificationResponse(response1);

    auto request1 = transport.createNotificationRequest();

    BOOST_CHECK(!request1->acceptedUnicastNotifications.is_null());

    auto acceptedUnicastNfs = request1->acceptedUnicastNotifications.get_array();
    BOOST_CHECK(acceptedUnicastNfs.size() == 1);
    BOOST_CHECK(acceptedUnicastNfs.front() == unicastNfUid);

    NotificationSyncResponse response2;
    response2.responseStatus = SyncResponseStatus::NO_DELTA;
    transport.onNotificationResponse(response2);

    auto request2 = transport.createNotificationRequest();
    BOOST_CHECK(request2->acceptedUnicastNotifications.is_null());
}

BOOST_AUTO_TEST_CASE(DetailedTopicStateTest)
{
    properties.setStateFileName("fakePath");
    KaaClientContext clientContext(properties, tmp_logger, context);
    IKaaClientStateStoragePtr status(new ClientStatus(clientContext));
    clientContext.setStatus(status);
    MockChannelManager channelManager;
    NotificationTransport transport(channelManager, clientContext);

    const std::int64_t topicId1(1);
    const std::int64_t topicId2(2);
    const std::int64_t topicId3(3);
    const std::int64_t topicId4(4);

    Topic topic1;
    topic1.id = topicId1;
    topic1.subscriptionType = OPTIONAL_SUBSCRIPTION;

    Topic topic2;
    topic2.id = topicId2;
    topic2.subscriptionType = MANDATORY_SUBSCRIPTION;

    Topic topic3;
    topic3.id = topicId3;
    topic3.subscriptionType = MANDATORY_SUBSCRIPTION;

    Topic topic4;
    topic4.id = topicId4;
    topic4.subscriptionType = OPTIONAL_SUBSCRIPTION;

    std::vector<Topic> topics = {topic1, topic2, topic3, topic4};

    NotificationSyncResponse response1;
    response1.responseStatus = SyncResponseStatus::DELTA;
    response1.availableTopics.set_array(topics);
    transport.onNotificationResponse(response1);

    auto detailedTopicState = status->getTopicStates();
    auto topicListHash = status->getTopicListHash();
    /* This check serves for ensuring that topic list hash has been generated
     * provided topic list
     */
    BOOST_CHECK(topicListHash != 0);
    auto topicList = status->getTopicList();
    BOOST_CHECK(topicList.size() == topics.size());

    for (const auto& topicInfo : detailedTopicState) {
        BOOST_CHECK(topicInfo.second == 0);
    }

    std::int32_t seqNm1 = 1;
    std::int32_t seqNm2 = 5;

    Notification nf1;
    nf1.topicId = topicId1;
    nf1.seqNumber.set_int(seqNm1++);
    nf1.uid.set_null();

    Notification nf2;
    nf2.topicId = topicId2;
    nf2.seqNumber.set_int(seqNm2);
    nf2.uid.set_null();

    Notification nf3;
    nf3.topicId = topicId1;
    nf3.seqNumber.set_int(seqNm1);
    nf3.uid.set_null();

    Notification nf4;
    nf4.topicId = topicId4;
    nf4.uid.set_null();

    NotificationSyncResponse response2;
    response2.notifications.set_array(std::vector<Notification>({nf2, nf1, nf3, nf4}));
    transport.onNotificationResponse(response2);

    detailedTopicState = clientContext.getStatus().getTopicStates();

    for (const auto& topicInfo : detailedTopicState) {
        if (topicInfo.first == topicId1) {
            BOOST_CHECK(topicInfo.second == seqNm1);
        } else if (topicInfo.first == topicId2) {
            BOOST_CHECK(topicInfo.second == seqNm2);
        }
    }
}

BOOST_AUTO_TEST_SUITE_END()

}
