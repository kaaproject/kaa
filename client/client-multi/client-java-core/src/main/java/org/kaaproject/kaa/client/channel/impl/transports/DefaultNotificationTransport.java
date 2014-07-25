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

package org.kaaproject.kaa.client.channel.impl.transports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationProcessor;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Notification;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SubscriptionCommand;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.endpoint.gen.TopicState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNotificationTransport extends AbstractKaaTransport implements NotificationTransport {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultNotificationTransport.class);

    private NotificationProcessor processor;
    private NotificationManager manager;
    private final Set<String> acceptedUnicastNotificationIds = new HashSet<>();
    private final List<SubscriptionCommand> sentNotificationCommands = new LinkedList<SubscriptionCommand>();

    private List<TopicState> getTopicStates() {
        List<TopicState> states = null;
        Map<String, Integer> nfSubscriptions = clientState.getNfSubscriptions();
        if(!nfSubscriptions.isEmpty()){
            states = new ArrayList<>();
            LOG.info("Topic States:");
            for(Entry<String, Integer> nfSubscription : nfSubscriptions.entrySet()){
                TopicState state = new TopicState(nfSubscription.getKey(), nfSubscription.getValue());
                states.add(state);
                LOG.info("{} : {}", state.getTopicId(), state.getSeqNumber());
            }

        }
        return states;
    }

    @Override
    public void sync() {
        syncByType(TransportType.NOTIFICATION);
    }

    @Override
    public NotificationSyncRequest createNotificationRequest() {
        if (clientState != null && manager != null) {
            NotificationSyncRequest request = new NotificationSyncRequest();
            request.setAppStateSeqNumber(clientState.getNotificationSeqNumber());
            if(!acceptedUnicastNotificationIds.isEmpty()){
                LOG.info("Accepted unicast Notifications: {}", acceptedUnicastNotificationIds.size());
                request.setAcceptedUnicastNotifications(new ArrayList<>(acceptedUnicastNotificationIds));
            }
            sentNotificationCommands.addAll(manager.releaseSubscriptionCommands());
            request.setSubscriptionCommands(sentNotificationCommands);
            request.setTopicStates(getTopicStates());
            return request;
        }
        return null;
    }

    @Override
    public void onNotificationResponse(NotificationSyncResponse response) throws IOException {
        if (processor != null && clientState != null) {
            acceptedUnicastNotificationIds.clear();
            List<Topic> topics = response.getAvailableTopics();
            if (topics != null) {
                for (Topic topic : topics) {
                    clientState.addTopic(topic);
                }
                processor.topicsListUpdated(topics);
            }
            List<Notification> notifications = response.getNotifications();
            if (notifications != null) {
                for (Notification notification : notifications) {
                    LOG.info("Received {}", notification);
                    if (notification.getUid() != null) {
                        LOG.info("Adding {} to unicast accepted notifications", notification.getUid());
                        acceptedUnicastNotificationIds.add(notification.getUid());
                    } else {
                        clientState.updateTopicSubscriptionInfo(notification.getTopicId(), notification.getSeqNumber());
                    }
                }
                processor.notificationReceived(notifications);
            }
            sentNotificationCommands.clear();
            clientState.setNotificationSeqNumber(response.getAppStateSeqNumber());
            LOG.info("Processed notification response");
        }
    }

    @Override
    public void setNotificationProcessor(NotificationProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void setNotificationManager(NotificationManager manager) {
        this.manager = manager;
    }

}
