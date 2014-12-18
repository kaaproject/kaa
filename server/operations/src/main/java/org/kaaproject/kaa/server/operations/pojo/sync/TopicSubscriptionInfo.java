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

public class TopicSubscriptionInfo {
   private org.kaaproject.kaa.server.operations.pojo.sync.Topic topicInfo;
   private int seqNumber;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public TopicSubscriptionInfo() {}

  /**
   * All-args constructor.
   */
  public TopicSubscriptionInfo(org.kaaproject.kaa.server.operations.pojo.sync.Topic topicInfo, java.lang.Integer seqNumber) {
    this.topicInfo = topicInfo;
    this.seqNumber = seqNumber;
  }

  /**
   * Gets the value of the 'topicInfo' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.Topic getTopicInfo() {
    return topicInfo;
  }

  /**
   * Sets the value of the 'topicInfo' field.
   * @param value the value to set.
   */
  public void setTopicInfo(org.kaaproject.kaa.server.operations.pojo.sync.Topic value) {
    this.topicInfo = value;
  }

  /**
   * Gets the value of the 'seqNumber' field.
   */
  public java.lang.Integer getSeqNumber() {
    return seqNumber;
  }

  /**
   * Sets the value of the 'seqNumber' field.
   * @param value the value to set.
   */
  public void setSeqNumber(java.lang.Integer value) {
    this.seqNumber = value;
  }
}
