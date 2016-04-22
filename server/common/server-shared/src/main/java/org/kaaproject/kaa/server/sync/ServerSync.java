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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.server.sync.bootstrap.BootstrapServerSync;

/**
 * 
 * @author Andrew Shvayka
 *
 */
public final class ServerSync {

    private int requestId;
    private SyncStatus status;
    private BootstrapServerSync bootstrapSync;
    private ProfileServerSync profileSync;
    private ConfigurationServerSync configurationSync;
    private NotificationServerSync notificationSync;
    private UserServerSync userSync;
    private EventServerSync eventSync;
    private RedirectServerSync redirectSync;
    private LogServerSync logSync;

    public ServerSync() {
    }

    /**
     * All-args constructor.
     */
    public ServerSync(int requestId, SyncStatus status, ProfileServerSync profileSync,
            ConfigurationServerSync configurationSync, NotificationServerSync notificationSync, UserServerSync userSync,
            EventServerSync eventSync, RedirectServerSync redirectSync, LogServerSync logSync) {
        this.requestId = requestId;
        this.status = status;
        this.profileSync = profileSync;
        this.configurationSync = configurationSync;
        this.notificationSync = notificationSync;
        this.userSync = userSync;
        this.eventSync = eventSync;
        this.redirectSync = redirectSync;
        this.logSync = logSync;
    }

