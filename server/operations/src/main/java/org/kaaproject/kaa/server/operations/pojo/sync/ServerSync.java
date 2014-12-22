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
package org.kaaproject.kaa.server.operations.pojo.sync;

public class ServerSync {

    private Integer requestId;
    private SyncStatus status;
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
    public ServerSync(Integer requestId, SyncStatus status, ProfileServerSync profileSync,
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
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(Integer value) {
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
}
