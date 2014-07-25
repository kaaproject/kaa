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

#ifndef INOTIFICATIONMANAGER_HPP_
#define INOTIFICATIONMANAGER_HPP_

#include <map>
#include <list>
#include <string>

#include "kaa/notification/INotificationListener.hpp"
#include "kaa/notification/INotificationTopicsListener.hpp"

namespace kaa {

struct TopicSubscriberInfo {
    enum Action {
        ADD = 0,
        REMOVE
    };

    Action action_;
    INotificationListener* lisnener_;
};

typedef std::list<std::string> TopicNames;
typedef std::multimap<std::string/*topic's id*/, TopicSubscriberInfo> TopicSubscribers;

/**
 * Interface for the notification manager
 * Responsible for the subscription of custom listeners on topic updates
 */
class INotificationManager {
public:

    /**
     * Registers listener for all mandatory topics' updates.
     * If there is no specific listener for some mandatory topic,
     * notifications with that topic will be caught by this handler.
     *
     * @param listener the listener to receive notification.
     * @see AbstractNotificationListener
     *
     */
    virtual void addMandatoryTopicsListener(INotificationListener* listener) = 0;

    /**
     * Removes listener of mandatory topics' updates.
     *
     * @param listener the listener which is no longer needs updates.
     * @see AbstractNotificationListener
     *
     */
    virtual void removeMandatoryTopicsListener(INotificationListener* listener) = 0;

    /**
     * Adds listener of topics' list updates.
     *
     * @param listener the listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void addTopicsListener(INotificationTopicsListener* listener) = 0;

    /**
     * Removes listener of topics' list updates.
     *
     * @param listener listener the listener which is no longer needs updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void removeTopicsListener(INotificationTopicsListener* listener) = 0;

    /**
     * Updates (subscribes/unsubscribes) info about topic's subscriptions.
     * May consist of several subscribers for the same topic.
     *
     * @throws KaaException when topic isn't found or bad subscription
     * info was passed (empty id or null subscriber).
     *
     * @param subscribers collections of pairs topic id/subscriber info.
     * @see NotificationListenerInfo
     *
     */
    virtual void updateTopicSubscriptions(const TopicSubscribers& subscribers) = 0;

    /**
     * Retrieves the current topics' list.
     *
     * @return the list of topics' names.
     * @see Topic
     *
     */
    virtual const std::map<std::string, Topic>& getTopics() = 0;

    virtual ~INotificationManager() {}
};

} /* namespace kaa */

#endif /* INOTIFICATIONMANAGER_HPP_ */
