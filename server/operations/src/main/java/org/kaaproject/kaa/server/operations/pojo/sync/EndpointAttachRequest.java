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

public class EndpointAttachRequest {
    private String requestId;
    private String endpointAccessToken;

    public EndpointAttachRequest() {
    }

    /**
     * All-args constructor.
     */
    public EndpointAttachRequest(String requestId, String endpointAccessToken) {
        this.requestId = requestId;
        this.endpointAccessToken = endpointAccessToken;
    }

    /**
     * Gets the value of the 'requestId' field.
     */
    public java.lang.String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(java.lang.String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the 'endpointAccessToken' field.
     */
    public java.lang.String getEndpointAccessToken() {
        return endpointAccessToken;
    }

    /**
     * Sets the value of the 'endpointAccessToken' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointAccessToken(java.lang.String value) {
        this.endpointAccessToken = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointAccessToken == null) ? 0 : endpointAccessToken.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
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
        EndpointAttachRequest other = (EndpointAttachRequest) obj;
        if (endpointAccessToken == null) {
            if (other.endpointAccessToken != null) {
                return false;
            }
        } else if (!endpointAccessToken.equals(other.endpointAccessToken)) {
            return false;
        }
        if (requestId == null) {
            if (other.requestId != null) {
                return false;
            }
        } else if (!requestId.equals(other.requestId)) {
            return false;
        }
        return true;
    }

}
