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
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/ClientStatus.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/exception/UnavailableTopicException.hpp"

namespace kaa {

NotificationManager::NotificationManager(IKaaClientStateStoragePtr status)
    : clientStatus_(status), topicUpdateNotifying_(false)
    , mandatoryListenersNotifying_(false), voluntaryListenersNotifying_(false)
{
    const DetailedTopicStates& topicStates = clientStatus_->getTopicStates();

    for (auto &topicState : topicStates) {
        Topic topic;
        topic.id = topicState.second.topicId;
        topic.name = topicState.second.topicName;
        topic.subscriptionType = topicState.second.subscriptionType;

        if (topics_.insert(std::make_pair(topicState.first, topic)).second) {
            KAA_LOG_INFO(boost::format("Loaded topic: id='%s', name='%s', type=%s")
                % topic.id % topic.name % LoggingUtils::TopicSubscriptionTypeToString(topic.subscriptionType));
        }
    }
}

void NotificationManager::topicsListUpdated(const Topics& topicList)
{
    KAA_MUTEX_LOCKING("topicsGuard_");
    GuardLock topicsLock(topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::unordered_map<std::string/*Topic ID*/, Topic> newTopics;

    for (const auto& topic : topicList) {
        newTopics.insert(std::make_pair(topic.id, topic));
        topics_.erase(topic.id);
    }

    {
        KAA_MUTEX_LOCKING("voluntaryListenersGuard_");
        GuardLock voluntaryListenersLock(voluntaryListenersGuard_);
        KAA_MUTEX_LOCKED("voluntaryListenersGuard_");

        for (const auto& pair : topics_) {
            voluntaryListeners_.erase(pair.second.id);
        }
    }

    topics_ = newTopics;
    KAA_MUTEX_UNLOCKING("topicsGuard_");
    topicsLock.unlock();
    KAA_MUTEX_UNLOCKED("topicsGuard_");

    notifyTopicUpdateSubscribers(topicList);
}

void NotificationManager::notificationReceived(const Notifications& notifications)
{
    for (const Notification& notification : notifications) {
        try {
            findTopic(notification.topicId);

            if (!notifyVoluntaryNotificationSubscribers(notification)) {
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

    KAA_MUTEX_LOCKING("topicListenersGuard_");
    GuardLock topicListenersLock(topicListenersGuard_);
    KAA_MUTEX_LOCKED("topicListenersGuard_");

    if (!topicUpdateNotifying_) {
        topicListeners_.insert(listener);
    } else {
        topicListenersPendingAdd_.insert(listener);
    }
}

void NotificationManager::removeTopicListListener(INotificationTopicListListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove topic updates listener: null listener");
        throw KaaException("Bad topic update listener");
    }

    KAA_MUTEX_LOCKING("topicListenersGuard_");
    GuardLock topicListenersLock(topicListenersGuard_);
    KAA_MUTEX_LOCKED("topicListenersGuard_");

    if (!topicUpdateNotifying_) {
        topicListeners_.erase(listener);
    } else {
        topicListenersPendingRemove_.insert(listener);
    }
}

Topics NotificationManager::getTopics()
{
    KAA_MUTEX_LOCKING("topicsGuard_");
    GuardLock topicsLock(topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::vector<Topic> topicList;
    topicList.resize(topics_.size());

    size_t i = 0;
    for (const auto& it : topics_) {
        topicList[i] = it.second;
    }

    return topicList;
}

void NotificationManager::addNotificationListener(INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to add topic notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    KAA_MUTEX_LOCKING("mandatoryListenersGuard_");
    GuardLock notificationListenersLock(mandatoryListenersGuard_);
    KAA_MUTEX_LOCKED("mandatoryListenersGuard_");

    if (!mandatoryListenersNotifying_) {
        mandatoryListeners_.insert(listener);
    } else {
        mandatoryListenersPendingAdd_.insert(listener);
    }
}

void NotificationManager::addNotificationListener(const std::string& topidId, INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to add notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    if (findTopic(topidId).subscriptionType != VOLUNTARY) {
        KAA_LOG_WARN("Failed to add notification listener: topic isn't voluntary");
        throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
    }

    KAA_MUTEX_LOCKING("voluntaryListenersGuard_");
    GuardLock notificationListenersLock(voluntaryListenersGuard_);
    KAA_MUTEX_LOCKED("voluntaryListenersGuard_");

    if (!voluntaryListenersNotifying_) {
        auto it = voluntaryListeners_.find(topidId);
        if (it != voluntaryListeners_.end()) {
            it->second.insert(listener);
        } else {
            std::unordered_set<INotificationListenerPtr> listeners;
            listeners.insert(listener);
            voluntaryListeners_.insert(std::make_pair(topidId, listeners));
        }
    } else {
        voluntaryListenersPendingAdd_.insert(std::make_pair(topidId, listener));
    }
}

void NotificationManager::removeNotificationListener(INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove topic notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    KAA_MUTEX_LOCKING("mandatoryListenersGuard_");
    GuardLock notificationListenersLock(mandatoryListenersGuard_);
    KAA_MUTEX_LOCKED("mandatoryListenersGuard_");

    if (!mandatoryListenersNotifying_) {
        mandatoryListeners_.erase(listener);
    } else {
        mandatoryListenersPendingRemove_.insert(listener);
    }
}

void NotificationManager::removeNotificationListener(const std::string& topidId, INotificationListenerPtr listener)
{
    if (!listener) {
        KAA_LOG_WARN("Failed to remove notification listener: null listener");
        throw KaaException("Bad notification listener");
    }

    if (findTopic(topidId).subscriptionType != VOLUNTARY) {
        KAA_LOG_WARN("Failed to remove notification listener: topic isn't voluntary");
        throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
    }

    KAA_MUTEX_LOCKING("voluntaryListenersGuard_");
    GuardLock notificationListenersLock(voluntaryListenersGuard_);
    KAA_MUTEX_LOCKED("voluntaryListenersGuard_");

    if (!voluntaryListenersNotifying_) {
        auto it = voluntaryListeners_.find(topidId);
        if (it != voluntaryListeners_.end()) {
            it->second.erase(listener);
        }
    } else {
        voluntaryListenersPendingRemove_.insert(std::make_pair(topidId, listener));
    }
}

void NotificationManager::subscribeOnTopic(const std::string& id, bool forceSync)
{
    if (findTopic(id).subscriptionType != VOLUNTARY) {
        KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't voluntary") % id);
        throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
    }

    updateSubscriptionInfo(id, SubscriptionCommandType::ADD);

    if (forceSync) {
        sync();
    }
}

void NotificationManager::subscribeOnTopics(const std::list<std::string>& idList, bool forceSync)
{
    SubscriptionCommands subscriptions;

    for (const auto& id : idList) {
        if (findTopic(id).subscriptionType != VOLUNTARY) {
            KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't voluntary") % id);
            throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
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
    if (findTopic(id).subscriptionType != VOLUNTARY) {
        KAA_LOG_WARN(boost::format("Failed to unsubscribe: topic '%s' isn't voluntary") % id);
        throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
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
        if (findTopic(id).subscriptionType != VOLUNTARY) {
            KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%s' isn't voluntary") % id);
            throw UnavailableTopicException(boost::format("Topic '%s' isn't voluntary"));
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
    GuardLock subscriptionLock(subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    if (!subscriptions_.empty()) {
        KAA_LOG_INFO("Sending info about new voluntary topic subscription...");
        transport_->onSubscriptionChanged(subscriptions_);
        subscriptions_.clear();
        transport_->sync();
    }
}

void NotificationManager::updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type)
{
    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    GuardLock subscriptionLock(subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    SubscriptionCommand cmd;
    cmd.command = type;
    cmd.topicId = id;
    subscriptions_.push_back(cmd);
}

void NotificationManager::updateSubscriptionInfo(const SubscriptionCommands& newSubscriptions)
{
    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    GuardLock subscriptionLock(subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    subscriptions_.insert(subscriptions_.end(), newSubscriptions.begin(), newSubscriptions.end());
}

const Topic& NotificationManager::findTopic(const std::string& id)
{
    try {
        KAA_MUTEX_LOCKING("topicsGuard_");
        GuardLock topicsLock(topicsGuard_);
        KAA_MUTEX_LOCKED("topicsGuard_");

        return topics_.at(id);
    } catch (const std::out_of_range&) {
        KAA_LOG_WARN(boost::format("Failed to find topic by id '%s'") % id);
        throw UnavailableTopicException(boost::format("Unknown topic id '%s'") % id);
    }
}

void NotificationManager::notifyTopicUpdateSubscribers(const Topics& topics)
{
    KAA_MUTEX_LOCKING("topicNotifyGuard_");
    GuardLock topicNotifyLock(topicNotifyGuard_);
    KAA_MUTEX_LOCKED("topicNotifyGuard_");

    KAA_MUTEX_LOCKING("topicListenersGuard_");
    GuardLock topicListenersLock(topicListenersGuard_);
    KAA_MUTEX_LOCKED("topicListenersGuard_");

    topicUpdateNotifying_ = true;

    KAA_MUTEX_UNLOCKING("topicListenersGuard_");
    topicListenersLock.unlock();
    KAA_MUTEX_UNLOCKED("topicListenersGuard_");

    for (auto& listener : topicListeners_) {
        listener->onListUpdated(topics);
    }

    KAA_MUTEX_LOCKING("topicNotifyGuard_");
    topicListenersLock.lock();
    KAA_MUTEX_LOCKED("topicNotifyGuard_");

    topicUpdateNotifying_ = false;

    for (auto l : topicListenersPendingAdd_) {
        topicListeners_.insert(l);
    }
    topicListenersPendingAdd_.clear();

    for (auto l : topicListenersPendingRemove_) {
        topicListeners_.erase(l);
    }
    topicListenersPendingRemove_.clear();
}

void NotificationManager::notifyMandatoryNotificationSubscribers(const Notification& notification)
{
    KAA_MUTEX_LOCKING("mandatoryListenersNotifyGuard_");
    GuardLock mandatoryListenersNotifyLock(mandatoryListenersNotifyGuard_);
    KAA_MUTEX_LOCKED("mandatoryListenersNotifyGuard_");

    KAA_MUTEX_LOCKING("mandatoryListenersGuard_");
    GuardLock mandatoryListenersLock(mandatoryListenersGuard_);
    KAA_MUTEX_LOCKED("mandatoryListenersGuard_");

    mandatoryListenersNotifying_ = true;

    KAA_MUTEX_UNLOCKING("mandatoryListenersGuard_");
    mandatoryListenersLock.unlock();
    KAA_MUTEX_UNLOCKED("mandatoryListenersGuard_");

    for (auto& listener : mandatoryListeners_) {
        listener->onNotificationRaw(notification.topicId, notification.body);
    }

    KAA_MUTEX_LOCKING("mandatoryListenersGuard_");
    mandatoryListenersLock.lock();
    KAA_MUTEX_LOCKED("mandatoryListenersGuard_");

    mandatoryListenersNotifying_ = false;

    for (auto l : mandatoryListenersPendingAdd_) {
        mandatoryListeners_.insert(l);
    }
    mandatoryListenersPendingAdd_.clear();

    for (auto l : mandatoryListenersPendingRemove_) {
        mandatoryListeners_.erase(l);
    }
    mandatoryListenersPendingRemove_.clear();
}

bool NotificationManager::notifyVoluntaryNotificationSubscribers(const Notification& notification)
{
    bool notified = false;

    KAA_MUTEX_LOCKING("voluntaryListenersNotifyGuard_");
    GuardLock voluntaryListenersNotifyLock(voluntaryListenersNotifyGuard_);
    KAA_MUTEX_LOCKED("voluntaryListenersNotifyGuard_");

    KAA_MUTEX_LOCKING("voluntaryListenersGuard_");
    GuardLock voluntaryListenersLock(voluntaryListenersGuard_);
    KAA_MUTEX_LOCKED("voluntaryListenersGuard_");

    auto it = voluntaryListeners_.find(notification.topicId);
    if (it != voluntaryListeners_.end()) {
        notified = true;
        voluntaryListenersNotifying_ = true;

        KAA_MUTEX_UNLOCKING("voluntaryListenersGuard_");
        voluntaryListenersLock.unlock();
        KAA_MUTEX_UNLOCKED("voluntaryListenersGuard_");

        for (auto& listener : it->second) {
            listener->onNotificationRaw(notification.topicId, notification.body);
        }

        KAA_MUTEX_LOCKING("voluntaryListenersGuard_");
        voluntaryListenersLock.lock();
        KAA_MUTEX_LOCKED("voluntaryListenersGuard_");

        voluntaryListenersNotifying_ = false;
    }

    for (auto it : voluntaryListenersPendingAdd_) {
        auto volIt = voluntaryListeners_.find(it.first);
        if (volIt != voluntaryListeners_.end()) {
            volIt->second.insert(it.second);
        } else {
            std::unordered_set<INotificationListenerPtr> listeners;
            listeners.insert(it.second);
            voluntaryListeners_.insert(std::make_pair(notification.topicId, listeners));
        }
    }
    voluntaryListenersPendingAdd_.clear();

    for (auto it : voluntaryListenersPendingRemove_) {
        auto volIt = voluntaryListeners_.find(it.first);
        if (volIt != voluntaryListeners_.end()) {
            volIt->second.erase(it.second);
        }
    }
    voluntaryListenersPendingRemove_.clear();

    return notified;
}

void NotificationManager::setTransport(std::shared_ptr<NotificationTransport> transport) {
       if (transport) {
           transport_ = transport;
           transport_->setNotificationProcessor(this);
       }
   }

} /* namespace kaa */
