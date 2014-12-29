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

public class EndpointAttachResponse {
    private String requestId;
    private String endpointKeyHash;
    private SyncStatus result;

    public EndpointAttachResponse() {
    }

    /**
     * All-args constructor.
     */
    public EndpointAttachResponse(String requestId, String endpointKeyHash, SyncStatus result) {
        this.requestId = requestId;
        this.endpointKeyHash = endpointKeyHash;
        this.result = result;
    }

    /**
     * Gets the value of the 'requestId' field.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the 'requestId' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the 'endpointKeyHash' field.
     */
    public java.lang.String getEndpointKeyHash() {
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

    /**
     * Gets the value of the 'result' field.
     */
    public SyncStatus getResult() {
        return result;
    }

    /**
     * Sets the value of the 'result' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setResult(SyncStatus value) {
        this.result = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointAttachResponse [requestId=");
        builder.append(requestId);
        builder.append(", endpointKeyHash=");
        builder.append(endpointKeyHash);
        builder.append(", result=");
        builder.append(result);
        builder.append("]");
        return builder.toString();
    }
}
