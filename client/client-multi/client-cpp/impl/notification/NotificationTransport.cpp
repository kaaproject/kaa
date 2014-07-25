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

#include "kaa/notification/NotificationTransport.hpp"

#include <vector>
#include <string>

#include "kaa/logging/Log.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/notification/INotificationProcessor.hpp"

namespace kaa {

NotificationSyncRequestPtr NotificationTransport::createNotificationRequest()
{
    NotificationSyncRequestPtr request(new NotificationSyncRequest);

    request->appStateSeqNumber = clientStatus_->getNotificationSequenceNumber();

    /*TODO: topic list hash is a future feature */
    request->topicListHash.set_null();

    if (!acceptedUnicastNotificationIds_.empty()) {
        request->acceptedUnicastNotifications.set_array(std::vector<std::string>(
                acceptedUnicastNotificationIds_.begin(), acceptedUnicastNotificationIds_.end()));
    } else {
        request->acceptedUnicastNotifications.set_null();
    }

    const DetailedTopicStates& detailedStatesContainer = clientStatus_->getTopicStates();
    if (!detailedStatesContainer.empty()) {
        std::vector<TopicState> container(detailedStatesContainer.size());
        auto it = detailedStatesContainer.begin();

        for (auto& state : container) {
            state.topicId = it->second.topicId;
            state.seqNumber = it->second.sequenceNumber;
            ++it;
        }

        request->topicStates.set_array(container);
    } else {
        request->topicStates.set_null();
    }

    if (!subscriptions_.empty()) {
        request->subscriptionCommands.set_array(std::vector<SubscriptionCommand>(
                                    subscriptions_.begin(), subscriptions_.end()));
    } else {
        request->subscriptionCommands.set_null();
    }

    return request;
}

void NotificationTransport::onNotificationResponse(const NotificationSyncResponse& response)
{
    subscriptions_.clear();
    acceptedUnicastNotificationIds_.clear();
    clientStatus_->setNotificationSequenceNumber(response.appStateSeqNumber);

    DetailedTopicStates detailedStatesContainer = clientStatus_->getTopicStates();

    if (!response.availableTopics.is_null()) {
        const auto& topics = response.availableTopics.get_array();

        detailedStatesContainer.clear();

        for (const auto& topic : topics) {
            DetailedTopicState dts;

            dts.topicId = topic.id;
            dts.topicName = topic.name;
            dts.subscriptionType = topic.subscriptionType;
            dts.sequenceNumber = 0;

            auto insertResult = notificationSubscriptions_.insert(std::make_pair(topic.id, 0));
            if (!insertResult.second) {
                dts.sequenceNumber = notificationSubscriptions_[topic.id];
            }

            detailedStatesContainer[topic.id] = dts;
        }

        if (notificationProcessor_ != nullptr) {
            notificationProcessor_->topicsListUpdated(topics);
        }
    }

    if (!response.notifications.is_null()) {
        const auto& notifications = response.notifications.get_array();

        for (const auto& notification : notifications) {
            if (notification.uid.is_null()) {
                auto& sequenceNumber = notificationSubscriptions_[notification.topicId];
                boost::int32_t notificationSequenceNumber =
                        (notification.seqNumber.is_null()) ? 0 : notification.seqNumber.get_int();

                if (sequenceNumber > 0) {
                    sequenceNumber = std::max(notificationSequenceNumber, sequenceNumber);
                } else {
                    sequenceNumber = notificationSequenceNumber;
                }
                detailedStatesContainer[notification.topicId].sequenceNumber = sequenceNumber;
            } else {
                const std::string& uid = notification.uid.get_string();
                KAA_LOG_INFO(boost::format("Adding '%1%' to unicast accepted notifications") % uid);
                acceptedUnicastNotificationIds_.push_back(uid);
            }
        }

        if (notificationProcessor_ != nullptr) {
            notificationProcessor_->notificationReceived(notifications);
        }
    }

    clientStatus_->setTopicStates(detailedStatesContainer);
}

void NotificationTransport::onSubscriptionChanged(const SubscriptionCommands& commands)
{
    if (!commands.empty()) {
        subscriptions_.insert(subscriptions_.end(), commands.begin(), commands.end());
    }
}

} /* namespace kaa */
