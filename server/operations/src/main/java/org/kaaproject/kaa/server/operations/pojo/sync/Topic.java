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

public class Topic {
   private java.lang.String id;
   private java.lang.String name;
   private org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionType subscriptionType;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public Topic() {}

  /**
   * All-args constructor.
   */
  public Topic(java.lang.String id, java.lang.String name, org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionType subscriptionType) {
    this.id = id;
    this.name = name;
    this.subscriptionType = subscriptionType;
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.String value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'subscriptionType' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionType getSubscriptionType() {
    return subscriptionType;
  }

  /**
   * Sets the value of the 'subscriptionType' field.
   * @param value the value to set.
   */
  public void setSubscriptionType(org.kaaproject.kaa.server.operations.pojo.sync.SubscriptionType value) {
    this.subscriptionType = value;
  }

}
