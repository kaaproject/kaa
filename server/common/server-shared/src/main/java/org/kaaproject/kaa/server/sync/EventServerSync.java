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

public final class EventServerSync {
    private EventSequenceNumberResponse eventSequenceNumberResponse;
    private List<EventListenersResponse> eventListenersResponses;
    private List<Event> events;

    public EventServerSync() {
    }

    /**
     * All-args constructor.
     */
    public EventServerSync(EventSequenceNumberResponse eventSequenceNumberResponse,
            List<EventListenersResponse> eventListenersResponses,
            List<Event> events) {
        this.eventSequenceNumberResponse = eventSequenceNumberResponse;
        this.eventListenersResponses = eventListenersResponses;
        this.events = events;
    }

    /**
     * Gets the value of the 'eventSequenceNumberResponse' field.
     */
    public EventSequenceNumberResponse getEventSequenceNumberResponse() {
        return eventSequenceNumberResponse;
    }

    /**
     * Sets the value of the 'eventSequenceNumberResponse' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventSequenceNumberResponse(EventSequenceNumberResponse value) {
        this.eventSequenceNumberResponse = value;
    }

    /**
     * Gets the value of the 'eventListenersResponses' field.
     */
    public List<EventListenersResponse> getEventListenersResponses() {
        return eventListenersResponses;
    }

    /**
     * Sets the value of the 'eventListenersResponses' field.
     * 
     * @param value
     *            the value to set.
     */
    public void setEventListenersResponses(List<EventListenersResponse> value) {
        this.eventListenersResponses = value;
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventServerSync that = (EventServerSync) o;

        if (eventListenersResponses != null ? !eventListenersResponses.equals(that.eventListenersResponses) : that.eventListenersResponses != null) {
            return false;
        }
        if (eventSequenceNumberResponse != null ? !eventSequenceNumberResponse.equals(that.eventSequenceNumberResponse) : that.eventSequenceNumberResponse != null) {
            return false;
        }
        if (events != null ? !events.equals(that.events) : that.events != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventSequenceNumberResponse != null ? eventSequenceNumberResponse.hashCode() : 0;
        result = 31 * result + (eventListenersResponses != null ? eventListenersResponses.hashCode() : 0);
        result = 31 * result + (events != null ? events.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EventServerSync [eventSequenceNumberResponse=");
        builder.append(eventSequenceNumberResponse);
        builder.append(", eventListenersResponses=");
        builder.append(eventListenersResponses);
        builder.append(", events=");
        builder.append(events);
        builder.append("]");
        return builder.toString();
    }
}
