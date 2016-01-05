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
 * <p>Interface for log storage.</p>
 *
 * <p>Persists each new log record, forms on demand new log block for sending
 * it to the Operation server, removes already sent records, cleans up elder
 * records in case if there is some limitation on a size of log storage.</p>
 *
 * <p>Reference implementation used by default ({@link org.kaaproject.kaa.client.logging.memory.MemLogStorage}).</p>
 */
public interface LogStorage {

    /**
     * Persists new log record.
     *
     * @param record New record ({@link LogRecord})
     */
    BucketInfo addLogRecord(LogRecord record);

    /**
     * Gets log storage status.
     *
     * @return current status of log storage
     */
    LogStorageStatus getStatus();

    /**
     * <p>Retrieves new log block of specified size or null if there is no logs.</p>
     *
     * <p>The size of retrieved log records should NOT be greater than specified
     * block size.</p>
     *
     * @return New log block ({@link  LogBlock})
     */
    LogBlock getRecordBlock();

    /**
     * <p>Removes already sent log records by its block id.</p>
     *
     * <p>Use in case of a successful upload.</p>
     *
     * @param id Unique id of sent log block
     */
    void removeRecordBlock(int id);

    /**
     * Notifies if sending of a log block with a specified id was failed.
     *
     * @param id Unique id of log block.
     */
    void notifyUploadFailed(int id);

    /**
     * Closes log storage and releases all used resources (if any)
     */
    void close();
}
