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

package org.kaaproject.kaa.client.notification;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommandType;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
/**
 * Default {@link NotificationManager} implementation.
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultNotificationManager implements NotificationManager, NotificationProcessor {
    private Map<String, Topic> topics = new HashMap<String, Topic>();

    private final Map<String, List<NotificationListener>> notificationListeners = new HashMap<String, List<NotificationListener>>();
    private final Set<NotificationListener> mandatoryListeners = new HashSet<NotificationListener>();
    private final Set<NotificationTopicListListener> topicsListeners = new HashSet<NotificationTopicListListener>();

    private List<SubscriptionCommand> subscriptionInfo = new LinkedList<SubscriptionCommand>();

    private KaaClientState state;
    private final UpdateManager updateManager;

    public DefaultNotificationManager(UpdateManager manager, KaaClientState state) {
        this.state = state;
        this.updateManager = manager;

        List<Topic> topicList = state.getTopics();

        if (topicList != null) {
            for (Topic topic : topicList) {
                topics.put(topic.getId(), topic);
            }
        }
    }

    private void updateSubscriptionInfo(String id, SubscriptionCommandType type) {
        SubscriptionCommand cmd = new SubscriptionCommand();
        cmd.setCommand(type);
        cmd.setTopicId(id);
        subscriptionInfo.add(cmd);
    }

    private void onSubscriptionInfoUpdated() {
        synchronized (subscriptionInfo) {
            if (!subscriptionInfo.isEmpty()) {
                updateManager.updateSubscriptionCommands(subscriptionInfo);
                subscriptionInfo.clear();
            }
        }
    }

    private Topic findTopicById(String id) {
        synchronized (topics) {
            return topics.get(id);
        }
    }

    @Override
    public void updateTopicSubscriptions(Map<String, List<NotificationListenerInfo>> subscribers) throws UnavailableTopicException {
        if (subscribers != null) {
            for (Map.Entry<String, List<NotificationListenerInfo>> cursor : subscribers.entrySet()) {
                Topic listenerTopic = findTopicById(cursor.getKey());

                if (listenerTopic == null) {
                    throw new UnavailableTopicException("Topic with id " + cursor.getKey() + " is not available");
                }

                synchronized (notificationListeners) {
                    List<NotificationListener> listeners = notificationListeners.get(cursor.getKey());

                    if (cursor.getValue() != null) {
                        for (NotificationListenerInfo subscriberInfo : cursor.getValue()) {
                            if (subscriberInfo.getAction() == NotificationListenerInfo.Action.ADD) {
                                if (listeners == null) {
                                    listeners = new LinkedList<NotificationListener>();
                                    notificationListeners.put(listenerTopic.getId(), listeners);
                                }

                                if (listeners.isEmpty() && listenerTopic.getSubscriptionType() == SubscriptionType.VOLUNTARY) {
                                    updateSubscriptionInfo(listenerTopic.getId(), SubscriptionCommandType.ADD);
                                }

                                if (subscriberInfo.getListener() != null && !listeners.contains(subscriberInfo.getListener())) {
                                    listeners.add(subscriberInfo.getListener());
                                }
                            } else if (listeners != null) {
                                listeners.remove(subscriberInfo.getListener());

                                if (listeners.isEmpty() && listenerTopic.getSubscriptionType() == SubscriptionType.VOLUNTARY) {
                                    updateSubscriptionInfo(listenerTopic.getId(), SubscriptionCommandType.REMOVE);
                                }
                            }
                        }
                    }
                }
            }

            onSubscriptionInfoUpdated();
        }
    }

    @Override
    public void addMandatoryTopicsListener(NotificationListener listener) {
        if (listener != null) {
            synchronized (mandatoryListeners) {
                mandatoryListeners.add(listener);
            }
        }
    }

    @Override
    public void removeMandatoryTopicsListener(NotificationListener listener) {
        if (listener != null) {
            synchronized (mandatoryListeners) {
                mandatoryListeners.remove(listener);
            }
        }
    }

    @Override
    public void addTopicListListener(NotificationTopicListListener listener) {
        if (listener != null) {
            synchronized (topicsListeners) {
                topicsListeners.add(listener);
            }
        }
    }

    @Override
    public void removeTopicListListener(NotificationTopicListListener listener) {
        if (listener != null) {
            synchronized (topicsListeners) {
                topicsListeners.remove(listener);
            }
        }
    }

    @Override
    public List<Topic> getTopics() {
        List<Topic> topicList = new LinkedList<Topic>();

        synchronized (topics) {
            for (Topic topic : topics.values()) {
                topicList.add(topic);
            }
        }

        return topicList;
    }

    @Override
    public void topicsListUpdated(List<Topic> list) {
        Map<String, Topic> newTopics = new HashMap<String, Topic>();

        synchronized (topics) {
            for (Topic topic : list) {
                newTopics.put(topic.getId(), topic);
                if (topics.remove(topic.getId()) == null) {
                    state.addTopic(topic);
                }
            }
            synchronized (notificationListeners) {
                for (Topic topic : topics.values()) {
                    notificationListeners.remove(topic.getId());
                    state.removeTopic(topic.getId());
                }
            }
            topics = newTopics;
        }

        synchronized (topicsListeners) {
            for (NotificationTopicListListener listener : topicsListeners) {
                listener.onListUpdated(list);
            }
        }
    }

    @Override
    public void notificationReceived(List<Notification> notifications) throws IOException {
        for (Notification notification : notifications) {
            Topic topic = findTopicById(notification.getTopicId());

            if (topic != null) {
                boolean hasOwner = false;
                synchronized (notificationListeners) {
                    List<NotificationListener> listeners = notificationListeners.get(topic.getId());
                    if (listeners != null && !listeners.isEmpty()) {
                        hasOwner = true;
                        for (NotificationListener listener : listeners) {
                            listener.onNotificationRaw(topic.getId(), notification.getBody());
                        }
                    }
                }

                if (!hasOwner) {
                    synchronized (mandatoryListeners) {
                        for (NotificationListener listener : mandatoryListeners) {
                            listener.onNotificationRaw(topic.getId(), notification.getBody());
                        }
                    }
                }
            }
        }
    }

}
