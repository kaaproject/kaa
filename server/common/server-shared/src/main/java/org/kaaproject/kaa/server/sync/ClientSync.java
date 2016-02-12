/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.sync;

import org.kaaproject.kaa.server.sync.bootstrap.BootstrapClientSync;

/**
 * The Class ClientSync represents sync information sent from client to server.
 */
public final class ClientSync {

    /** The request id. */
    private int requestId;

    /** The client sync meta data. */
    private ClientSyncMetaData clientSyncMetaData;

    /** The client sync meta data. */
    private BootstrapClientSync bootstrapSync;

    /** The profile sync. */
    private ProfileClientSync profileSync;

    /** The force configuration sync flag */
    private boolean forceConfigurationSync;

    /** The configuration sync. */
    private ConfigurationClientSync configurationSync;

    /** The force notification sync flag */
    private boolean forceNotificationSync;

    /** The notification sync. */
    private NotificationClientSync notificationSync;

    /** The user sync. */
    private UserClientSync userSync;

    /** The event sync. */
    private EventClientSync eventSync;

    /** The log sync. */
    private LogClientSync logSync;

    public ClientSync() {
    }

    /**
     * All-args constructor.
     *
     * @param requestId
     *            the request id
     * @param syncRequestMetaData
     *            the sync request meta data
     * @param profileSync
     *            the profile sync request
     * @param configurationSyncRequest
     *            the configuration sync request
     * @param notificationSyncRequest
     *            the notification sync request
     * @param userSyncRequest
     *            the user sync request
     * @param eventSyncRequest
     *            the event sync request
     * @param logSyncRequest
     *            the log sync request
     */
    public ClientSync(int requestId, ClientSyncMetaData clientSyncMetaData, ProfileClientSync profileSync,
            ConfigurationClientSync configurationSync, NotificationClientSync notificationSync, UserClientSync userSync,
            EventClientSync eventSync, LogClientSync logSync) {
        this.requestId = requestId;
        this.clientSyncMetaData = clientSyncMetaData;
        this.profileSync = profileSync;
        this.configurationSync = configurationSync;
        this.notificationSync = notificationSync;
        this.userSync = userSync;
        this.eventSync = eventSync;
        this.logSync = logSync;
    }

    /**
     * Gets the value of the 'requestId' field.
     *
     * @return the request id
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
     * Gets the value of the 'clientSyncMetaData' field.
     *
     * @return the client sync meta data
     */
    public ClientSyncMetaData getClientSyncMetaData() {
        return clientSyncMetaData;
    }

    /**
     * Sets the value of the 'clientSyncMetaData' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setClientSyncMetaData(ClientSyncMetaData value) {
        this.clientSyncMetaData = value;
    }

    /**
     * Gets the value of the 'profileSync' field.
     *
     * @return the profile sync
     */
    public ProfileClientSync getProfileSync() {
        return profileSync;
    }

    /**
     * Sets the value of the 'profileSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setProfileSync(ProfileClientSync value) {
        this.profileSync = value;
    }

    /**
     * Gets the value of the 'configurationSync' field.
     *
     * @return the configuration sync
     */
    public ConfigurationClientSync getConfigurationSync() {
        return configurationSync;
    }

    /**
     * Sets the value of the 'configurationSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setConfigurationSync(ConfigurationClientSync value) {
        this.configurationSync = value;
    }

    /**
     * Gets the value of the 'notificationSync' field.
     *
     * @return the notification sync
     */
    public NotificationClientSync getNotificationSync() {
        return notificationSync;
    }

    /**
     * Sets the value of the 'notificationSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setNotificationSync(NotificationClientSync value) {
        this.notificationSync = value;
    }

    /**
     * Gets the value of the 'userSync' field.
     *
     * @return the user sync
     */
    public UserClientSync getUserSync() {
        return userSync;
    }

    /**
     * Sets the value of the 'userSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserSync(UserClientSync value) {
        this.userSync = value;
    }

    /**
     * Gets the value of the 'eventSync' field.
     *
     * @return the event sync
     */
    public EventClientSync getEventSync() {
        return eventSync;
    }

    /**
     * Sets the value of the 'eventSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventSync(EventClientSync value) {
        this.eventSync = value;
    }

    /**
     * Gets the value of the 'logSync' field.
     *
     * @return the log sync
     */
    public LogClientSync getLogSync() {
        return logSync;
    }

