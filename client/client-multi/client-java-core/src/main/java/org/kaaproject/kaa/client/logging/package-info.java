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

/**
 * <p>Provides log collection stuff.</p>
 *
 * <p>The Kaa Logging subsystem is designed to collect records (logs) of
 * pre-configured structure, periodically deliver them from endpoints to
 * Operation servers, and persist in the server for further processing,
 * or submit to immediate stream analytics.</p>
 *
 * <p>The Kaa logs structure is determined by the schema that is configurable.</p>
 *
 * <p>Assume, log record schema has the following form:</p>
 * <pre>
 * {@code
 * {
 *  "type": "record",
 *  "name": "LogData",
 *  "namespace": "org.kaaproject.sample",
 *  "fields": [
 *          {
 *              "name": "level",
 *              "type": {
 *                  "type": "enum",
 *                  "name": "Level",
 *                  "symbols": [
 *                      "DEBUG",
 *                      "ERROR",
 *                      "FATAL",
 *                      "INFO",
 *                      "TRACE",
 *                      "WARN"
 *                  ]
 *              }
 *          },
 *          {
 *              "name": "tag",
 *              "type": "string"
 *          },
 *          {
 *              "name": "message",
 *              "type": "string"
 *          }
 *    ]
 * }
 * }
 * </pre>
 *
 * <h3>Add new log record</h3>
 * <pre>
 * {@code
 * // Get a Log Collector reference
 * LogCollector logCollector = Kaa.getKaaClient().getLogCollector();
 * // Create a log entity (according to the org.kaaproject.sample.LogData sample schema above)
 * LogData logRecord = new LogData(Level.INFO, "tag", "message");
 * // Push record to collector
 * logCollector.addLogRecord(logRecord);
 * }
 * </pre>
 *
 * <h3>Logging components</h3>
 *
 * <p>Kaa SDK logging stuff is based on three main components - log storage
 * ({@link org.kaaproject.kaa.client.logging.LogStorage},
 * {@link org.kaaproject.kaa.client.logging.LogStorageStatus}), upload strategy
 * ({@link org.kaaproject.kaa.client.logging.LogUploadStrategy}) and configuration.
 * For each
 * of these components there is a reference implementation using by defaults
 * {@link org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy},
 * correspondingly).</p>
 *
 * <p>The log storage is responsible for a log persistence.<b>The reference
 * implementation stores all added logs in a dynamic memory, so if there are
 * some logs but application has been closed immediately or crashes, logs will
 * be lost.</b></p>
 *
 * <p>The log upload strategy is used to decide what Kaa should do after each
 * log record is added ({@link org.kaaproject.kaa.client.logging.LogUploadStrategyDecision}).</p>
 *
 * <p>The configuration is used to define all limitations that affects
 * on a log collection stuff. The reference implementation works with three
 * parameters: batch volume (8KB), threshold volume (32KB) and maximum allowed
 * volume (1MB).</p>
 *
 * <p>If there are need in some specific triggers for upload or you want to use
 * more reliable storage, simply set your own implementation for interested
 * component ({@link org.kaaproject.kaa.client.logging.LogCollector#setStorage(LogStorage)},
 * {@link org.kaaproject.kaa.client.logging.LogCollector#setStrategy(LogUploadStrategy)}.</p>
 */
package org.kaaproject.kaa.client.logging;