    /**
     * Gets the value of the 'requestId' field.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(int value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the 'status' field.
     */
    public SyncStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the 'status' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setStatus(SyncStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the 'profileSync' field.
     */
    public ProfileServerSync getProfileSync() {
        return profileSync;
    }

    /**
     * Sets the value of the 'profileSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setProfileSync(ProfileServerSync value) {
        this.profileSync = value;
    }

    /**
     * Gets the value of the 'configurationSync' field.
     */
    public ConfigurationServerSync getConfigurationSync() {
        return configurationSync;
    }

    /**
     * Sets the value of the 'configurationSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfigurationSync(ConfigurationServerSync value) {
        this.configurationSync = value;
    }

    /**
     * Gets the value of the 'notificationSync' field.
     */
    public NotificationServerSync getNotificationSync() {
        return notificationSync;
    }

    /**
     * Sets the value of the 'notificationSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setNotificationSync(NotificationServerSync value) {
        this.notificationSync = value;
    }

    /**
     * Gets the value of the 'userSync' field.
     */
    public UserServerSync getUserSync() {
        return userSync;
    }

    /**
     * Sets the value of the 'userSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserSync(UserServerSync value) {
        this.userSync = value;
    }

    /**
     * Gets the value of the 'eventSync' field.
     */
    public EventServerSync getEventSync() {
        return eventSync;
    }

    /**
     * Sets the value of the 'eventSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventSync(EventServerSync value) {
        this.eventSync = value;
    }

    /**
     * Gets the value of the 'redirectSync' field.
     */
    public RedirectServerSync getRedirectSync() {
        return redirectSync;
    }

    /**
     * Sets the value of the 'redirectSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRedirectSync(RedirectServerSync value) {
        this.redirectSync = value;
    }

    /**
     * Gets the value of the 'logSync' field.
     */
    public LogServerSync getLogSync() {
        return logSync;
    }

    /**
     * Sets the value of the 'logSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setLogSync(LogServerSync value) {
        this.logSync = value;
    }

    public BootstrapServerSync getBootstrapSync() {
        return bootstrapSync;
    }

    public void setBootstrapSync(BootstrapServerSync bootstrapSync) {
        this.bootstrapSync = bootstrapSync;
    }
    
    public static ServerSync deepCopy(ServerSync source) {
        if (source == null) {
            return null;
        }
        ServerSync copy = new ServerSync();
        copy.setRequestId(source.getRequestId());
        copy.setStatus(source.getStatus());
        copy.setUserSync(deepCopy(source.getUserSync()));
        copy.setRedirectSync(deepCopy(source.getRedirectSync()));
        copy.setProfileSync(deepCopy(source.getProfileSync()));
        copy.setNotificationSync(deepCopy(source.getNotificationSync()));
        copy.setLogSync(deepCopy(source.getLogSync()));
        copy.setEventSync(deepCopy(source.getEventSync()));
        copy.setConfigurationSync(deepCopy(source.getConfigurationSync()));
        return copy;
    }
    

    public static void cleanup(ServerSync syncResponse) {
        if (syncResponse == null) {
            return;
        }
        syncResponse.setUserSync(null);
        syncResponse.setRedirectSync(null);
        syncResponse.setProfileSync(null);
        syncResponse.setNotificationSync(null);
        syncResponse.setLogSync(null);
        syncResponse.setEventSync(null);
        syncResponse.setConfigurationSync(null);
    }

    private static ConfigurationServerSync deepCopy(ConfigurationServerSync source) {
        if (source == null) {
            return null;
        }
        ConfigurationServerSync copy = new ConfigurationServerSync();
        copy.setResponseStatus(source.getResponseStatus());
        copy.setConfDeltaBody(source.getConfDeltaBody());
        copy.setConfSchemaBody(source.getConfSchemaBody());
        return copy;
    }

    private static EventServerSync deepCopy(EventServerSync source) {
        if (source == null) {
            return null;
        }
        EventServerSync copy = new EventServerSync();
        if (source.getEventSequenceNumberResponse() != null) {
            copy.setEventSequenceNumberResponse(source.getEventSequenceNumberResponse());
        }
        if (source.getEvents() != null) {
            copy.setEvents(new ArrayList<>(source.getEvents()));
        }
        if (source.getEventListenersResponses() != null) {
            copy.setEventListenersResponses(new ArrayList<>(source.getEventListenersResponses()));
        }
        return copy;
    }

    private static LogServerSync deepCopy(LogServerSync source) {
        if (source == null) {
            return null;
        }
        if (source.getDeliveryStatuses() != null) {
            List<LogDeliveryStatus> statusList = new ArrayList<>(source.getDeliveryStatuses().size());
            for (LogDeliveryStatus status : source.getDeliveryStatuses()) {
                statusList.add(new LogDeliveryStatus(status.getRequestId(), status.getResult(), status.getErrorCode()));
            }
            return new LogServerSync(statusList);
        } else {
            return new LogServerSync();
        }
    }

    private static NotificationServerSync deepCopy(NotificationServerSync source) {
        if (source == null) {
            return null;
        }
        NotificationServerSync copy = new NotificationServerSync();
        copy.setResponseStatus(source.getResponseStatus());
        if (source.getNotifications() != null) {
            copy.setNotifications(new ArrayList<>(source.getNotifications()));
        }
        if (source.getAvailableTopics() != null) {
            copy.setAvailableTopics(new ArrayList<>(source.getAvailableTopics()));
        }
        return copy;
    }

    private static ProfileServerSync deepCopy(ProfileServerSync source) {
        if (source == null) {
            return null;
        }
        return new ProfileServerSync(source.getResponseStatus());
    }

    private static RedirectServerSync deepCopy(RedirectServerSync source) {
        if (source == null) {
            return null;
        }
        return new RedirectServerSync(source.getAccessPointId());
    }

    private static UserServerSync deepCopy(UserServerSync source) {
        if (source == null) {
            return null;
        }
        UserServerSync copy = new UserServerSync();
        if (source.getEndpointAttachResponses() != null) {
            copy.setEndpointAttachResponses(new ArrayList<>(source.getEndpointAttachResponses()));
        }
        if (source.getEndpointDetachResponses() != null) {
            copy.setEndpointDetachResponses(new ArrayList<>(source.getEndpointDetachResponses()));
        }
        if (source.getUserAttachNotification() != null) {
            copy.setUserAttachNotification(new UserAttachNotification(source.getUserAttachNotification().getUserExternalId(), source
                    .getUserAttachNotification().getEndpointAccessToken()));
        }
        if (source.getUserAttachResponse() != null) {
            UserAttachResponse uarSource = source.getUserAttachResponse();
            copy.setUserAttachResponse(new UserAttachResponse(uarSource.getResult(), uarSource.getErrorCode(), uarSource.getErrorReason()));
        }
        if (source.getUserDetachNotification() != null) {
            copy.setUserDetachNotification(new UserDetachNotification(source.getUserDetachNotification().getEndpointAccessToken()));
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerSync that = (ServerSync) o;

        if (requestId != that.requestId) {
            return false;
        }
        if (bootstrapSync != null ? !bootstrapSync.equals(that.bootstrapSync) : that.bootstrapSync != null) {
            return false;
        }
        if (configurationSync != null ? !configurationSync.equals(that.configurationSync) : that.configurationSync != null) {
            return false;
        }
        if (eventSync != null ? !eventSync.equals(that.eventSync) : that.eventSync != null) {
            return false;
        }
        if (logSync != null ? !logSync.equals(that.logSync) : that.logSync != null) {
            return false;
        }
        if (notificationSync != null ? !notificationSync.equals(that.notificationSync) : that.notificationSync != null) {
            return false;
        }
        if (profileSync != null ? !profileSync.equals(that.profileSync) : that.profileSync != null) {
            return false;
        }
        if (redirectSync != null ? !redirectSync.equals(that.redirectSync) : that.redirectSync != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }
        if (userSync != null ? !userSync.equals(that.userSync) : that.userSync != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = requestId;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (bootstrapSync != null ? bootstrapSync.hashCode() : 0);
        result = 31 * result + (profileSync != null ? profileSync.hashCode() : 0);
        result = 31 * result + (configurationSync != null ? configurationSync.hashCode() : 0);
        result = 31 * result + (notificationSync != null ? notificationSync.hashCode() : 0);
        result = 31 * result + (userSync != null ? userSync.hashCode() : 0);
        result = 31 * result + (eventSync != null ? eventSync.hashCode() : 0);
        result = 31 * result + (redirectSync != null ? redirectSync.hashCode() : 0);
        result = 31 * result + (logSync != null ? logSync.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ServerSync [requestId=");
        builder.append(requestId);
        builder.append(", status=");
        builder.append(status);
        builder.append(", bootstrapSync=");
        builder.append(bootstrapSync);
        builder.append(", profileSync=");
        builder.append(profileSync);
        builder.append(", configurationSync=");
        builder.append(configurationSync);
        builder.append(", notificationSync=");
        builder.append(notificationSync);
        builder.append(", userSync=");
        builder.append(userSync);
        builder.append(", eventSync=");
        builder.append(eventSync);
        builder.append(", redirectSync=");
        builder.append(redirectSync);
        builder.append(", logSync=");
        builder.append(logSync);
        builder.append("]");
        return builder.toString();
    }
}
