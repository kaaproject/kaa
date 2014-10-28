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

import java.io.IOException;

import org.kaaproject.kaa.client.logging.gen.SuperRecord;

/**
 * <p>Interface for a log collector.</p>
 *
 * <p>Adds new log record to a local storage.</p>
 *
 * <p>May be configured by setting user defined log record storage,
 * storage status, upload configuration and log upload strategy.
 * Each of them may be set independently of others.</p>
 *
 * <p>Reference implementation of each module used by default.</p>
 *
 * @see LogStorage
 * @see LogStorageStatus
 * @see LogUploadStrategy
 * @see LogUploadConfiguration
 */
public interface LogCollector {

    /**
     * Adds new log record to local storage.
     *
     * @param record New log record object
     */
    void addLogRecord(SuperRecord record) throws IOException;

    /**
     * Set user implementation of a log storage.
     *
     * @param storage User-defined log storage object
     */
    void setStorage(LogStorage storage);

    /**
     * Set user implementation of a log storage status.
     *
     * @param status User-defined log storage status object
     */
    void setStorageStatus(LogStorageStatus status);

    /**
     * Set user implementation of a log upload strategy.
     *
     * @param strategy User-defined log upload strategy object.
     */
    void setUploadStrategy(LogUploadStrategy strategy);

    /**
     * Set user implementation of a log upload configuration.
     *
     * @param configuration User-defined log upload configuration object.
     */
    void setConfiguration(LogUploadConfiguration configuration);
}