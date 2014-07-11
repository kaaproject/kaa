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

package org.kaaproject.kaa.client.logging;

/**
 * Interface for log storage status.
 * 
 * Retrieve information about current status of a log storage.
 * Use by a log upload strategy on each adding of new log record to check
 * whether there is need to send logs to a server or clean up local storage.
 * 
 * Reference implementation is present and use by default (@see MemoryLogStorage).
 */
public interface LogStorageStatus {
    /**
     * Retrieve current log storage size used by added records
     * 
     * @return Amount of bytes consumed by added records
     */
    long getConsumedVolume();

    /**
     * Retrieve current number of added records.
     * 
     * @return Number of records in a local storage
     */
    long getRecordCount();
}
