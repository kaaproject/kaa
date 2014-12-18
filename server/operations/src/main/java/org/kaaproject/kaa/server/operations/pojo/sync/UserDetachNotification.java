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

public class UserDetachNotification {
   private java.lang.String endpointAccessToken;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public UserDetachNotification() {}

  /**
   * All-args constructor.
   */
  public UserDetachNotification(java.lang.String endpointAccessToken) {
    this.endpointAccessToken = endpointAccessToken;
  }

  /**
   * Gets the value of the 'endpointAccessToken' field.
   */
  public java.lang.String getEndpointAccessToken() {
    return endpointAccessToken;
  }

  /**
   * Sets the value of the 'endpointAccessToken' field.
   * @param value the value to set.
   */
  public void setEndpointAccessToken(java.lang.String value) {
    this.endpointAccessToken = value;
  }
}
