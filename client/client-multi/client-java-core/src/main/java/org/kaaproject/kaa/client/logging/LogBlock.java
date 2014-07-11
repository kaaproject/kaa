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

import java.util.List;

/**
 * Wrapper class for sending log block.
 * Each this block should have its unique id 
 * to be mapped in the log storage and delivery stuff.
 */
public class LogBlock {
    /**
     * Unique id for sending log block
     */
    private final String    id;

    /**
     * Sending log block
     */
    private List<LogRecord> logRecords;

    /**
     * Construct wrapper for sending log block.
     * 
     * @param id Unique id using for mapping in a delivery stuff and local log storage 
     * @param records Sending log block
     */
    public LogBlock(String id, List<LogRecord> records) {
        this.id = id;
        this.logRecords = records;
    }

    /**
     * Retrieve log block id
     * 
     * @return Unique log block id
     */
    String getBlockId() {
        return id;
    }

    /**
     * Retrieve sending log records.
     * 
     * @return List of sending log records.
     */
    List<LogRecord> getRecords() {
        return logRecords;
    }
}
