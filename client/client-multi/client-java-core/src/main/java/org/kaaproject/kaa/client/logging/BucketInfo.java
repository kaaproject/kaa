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
 * <p>Describes a unique log bucket.</p>
 *
 * <p>By uniqueness it means that any of log records in a bucket is not repeated in any other log
 * bucket.</p>
 *
 * <p><b>Note:</b>The id should be unique across all available log buckets.</p>
 */
public class BucketInfo {
  private final int bucketId;
  private final int logCount;

  /**
   * Constructs the {@link BucketInfo} object which contains a useful information about a log
   * bucket.
   *
   * @param bucketId The id of a bucket. <b>Note:</b>The id should be unique across all available
   *                 log buckets.
   * @param logCount The number of logs the bucket contains.
   */
  public BucketInfo(int bucketId, int logCount) {
    this.bucketId = bucketId;
    this.logCount = logCount;
  }

  /**
   * <p>Returns the id of a bucket.</p>
   *
   * <p><b>Note:</b>The id should be unique across all available log buckets.</p>
   *
   * @return The id of a bucket.
   */
  public int getBucketId() {
    return bucketId;
  }

  /**
   * @return The number of logs a bucket contains.
   */
  public int getLogCount() {
    return logCount;
  }

}
