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

public class EventServerSync {
   private org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberResponse eventSequenceNumberResponse;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersResponse> eventListenersResponses;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> events;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public EventServerSync() {}

  /**
   * All-args constructor.
   */
  public EventServerSync(org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberResponse eventSequenceNumberResponse, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersResponse> eventListenersResponses, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.Event> events) {
    this.eventSequenceNumberResponse = eventSequenceNumberResponse;
    this.eventListenersResponses = eventListenersResponses;
    this.events = events;
  }

  /**
   * Gets the value of the 'eventSequenceNumberResponse' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberResponse getEventSequenceNumberResponse() {
    return eventSequenceNumberResponse;
  }

  /**
   * Sets the value of the 'eventSequenceNumberResponse' field.
   * @param value the value to set.
   */
  public void setEventSequenceNumberResponse(org.kaaproject.kaa.server.operations.pojo.sync.EventSequenceNumberResponse value) {
    this.eventSequenceNumberResponse = value;
  }

  /**
   * Gets the value of the 'eventListenersResponses' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersResponse> getEventListenersResponses() {
    return eventListenersResponses;
  }

  /**
   * Sets the value of the 'eventListenersResponses' field.
   * @param value the value to set.
   */
  public void setEventListenersResponses(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EventListenersResponse> value) {
    this.eventListenersResponses = value;
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
