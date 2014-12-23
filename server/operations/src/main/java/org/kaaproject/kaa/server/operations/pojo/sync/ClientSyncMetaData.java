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

import java.nio.ByteBuffer;

public class ClientSyncMetaData {
    private String applicationToken;
    private ByteBuffer endpointPublicKeyHash;
    private ByteBuffer profileHash;
    private long timeout;

    public ClientSyncMetaData() {
    }

    /**
     * All-args constructor.
     */
    public ClientSyncMetaData(String applicationToken, ByteBuffer endpointPublicKeyHash, ByteBuffer profileHash, Long timeout) {
        this.applicationToken = applicationToken;
        this.endpointPublicKeyHash = endpointPublicKeyHash;
        this.profileHash = profileHash;
        this.timeout = timeout;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationToken == null) ? 0 : applicationToken.hashCode());
        result = prime * result + ((endpointPublicKeyHash == null) ? 0 : endpointPublicKeyHash.hashCode());
        result = prime * result + ((profileHash == null) ? 0 : profileHash.hashCode());
        result = prime * result + (int) (timeout ^ (timeout >>> 32));
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
        ClientSyncMetaData other = (ClientSyncMetaData) obj;
        if (applicationToken == null) {
            if (other.applicationToken != null) {
                return false;
            }
        } else if (!applicationToken.equals(other.applicationToken)) {
            return false;
        }
        if (endpointPublicKeyHash == null) {
            if (other.endpointPublicKeyHash != null) {
                return false;
            }
        } else if (!endpointPublicKeyHash.equals(other.endpointPublicKeyHash)) {
            return false;
        }
        if (profileHash == null) {
            if (other.profileHash != null) {
                return false;
            }
        } else if (!profileHash.equals(other.profileHash)) {
            return false;
        }
        if (timeout != other.timeout) {
            return false;
        }
        return true;
    }
}
