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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.client.notification.TopicListHashCalculator;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommandType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.TopicState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DefaultNotificationTransport extends AbstractKaaTransport
        implements NotificationTransport {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationTransport.class);
  private final Set<String> acceptedUnicastNotificationIds = new HashSet<>();
  private final List<SubscriptionCommand> sentNotificationCommands = new LinkedList<>();
  private NotificationProcessor processor;

  private List<TopicState> getTopicStates() {
    List<TopicState> states = null;
    Map<Long, Integer> nfSubscriptions = clientState.getNfSubscriptions();
    if (!nfSubscriptions.isEmpty()) {
      states = new ArrayList<>();
      LOG.info("Topic States:");
      for (Entry<Long, Integer> nfSubscription : nfSubscriptions.entrySet()) {
        TopicState state = new TopicState(nfSubscription.getKey(), nfSubscription.getValue());
        states.add(state);
        LOG.info("{} : {}", state.getTopicId(), state.getSeqNumber());
      }

    }
    return states;
  }

  @Override
  public NotificationSyncRequest createEmptyNotificationRequest() {
    if (clientState != null) {
      NotificationSyncRequest request = new NotificationSyncRequest();
      request.setTopicListHash(clientState.getTopicListHash());
      request.setTopicStates(getTopicStates());
      return request;
    }
    return null;
  }

  @Override
  public NotificationSyncRequest createNotificationRequest() {
    if (clientState != null) {
      NotificationSyncRequest request = new NotificationSyncRequest();

      if (!acceptedUnicastNotificationIds.isEmpty()) {
        LOG.info("Accepted unicast Notifications: {}", acceptedUnicastNotificationIds.size());
        request.setAcceptedUnicastNotifications(new ArrayList<>(acceptedUnicastNotificationIds));
      }
      request.setSubscriptionCommands(sentNotificationCommands);
      request.setTopicListHash(clientState.getTopicListHash());
      request.setTopicStates(getTopicStates());
      return request;
    }
    return null;
  }

  @Override
  public void onNotificationResponse(NotificationSyncResponse response) throws IOException {
    if (processor != null && clientState != null) {
      if (response.getResponseStatus() == SyncResponseStatus.NO_DELTA) {
        acceptedUnicastNotificationIds.clear();
      } else {
        List<Topic> topics = response.getAvailableTopics();
        if (topics != null) {
          clientState.setTopicListHash(TopicListHashCalculator.calculateTopicListHash(topics));
          processor.topicsListUpdated(topics);
        }
      }
      for (SubscriptionCommand subscriptionCommand : sentNotificationCommands) {
        if (subscriptionCommand.getCommand() == SubscriptionCommandType.ADD) {
          clientState.addTopicSubscription(subscriptionCommand.getTopicId());
        } else if (subscriptionCommand.getCommand() == SubscriptionCommandType.REMOVE) {
          clientState.removeTopicSubscription(subscriptionCommand.getTopicId());
        }
      }
      List<Notification> notifications = response.getNotifications();
      if (notifications != null) {
        List<Notification> newNotifications = new ArrayList<>(notifications.size());

        List<Notification> unicastNotifications = getUnicastNotifications(notifications);
        List<Notification> multicastNotifications = getMulticastNotifications(notifications);

        for (Notification notification : unicastNotifications) {
          LOG.info("Received {}", notification);
          if (acceptedUnicastNotificationIds.add(notification.getUid())) {
            newNotifications.add(notification);
          } else {
            LOG.info("Notification with uid [{}] was already received", notification.getUid());
          }
        }

        for (Notification notification : multicastNotifications) {
          LOG.info("Received {}", notification);
          if (clientState.updateTopicSubscriptionInfo(notification.getTopicId(),
                  notification.getSeqNumber())) {
            newNotifications.add(notification);
          } else {
            LOG.info("Notification with seq number {} was already received",
                    notification.getSeqNumber());
          }
        }
        processor.notificationReceived(newNotifications);
      }
      sentNotificationCommands.clear();

      syncAck(response.getResponseStatus());

      LOG.info("Processed notification response.");
    }
  }

  @Override
  public void onSubscriptionChanged(List<SubscriptionCommand> commands) {
    synchronized (sentNotificationCommands) {
      sentNotificationCommands.addAll(commands);
    }
  }

  private List<Notification> getUnicastNotifications(List<Notification> notifications) {
    List<Notification> result = new ArrayList<>();
    for (Notification notification : notifications) {
      if (notification.getUid() != null) {
        result.add(notification);
      }
    }
    return result;
  }

  private List<Notification> getMulticastNotifications(List<Notification> notifications) {
    List<Notification> result = new ArrayList<>();
    for (Notification notification : notifications) {
      if (notification.getUid() == null) {
        result.add(notification);
      }
    }
    Collections.sort(result, new Comparator<Notification>() {
      @Override
      public int compare(Notification o1, Notification o2) {
        return o1.getSeqNumber() - o2.getSeqNumber();
      }
    });
    return result;
  }

  @Override
  public void setNotificationProcessor(NotificationProcessor processor) {
    this.processor = processor;
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.NOTIFICATION;
  }

}
