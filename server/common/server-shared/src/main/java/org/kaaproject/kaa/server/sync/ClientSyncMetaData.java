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

import java.nio.ByteBuffer;

public class ClientSyncMetaData {
    private String applicationToken;
    private String sdkToken;
    private ByteBuffer endpointPublicKeyHash;
    private ByteBuffer profileHash;
    private long timeout;

    public ClientSyncMetaData() {
    }

    /**
     * All-args constructor.
     */
    public ClientSyncMetaData(String applicationToken, String sdkToken, ByteBuffer endpointPublicKeyHash, ByteBuffer profileHash, Long timeout) {
        this.applicationToken = applicationToken;
        this.sdkToken = sdkToken;
        this.endpointPublicKeyHash = endpointPublicKeyHash;
        this.profileHash = profileHash;
        this.timeout = timeout == null ? 0 : timeout.longValue();
    }

    /**
     * Gets the value of the 'applicationToken' field.
     */
    public String getApplicationToken() {
        return applicationToken;
    }

    /**
     * Sets the value of the 'applicationToken' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setApplicationToken(String value) {
        this.applicationToken = value;
    }

    /**
     * Gets the value of the 'endpointPublicKeyHash' field.
     */
    public ByteBuffer getEndpointPublicKeyHash() {
        return endpointPublicKeyHash;
    }

    /**
     * Sets the value of the 'endpointPublicKeyHash' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointPublicKeyHash(ByteBuffer value) {
        this.endpointPublicKeyHash = value;
    }

    /**
     * Gets the value of the 'profileHash' field.
     */
    public ByteBuffer getProfileHash() {
        return profileHash;
    }

    /**
     * Sets the value of the 'profileHash' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setProfileHash(ByteBuffer value) {
        this.profileHash = value;
    }

    /**
     * Gets the value of the 'timeout' field.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the value of the 'timeout' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setTimeout(Long value) {
        this.timeout = value;
    }

    public String getSdkToken() {
        return sdkToken;
    }

    public void setSdkToken(String sdkToken) {
        this.sdkToken = sdkToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientSyncMetaData that = (ClientSyncMetaData) o;

        if (timeout != that.timeout) {
            return false;
        }
        if (applicationToken != null ? !applicationToken.equals(that.applicationToken) : that.applicationToken != null) {
            return false;
        }
        if (endpointPublicKeyHash != null ? !endpointPublicKeyHash.equals(that.endpointPublicKeyHash) : that.endpointPublicKeyHash != null) {
            return false;
        }
        if (profileHash != null ? !profileHash.equals(that.profileHash) : that.profileHash != null) {
            return false;
        }
        if (sdkToken != null ? !sdkToken.equals(that.sdkToken) : that.sdkToken != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationToken != null ? applicationToken.hashCode() : 0;
        result = 31 * result + (sdkToken != null ? sdkToken.hashCode() : 0);
        result = 31 * result + (endpointPublicKeyHash != null ? endpointPublicKeyHash.hashCode() : 0);
        result = 31 * result + (profileHash != null ? profileHash.hashCode() : 0);
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientSyncMetaData [applicationToken=");
        builder.append(applicationToken);
        builder.append(", sdkToken=");
        builder.append(sdkToken);
        builder.append(", endpointPublicKeyHash=");
        builder.append(endpointPublicKeyHash);
        builder.append(", profileHash=");
        builder.append(profileHash);
        builder.append(", timeout=");
        builder.append(timeout);
        builder.append("]");
        return builder.toString();
    }
}
