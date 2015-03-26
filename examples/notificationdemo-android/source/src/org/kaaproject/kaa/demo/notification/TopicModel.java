/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.notification;

import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.LinkedList;
import java.util.List;

public class TopicModel {

    private final Topic topic;
    private final LinkedList<Notification> notifications;

    private boolean selected;
    private boolean subscribedTo;

    public TopicModel(Topic topic) {
        this.topic = topic;
        if (topic.getSubscriptionType() == SubscriptionType.MANDATORY) {
            selected = true;
        }
        notifications = new LinkedList<>();
    }

    public String getTopicName() {
        return topic.getName();
    }

    public String getTopicId() {
        return topic.getId();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isMandatoryTopic() {
        return topic.getSubscriptionType() == SubscriptionType.MANDATORY;
    }

    public int getNotificationsCount() {
        return notifications.size();
    }

    @SuppressWarnings("serial")
    public List<Notification> getNotifications() {
        if (notifications.size() > 0) {
            return notifications;
        } else {
            return new LinkedList<Notification>() {{
                add(new Notification("no notifications on this topic at the moment", ""));
            }};
        }
    }

    public void addNotification(Notification notification) {
        notifications.addFirst(notification);
    }

    public void setSubscribedTo(boolean subscribedTo) {
        this.subscribedTo = subscribedTo;
    }

    public boolean isSubscribedTo() {
        return subscribedTo;
    }
}
