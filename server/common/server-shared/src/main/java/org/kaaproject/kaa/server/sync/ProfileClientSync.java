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

import java.nio.ByteBuffer;

public class ProfileClientSync {
    private ByteBuffer endpointPublicKey;
    private ByteBuffer profileBody;
    private String endpointAccessToken;

    public ProfileClientSync() {
    }

    /**
     * All-args constructor.
     */
    public ProfileClientSync(ByteBuffer endpointPublicKey, ByteBuffer profileBody, String sdkToken,
            String endpointAccessToken) {
        this.endpointPublicKey = endpointPublicKey;
        this.profileBody = profileBody;
        this.endpointAccessToken = endpointAccessToken;
    }

    /**
     * Gets the value of the 'endpointPublicKey' field.
     */
    public ByteBuffer getEndpointPublicKey() {
        return endpointPublicKey;
    }

    /**
     * Sets the value of the 'endpointPublicKey' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointPublicKey(ByteBuffer value) {
        this.endpointPublicKey = value;
    }

    /**
     * Gets the value of the 'profileBody' field.
     */
    public ByteBuffer getProfileBody() {
        return profileBody;
    }

    /**
     * Sets the value of the 'profileBody' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setProfileBody(ByteBuffer value) {
        this.profileBody = value;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointAccessToken == null) ? 0 : endpointAccessToken.hashCode());
        result = prime * result + ((endpointPublicKey == null) ? 0 : endpointPublicKey.hashCode());
        result = prime * result + ((profileBody == null) ? 0 : profileBody.hashCode());
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
        ProfileClientSync other = (ProfileClientSync) obj;
        if (endpointAccessToken == null) {
            if (other.endpointAccessToken != null) {
                return false;
            }
        } else if (!endpointAccessToken.equals(other.endpointAccessToken)) {
            return false;
        }
        if (endpointPublicKey == null) {
            if (other.endpointPublicKey != null) {
                return false;
            }
        } else if (!endpointPublicKey.equals(other.endpointPublicKey)) {
            return false;
        }
        if (profileBody == null) {
            if (other.profileBody != null) {
                return false;
            }
        } else if (!profileBody.equals(other.profileBody)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProfileClientSync [endpointPublicKey=");
        builder.append(endpointPublicKey);
        builder.append(", profileBody=");
        builder.append(profileBody);
        builder.append(", endpointAccessToken=");
        builder.append(endpointAccessToken);
        builder.append("]");
        return builder.toString();
    }
}
