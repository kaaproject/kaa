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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.NotificationDto;


/**
 * The Class NotificationMessage.
 */
public class NotificationMessage {

    /** The notifications. */
    private final List<NotificationDto> notifications;
    
    /** The unicast notification id. */
    private final String unicastNotificationId;
    
    /**
     * From notifications.
     *
     * @param notifications the notifications
     * @return the notification message
     */
    public static NotificationMessage fromNotifications(List<NotificationDto> notifications) {
        return new NotificationMessage(notifications, null);
    }    
    
    /**
     * From unicast id.
     *
     * @param unicastNotificationId the unicast notification id
     * @return the notification message
     */
    public static NotificationMessage fromUnicastId(String unicastNotificationId) {
        return new NotificationMessage(new ArrayList<NotificationDto>(), unicastNotificationId);
    }
    
    /**
     * Instantiates a new notification message.
     *
     * @param notifications the notifications
     * @param unicastNotificationId the unicast notification id
     */
    private NotificationMessage(List<NotificationDto> notifications, String unicastNotificationId) {
        super();
        this.notifications = Collections.unmodifiableList(notifications);
        this.unicastNotificationId = unicastNotificationId;
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
     * Gets the unicast notification id.
     *
     * @return the unicast notification id
     */
    public String getUnicastNotificationId() {
        return unicastNotificationId;
    }

    @Override
    public String toString() {
        return "NotificationMessage [notifications=" + notifications + ", unicastNotificationId=" + unicastNotificationId + "]";
    }
}
