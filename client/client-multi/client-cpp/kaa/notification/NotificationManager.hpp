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

#include <mutex>
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

namespace kaa {

class NotificationManager : public INotificationManager, public INotificationProcessor {
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

    virtual void subscribeOnTopic(const std::string& id, bool forceSync);

    virtual void subscribeOnTopics(const std::list<std::string>& idList, bool forceSync);

    virtual void unsubscribeFromTopic(const std::string& id, bool forceSync);

    virtual void unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync);

    virtual void sync();

    /**
     * Provide notification transport to manager.
     */
    virtual void setTransport(boost::shared_ptr<NotificationTransport> transport);
private:
    void updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type);
    void updateSubscriptionInfo(const SubscriptionCommands& newSubscriptions);

    const Topic& findTopic(const std::string& id);

    void notifyTopicUpdateSubscribers(const Topics& topics);
    void notifyMandatoryNotificationSubscribers(const Notification& notification);
    bool notifyVoluntaryNotificationSubscribers(const Notification& notification);

private:
    typedef std::unique_lock<std::mutex> GuardLock;

    boost::shared_ptr<NotificationTransport>                         transport_;
    IKaaClientStateStoragePtr                                        clientStatus_;

    std::unordered_map<std::string/*Topic ID*/, Topic>               topics_;
    std::mutex                                                       topicsGuard_;

    bool                                                             topicUpdateNotifying_;
    bool                                                             mandatoryListenersNotifying_;
    bool                                                             voluntaryListenersNotifying_;

    std::mutex                                                       topicNotifyGuard_;
    std::mutex                                                       mandatoryListenersNotifyGuard_;
    std::mutex                                                       voluntaryListenersNotifyGuard_;

    std::unordered_set<INotificationTopicListListenerPtr>            topicListeners_;
    std::unordered_set<INotificationTopicListListenerPtr>            topicListenersPendingAdd_;
    std::unordered_set<INotificationTopicListListenerPtr>            topicListenersPendingRemove_;
    std::mutex                                                       topicListenersGuard_;

    std::unordered_set<INotificationListenerPtr>                     mandatoryListeners_;
    std::unordered_set<INotificationListenerPtr>                     mandatoryListenersPendingAdd_;
    std::unordered_set<INotificationListenerPtr>                     mandatoryListenersPendingRemove_;
    std::mutex                                                       mandatoryListenersGuard_;

    std::unordered_map<std::string/*Topic ID*/,
                std::unordered_set<INotificationListenerPtr>>        voluntaryListeners_;
    std::unordered_multimap<std::string, INotificationListenerPtr>   voluntaryListenersPendingAdd_;
    std::unordered_multimap<std::string, INotificationListenerPtr>   voluntaryListenersPendingRemove_;
    std::mutex                                                       voluntaryListenersGuard_;

    SubscriptionCommands                                             subscriptions_;
    std::mutex                                                       subscriptionsGuard_;
};

} /* namespace kaa */

#endif /* DEFAULTNOTIFICATONMANAGER_HPP_ */
