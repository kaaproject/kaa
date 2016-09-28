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
  private List<String> eventClassFqns;

  public EventListenersRequest() {
  }

  /**
   * All-args constructor.
   */
  public EventListenersRequest(int requestId, List<String> eventClassFqns) {
    this.requestId = requestId;
    this.eventClassFqns = eventClassFqns;
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
   * @param value the value to set.
   */
  public void setRequestId(int value) {
    this.requestId = value;
  }

  /**
   * Gets the value of the 'eventClassFqns' field.
   */
  public List<String> getEventClassFqns() {
    return eventClassFqns;
  }

  /**
   * Sets the value of the 'eventClassFqns' field.
   *
   * @param value the value to set.
   */
  public void setEventClassFqns(List<String> value) {
    this.eventClassFqns = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((eventClassFqns == null) ? 0 : eventClassFqns.hashCode());
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
    if (eventClassFqns == null) {
      if (other.eventClassFqns != null) {
        return false;
      }
    } else if (!eventClassFqns.equals(other.eventClassFqns)) {
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
    builder.append(", eventClassFqns=");
    builder.append(eventClassFqns);
    builder.append("]");
    return builder.toString();
  }
}
