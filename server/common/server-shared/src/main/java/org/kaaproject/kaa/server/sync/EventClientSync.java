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

public final class EventClientSync {
    private boolean seqNumberRequest;
    private List<EventListenersRequest> eventListenersRequests;
    private List<Event> events;

    public EventClientSync() {
    }

    public EventClientSync(boolean seqNumberRequest, List<EventListenersRequest> eventListenersRequests, List<Event> events) {
        super();
        this.seqNumberRequest = seqNumberRequest;
        this.eventListenersRequests = eventListenersRequests;
        this.events = events;
    }

    public boolean isSeqNumberRequest() {
        return seqNumberRequest;
    }

    public void setSeqNumberRequest(boolean seqNumberRequest) {
        this.seqNumberRequest = seqNumberRequest;
    }

    /**
     * Gets the value of the 'eventListenersRequests' field.
     */
    public List<EventListenersRequest> getEventListenersRequests() {
        return eventListenersRequests;
    }

    /**
     * Sets the value of the 'eventListenersRequests' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventListenersRequests(List<EventListenersRequest> value) {
        this.eventListenersRequests = value;
    }

    /**
     * Gets the value of the 'events' field.
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * Sets the value of the 'events' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEvents(List<Event> value) {
        this.events = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventListenersRequests == null) ? 0 : eventListenersRequests.hashCode());
        result = prime * result + ((events == null) ? 0 : events.hashCode());
        result = prime * result + (seqNumberRequest ? 1231 : 1237);
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
        EventClientSync other = (EventClientSync) obj;
        if (eventListenersRequests == null) {
            if (other.eventListenersRequests != null) {
                return false;
            }
        } else if (!eventListenersRequests.equals(other.eventListenersRequests)) {
            return false;
        }
        if (events == null) {
            if (other.events != null) {
                return false;
            }
        } else if (!events.equals(other.events)) {
            return false;
        }
        if (seqNumberRequest != other.seqNumberRequest) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventClientSync [seqNumberRequest=");
        builder.append(seqNumberRequest);
        builder.append(", eventListenersRequests=");
        builder.append(eventListenersRequests);
        builder.append(", events=");
        builder.append(events);
        builder.append("]");
        return builder.toString();
    }
    
    
}
