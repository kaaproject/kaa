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

#ifdef KAA_USE_NOTIFICATIONS

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/exception/UnavailableTopicException.hpp"

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

        if (topics_.insert(std::make_pair(topicState.first, topic)).second) {
            KAA_LOG_INFO(
                    boost::format("Loaded topic: id='%s', name='%s', type=%s") % topic.id % topic.name
                    % LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType));
        }
    }
}

void NotificationManager::topicsListUpdated(const Topics& topicList)
{
    KAA_MUTEX_LOCKING("topicsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::unordered_map<std::string/*Topic ID*/, Topic> newTopics;

    for (const auto& topic : topicList) {
        newTopics.insert(std::make_pair(topic.id, topic));
        topics_.erase(topic.id);
    }

    {
        KAA_MUTEX_LOCKING("optionalListenersGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
        KAA_MUTEX_LOCKED("optionalListenersGuard_");

        for (const auto& pair : topics_) {
            optionalListeners_.erase(pair.second.id);
        }
    }

    topics_ = newTopics;
    KAA_MUTEX_UNLOCKING("topicsGuard_");
    KAA_UNLOCK(topicsLock); KAA_MUTEX_UNLOCKED("topicsGuard_");

    notifyTopicUpdateSubscribers(topicList);
}

void NotificationManager::notificationReceived(const Notifications& notifications)
{
    for (const Notification& notification : notifications) {
        try {
            findTopic(notification.topicId);

            if (!notifyOptionalNotificationSubscribers(notification)) {
                notifyMandatoryNotificationSubscribers(notification);
            }
        } catch (const UnavailableTopicException& e) {
            KAA_LOG_WARN(boost::format("Received notification for unknown topic (id='%s')") % notification.topicId);
        }
    }
}

void NotificationManager::addTopicListListener(INotificationTopicListListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to add topic updates listener: null listener");
        throw KaaException("Bad topic update listener");
    }

    topicListeners_.addCallback(
            listener, std::bind(&INotificationTopicListListener::onListUpdated, listener, std::placeholders::_1));
}

void NotificationManager::removeTopicListListener(INotificationTopicListListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove topic updates listener: null listener");
        throw KaaException("Bad topic update listener");
    }

    topicListeners_.removeCallback(listener);
}

