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
 * Interface for log storage.
 *
 * Responsible for persistence of each new log record, forming on demand
 * new log block for sending to the operation server, removal of sent records,
 * clean up of elder records if there is some limitation on a storage size. 
 *
 * Reference implementation is present and use by default (@see MemoryLogStorage).
 */
public interface LogStorage {
    /**
     * Persist new log record.
     *
     * @param record New record (@see LogRecord)
     */
    void addLogRecord(LogRecord record);

    /**
     * Retrieve new log block of specified size for sending or 
     * null if there is no logs. The size of retrieved log records 
     * should NOT be greater than specified block size.
     *
     * @param blockSize Maximum size of sending log block
     * @return New log block (@see LogBlock)
     */
    LogBlock getRecordBlock(long blockSize);

    /**
     * Remove sent log records by its bloc id.
     *
     * @param id Unique id of sent log block
     */
    void removeRecordBlock(String id);

    /**
     * Remove records till inner storage has equal or less size
     * than specified one.
     *
     * @param maximumAllowedVolume Maximum size of inner storage
     */
    void removeOldestRecord(long maximumAllowedVolume);

    /**
     * Notify that sending of log block with specified id was failed.
     *
     * @param id Unique id of log block.
     */
    void notifyUploadFailed(String id);
}