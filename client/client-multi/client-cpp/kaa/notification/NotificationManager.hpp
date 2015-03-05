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

#ifdef KAA_USE_NOTIFICATIONS

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

class NotificationManager: public INotificationManager, public INotificationProcessor {
public:
    NotificationManager(IKaaClientStateStoragePtr status);

    virtual void topicsListUpdated(const Topics& topics);

    virtual void notificationReceived(const Notifications& notifications);

    virtual void addTopicListListener(INotificationTopicListListenerPtr listener);

    virtual void removeTopicListListener(INotificationTopicListListenerPtr listener);

    virtual Topics getTopics();

    virtual void addNotificationListener(INotificationListenerPtr listener);

    virtual void addNotificationListener(const std::string& topidId, INotificationListenerPtr listener);

    virtual void removeNotificationListener(INotificationListenerPtr listener);

    virtual void removeNotificationListener(const std::string& topidId, INotificationListenerPtr listener);

    virtual void subscribeToTopic(const std::string& id, bool forceSync);

    virtual void subscribeToTopics(const std::list<std::string>& idList, bool forceSync);

    virtual void unsubscribeFromTopic(const std::string& id, bool forceSync);

    virtual void unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync);

    virtual void sync();

    /**
     * Provide notification transport to manager.
     */
    virtual void setTransport(std::shared_ptr<NotificationTransport> transport);
private:
    void updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type);
    void updateSubscriptionInfo(const SubscriptionCommands& newSubscriptions);

    const Topic& findTopic(const std::string& id);

    void notifyTopicUpdateSubscribers(const Topics& topics);
    void notifyMandatoryNotificationSubscribers(const Notification& notification);
    bool notifyOptionalNotificationSubscribers(const Notification& notification);

private:
    std::shared_ptr<NotificationTransport> transport_;
    IKaaClientStateStoragePtr clientStatus_;

    std::unordered_map<std::string/*Topic ID*/, Topic> topics_;

    KAA_MUTEX_DECLARE(topicsGuard_);
    KAA_MUTEX_DECLARE(optionalListenersGuard_);

    typedef KaaObservable<
    void(const std::string& topicId,
            const std::vector<std::uint8_t>& notification),
    INotificationListenerPtr> NotificationObservable;

    typedef std::shared_ptr<NotificationObservable> NotificationObservablePtr;

    KaaObservable<void (const Topics& list), INotificationTopicListListenerPtr> topicListeners_;
    NotificationObservable mandatoryListeners_;
    std::unordered_map<std::string/*Topic ID*/, NotificationObservablePtr> optionalListeners_;

    SubscriptionCommands subscriptions_;
    KAA_MUTEX_DECLARE(subscriptionsGuard_);
};

}
/* namespace kaa */

#endif

#endif /* DEFAULTNOTIFICATONMANAGER_HPP_ */