Topics NotificationManager::getTopics()
{
    KAA_MUTEX_LOCKING("topicsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::vector<Topic> topicList;
    topicList.resize(topics_.size());

    size_t i = 0;
    for (const auto& it : topics_) {
        topicList[i++] = it.second;
    }

    return topicList;
}

void NotificationManager::addNotificationListener(INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to add topic notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    mandatoryListeners_.addCallback(
            listener,
            std::bind(&INotificationListener::onNotificationRaw, listener, std::placeholders::_1,
                      std::placeholders::_2));
}

void NotificationManager::addNotificationListener(const std::string& topidId, INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to add notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    if (findTopic(topidId).subscriptionType != OPTIONAL) {
        KAA_LOG_WARN("Failed to add notification listener: topic isn't optional");
        throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
    }

    auto it = optionalListeners_.find(topidId);
    if (it != optionalListeners_.end()) {
        it->second->addCallback(
                listener,
                std::bind(&INotificationListener::onNotificationRaw, listener, std::placeholders::_1,
                          std::placeholders::_2));
    } else {
        NotificationObservablePtr listeners(new NotificationObservable);
        listeners->addCallback(
                listener,
                std::bind(&INotificationListener::onNotificationRaw, listener, std::placeholders::_1,
                          std::placeholders::_2));
        optionalListeners_.insert(std::make_pair(topidId, listeners));
    }
}

void NotificationManager::removeNotificationListener(INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove topic notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    mandatoryListeners_.removeCallback(listener);
}

void NotificationManager::removeNotificationListener(const std::string& topidId, INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    if (findTopic(topidId).subscriptionType != OPTIONAL) {
        KAA_LOG_WARN("Failed to remove notification listener: topic isn't optional");
        throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
    }

    KAA_MUTEX_LOCKING("optionalListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(notificationListenersLock, optionalListenersGuard_);
    KAA_MUTEX_LOCKED("optionalListenersGuard_");

    auto it = optionalListeners_.find(topidId);
    if (it != optionalListeners_.end()) {
        it->second->removeCallback(listener);
    }
}

void NotificationManager::subscribeToTopic(const std::string& id, bool forceSync)
{
    if (findTopic(id).subscriptionType != OPTIONAL) {
        KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't optional") % id);
        throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
    }

    updateSubscriptionInfo(id, SubscriptionCommandType::ADD);

    if (forceSync) {
        sync();
    }
}

void NotificationManager::subscribeToTopics(const std::list<std::string>& idList, bool forceSync)
{
    SubscriptionCommands subscriptions;

    for (const auto& id : idList) {
        if (findTopic(id).subscriptionType != OPTIONAL) {
            KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't optional") % id);
            throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
        }

        SubscriptionCommand cmd;
        cmd.command = SubscriptionCommandType::ADD;
        cmd.topicId = id;
        subscriptions.push_back(cmd);
    }

    updateSubscriptionInfo(subscriptions);
    if (forceSync) {
        sync();
    }
}

void NotificationManager::unsubscribeFromTopic(const std::string& id, bool forceSync)
{
    if (findTopic(id).subscriptionType != OPTIONAL) {
        KAA_LOG_WARN(boost::format("Failed to unsubscribe: topic '%s' isn't optional") % id);
        throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
    }

    updateSubscriptionInfo(id, SubscriptionCommandType::REMOVE);

    if (forceSync) {
        sync();
    }
}

void NotificationManager::unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync)
{
    SubscriptionCommands subscriptions;

    for (const auto& id : idList) {
        if (findTopic(id).subscriptionType != OPTIONAL) {
            KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't optional") % id);
            throw UnavailableTopicException(boost::format("Topic '%s' isn't optional"));
        }

        SubscriptionCommand cmd;
        cmd.command = SubscriptionCommandType::REMOVE;
        cmd.topicId = id;
        subscriptions.push_back(cmd);
    }

    updateSubscriptionInfo(subscriptions);

    if (forceSync) {
        sync();
    }
}

void NotificationManager::sync()
{
    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(subscriptionLock, subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    if (!subscriptions_.empty()) {
        KAA_LOG_INFO("Sending info about new optional topic subscription...");
        transport_->onSubscriptionChanged(subscriptions_);
        subscriptions_.clear();
        transport_->sync();
    }
}

void NotificationManager::updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type)
{
    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(subscriptionLock, subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    SubscriptionCommand cmd;
    cmd.command = type;
    cmd.topicId = id;
    subscriptions_.push_back(cmd);
}

void NotificationManager::updateSubscriptionInfo(const SubscriptionCommands& newSubscriptions)
{
    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(subscriptionLock, subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    subscriptions_.insert(subscriptions_.end(), newSubscriptions.begin(), newSubscriptions.end());
}

const Topic& NotificationManager::findTopic(const std::string& id)
{
    try {
        KAA_MUTEX_LOCKING("topicsGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
        KAA_MUTEX_LOCKED("topicsGuard_");

        return topics_.at(id);
    } catch (const std::out_of_range&) {
        KAA_LOG_WARN(boost::format("Failed to find topic by id '%s'") % id);
        throw UnavailableTopicException(boost::format("Unknown topic id '%s'") % id);
    }
}

void NotificationManager::notifyTopicUpdateSubscribers(const Topics& topics)
{
    topicListeners_(topics);
}

void NotificationManager::notifyMandatoryNotificationSubscribers(const Notification& notification)
{
    mandatoryListeners_(notification.topicId, notification.body);
}

bool NotificationManager::notifyOptionalNotificationSubscribers(const Notification& notification)
{
    bool notified = false;
    KAA_MUTEX_LOCKING("optionalListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
    KAA_MUTEX_LOCKED("optionalListenersGuard_");

    auto it = optionalListeners_.find(notification.topicId);
    if (it != optionalListeners_.end()) {
        notified = true;
        (*it->second)(notification.topicId, notification.body);
    }

    return notified;
}

void NotificationManager::setTransport(std::shared_ptr<NotificationTransport> transport)
{
    if (transport) {
        transport_ = transport;
        transport_->setNotificationProcessor(this);
    }
}

} /* namespace kaa */

#endif
