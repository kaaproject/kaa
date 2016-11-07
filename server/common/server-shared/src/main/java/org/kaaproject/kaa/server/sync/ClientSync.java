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

import org.kaaproject.kaa.server.sync.bootstrap.BootstrapClientSync;

/**
 * The Class ClientSync represents sync information sent from client to server.
 */
public final class ClientSync {

    private int requestId;

    private ClientSyncMetaData clientSyncMetaData;

    private BootstrapClientSync bootstrapSync;

    private ProfileClientSync profileSync;

    private boolean forceConfigurationSync;

    private ConfigurationClientSync configurationSync;

    private boolean forceNotificationSync;

    private NotificationClientSync notificationSync;

    private UserClientSync userSync;

    private EventClientSync eventSync;

    private LogClientSync logSync;

    // Kaa platform before 0.10.0 version use base scheme for configuration
    // but since 0.10.0 one starts to use raw configuration schema
    private boolean useConfigurationRawSchema;

    public ClientSync() {

    }


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


    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int value) {
        this.requestId = value;
    }


    public ClientSyncMetaData getClientSyncMetaData() {
        return clientSyncMetaData;
    }

    public void setClientSyncMetaData(ClientSyncMetaData value) {
        this.clientSyncMetaData = value;
    }


    public ProfileClientSync getProfileSync() {
        return profileSync;
    }

    public void setProfileSync(ProfileClientSync value) {
        this.profileSync = value;
    }


    public ConfigurationClientSync getConfigurationSync() {
        return configurationSync;
    }

    public void setConfigurationSync(ConfigurationClientSync value) {
        this.configurationSync = value;
    }


    public NotificationClientSync getNotificationSync() {
        return notificationSync;
    }

    public void setNotificationSync(NotificationClientSync value) {
        this.notificationSync = value;
    }


    public UserClientSync getUserSync() {
        return userSync;
    }

    public void setUserSync(UserClientSync value) {
        this.userSync = value;
    }


    public EventClientSync getEventSync() {
        return eventSync;
    }

    public void setEventSync(EventClientSync value) {
        this.eventSync = value;
    }


    public LogClientSync getLogSync() {
        return logSync;
    }

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


    public boolean isUseConfigurationRawSchema() {
        return useConfigurationRawSchema;
    }

    public void setUseConfigurationRawSchema(boolean useConfigurationRawSchema) {
        this.useConfigurationRawSchema = useConfigurationRawSchema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientSync that = (ClientSync) o;

        if (requestId != that.requestId) return false;
        if (forceConfigurationSync != that.forceConfigurationSync) return false;
        if (forceNotificationSync != that.forceNotificationSync) return false;
        if (useConfigurationRawSchema != that.useConfigurationRawSchema) return false;
        if (clientSyncMetaData != null ? !clientSyncMetaData.equals(that.clientSyncMetaData) : that.clientSyncMetaData != null)
            return false;
        if (bootstrapSync != null ? !bootstrapSync.equals(that.bootstrapSync) : that.bootstrapSync != null)
            return false;
        if (profileSync != null ? !profileSync.equals(that.profileSync) : that.profileSync != null) return false;
        if (configurationSync != null ? !configurationSync.equals(that.configurationSync) : that.configurationSync != null)
            return false;
        if (notificationSync != null ? !notificationSync.equals(that.notificationSync) : that.notificationSync != null)
            return false;
        if (userSync != null ? !userSync.equals(that.userSync) : that.userSync != null) return false;
        if (eventSync != null ? !eventSync.equals(that.eventSync) : that.eventSync != null) return false;
        return logSync != null ? logSync.equals(that.logSync) : that.logSync == null;

    }

    @Override
    public int hashCode() {
        int result = requestId;
        result = 31 * result + (clientSyncMetaData != null ? clientSyncMetaData.hashCode() : 0);
        result = 31 * result + (bootstrapSync != null ? bootstrapSync.hashCode() : 0);
        result = 31 * result + (profileSync != null ? profileSync.hashCode() : 0);
        result = 31 * result + (forceConfigurationSync ? 1 : 0);
        result = 31 * result + (configurationSync != null ? configurationSync.hashCode() : 0);
        result = 31 * result + (forceNotificationSync ? 1 : 0);
        result = 31 * result + (notificationSync != null ? notificationSync.hashCode() : 0);
        result = 31 * result + (userSync != null ? userSync.hashCode() : 0);
        result = 31 * result + (eventSync != null ? eventSync.hashCode() : 0);
        result = 31 * result + (logSync != null ? logSync.hashCode() : 0);
        result = 31 * result + (useConfigurationRawSchema ? 1 : 0);
        return result;
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
        builder.append(", useRawSchema=");
        builder.append(useConfigurationRawSchema);
        builder.append("]");
        return builder.toString();
    }

}
