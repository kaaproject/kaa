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

#include "kaa/notification/NotificationManager.hpp"

#include "kaa/logging/Log.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/common/exception/KaaException.hpp"

namespace kaa {

NotificationManager::NotificationManager(IKaaClientStateStoragePtr status)
    : clientStatus_(status)
{
    const DetailedTopicStates& topicStates = clientStatus_->getTopicStates();

    for (auto &topicState : topicStates) {
        Topic topic;
        topic.id = topicState.second.topicId;
        topic.name = topicState.second.topicName;
        topic.subscriptionType = topicState.second.subscriptionType;
        topics_.insert(std::make_pair(topicState.first, topic));
    }
}

void NotificationManager::updateTopicSubscriptions(const TopicSubscribers& subscribers)
{
    for (const auto& subscriber : subscribers) {
        if (subscriber.first.empty() || !subscriber.second.lisnener_) {
            throw KaaException("Bad subscription data");
        }

        KAA_MUTEX_LOCKING("topicsGuard_");
        boost::mutex::scoped_lock topicsLock(topicsGuard_);
        KAA_MUTEX_LOCKED("topicsGuard_");

        auto topicIt = topics_.find(subscriber.first);

        if (topicIt == topics_.end()) {
            throw KaaException("Topic with id " + subscriber.first + " is not available");
        }

        KAA_MUTEX_LOCKING("notificationListenersGuard_");
        boost::mutex::scoped_lock listenersLock(notificationListenersGuard_);
        KAA_MUTEX_LOCKED("notificationListenersGuard_");

        if (subscriber.second.action_ == TopicSubscriberInfo::ADD) {
            auto& listeners = notificationListeners_[subscriber.first];

            if (!listeners) {
                listeners.reset(new NotificationListeners());
            }

            if (listeners->empty() && topicIt->second.subscriptionType == SubscriptionType::VOLUNTARY) {
                updateSubscriptionInfo(subscriber.first, SubscriptionCommandType::ADD);
            }

            const auto connection = boost::bind(&INotificationListener::onNotificationRaw
                                                , subscriber.second.lisnener_, _1, _2);
            /* Disconnecting for case whether listener was already subscribed, no effect otherwise */
            listeners->disconnect(connection);
            listeners->connect(connection);
        } else {
            auto listenersIt = notificationListeners_.find(subscriber.first);

            if (listenersIt != notificationListeners_.end()) {
                auto& listeners = listenersIt->second;

                listeners->disconnect(boost::bind(&INotificationListener::onNotificationRaw
                                                    , subscriber.second.lisnener_, _1, _2));

                if (listeners->empty() && topicIt->second.subscriptionType == SubscriptionType::VOLUNTARY) {
                    updateSubscriptionInfo(subscriber.first, SubscriptionCommandType::REMOVE);
                }
            }
        }
    }

    onSubscriptionInfoUpdated();
}

void NotificationManager::addMandatoryTopicsListener(INotificationListener* listener)
{
    if (listener) {
        /* Disconnecting for case whether listener was already subscribed, no effect otherwise */
        mandatoryListeners_.disconnect(boost::bind(&INotificationListener::onNotificationRaw, listener, _1, _2));
        mandatoryListeners_.connect(boost::bind(&INotificationListener::onNotificationRaw, listener, _1, _2));
    }
}

void NotificationManager::removeMandatoryTopicsListener(INotificationListener* listener)
{
    if (listener) {
        mandatoryListeners_.disconnect(boost::bind(&INotificationListener::onNotificationRaw, listener, _1, _2));
    }
}

void NotificationManager::addTopicsListener(INotificationTopicsListener* listener)
{
    if (listener) {
        /* Disconnecting for case whether listener was already subscribed, no effect otherwise */
        topicListeners_.disconnect(boost::bind(&INotificationTopicsListener::onListUpdated, listener, _1));
        topicListeners_.connect(boost::bind(&INotificationTopicsListener::onListUpdated, listener, _1));
    }
}

void NotificationManager::removeTopicsListener(INotificationTopicsListener* listener)
{
    if (listener) {
        topicListeners_.disconnect(boost::bind(&INotificationTopicsListener::onListUpdated, listener, _1));
    }
}

void NotificationManager::topicsListUpdated(const Topics& topicList)
{
    std::map<std::string/*Topic ID*/, Topic> newTopics;

    KAA_MUTEX_LOCKING("topicsGuard_");
    boost::mutex::scoped_lock topicLock(topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    for (const auto& topic : topicList) {
        newTopics[topic.id] = topic;
        topics_.erase(topic.id);
    }

    {
        KAA_MUTEX_LOCKING("notificationListenersGuard_");
        boost::mutex::scoped_lock notificationListenersLock(notificationListenersGuard_);
        KAA_MUTEX_LOCKED("notificationListenersGuard_");

        for (const auto& pair : topics_) {
            notificationListeners_.erase(pair.second.id);
        }
    }

    topics_ = newTopics;
    topicLock.unlock();

    topicListeners_(topicList);
}

void NotificationManager::notificationReceived(const Notifications& notifications)
{
    for (const Notification& notification : notifications) {
        KAA_MUTEX_LOCKING("topicsGuard_");
        boost::mutex::scoped_lock lock(topicsGuard_);
        KAA_MUTEX_LOCKED("topicsGuard_");

        auto topicIt = topics_.find(notification.topicId);

        if (topicIt != topics_.end() && topicIt->second.id == notification.topicId) {
            bool hasOwner = false;

            {
                KAA_MUTEX_LOCKING("notificationListenersGuard_");
                boost::mutex::scoped_lock notificationListenersLock(notificationListenersGuard_);
                KAA_MUTEX_LOCKED("notificationListenersGuard_");

                auto listenerIt = notificationListeners_.find(notification.topicId);

                if (listenerIt != notificationListeners_.end() && !listenerIt->second->empty()) {
                    hasOwner = true;
                    (*listenerIt->second)(notification.topicId, notification.body);
                }
            }

            if (!hasOwner) {
                mandatoryListeners_(notification.topicId, notification.body);
            }
        }
    }
}

void NotificationManager::updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type)
{
    SubscriptionCommand cmd;
    cmd.command = type;
    cmd.topicId = id;
    subscriptions_.push_back(cmd);
}

void NotificationManager::onSubscriptionInfoUpdated()
{
    if (!subscriptions_.empty()) {
        KAA_LOG_INFO("Sending info about new voluntary topic subscription...");
        transport_->onSubscriptionChanged(subscriptions_);
        subscriptions_.clear();
        transport_->sync();
    }
}

void NotificationManager::setTransport(std::shared_ptr<NotificationTransport> transport) {
       if (transport) {
           transport_ = transport;
           transport_->setNotificationProcessor(this);
       }
   }

} /* namespace kaa */
