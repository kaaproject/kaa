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

import java.util.List;

public class EventListenersResponse {
    private int requestId;
    private List<String> listeners;
    private SyncStatus result;

    public EventListenersResponse() {
    }

    /**
     * All-args constructor.
     */
    public EventListenersResponse(int requestId, List<String> listeners,
            SyncStatus result) {
        this.requestId = requestId;
        this.listeners = listeners;
        this.result = result;
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
     * Gets the value of the 'listeners' field.
     */
    public List<String> getListeners() {
        return listeners;
    }

    /**
     * Sets the value of the 'listeners' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setListeners(List<String> value) {
        this.listeners = value;
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
}
