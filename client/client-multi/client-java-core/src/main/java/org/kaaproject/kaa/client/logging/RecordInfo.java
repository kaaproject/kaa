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

package org.kaaproject.kaa.client.logging;

/**
 * <p>Describes unique log record delivery info.</p>
 */
public class RecordInfo {

  private final BucketInfo bucketInfo;
  private long recordAddedTimestampMs;
  private long recordDeliveryTimeMs;

  /**
   * Constructs the {@link RecordInfo} object which contains useful information about a log record
   * delivery info.
   *
   * @param bucketInfo The {@link BucketInfo} to which belongs this record.
   */
  public RecordInfo(BucketInfo bucketInfo) {
    super();
    this.bucketInfo = bucketInfo;
  }

  /**
   * <p>Returns the parent bucket.</p>
   *
   * @return The bucket info.
   */
  public BucketInfo getBucketInfo() {
    return bucketInfo;
  }

  /**
   * <p>Returns the timestamp indicating when log record was scheduled for delivery</p>
   *
   * @return The timestamp in milliseconds.
   */
  public long getRecordAddedTimestampMs() {
    return recordAddedTimestampMs;
  }

  public void setRecordAddedTimestampMs(long recordAddedTimestampMs) {
    this.recordAddedTimestampMs = recordAddedTimestampMs;
  }

  /**
   * <p>Returns the total spent time to deliver log record in milliseconds</p>
   *
   * @return The log delivery time in milliseconds.
   */
  public long getRecordDeliveryTimeMs() {
    return recordDeliveryTimeMs;
  }

  public void setRecordDeliveryTimeMs(long recordDeliveryTimeMs) {
    this.recordDeliveryTimeMs = recordDeliveryTimeMs;
  }

}