    /**
     * Sets the value of the 'logSync' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setLogSync(LogClientSync value) {
        this.logSync = value;
    }

    public BootstrapClientSync getBootstrapSync() {
        return bootstrapSync;
    }

    public void setBootstrapSync(BootstrapClientSync bootstrapSync) {
        this.bootstrapSync = bootstrapSync;
    }

    public boolean isValid() {
        ClientSyncMetaData md = this.getClientSyncMetaData();
        // TODO: validate if public key hash matches hash of public key during
        // profile registration command.
        if (md.getProfileHash() == null) {
            ProfileClientSync profileRequest = this.getProfileSync();
            if (profileRequest == null || profileRequest.getEndpointPublicKey() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isForceConfigurationSync() {
        return forceConfigurationSync;
    }

    public void setForceConfigurationSync(boolean forceConfigurationSync) {
        this.forceConfigurationSync = forceConfigurationSync;
    }

    public boolean isForceNotificationSync() {
        return forceNotificationSync;
    }

    public void setForceNotificationSync(boolean forceNotificationSync) {
        this.forceNotificationSync = forceNotificationSync;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bootstrapSync == null) ? 0 : bootstrapSync.hashCode());
        result = prime * result + ((clientSyncMetaData == null) ? 0 : clientSyncMetaData.hashCode());
        result = prime * result + ((configurationSync == null) ? 0 : configurationSync.hashCode());
        result = prime * result + ((eventSync == null) ? 0 : eventSync.hashCode());
        result = prime * result + (forceConfigurationSync ? 1231 : 1237);
        result = prime * result + (forceNotificationSync ? 1231 : 1237);
        result = prime * result + ((logSync == null) ? 0 : logSync.hashCode());
        result = prime * result + ((notificationSync == null) ? 0 : notificationSync.hashCode());
        result = prime * result + ((profileSync == null) ? 0 : profileSync.hashCode());
        result = prime * result + requestId;
        result = prime * result + ((userSync == null) ? 0 : userSync.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientSync other = (ClientSync) obj;
        if (bootstrapSync == null) {
            if (other.bootstrapSync != null)
                return false;
        } else if (!bootstrapSync.equals(other.bootstrapSync))
            return false;
        if (clientSyncMetaData == null) {
            if (other.clientSyncMetaData != null)
                return false;
        } else if (!clientSyncMetaData.equals(other.clientSyncMetaData))
            return false;
        if (configurationSync == null) {
            if (other.configurationSync != null)
                return false;
        } else if (!configurationSync.equals(other.configurationSync))
            return false;
        if (eventSync == null) {
            if (other.eventSync != null)
                return false;
        } else if (!eventSync.equals(other.eventSync))
            return false;
        if (forceConfigurationSync != other.forceConfigurationSync)
            return false;
        if (forceNotificationSync != other.forceNotificationSync)
            return false;
        if (logSync == null) {
            if (other.logSync != null)
                return false;
        } else if (!logSync.equals(other.logSync))
            return false;
        if (notificationSync == null) {
            if (other.notificationSync != null)
                return false;
        } else if (!notificationSync.equals(other.notificationSync))
            return false;
        if (profileSync == null) {
            if (other.profileSync != null)
                return false;
        } else if (!profileSync.equals(other.profileSync))
            return false;
        if (requestId != other.requestId)
            return false;
        if (userSync == null) {
            if (other.userSync != null)
                return false;
        } else if (!userSync.equals(other.userSync))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientSync [requestId=");
        builder.append(requestId);
        builder.append(", clientSyncMetaData=");
        builder.append(clientSyncMetaData);
        builder.append(", bootstrapSync=");
        builder.append(bootstrapSync);
        builder.append(", profileSync=");
        builder.append(profileSync);
        builder.append(", forceConfigurationSync=");
        builder.append(forceConfigurationSync);
        builder.append(", configurationSync=");
        builder.append(configurationSync);
        builder.append(", forceNotificationSync=");
        builder.append(forceNotificationSync);
        builder.append(", notificationSync=");
        builder.append(notificationSync);
        builder.append(", userSync=");
        builder.append(userSync);
        builder.append(", eventSync=");
        builder.append(eventSync);
        builder.append(", logSync=");
        builder.append(logSync);
        builder.append("]");
        return builder.toString();
    }
}
