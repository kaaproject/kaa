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

#ifdef KAA_USE_NOTIFICATIONS

#include "kaa/notification/NotificationManager.hpp"

#include <sstream>

#include "kaa/logging/Log.hpp"
#include "kaa/logging/LoggingUtils.hpp"
#include "kaa/utils/IThreadPool.hpp"
#include "kaa/context/IExecutorContext.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/common/AvroByteArrayConverter.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/common/exception/UnavailableTopicException.hpp"
#include "kaa/common/exception/TransportNotFoundException.hpp"

namespace kaa {

NotificationManager::NotificationManager(IKaaClientContext &context)
    : context_(context)
{
    auto topicList = context_.getStatus().getTopicList();

    for (auto &topic : topicList) {
         if (topics_.insert(std::make_pair(topic.id, topic)).second) {
             KAA_LOG_INFO(boost::format("Loaded topic: id='%1%', name='%2%', type=%3%")
                 % topic.id % topic.name % LoggingUtils::toString(topic.subscriptionType));
         }
    }
}

void NotificationManager::topicsListUpdated(const Topics& topicList)
{
    KAA_LOG_INFO(boost::format("New list of available topics received (topic_count=%1%)") % topicList.size());

    KAA_MUTEX_LOCKING("topicsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::unordered_map<std::int64_t/*Topic ID*/, Topic> newTopics;

    for (const auto& topic : topicList) {
        newTopics.insert(std::make_pair(topic.id, topic));
        topics_.erase(topic.id);
    }

    {
        KAA_MUTEX_LOCKING("optionalListenersGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
        KAA_MUTEX_LOCKED("optionalListenersGuard_");
        auto &topicStates = context_.getStatus().getTopicStates();
        if (!topics_.empty()) {
            KAA_LOG_INFO(boost::format("Going to remove optional listener(s) for %1% obsolete topics") % topics_.size());
            for (const auto& pair : topics_) {
                 topicStates.erase(pair.first);
                 if (optionalListeners_.erase(pair.first)) {
                     KAA_LOG_TRACE(boost::format("Removed optional listener(s) for obsolete topic '%1%'") % pair.first);
                 }
            }
        }
    }

    topics_ = newTopics;

    KAA_MUTEX_UNLOCKING("topicsGuard_");
    KAA_UNLOCK(topicsLock);
    KAA_MUTEX_UNLOCKED("topicsGuard_");

    notifyTopicUpdateSubscribers(topicList);
}

void NotificationManager::notificationReceived(const Notifications& notifications)
{
    AvroByteArrayConverter<KaaNotification> deserializer;

    for (const Notification& notification : notifications) {
        try {
            findTopic(notification.topicId);

            auto deserializedNotification = std::make_shared<KaaNotification>();
            deserializer.fromByteArray(notification.body.data(), notification.body.size(), *deserializedNotification);

            if (!notifyOptionalNotificationSubscribers(notification.topicId, deserializedNotification)) {
                notifyMandatoryNotificationSubscribers(notification.topicId, deserializedNotification);
            }
        } catch (const UnavailableTopicException& e) {
            KAA_LOG_WARN(boost::format("Received notification for unknown topic (id='%1%')") % notification.topicId);
        }
    }
}

void NotificationManager::addTopicListListener(INotificationTopicListListener& listener)
{
    topicListeners_.addCallback(&listener, std::bind(&INotificationTopicListListener::onListUpdated, &listener,
                                                     std::placeholders::_1));
}

void NotificationManager::removeTopicListListener(INotificationTopicListListener& listener)
{
    topicListeners_.removeCallback(&listener);
}

Topics NotificationManager::getTopics()
{
    KAA_MUTEX_LOCKING("topicsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
    KAA_MUTEX_LOCKED("topicsGuard_");

    std::vector<Topic> topics;
    topics.reserve(topics_.size());

    for (const auto& it : topics_) {
        topics.push_back(it.second);
    }

    return topics;
}

void NotificationManager::addNotificationListener(INotificationListener& listener)
{
    mandatoryListeners_.addCallback(&listener, std::bind(&INotificationListener::onNotification, &listener,
                                                         std::placeholders::_1, std::placeholders::_2));
}

void NotificationManager::addNotificationListener(std::int64_t topidId, INotificationListener& listener)
{
    findTopic(topidId);

    KAA_MUTEX_LOCKING("optionalListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
    KAA_MUTEX_LOCKED("optionalListenersGuard_");

    auto it = optionalListeners_.find(topidId);
    if (it == optionalListeners_.end()) {
        it = optionalListeners_.insert(std::make_pair(topidId, std::make_shared<NotificationObservable>())).first;
    }

    it->second->addCallback(&listener, std::bind(&INotificationListener::onNotification, &listener,
                            std::placeholders::_1, std::placeholders::_2));
}

void NotificationManager::removeNotificationListener(INotificationListener& listener)
{
    mandatoryListeners_.removeCallback(&listener);
}

void NotificationManager::removeNotificationListener(std::int64_t topidId, INotificationListener& listener)
{
    findTopic(topidId);

    KAA_MUTEX_LOCKING("optionalListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
    KAA_MUTEX_LOCKED("optionalListenersGuard_");

    auto it = optionalListeners_.find(topidId);
    if (it != optionalListeners_.end()) {
        it->second->removeCallback(&listener);
        if (it->second->isEmpty()) {
            optionalListeners_.erase(topidId);
        }
    }
}

void NotificationManager::subscribeToTopic(std::int64_t id, bool forceSync)
{
    if (findTopic(id).subscriptionType != OPTIONAL_SUBSCRIPTION) {
        KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%1%' isn't optional") % id);
        throw UnavailableTopicException(boost::format("Topic '%1%' isn't optional") % id);
    }

    updateSubscriptionInfo(id, SubscriptionCommandType::ADD);

    if (forceSync) {
        KAA_LOG_INFO(boost::format("Going to subscribe to topic '%1%'") % id);
        sync();
    } else {
        KAA_LOG_INFO(boost::format("Subscription to topic '%1%' is postponed till sync") % id);
    }
}

void NotificationManager::subscribeToTopics(const std::list<std::int64_t>& idList, bool forceSync)
{
    std::ostringstream iss;
    SubscriptionCommands subscriptions;

    for (const auto& id : idList) {
        if (findTopic(id).subscriptionType != OPTIONAL_SUBSCRIPTION) {
            KAA_LOG_WARN(boost::format("Failed to subscribe: topic '%1%' isn't optional") % id);
            throw UnavailableTopicException(boost::format("Topic '%1%' isn't optional") % id);
        }

        SubscriptionCommand cmd;
        cmd.command = SubscriptionCommandType::ADD;
        cmd.topicId = id;
        subscriptions.push_back(cmd);
        iss << "'" << id << "' ";
    }

    updateSubscriptionInfo(subscriptions);

    if (forceSync) {
        KAA_LOG_INFO(boost::format("Going to subscribe to topics %1%") % iss.str());
        sync();
    } else {
        KAA_LOG_INFO(boost::format("Subscription to topics '%1%' is postponed till sync") % iss.str());
    }
}

void NotificationManager::unsubscribeFromTopic(std::int64_t id, bool forceSync)
{
    if (findTopic(id).subscriptionType != OPTIONAL_SUBSCRIPTION) {
        KAA_LOG_WARN(boost::format("Failed to unsubscribe: topic '%1%' isn't optional") % id);
        throw UnavailableTopicException(boost::format("Topic '%1%' isn't optional") % id);
    }

    updateSubscriptionInfo(id, SubscriptionCommandType::REMOVE);

    if (forceSync) {
        KAA_LOG_INFO(boost::format("Going to unsubscribe from topic '%1%'") % id);
        sync();
    } else {
        KAA_LOG_INFO(boost::format("Unsubscription from topic '%1%' is postponed till sync") % id);
    }
}

void NotificationManager::unsubscribeFromTopics(const std::list<std::int64_t>& idList, bool forceSync)
{
    std::ostringstream iss;
    SubscriptionCommands subscriptions;

    for (const auto& id : idList) {
        if (findTopic(id).subscriptionType != OPTIONAL_SUBSCRIPTION) {
            KAA_LOG_WARN(boost::format("Failed to unsubscribe: topic '%1%' isn't optional") % id);
            throw UnavailableTopicException(boost::format("Topic '%1%' isn't optional") % id);
        }

        SubscriptionCommand cmd;
        cmd.command = SubscriptionCommandType::REMOVE;
        cmd.topicId = id;
        subscriptions.push_back(cmd);
        iss << "'" << id << "' ";
    }

    updateSubscriptionInfo(subscriptions);

    if (forceSync) {
        KAA_LOG_INFO(boost::format("Going to unsubscribe from topics %1%") % iss.str());
        sync();
    } else {
        KAA_LOG_INFO(boost::format("Unsubscription from topics '%1%' is postponed till sync") % iss.str());
    }
}

void NotificationManager::sync()
{
    if (!transport_) {
        KAA_LOG_ERROR("Failed to sync: notification transport not found");
        throw TransportNotFoundException("Notification transport not found");
    }

    KAA_MUTEX_LOCKING("subscriptionsGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(subscriptionLock, subscriptionsGuard_);
    KAA_MUTEX_LOCKED("subscriptionsGuard_");

    if (!subscriptions_.empty()) {
        KAA_LOG_INFO(boost::format("Going to sync %1% topic subscription(s)...") % subscriptions_.size());
        transport_->onSubscriptionChanged(std::move(subscriptions_));
        subscriptions_ = SubscriptionCommands();
        transport_->sync();
    } else {
        KAA_LOG_TRACE("Nothing to sync. There are no topic subscriptions");
    }
}

void NotificationManager::updateSubscriptionInfo(std::int64_t id, SubscriptionCommandType type)
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

const Topic& NotificationManager::findTopic(std::int64_t id)
{
    try {
        KAA_MUTEX_LOCKING("topicsGuard_");
        KAA_MUTEX_UNIQUE_DECLARE(topicsLock, topicsGuard_);
        KAA_MUTEX_LOCKED("topicsGuard_");

        return topics_.at(id);
    } catch (const std::out_of_range&) {
        KAA_LOG_WARN(boost::format("Failed to find topic by id '%1%'") % id);
        throw UnavailableTopicException(boost::format("Unknown topic id '%1%'") % id);
    }
}

void NotificationManager::notifyTopicUpdateSubscribers(const Topics& topics)
{
    context_.getExecutorContext().getCallbackExecutor().add([this, topics] () { topicListeners_(topics); });
}

void NotificationManager::notifyMandatoryNotificationSubscribers(std::int64_t id, KaaNotificationPtr notification)
{
    context_.getExecutorContext().getCallbackExecutor().add([this, id, notification] () { mandatoryListeners_(id, *notification); });
}

bool NotificationManager::notifyOptionalNotificationSubscribers(std::int64_t id, KaaNotificationPtr notification)
{
    bool notified = false;

    KAA_MUTEX_LOCKING("optionalListenersGuard_");
    KAA_MUTEX_UNIQUE_DECLARE(optionalListenersLock, optionalListenersGuard_);
    KAA_MUTEX_LOCKED("optionalListenersGuard_");

    auto it = optionalListeners_.find(id);
    if (it != optionalListeners_.end()) {
        auto notifier = it->second;

        KAA_MUTEX_UNLOCKING("optionalListenersGuard_");
        KAA_UNLOCK(optionalListenersLock);
        KAA_MUTEX_UNLOCKED("optionalListenersGuard_");

        notified = true;

        context_.getExecutorContext().getCallbackExecutor().add([notifier, id, notification] () { (*notifier)(id, *notification); });
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
