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

public final class UserDetachNotification {
    private String endpointAccessToken;

    public UserDetachNotification() {
    }

    /**
     * All-args constructor.
     */
    public UserDetachNotification(String endpointAccessToken) {
        this.endpointAccessToken = endpointAccessToken;
    }

    /**
     * Gets the value of the 'endpointAccessToken' field.
     */
    public String getEndpointAccessToken() {
        return endpointAccessToken;
    }

    /**
     * Sets the value of the 'endpointAccessToken' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointAccessToken(String value) {
        this.endpointAccessToken = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDetachNotification that = (UserDetachNotification) o;

        if (endpointAccessToken != null ? !endpointAccessToken.equals(that.endpointAccessToken) : that.endpointAccessToken != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return endpointAccessToken != null ? endpointAccessToken.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserDetachNotification [endpointAccessToken=");
        builder.append(endpointAccessToken);
        builder.append("]");
        return builder.toString();
    }
}
