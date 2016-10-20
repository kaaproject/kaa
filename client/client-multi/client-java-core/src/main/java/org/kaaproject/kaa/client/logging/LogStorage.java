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
 * <p>Interface of a log storage.</p>
 *
 * <p>Persists log records, forms on demand a new log bucket for sending
 * it to the Operation server, removes already sent log buckets, cleans up elder
 * records in case if there is some limitation on a size of a log storage.</p>
 *
 * <p>{@link org.kaaproject.kaa.client.logging.memory.MemLogStorage} is used by default.</p>
 */
public interface LogStorage {

  /**
   * Persists a log record.
   *
   * @param record The {@link LogRecord} object.
   * @return The {@link BucketInfo} object which contains information about a bucket the log record
   *         is added.
   * @see LogRecord
   * @see BucketInfo
   */
  BucketInfo addLogRecord(LogRecord record);

  /**
   * Returns a log storage status.
   *
   * @return The {@link LogStorageStatus} object.
   * @see LogStorageStatus
   */
  LogStorageStatus getStatus();

  /**
   * Returns a new log bucket.
   *
   * @return The {@link  LogBucket} object or <i>null</i> if there is no logs.
   * @see LogBucket
   */
  LogBucket getNextBucket();

  /**
   * Tells a log storage to remove a log bucket.
   *
   * @param bucketId The id of a log bucket.
   * @see LogBucket
   * @see BucketInfo
   */
  void removeBucket(int bucketId);

  /**
   * Tells a log storage to consider a log bucket as unused, i.e. a log bucket will
   * be accessible again via {@link #getNextBucket()}.
   *
   * @param bucketId The id of a log bucket.
   * @see LogBucket
   * @see BucketInfo
   */
  void rollbackBucket(int bucketId);

  /**
   * Closes a log storage and releases all used resources (if any).
   */
  void close();
}
