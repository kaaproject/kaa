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

package org.kaaproject.kaa.server.sync;

import java.util.List;

public final class NotificationServerSync {
    private SyncResponseStatus responseStatus;
    private List<Notification> notifications;
    private List<Topic> availableTopics;

    /**
     * Default constructor. Note that this does not initialize fields to their
     * default values from the schema. If that is desired then one should use
     * {@link \#newBuilder()}.
     */
    public NotificationServerSync() {
    }

    /**
     * All-args constructor.
     */
    public NotificationServerSync(SyncResponseStatus responseStatus, List<Notification> notifications, List<Topic> availableTopics) {
        this.responseStatus = responseStatus;
        this.notifications = notifications;
        this.availableTopics = availableTopics;
    }

    /**
     * Gets the value of the 'responseStatus' field.
     */
    public SyncResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * Sets the value of the 'responseStatus' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setResponseStatus(SyncResponseStatus value) {
        this.responseStatus = value;
    }

    /**
     * Gets the value of the 'notifications' field.
     */
    public List<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Sets the value of the 'notifications' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setNotifications(List<Notification> value) {
        this.notifications = value;
    }

    /**
     * Gets the value of the 'availableTopics' field.
     */
    public List<Topic> getAvailableTopics() {
        return availableTopics;
    }

    /**
     * Sets the value of the 'availableTopics' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setAvailableTopics(List<Topic> value) {
        this.availableTopics = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NotificationServerSync that = (NotificationServerSync) o;

        if (availableTopics != null ? !availableTopics.equals(that.availableTopics) : that.availableTopics != null) {
            return false;
        }
        if (notifications != null ? !notifications.equals(that.notifications) : that.notifications != null) {
            return false;
        }
        if (responseStatus != that.responseStatus) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (responseStatus != null ? responseStatus.hashCode() : 0);
        result = 31 * result + (notifications != null ? notifications.hashCode() : 0);
        result = 31 * result + (availableTopics != null ? availableTopics.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NotificationServerSync [responseStatus=");
        builder.append(responseStatus);
        builder.append(", notifications=");
        builder.append(notifications);
        builder.append(", availableTopics=");
        builder.append(availableTopics);
        builder.append("]");
        return builder.toString();
    }
}
