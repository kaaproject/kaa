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

package org.kaaproject.kaa.server.operations.pojo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.TopicDto;


/**
 * The Class for modeling of delta response. It is used to communicate with
 * {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService
 * DeltaService}
 * 
 * @author ashvayka
 */

public class GetNotificationResponse {

    /** The notifications. */
    private List<NotificationDto> notifications;

    /** The topic list. */
    private List<TopicDto> topicList;

    /** The subscription states. */
    private Map<String, Integer> subscriptionStates;
    
    private Set<String> subscriptionSet;
    
    private boolean subscriptionSetChanged;

    /**
     * Instantiates a new gets the notification response.
     */
    public GetNotificationResponse() {
        super();
    }

    /**
     * Gets the notifications.
     *
     * @return the notifications
     */
    public List<NotificationDto> getNotifications() {
        return notifications;
    }

    /**
     * Sets the notifications.
     *
     * @param notifications the new notifications
     */
    public void setNotifications(List<NotificationDto> notifications) {
        this.notifications = notifications;
    }

    /**
     * Gets the topic list.
     *
     * @return the topic list
     */
    public List<TopicDto> getTopicList() {
        return topicList;
    }

    /**
     * Sets the topic list.
     *
     * @param topicList the new topic list
     */
    public void setTopicList(List<TopicDto> topicList) {
        this.topicList = topicList;
    }

    /**
     * Gets the subscription states.
     *
     * @return the subscription states
     */
    public Map<String, Integer> getSubscriptionStates() {
        return subscriptionStates;
    }

    /**
     * Sets the subscription states.
     *
     * @param subscriptionStates the subscription states
     */
    public void setSubscriptionStates(Map<String, Integer> subscriptionStates) {
        this.subscriptionStates = subscriptionStates;
    }

    /**
     * Checks for delta.
     *
     * @return true, if successful
     */
    public boolean hasDelta() {
        return (notifications != null && !notifications.isEmpty()) || (topicList != null && !topicList.isEmpty());
    }

    public boolean isSubscriptionListChanged() {
        return subscriptionSetChanged;
    }

    public void setSubscriptionSetChanged(boolean subscriptionSetChanged) {
        this.subscriptionSetChanged = subscriptionSetChanged;
    }

    public Set<String> getSubscriptionSet() {
        return subscriptionSet;
    }

    public void setSubscriptionSet(Set<String> subscriptionSet) {
        this.subscriptionSet = subscriptionSet;
    }
}
