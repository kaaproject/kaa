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

#include "kaa/notification/NotificationTransport.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include <vector>
#include <string>
#include <algorithm>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/ClientStatus.hpp"

namespace kaa {

NotificationTransport::NotificationTransport(IKaaChannelManager& manager, IKaaClientContext &context)
    : AbstractKaaTransport(manager, context), notificationProcessor_(nullptr)
{}

std::vector<TopicState> NotificationTransport::prepareTopicStatesForRequest()
{
    auto &topicStates = context_.getStatus().getTopicStates();
    std::vector<TopicState> requestTopicStates(topicStates.size());
    auto currentTopicState = topicStates.begin();
    for (auto& topicState : requestTopicStates) {
         topicState.topicId = currentTopicState->first;
         topicState.seqNumber = currentTopicState->second;
         currentTopicState++;
    }

    return requestTopicStates;
}

NotificationSyncRequestPtr NotificationTransport::createEmptyNotificationRequest()
{
    NotificationSyncRequestPtr request(new NotificationSyncRequest);

    request->topicListHash = context_.getStatus().getTopicListHash();
    auto topicStates = prepareTopicStatesForRequest();

    if (!topicStates.empty()) {
        request->topicStates.set_array(topicStates);
    } else {
        request->topicStates.set_null();
    }

    request->acceptedUnicastNotifications.set_null();
    request->subscriptionCommands.set_null();

    return request;
}

NotificationSyncRequestPtr NotificationTransport::createNotificationRequest()
{
    NotificationSyncRequestPtr request(new NotificationSyncRequest);
    request->topicListHash = context_.getStatus().getTopicListHash();
    if (!acceptedUnicastNotificationIds_.empty()) {
        request->acceptedUnicastNotifications.set_array(std::vector<std::string>(
                acceptedUnicastNotificationIds_.begin(), acceptedUnicastNotificationIds_.end()));
    } else {
        request->acceptedUnicastNotifications.set_null();
    }
    auto topicStates = prepareTopicStatesForRequest();
    if (!topicStates.empty()) {
        request->topicStates.set_array(topicStates);
    } else {
        request->topicStates.set_null();
    }

    if (!subscriptions_.empty()) {
        request->subscriptionCommands.set_array(std::vector<SubscriptionCommand>(subscriptions_.begin(),
                                                                                 subscriptions_.end()));
    } else {
        request->subscriptionCommands.set_null();
    }

    return request;
}

void NotificationTransport::onNotificationResponse(const NotificationSyncResponse& response)
{
    auto &topicStates = context_.getStatus().getTopicStates();

    if (response.responseStatus == SyncResponseStatus::NO_DELTA) {
        acceptedUnicastNotificationIds_.clear();
    } else {
        if (!response.availableTopics.is_null()) {
            auto&& topics = response.availableTopics.get_array();
            std::sort(topics.begin(), topics.end(), [](const Topic& topic1, const Topic& topic2) { return topic1.id < topic2.id; });
            std::int64_t tId;
            std::int32_t topicListHash = 1;

            /* Calculating topic list hash */
            for (const auto& topic : topics) {
                 tId = topic.id;
                 topicListHash = 31 * topicListHash + (uint32_t)(tId ^ (tId >> 32));
            }

            /* If The current topic list hash are differs from the calculated one - update topic list and hash */
            if (context_.getStatus().getTopicListHash() != topicListHash) {
                context_.getStatus().setTopicListHash(topicListHash);
                context_.getStatus().setTopicList(topics);
            }

            if (notificationProcessor_) {
                notificationProcessor_->topicsListUpdated(topics);
            }

            /* In case when we received new topic list, we need to remove
             * outdated subscription commands.
             */
            subscriptions_.remove_if([&](SubscriptionCommand &subscription) {
                 auto topicIsAvailable = std::find_if(topics.begin(), topics.end(), [&](Topic &topic)
                                                      { return topic.id == subscription.topicId; });

                 return topicIsAvailable == topics.end();
            });
        }
    }
    /* Add/remove valid subscriptions */
    for (auto& subscription : subscriptions_) {
        if (subscription.command == ADD) {
            topicStates.insert(std::make_pair(subscription.topicId, 0));
        } else {
            topicStates.erase(subscription.topicId);
        }
    }

    subscriptions_.clear();
    if (!response.notifications.is_null()) {

        KAA_LOG_INFO(boost::format("Received notifications array: %1%") % LoggingUtils::toString(response.notifications));

        const auto& notifications = response.notifications.get_array();

        Notifications unicast = getUnicastNotifications(notifications);
        Notifications multicast = getMulticastNotifications(notifications);

        Notifications newNotifications;

        for (const auto& n : unicast) {
            const std::string& uid = n.uid.get_string();
            KAA_LOG_INFO(boost::format("Adding '%1%' to unicast accepted notifications") % uid);
            auto addResultPair = acceptedUnicastNotificationIds_.insert(uid);
            if (addResultPair.second) {
                newNotifications.push_back(n);
            } else {
                KAA_LOG_INFO(boost::format("Notification with uid [%1%] was already received") % uid);
            }
        }

        for (const auto& n : multicast) {
            KAA_LOG_DEBUG(boost::format("Notification: %1%, Stored sequence number: %2%")
                    % LoggingUtils::toString(n)
                    % topicStates[n.topicId]);
            auto& currentSequenceNumber = topicStates[n.topicId];
            std::int32_t notificationSequenceNumber = (n.seqNumber.is_null()) ? 0 : n.seqNumber.get_int();
            if (notificationSequenceNumber > currentSequenceNumber) {
                newNotifications.push_back(n);
                topicStates[n.topicId] = notificationSequenceNumber;
            }
        }
        if (notificationProcessor_) {
            notificationProcessor_->notificationReceived(newNotifications);
        }
    }

    context_.getStatus().setTopicStates(topicStates);

    if (response.responseStatus != SyncResponseStatus::NO_DELTA) {
        syncAck();
    }
}

Notifications NotificationTransport::getUnicastNotifications(const Notifications & notifications)
{
    Notifications result;
    for (const auto& n : notifications) {
        if (!n.uid.is_null()) {
            result.push_back(n);
        }
    }
    return result;
}

Notifications NotificationTransport::getMulticastNotifications(const Notifications & notifications)
{
    Notifications result;
    for (const auto& n : notifications) {
        if (n.uid.is_null()) {
            result.push_back(n);
        }
    }

    std::sort(result.begin(), result.end(), [&](const Notification& l, const Notification& r) -> bool { return l.seqNumber.get_int() < r.seqNumber.get_int(); });

    return result;
}

void NotificationTransport::onSubscriptionChanged(SubscriptionCommands&& commands)
{
    if (!commands.empty()) {
        subscriptions_.splice(subscriptions_.end(), commands, commands.begin(), commands.end());
    }
}

} /* namespace kaa */

#endif

