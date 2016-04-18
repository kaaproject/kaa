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

public final class UserAttachRequest {
    private String userVerifierId;
    private String userExternalId;
    private String userAccessToken;

    /**
     * All-args constructor.
     */
    public UserAttachRequest(String userVerifierId, String userExternalId, String userAccessToken) {
        this.userVerifierId = userVerifierId;
        this.userExternalId = userExternalId;
        this.userAccessToken = userAccessToken;
    }

    /**
     * Gets the value of the 'userExternalId' field.
     */
    public String getUserVerifierId() {
        return userVerifierId;
    }

    /**
     * Gets the value of the 'userExternalId' field.
     */
    public String getUserExternalId() {
        return userExternalId;
    }

    /**
     * Gets the value of the 'userAccessToken' field.
     */
    public String getUserAccessToken() {
        return userAccessToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userAccessToken == null) ? 0 : userAccessToken.hashCode());
        result = prime * result + ((userExternalId == null) ? 0 : userExternalId.hashCode());
        result = prime * result + ((userVerifierId == null) ? 0 : userVerifierId.hashCode());
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
        UserAttachRequest other = (UserAttachRequest) obj;
        if (userAccessToken == null) {
            if (other.userAccessToken != null) {
                return false;
            }
        } else if (!userAccessToken.equals(other.userAccessToken)) {
            return false;
        }
        if (userExternalId == null) {
            if (other.userExternalId != null) {
                return false;
            }
        } else if (!userExternalId.equals(other.userExternalId)) {
            return false;
        }
        if (userVerifierId == null) {
            if (other.userVerifierId != null) {
                return false;
            }
        } else if (!userVerifierId.equals(other.userVerifierId)) {
            return false;
        }
        return true;
    }
}
