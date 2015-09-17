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

#ifndef DEFAULTNOTIFICATONMANAGER_HPP_
#define DEFAULTNOTIFICATONMANAGER_HPP_

#include "kaa/KaaDefaults.hpp"

#include "kaa/KaaThread.hpp"

#include <memory>
#include <string>
#include <unordered_map>
#include <unordered_set>

#include "kaa/gen/EndpointGen.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/notification/INotificationManager.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/notification/INotificationListener.hpp"
#include "kaa/notification/INotificationProcessor.hpp"
#include "kaa/notification/INotificationTopicListListener.hpp"

#include "kaa/observer/KaaObservable.hpp"

namespace kaa {

class IExecutorContext;

class NotificationManager : public INotificationManager, public INotificationProcessor {
public:
    NotificationManager(IKaaClientStateStoragePtr status, IExecutorContext& executorContext);

    virtual void addTopicListListener(INotificationTopicListListener& listener);
    virtual void removeTopicListListener(INotificationTopicListListener& listener);
    virtual Topics getTopics();

    virtual void addNotificationListener(INotificationListener& listener);
    virtual void addNotificationListener(const std::string& topidId, INotificationListener& listener);
    virtual void removeNotificationListener(INotificationListener& listener);
    virtual void removeNotificationListener(const std::string& topidId, INotificationListener& listener);

    virtual void subscribeToTopic(const std::string& id, bool forceSync = true);
    virtual void subscribeToTopics(const std::list<std::string>& idList, bool forceSync = true);
    virtual void unsubscribeFromTopic(const std::string& id, bool forceSync = true);
    virtual void unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync = true);
    virtual void sync();

    virtual void topicsListUpdated(const Topics& topics);
    virtual void notificationReceived(const Notifications& notifications);

    void setTransport(std::shared_ptr<NotificationTransport> transport);

private:
    typedef std::shared_ptr<KaaNotification> KaaNotificationPtr;

private:
    void updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type);
    void updateSubscriptionInfo(const SubscriptionCommands& newSubscriptions);

    const Topic& findTopic(const std::string& id);

    void notifyTopicUpdateSubscribers(const Topics& topics);
    void notifyMandatoryNotificationSubscribers(const std::string& id, KaaNotificationPtr notification);
    bool notifyOptionalNotificationSubscribers(const std::string& id, KaaNotificationPtr notification);

private:
    IExecutorContext& executorContext_;

    std::shared_ptr<NotificationTransport>    transport_;
    IKaaClientStateStoragePtr                 clientStatus_;

    std::unordered_map<std::string/*Topic ID*/, Topic>    topics_;
    KAA_MUTEX_DECLARE(topicsGuard_);

    typedef KaaObservable<void(const std::string& topicId, const KaaNotification& notification)
                        , INotificationListener*> NotificationObservable;
    typedef std::shared_ptr<NotificationObservable>    NotificationObservablePtr;

    KaaObservable<void (const Topics& list), INotificationTopicListListener*>    topicListeners_;
    NotificationObservable                                                       mandatoryListeners_;
    std::unordered_map<std::string/*Topic ID*/, NotificationObservablePtr>       optionalListeners_;
    KAA_MUTEX_DECLARE(optionalListenersGuard_);

    SubscriptionCommands    subscriptions_;
    KAA_MUTEX_DECLARE(subscriptionsGuard_);
};

} /* namespace kaa */

#endif /* DEFAULTNOTIFICATONMANAGER_HPP_ */
