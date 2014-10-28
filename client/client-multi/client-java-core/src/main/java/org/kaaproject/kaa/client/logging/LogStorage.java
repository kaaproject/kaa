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
 * <p>Reference implementation used by default ({@link MemoryLogStorage}).</p>
 */
public interface LogStorage {
    /**
     * Persists new log record.
     *
     * @param record New record ({@link LogRecord})
     */
    void addLogRecord(LogRecord record);

    /**
     * <p>Retrieves new log block of specified size or null if there is no logs.</p>
     *
     * <p>The size of retrieved log records should NOT be greater than specified
     * block size.</p>
     *
     * @param blockSize Maximum size of sending log block
     * @return New log block ({@link  LogBlock})
     */
    LogBlock getRecordBlock(long blockSize);

    /**
     * <p>Removes already sent log records by its block id.</p>
     *
     * <p>Use in case of a successful upload.</p>
     *
     * @param id Unique id of sent log block
     */
    void removeRecordBlock(String id);

    /**
     * Removes elder records until occupied size becomes equal or less than
     * specified in a passed parameter.
     *
     * @param maximumAllowedVolume Maximum size of inner storage
     */
    void removeOldestRecord(long maximumAllowedVolume);

    /**
     * Notifies if sending of a log block with a specified id was failed.
     *
     * @param id Unique id of log block.
     */
    void notifyUploadFailed(String id);
}