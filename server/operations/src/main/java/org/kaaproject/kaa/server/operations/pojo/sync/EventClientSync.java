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

public class EventClientSync {
   private org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberRequest eventSequenceNumberRequest;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersRequest> eventListenersRequests;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> events;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public EventClientSync() {}

  @Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventListenersRequests == null) ? 0 : eventListenersRequests.hashCode());
    result = prime * result + ((events == null) ? 0 : events.hashCode());
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
    if (eventSequenceNumberRequest == null) {
        if (other.eventSequenceNumberRequest != null) {
            return false;
        }
    } else if (!eventSequenceNumberRequest.equals(other.eventSequenceNumberRequest)) {
        return false;
    }
    if (events == null) {
        if (other.events != null) {
            return false;
        }
    } else if (!events.equals(other.events)) {
        return false;
    }
    return true;
}

/**
   * All-args constructor.
   */
  public EventClientSync(org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberRequest eventSequenceNumberRequest, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersRequest> eventListenersRequests, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> events) {
    this.eventSequenceNumberRequest = eventSequenceNumberRequest;
    this.eventListenersRequests = eventListenersRequests;
    this.events = events;
  }

  /**
   * Gets the value of the 'eventSequenceNumberRequest' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberRequest getEventSequenceNumberRequest() {
    return eventSequenceNumberRequest;
  }

  /**
   * Sets the value of the 'eventSequenceNumberRequest' field.
   * @param value the value to set.
   */
  public void setEventSequenceNumberRequest(org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberRequest value) {
    this.eventSequenceNumberRequest = value;
  }

  /**
   * Gets the value of the 'eventListenersRequests' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersRequest> getEventListenersRequests() {
    return eventListenersRequests;
  }

  /**
   * Sets the value of the 'eventListenersRequests' field.
   * @param value the value to set.
   */
  public void setEventListenersRequests(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersRequest> value) {
    this.eventListenersRequests = value;
  }

  /**
   * Gets the value of the 'events' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> getEvents() {
    return events;
  }

  /**
   * Sets the value of the 'events' field.
   * @param value the value to set.
   */
  public void setEvents(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> value) {
    this.events = value;
  }
}
