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

import java.util.List;

/**
 * <p>The helper class which is used to transfer logs from {@link LogStorage} to {@link
 * LogCollector}.</p>
 *
 * <p><b>Note:</b>The id should be unique across all available log buckets.</p>
 */
public class LogBucket {
  /**
   * The unique id of a log bucket.
   */
  private final int id;

  /**
   * Log records.
   */
  private final List<LogRecord> logRecords;

  /**
   * Constructs {@link LogBucket} object.
   *
   * @param id      The unique log bucket id.
   * @param records Log records.
   */
  public LogBucket(int id, List<LogRecord> records) {
    this.id = id;
    this.logRecords = records;
  }

  /**
   * <p>Returns a log bucket id.</p>
   *
   * <p>A log bucket id should be unique across all available buckets.</p>
   *
   * @return The log bucket id.
   */
  public int getBucketId() {
    return id;
  }

  /**
   * Retrieves log records of the bucket.
   *
   * @return The list of log records.
   */
  List<LogRecord> getRecords() {
    return logRecords;
  }
}
