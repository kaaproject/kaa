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
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.logging.gen.SuperRecord;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of @see LogCollector
 */
public class DefaultLogCollector implements LogCollector, LogProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogCollector.class);

    private LogUploadConfiguration configuration;
    private LogStorage             storage;
    private LogStorageStatus       storageStatus;
    private LogUploadStrategy      uploadStrategy;
    private final LogTransport    transport;

    boolean isUploading;

    public DefaultLogCollector(LogTransport transport) {
        configuration = new DefaultLogUploadConfiguration(8 * 1024, 4 * 8 * 1024, 1024 * 1024);
        storage = new MemoryLogStorage(configuration.getBatchVolume());
        storageStatus = (LogStorageStatus)storage;
        uploadStrategy = new DefaultLogUploadStrategy();
        this.transport = transport;

        isUploading = false;

    }

    @Override
    public synchronized void addLogRecord(SuperRecord record) throws IOException {
        storage.addLogRecord(new LogRecord(record));
        processUploadDecision(uploadStrategy.isUploadNeeded(configuration, storageStatus));
    }

    @Override
    public void setUploadStrategy(LogUploadStrategy strategy) {
        if (strategy != null) {
            this.uploadStrategy = strategy;
            LOG.info("New log upload strategy was set");
        }
    }

    @Override
    public void setStorage(LogStorage storage) {
        if (storage != null) {
            this.storage = storage;
            this.storageStatus = null;
            LOG.info("New log storage was set");
        }
    }

    @Override
    public void setStorageStatus(LogStorageStatus status) {
        if (status != null) {
            this.storageStatus = status;
            LOG.info("New log storage status was set");
        }
    }

    @Override
    public void setConfiguration(LogUploadConfiguration configuration) {
        if (configuration != null) {
            this.configuration = configuration;
            LOG.info("New log configuration was set");
            processUploadDecision(uploadStrategy.isUploadNeeded(configuration, storageStatus));
        }
    }

    @Override
    public void fillSyncRequest(LogSyncRequest request) {
        if (isUploading) {
            LogBlock group = storage.getRecordBlock(configuration.getBatchVolume());

            if (group != null) {
                List<LogRecord> recordList = group.getRecords();

                LOG.info("Sending {} log records", recordList.size());

                if (!recordList.isEmpty()) {
                    List<LogEntry> logs = new LinkedList<>();
                    for (LogRecord record : recordList) {
                        logs.add(new LogEntry(ByteBuffer.wrap(record.getData())));
                    }
                    request.setRequestId(group.getBlockId());
                    request.setLogEntries(logs);
                }
            } else {
                LOG.warn("Log group is null: storage is empty or log group size is too small");
            }
        } else {
            LOG.info("Skiping log upload");
        }
    }

    @Override
    public void onLogResponse(LogSyncResponse response) throws IOException {
        isUploading = false;

        if (response.getResult() == SyncResponseResultType.SUCCESS) {
            storage.removeRecordBlock(response.getRequestId());
            processUploadDecision(uploadStrategy.isUploadNeeded(configuration, storageStatus));
        } else {
            storage.notifyUploadFailed(response.getRequestId());
        }
    }

    private void processUploadDecision(LogUploadStrategyDecision decision) {
        switch (decision) {
        case UPLOAD:
            if (!isUploading) {
                isUploading = true;
                if (transport != null) {
                    transport.sync();
                } else {
                    LOG.warn("Log transport is null");
                }
            }
            break;
        case CLEANUP:
            storage.removeOldestRecord(configuration.getMaximumAllowedVolume());
            break;
        case NOOP:
        default:
            break;
        }
    }

}
