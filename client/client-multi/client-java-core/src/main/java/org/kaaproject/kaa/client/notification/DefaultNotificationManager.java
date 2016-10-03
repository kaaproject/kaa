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

package org.kaaproject.kaa.client.notification;

import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommandType;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default {@link NotificationManager} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultNotificationManager implements NotificationManager, NotificationProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationManager.class);
  private final ExecutorContext executorContext;
  private final NotificationDeserializer deserializer;
  private final Set<NotificationListener> mandatoryListeners = new HashSet<>();
  private final Map<Long, List<NotificationListener>> optionalListeners = new HashMap<>();
  private final Set<NotificationTopicListListener> topicsListeners = new HashSet<>();
  private final List<SubscriptionCommand> subscriptionInfo = new LinkedList<>();
  private final KaaClientState state;
  private Map<Long, Topic> topics = new HashMap<>();
  private volatile NotificationTransport transport;

  /**
   * All-args constructor.
   */
  public DefaultNotificationManager(KaaClientState state, ExecutorContext executorContext,
                                    NotificationTransport transport) {
    this.state = state;
    this.transport = transport;
    this.executorContext = executorContext;
    this.deserializer = new NotificationDeserializer(executorContext);

    Collection<Topic> topicList = state.getTopics();

    if (topicList != null) {
      for (Topic topic : topicList) {
        topics.put(topic.getId(), topic);
      }
    }
  }

  @Override
  public void addNotificationListener(NotificationListener listener) {
    if (listener == null) {
      LOG.warn("Failed to add notification listener: null");
      throw new IllegalArgumentException("NUll notification listener");
    }

    synchronized (mandatoryListeners) {
      if (!mandatoryListeners.contains(listener)) {
        mandatoryListeners.add(listener);
      }
    }
  }

  @Override
  public void addNotificationListener(Long topicId, NotificationListener listener)
      throws UnavailableTopicException {
    if (listener == null) {
      LOG.warn("Failed to add listener: id={}, listener={}", topicId, null);
      throw new IllegalArgumentException("Bad listener data");
    }

    findTopicById(topicId);

    synchronized (optionalListeners) {
      List<NotificationListener> listeners = optionalListeners.get(topicId);

      if (listeners == null) {
        listeners = new LinkedList<>();
        optionalListeners.put(topicId, listeners);
      }

      listeners.add(listener);
    }
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) {
    if (listener == null) {
      LOG.warn("Failed to remove notification listener: null");
      throw new IllegalArgumentException("NUll notification listener");
    }

    synchronized (mandatoryListeners) {
      mandatoryListeners.remove(listener);
    }
  }

  @Override
  public void removeNotificationListener(Long topicId, NotificationListener listener)
      throws UnavailableTopicException {
    if (topicId == null || listener == null) {
      LOG.warn("Failed to remove listener: id={}, listener={}", topicId, listener);
      throw new IllegalArgumentException("Bad listener data");
    }

    findTopicById(topicId);

    synchronized (optionalListeners) {
      List<NotificationListener> listeners = optionalListeners.get(topicId);

      if (listeners != null) {
        listeners.remove(listener);
      }
    }
  }

  @Override
  public void addTopicListListener(NotificationTopicListListener listener) {
    if (listener == null) {
      LOG.warn("Failed to add topic list listener: null");
      throw new IllegalArgumentException("NUll topic list listener");
    }

    synchronized (topicsListeners) {
      topicsListeners.add(listener);
    }
  }

  @Override
  public void removeTopicListListener(NotificationTopicListListener listener) {
    if (listener == null) {
      LOG.warn("Failed to remove topic list listener: null");
      throw new IllegalArgumentException("NUll topic list listener");
    }

    synchronized (topicsListeners) {
      topicsListeners.remove(listener);
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
  public void subscribeToTopic(Long topicId, boolean forceSync) throws UnavailableTopicException {
    Topic topic = findTopicById(topicId);
    if (topic.getSubscriptionType() != SubscriptionType.OPTIONAL_SUBSCRIPTION) {
      LOG.warn("Failed to subscribe: topic '{}' isn't optional", topicId);
      throw new UnavailableTopicException(String.format("Topic '%s' isn't optional", topicId));
    }

    updateSubscriptionInfo(topicId, SubscriptionCommandType.ADD);

    if (forceSync) {
      doSync();
    }
  }

  @Override
  public void subscribeToTopics(List<Long> topicIds, boolean forceSync)
      throws UnavailableTopicException {
    List<SubscriptionCommand> subscriptionUpdate = new LinkedList<>();

    for (Long id : topicIds) {
      Topic topic = findTopicById(id);
      if (topic.getSubscriptionType() != SubscriptionType.OPTIONAL_SUBSCRIPTION) {
        LOG.warn("Failed to subscribe: topic '{}' isn't optional", id);
        throw new UnavailableTopicException(String.format("Topic '%s' isn't optional", id));
      }

      subscriptionUpdate.add(new SubscriptionCommand(id, SubscriptionCommandType.ADD));
    }

    updateSubscriptionInfo(subscriptionUpdate);

    if (forceSync) {
      doSync();
    }
  }

  @Override
  public void unsubscribeFromTopic(Long topicId, boolean forceSync)
      throws UnavailableTopicException {
    Topic topic = findTopicById(topicId);
    if (topic.getSubscriptionType() != SubscriptionType.OPTIONAL_SUBSCRIPTION) {
      LOG.warn("Failed to unsubscribe: topic '{}' isn't optional", topicId);
      throw new UnavailableTopicException(String.format("Topic '%s' isn't optional", topicId));
    }

    updateSubscriptionInfo(topicId, SubscriptionCommandType.REMOVE);

    if (forceSync) {
      doSync();
    }
  }

  @Override
  public void unsubscribeFromTopics(List<Long> topicIds, boolean forceSync)
      throws UnavailableTopicException {
    List<SubscriptionCommand> subscriptionUpdate = new LinkedList<>();

    for (Long id : topicIds) {
      Topic topic = findTopicById(id);
      if (topic.getSubscriptionType() != SubscriptionType.OPTIONAL_SUBSCRIPTION) {
        LOG.warn("Failed to unsubscribe: topic '{}' isn't optional", id);
        throw new UnavailableTopicException(String.format("Topic '%s' isn't optional", id));
      }

      subscriptionUpdate.add(new SubscriptionCommand(id, SubscriptionCommandType.REMOVE));
    }

    updateSubscriptionInfo(subscriptionUpdate);

    if (forceSync) {
      doSync();
    }
  }


  @Override
  public void sync() {
    doSync();
  }

  @Override
  public void topicsListUpdated(final List<Topic> list) {
    Map<Long, Topic> newTopics = new HashMap<>();

    synchronized (topics) {
      for (Topic topic : list) {
        newTopics.put(topic.getId(), topic);
        if (topics.remove(topic.getId()) == null) {
          state.addTopic(topic);
        }
      }
      synchronized (optionalListeners) {
        for (Topic topic : topics.values()) {
          optionalListeners.remove(topic.getId());
          state.removeTopic(topic.getId());
        }
      }
      topics = newTopics;
    }

    synchronized (topicsListeners) {
      for (final NotificationTopicListListener listener : topicsListeners) {
        executorContext.getCallbackExecutor().submit(new Runnable() {
          @Override
          public void run() {
            listener.onListUpdated(list);
          }
        });
      }
    }
  }

  @Override
  public void notificationReceived(List<Notification> notifications) throws IOException {
    for (Notification notification : notifications) {
      try {
        Topic topic = findTopicById(notification.getTopicId());
        boolean hasOwner = false;

        synchronized (optionalListeners) {
          List<NotificationListener> listeners = optionalListeners.get(topic.getId());
          if (listeners != null && !listeners.isEmpty()) {
            hasOwner = true;
            notifyListeners(listeners, topic, notification);
          }
        }

        if (!hasOwner) {
          synchronized (mandatoryListeners) {
            notifyListeners(mandatoryListeners, topic, notification);
          }
        }
      } catch (UnavailableTopicException ex) {
        LOG.warn("Received notification for an unknown topic (id={}), exception catched: {}",
            notification.getTopicId(), ex);
      }
    }
  }

  private void notifyListeners(Collection<NotificationListener> listeners, final Topic topic,
                               final Notification notification) {
    final Collection<NotificationListener> listenersCopy =
            new ArrayList<NotificationListener>(listeners);
    if (notification.getBody() != null) {
      executorContext.getCallbackExecutor().submit(new Runnable() {
        @Override
        public void run() {
          try {
            deserializer.notify(
                Collections.unmodifiableCollection(listenersCopy),
                topic,
                notification.getBody().array());
          } catch (IOException ex) {
            LOG.error("Failed to process notification for topic {}", topic.getId(), ex);
          }
        }
      });
    }
  }

  private void updateSubscriptionInfo(Long id, SubscriptionCommandType type) {
    synchronized (subscriptionInfo) {
      subscriptionInfo.add(new SubscriptionCommand(id, type));
    }
  }

  private void updateSubscriptionInfo(List<SubscriptionCommand> subscriptionUpdate) {
    synchronized (subscriptionInfo) {
      subscriptionInfo.addAll(subscriptionUpdate);
    }
  }

  private Topic findTopicById(Long id) throws UnavailableTopicException {
    synchronized (topics) {
      Topic topic = topics.get(id);
      if (topic == null) {
        LOG.warn("Failed to find topic: id {} is unknown", id);
        throw new UnavailableTopicException(String.format("Topic id '%s' is unknown", id));
      }

      return topic;
    }
  }

  private void doSync() {
    synchronized (subscriptionInfo) {
      transport.onSubscriptionChanged(subscriptionInfo);
      subscriptionInfo.clear();
      transport.sync();
    }
  }
}
