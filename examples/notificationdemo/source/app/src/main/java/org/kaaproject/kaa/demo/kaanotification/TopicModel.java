package org.kaaproject.kaa.demo.kaanotification;

import org.kaaproject.kaa.common.endpoint.gen.SubscriptionType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TopicModel {

    private boolean selected;

    private Topic topic;
    private List<Notification> notifications;
    private volatile AtomicBoolean subscribedTo;

    public TopicModel(Topic topic) {
        this.topic = topic;
        if (topic.getSubscriptionType() == SubscriptionType.MANDATORY)
            selected = true;
        notifications = new ArrayList<>();
        subscribedTo = new AtomicBoolean(false);
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

    public List<Notification> getNotifications() {
        if (notifications.size() > 0)
            return notifications;
        else return new LinkedList() {{
            add(new Notification("no notifications on this topic at the moment", ""));
        }};
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public void setSubscribedTo(boolean subscribedTo) {
        this.subscribedTo.set(subscribedTo);
    }

    public boolean isSubscribedTo() {
        return subscribedTo.get();
    }
}
