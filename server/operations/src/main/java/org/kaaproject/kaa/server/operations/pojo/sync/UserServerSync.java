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

public class UserServerSync {
   private org.kaaproject.kaa.server.operations.pojo.sync.UserAttachResponse userAttachResponse;
   private org.kaaproject.kaa.server.operations.pojo.sync.UserAttachNotification userAttachNotification;
   private org.kaaproject.kaa.server.operations.pojo.sync.UserDetachNotification userDetachNotification;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachResponse> endpointAttachResponses;
   private java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachResponse> endpointDetachResponses;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public UserServerSync() {}

  /**
   * All-args constructor.
   */
  public UserServerSync(org.kaaproject.kaa.server.operations.pojo.sync.UserAttachResponse userAttachResponse, org.kaaproject.kaa.server.operations.pojo.sync.UserAttachNotification userAttachNotification, org.kaaproject.kaa.server.operations.pojo.sync.UserDetachNotification userDetachNotification, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachResponse> endpointAttachResponses, java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachResponse> endpointDetachResponses) {
    this.userAttachResponse = userAttachResponse;
    this.userAttachNotification = userAttachNotification;
    this.userDetachNotification = userDetachNotification;
    this.endpointAttachResponses = endpointAttachResponses;
    this.endpointDetachResponses = endpointDetachResponses;
  }

  /**
   * Gets the value of the 'userAttachResponse' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.UserAttachResponse getUserAttachResponse() {
    return userAttachResponse;
  }

  /**
   * Sets the value of the 'userAttachResponse' field.
   * @param value the value to set.
   */
  public void setUserAttachResponse(org.kaaproject.kaa.server.operations.pojo.sync.UserAttachResponse value) {
    this.userAttachResponse = value;
  }

  /**
   * Gets the value of the 'userAttachNotification' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.UserAttachNotification getUserAttachNotification() {
    return userAttachNotification;
  }

  /**
   * Sets the value of the 'userAttachNotification' field.
   * @param value the value to set.
   */
  public void setUserAttachNotification(org.kaaproject.kaa.server.operations.pojo.sync.UserAttachNotification value) {
    this.userAttachNotification = value;
  }

  /**
   * Gets the value of the 'userDetachNotification' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.UserDetachNotification getUserDetachNotification() {
    return userDetachNotification;
  }

  /**
   * Sets the value of the 'userDetachNotification' field.
   * @param value the value to set.
   */
  public void setUserDetachNotification(org.kaaproject.kaa.server.operations.pojo.sync.UserDetachNotification value) {
    this.userDetachNotification = value;
  }

  /**
   * Gets the value of the 'endpointAttachResponses' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachResponse> getEndpointAttachResponses() {
    return endpointAttachResponses;
  }

  /**
   * Sets the value of the 'endpointAttachResponses' field.
   * @param value the value to set.
   */
  public void setEndpointAttachResponses(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointAttachResponse> value) {
    this.endpointAttachResponses = value;
  }

  /**
   * Gets the value of the 'endpointDetachResponses' field.
   */
  public java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachResponse> getEndpointDetachResponses() {
    return endpointDetachResponses;
  }

  /**
   * Sets the value of the 'endpointDetachResponses' field.
   * @param value the value to set.
   */
  public void setEndpointDetachResponses(java.util.List<org.kaaproject.kaa.server.operations.pojo.sync.EndpointDetachResponse> value) {
    this.endpointDetachResponses = value;
  }
}
