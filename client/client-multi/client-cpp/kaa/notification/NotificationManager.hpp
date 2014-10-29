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

#include <map>
#include <string>

#include <boost/signals2.hpp>
#include <boost/thread/mutex.hpp>
#include <boost/shared_ptr.hpp>

#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/gen/EndpointGen.hpp"
#include "kaa/notification/INotificationManager.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/notification/INotificationListener.hpp"
#include "kaa/notification/INotificationProcessor.hpp"
#include "kaa/notification/INotificationTopicsListener.hpp"

namespace kaa {

class NotificationManager : public INotificationManager, public INotificationProcessor {
public:
    /**
     * Constructor
     * \param manager link to an instance of the Update Manager (\ref IUpdateManager)
     */
    NotificationManager(IKaaClientStateStoragePtr status);

    /**
     * Update (subscribe/unsubscribe) info about topic's subscriptions_
     * \throw KaaException when topic isn't found or bad subscription info was passed (empty id or null subscriber).
     * \param subscribers collections of pairs topic id/subscriber info.
     * May consist several subscribers for the same topic.
     */
    virtual void updateTopicSubscriptions(const TopicSubscribers& subscribers);

    /**
     * Subscribe mandatory listener for all topics.
     * Will be called if no specific (per-topic) exists
     * \param listener mandatory topic listener
     */
    virtual void addMandatoryTopicsListener(INotificationListener* listener);

    /**
     * Unsubscribe mandatory listener for all topics
     * \param listener mandatory topic listener
     */
    virtual void removeMandatoryTopicsListener(INotificationListener* listener);

    /**
     * Subscribe listener on notification updates of topics
     * \param listener notification topic listener
     */
    virtual void addTopicsListener(INotificationTopicsListener* listener);

    /**
     * Unsubscribe listener on notification updates of topics
     * \param listener notification topic listener
     */
    virtual void removeTopicsListener(INotificationTopicsListener* listener);

    /**
     *  Will be called when new topic list are received
     *  \param topics comprises of new topics
     */
    virtual void topicsListUpdated(const Topics& topics);

    /**
     *  Will be called when new topic list are received
     *  \param notifications comprises of new notifications
     */
    virtual void notificationReceived(const Notifications& notifications);

    /**
     * Returns currently available topics mapped by topic id
     */
    virtual const std::map<std::string, Topic>& getTopics() {
        return topics_;
    }

    /**
     * Provide notification transport to manager.
     */
    virtual void setTransport(std::shared_ptr<NotificationTransport> transport);
private:
    void updateSubscriptionInfo(const std::string& id, SubscriptionCommandType type);
    void onSubscriptionInfoUpdated();

private:
    typedef boost::signals2::signal<void (const Topics&)> TopicListeners;

    typedef boost::signals2::signal<void (const std::string&
                                        , const std::vector<boost::uint8_t>& )> NotificationListeners;

private:
    std::shared_ptr<NotificationTransport>                         transport_;
    IKaaClientStateStoragePtr                                        clientStatus_;

    std::map<std::string/*Topic ID*/, Topic>                         topics_;
    boost::mutex                                                     topicsGuard_;

    TopicListeners                                                   topicListeners_;
    NotificationListeners                                            mandatoryListeners_;

    std::map<std::string/*Topic ID*/, boost::shared_ptr<NotificationListeners> > notificationListeners_;
    boost::mutex                                                     notificationListenersGuard_;

    SubscriptionCommands subscriptions_;
};

} /* namespace kaa */

#endif /* DEFAULTNOTIFICATONMANAGER_HPP_ */
