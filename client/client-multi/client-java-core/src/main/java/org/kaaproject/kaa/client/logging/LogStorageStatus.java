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
 * <p>Interface for a log storage status.</p>
 *
 * <p>Retrieves information about current status of the log storage. Used by
 * a log upload strategy on each adding of new log record in order to check
 * whether to send logs to the server or clean up local storage.</p>
 */
public interface LogStorageStatus {
  /**
   * Retrieves current log storage size used by added records.
   *
   * @return Amount of bytes consumed by added records
   */
  long getConsumedVolume();

  /**
   * Retrieves current number of added records.
   *
   * @return Number of records in a local storage
   */
  long getRecordCount();
}
