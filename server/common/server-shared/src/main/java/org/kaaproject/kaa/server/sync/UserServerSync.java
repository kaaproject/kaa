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

public final class UserServerSync {
    private UserAttachResponse userAttachResponse;
    private UserAttachNotification userAttachNotification;
    private UserDetachNotification userDetachNotification;
    private List<EndpointAttachResponse> endpointAttachResponses;
    private List<EndpointDetachResponse> endpointDetachResponses;

    public UserServerSync() {
    }

    /**
     * All-args constructor.
     */
    public UserServerSync(UserAttachResponse userAttachResponse, UserAttachNotification userAttachNotification,
            UserDetachNotification userDetachNotification, List<EndpointAttachResponse> endpointAttachResponses,
            List<EndpointDetachResponse> endpointDetachResponses) {
        this.userAttachResponse = userAttachResponse;
        this.userAttachNotification = userAttachNotification;
        this.userDetachNotification = userDetachNotification;
        this.endpointAttachResponses = endpointAttachResponses;
        this.endpointDetachResponses = endpointDetachResponses;
    }
    
    public void cleanupNotifications() {
        this.userAttachNotification = null;
        this.userDetachNotification = null;
    }

    /**
     * Gets the value of the 'userAttachResponse' field.
     */
    public UserAttachResponse getUserAttachResponse() {
        return userAttachResponse;
    }

    /**
     * Sets the value of the 'userAttachResponse' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserAttachResponse(UserAttachResponse value) {
        this.userAttachResponse = value;
    }

    /**
     * Gets the value of the 'userAttachNotification' field.
     */
    public UserAttachNotification getUserAttachNotification() {
        return userAttachNotification;
    }

    /**
     * Sets the value of the 'userAttachNotification' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserAttachNotification(UserAttachNotification value) {
        this.userAttachNotification = value;
    }

    /**
     * Gets the value of the 'userDetachNotification' field.
     */
    public UserDetachNotification getUserDetachNotification() {
        return userDetachNotification;
    }

    /**
     * Sets the value of the 'userDetachNotification' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserDetachNotification(UserDetachNotification value) {
        this.userDetachNotification = value;
    }

    /**
     * Gets the value of the 'endpointAttachResponses' field.
     */
    public List<EndpointAttachResponse> getEndpointAttachResponses() {
        return endpointAttachResponses;
    }

    /**
     * Sets the value of the 'endpointAttachResponses' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointAttachResponses(List<EndpointAttachResponse> value) {
        this.endpointAttachResponses = value;
    }

    /**
     * Gets the value of the 'endpointDetachResponses' field.
     */
    public List<EndpointDetachResponse> getEndpointDetachResponses() {
        return endpointDetachResponses;
    }

    /**
     * Sets the value of the 'endpointDetachResponses' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointDetachResponses(List<EndpointDetachResponse> value) {
        this.endpointDetachResponses = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserServerSync that = (UserServerSync) o;

        if (endpointAttachResponses != null ? !endpointAttachResponses.equals(that.endpointAttachResponses) : that.endpointAttachResponses != null) {
            return false;
        }
        if (endpointDetachResponses != null ? !endpointDetachResponses.equals(that.endpointDetachResponses) : that.endpointDetachResponses != null) {
            return false;
        }
        if (userAttachNotification != null ? !userAttachNotification.equals(that.userAttachNotification) : that.userAttachNotification != null) {
            return false;
        }
        if (userAttachResponse != null ? !userAttachResponse.equals(that.userAttachResponse) : that.userAttachResponse != null) {
            return false;
        }
        if (userDetachNotification != null ? !userDetachNotification.equals(that.userDetachNotification) : that.userDetachNotification != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = userAttachResponse != null ? userAttachResponse.hashCode() : 0;
        result = 31 * result + (userAttachNotification != null ? userAttachNotification.hashCode() : 0);
        result = 31 * result + (userDetachNotification != null ? userDetachNotification.hashCode() : 0);
        result = 31 * result + (endpointAttachResponses != null ? endpointAttachResponses.hashCode() : 0);
        result = 31 * result + (endpointDetachResponses != null ? endpointDetachResponses.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserServerSync [userAttachResponse=");
        builder.append(userAttachResponse);
        builder.append(", userAttachNotification=");
        builder.append(userAttachNotification);
        builder.append(", userDetachNotification=");
        builder.append(userDetachNotification);
        builder.append(", endpointAttachResponses=");
        builder.append(endpointAttachResponses);
        builder.append(", endpointDetachResponses=");
        builder.append(endpointDetachResponses);
        builder.append("]");
        return builder.toString();
    }
}
