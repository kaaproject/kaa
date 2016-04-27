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

public final class UserClientSync {
    private UserAttachRequest userAttachRequest;
    private List<EndpointAttachRequest> endpointAttachRequests;
    private List<EndpointDetachRequest> endpointDetachRequests;

    public UserClientSync() {
    }

    /**
     * All-args constructor.
     */
    public UserClientSync(UserAttachRequest userAttachRequest, List<EndpointAttachRequest> endpointAttachRequests,
            List<EndpointDetachRequest> endpointDetachRequests) {
        this.userAttachRequest = userAttachRequest;
        this.endpointAttachRequests = endpointAttachRequests;
        this.endpointDetachRequests = endpointDetachRequests;
    }

    /**
     * Gets the value of the 'userAttachRequest' field.
     */
    public UserAttachRequest getUserAttachRequest() {
        return userAttachRequest;
    }

    /**
     * Sets the value of the 'userAttachRequest' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setUserAttachRequest(UserAttachRequest value) {
        this.userAttachRequest = value;
    }

    /**
     * Gets the value of the 'endpointAttachRequests' field.
     */
    public List<EndpointAttachRequest> getEndpointAttachRequests() {
        return endpointAttachRequests;
    }

    /**
     * Sets the value of the 'endpointAttachRequests' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointAttachRequests(List<EndpointAttachRequest> value) {
        this.endpointAttachRequests = value;
    }

    /**
     * Gets the value of the 'endpointDetachRequests' field.
     */
    public List<EndpointDetachRequest> getEndpointDetachRequests() {
        return endpointDetachRequests;
    }

    /**
     * Sets the value of the 'endpointDetachRequests' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointDetachRequests(List<EndpointDetachRequest> value) {
        this.endpointDetachRequests = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointAttachRequests == null) ? 0 : endpointAttachRequests.hashCode());
        result = prime * result + ((endpointDetachRequests == null) ? 0 : endpointDetachRequests.hashCode());
        result = prime * result + ((userAttachRequest == null) ? 0 : userAttachRequest.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserClientSync other = (UserClientSync) obj;
        if (endpointAttachRequests == null) {
            if (other.endpointAttachRequests != null) {
                return false;
            }
        } else if (!endpointAttachRequests.equals(other.endpointAttachRequests)) {
            return false;
        }
        if (endpointDetachRequests == null) {
            if (other.endpointDetachRequests != null) {
                return false;
            }
        } else if (!endpointDetachRequests.equals(other.endpointDetachRequests)) {
            return false;
        }
        if (userAttachRequest == null) {
            if (other.userAttachRequest != null) {
                return false;
            }
        } else if (!userAttachRequest.equals(other.userAttachRequest)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserClientSync [userAttachRequest=");
        builder.append(userAttachRequest);
        builder.append(", endpointAttachRequests=");
        builder.append(endpointAttachRequests);
        builder.append(", endpointDetachRequests=");
        builder.append(endpointDetachRequests);
        builder.append("]");
        return builder.toString();
    }
}
