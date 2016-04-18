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

public final class EndpointDetachRequest {
    private int requestId;
    private String endpointKeyHash;

    public EndpointDetachRequest() {
    }

    /**
     * All-args constructor.
     */
    public EndpointDetachRequest(int requestId, String endpointKeyHash) {
        this.requestId = requestId;
        this.endpointKeyHash = endpointKeyHash;
    }

    /**
     * Gets the value of the 'requestId' field.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(int value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the 'endpointKeyHash' field.
     */
    public String getEndpointKeyHash() {
        return endpointKeyHash;
    }

    /**
     * Sets the value of the 'endpointKeyHash' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEndpointKeyHash(String value) {
        this.endpointKeyHash = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpointKeyHash == null) ? 0 : endpointKeyHash.hashCode());
        result = prime * result + requestId;
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
        EndpointDetachRequest other = (EndpointDetachRequest) obj;
        if (endpointKeyHash == null) {
            if (other.endpointKeyHash != null) {
                return false;
            }
        } else if (!endpointKeyHash.equals(other.endpointKeyHash)) {
            return false;
        }
        if (requestId != other.requestId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointDetachRequest [requestId=");
        builder.append(requestId);
        builder.append(", endpointKeyHash=");
        builder.append(endpointKeyHash);
        builder.append("]");
        return builder.toString();
    }
}
