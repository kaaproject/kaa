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

public final class EventListenersRequest {
    private int requestId;
    private List<String> eventClassFQNs;

    public EventListenersRequest() {
    }

    /**
     * All-args constructor.
     */
    public EventListenersRequest(int requestId, List<String> eventClassFQNs) {
        this.requestId = requestId;
        this.eventClassFQNs = eventClassFQNs;
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
     * Gets the value of the 'eventClassFQNs' field.
     */
    public List<String> getEventClassFQNs() {
        return eventClassFQNs;
    }

    /**
     * Sets the value of the 'eventClassFQNs' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventClassFQNs(List<String> value) {
        this.eventClassFQNs = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventClassFQNs == null) ? 0 : eventClassFQNs.hashCode());
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
        EventListenersRequest other = (EventListenersRequest) obj;
        if (eventClassFQNs == null) {
            if (other.eventClassFQNs != null) {
                return false;
            }
        } else if (!eventClassFQNs.equals(other.eventClassFQNs)) {
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
        builder.append("EventListenersRequest [requestId=");
        builder.append(requestId);
        builder.append(", eventClassFQNs=");
        builder.append(eventClassFQNs);
        builder.append("]");
        return builder.toString();
    }
}
