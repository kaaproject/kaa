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

public class Notification {
   private java.lang.String topicId;
   private org.kaaproject.kaa.server.operations.pojo.sync.NotificationType type;
   private java.lang.String uid;
   private java.lang.Integer seqNumber;
   private java.nio.ByteBuffer body;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use {@link \#newBuilder()}. 
   */
  public Notification() {}

  /**
   * All-args constructor.
   */
  public Notification(java.lang.String topicId, org.kaaproject.kaa.server.operations.pojo.sync.NotificationType type, java.lang.String uid, java.lang.Integer seqNumber, java.nio.ByteBuffer body) {
    this.topicId = topicId;
    this.type = type;
    this.uid = uid;
    this.seqNumber = seqNumber;
    this.body = body;
  }

  /**
   * Gets the value of the 'topicId' field.
   */
  public java.lang.String getTopicId() {
    return topicId;
  }

  /**
   * Sets the value of the 'topicId' field.
   * @param value the value to set.
   */
  public void setTopicId(java.lang.String value) {
    this.topicId = value;
  }

  /**
   * Gets the value of the 'type' field.
   */
  public org.kaaproject.kaa.server.operations.pojo.sync.NotificationType getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * @param value the value to set.
   */
  public void setType(org.kaaproject.kaa.server.operations.pojo.sync.NotificationType value) {
    this.type = value;
  }

  /**
   * Gets the value of the 'uid' field.
   */
  public java.lang.String getUid() {
    return uid;
  }

  /**
   * Sets the value of the 'uid' field.
   * @param value the value to set.
   */
  public void setUid(java.lang.String value) {
    this.uid = value;
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

  /**
   * Gets the value of the 'body' field.
   */
  public java.nio.ByteBuffer getBody() {
    return body;
  }

  /**
   * Sets the value of the 'body' field.
   * @param value the value to set.
   */
  public void setBody(java.nio.ByteBuffer value) {
    this.body = value;
  }

}
