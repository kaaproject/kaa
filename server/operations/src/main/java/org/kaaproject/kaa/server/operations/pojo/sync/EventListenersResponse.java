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

public class EventListenersResponse  {
   private java.lang.String requestId;
   private java.util.List<java.lang.String> listeners;
   private org.kaaproject.kaa.server.operations.pojo.sync.SyncStatus result;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public EventListenersResponse() {}

  /**
   * All-args constructor.
   */
  public EventListenersResponse(java.lang.String requestId, java.util.List<java.lang.String> listeners, org.kaaproject.kaa.server.operations.pojo.sync.SyncStatus result) {
    this.requestId = requestId;
    this.listeners = listeners;
    this.result = result;
  }

  /**
   * Gets the value of the 'requestId' field.
   */
  public java.lang.String getRequestId() {
    return requestId;
  }

  /**
   * Sets the value of the 'requestId' field.
   * @param value the value to set.
   */
  public void setRequestId(java.lang.String value) {
    this.requestId = value;
  }

  /**
   * Gets the value of the 'listeners' field.
   */
  public java.util.List<java.lang.String> getListeners() {
    return listeners;
  }

  /**
   * Sets the value of the 'listeners' field.
   * @param value the value to set.
   */
  public void setListeners(java.util.List<java.lang.String> value) {
    this.listeners = value;
  }

  /**
   * Gets the value of the 'result' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.SyncStatus getResult() {
    return result;
  }

  /**
   * Sets the value of the 'result' field.
   * @param value the value to set.
   */
  public void setResult(org.kaaproject.kaa.server.operations.pojo.sync.SyncStatus value) {
    this.result = value;
  }
}
