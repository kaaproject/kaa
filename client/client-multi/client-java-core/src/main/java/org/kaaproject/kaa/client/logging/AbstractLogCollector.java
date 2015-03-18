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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogEntry;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation of @see LogCollector
 * 
 * @author Andrew Shvayka
 */
public abstract class AbstractLogCollector implements LogCollector, LogProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractLogCollector.class);
    
    public static final long MAX_BATCH_VOLUME = 512 * 1024; // Framework limitation
    
    // TODO: reuse this scheduler in other subsystems
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final LogTransport transport;
    private final Map<Integer, Long> timeoutMap = new LinkedHashMap<>();
    private final KaaChannelManager channelManager;

    protected LogStorage storage;
    private LogUploadStrategy strategy;
    private LogFailoverCommand controller;

    private volatile boolean isUploading = false;

    public AbstractLogCollector(LogTransport transport, KaaChannelManager manager) {
        this.strategy = new DefaultLogUploadStrategy();
        this.storage = new MemoryLogStorage(strategy.getBatchSize());
        this.controller = new DefaultLogUploadController();
        this.channelManager = manager;
        this.transport = transport;
    }

    @Override
    public void setStrategy(LogUploadStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy is null!");
        }
        this.strategy = strategy;
        LOG.info("New log upload strategy was set: {}", strategy);
    }

    @Override
    public void setStorage(LogStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage is null!");
        }
        this.storage = storage;
        LOG.info("New log storage was set {}", storage);
    }

    @Override
    public void fillSyncRequest(LogSyncRequest request) {
        LogBlock group = null;
        synchronized (storage) {
            isUploading = false;
            if (storage.getStatus().getRecordCount() == 0) {
                LOG.debug("Storage is empty");
                return;
            }
            group = storage.getRecordBlock(strategy.getBatchSize());
        }

        if (group != null) {
            List<LogRecord> recordList = group.getRecords();

            if (!recordList.isEmpty()) {
                LOG.trace("Sending {} log records", recordList.size());

                List<LogEntry> logs = new LinkedList<>();
                for (LogRecord record : recordList) {
                    logs.add(new LogEntry(ByteBuffer.wrap(record.getData())));
                }

                request.setRequestId(group.getBlockId());
                request.setLogEntries(logs);

                timeoutMap.put(group.getBlockId(), System.currentTimeMillis() + strategy.getTimeout() * 1000);
            }
        } else {
            LOG.warn("Log group is null: log group size is too small");
        }
    }

    @Override
    public synchronized void onLogResponse(LogSyncResponse logSyncResponse) throws IOException {
        if (logSyncResponse.getDeliveryStatuses() != null) {
            for (LogDeliveryStatus response : logSyncResponse.getDeliveryStatuses()) {
                if (response.getResult() == SyncResponseResultType.SUCCESS) {
                    storage.removeRecordBlock(response.getRequestId());
                } else {
                    storage.notifyUploadFailed(response.getRequestId());
                    strategy.onFailure(controller, response.getErrorCode());
                }

                timeoutMap.remove(response.getRequestId());
            }

            processUploadDecision(strategy.isUploadNeeded(storage.getStatus()));
        }
    }
    
    @Override
    public void stop() {
        scheduler.shutdown();
    }

    private void processUploadDecision(LogUploadStrategyDecision decision) {
        switch (decision) {
        case UPLOAD:
            if (!isUploading) {
                isUploading = true;
                transport.sync();
            }
            break;
        case NOOP:
        default:
            break;
        }
    }

    //TODO: fix this. it is now executed only when new log record is added. 
    protected boolean isDeliveryTimeout() {
        boolean isTimeout = false;
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Integer, Long> logRequest : timeoutMap.entrySet()) {
            if (currentTime >= logRequest.getValue()) {
                isTimeout = true;
                break;
            }
        }

        if (isTimeout) {
            LOG.info("Log delivery timeout detected");

            for (Map.Entry<Integer, Long> logRequest : timeoutMap.entrySet()) {
                storage.notifyUploadFailed(logRequest.getKey());
            }

            timeoutMap.clear();
            strategy.onTimeout(controller);
        }

        return isTimeout;
    }

    protected void uploadIfNeeded() {
        processUploadDecision(strategy.isUploadNeeded(storage.getStatus()));
    }

    private class DefaultLogUploadController implements LogFailoverCommand {
        @Override
        public void switchAccessPoint() {
            KaaDataChannel channel = channelManager.getChannelByTransportType(TransportType.LOGGING);
            if (channel != null) {
                channelManager.onServerFailed(channel.getServer());
            } else {
                LOG.warn("Failed to switch Operation server. No channel is used for logging transport");
            }
        }

        @Override
        public void retryLogUpload() {
            uploadIfNeeded();
        }

        @Override
        public void retryLogUpload(int delay) {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    uploadIfNeeded();
                }
            }, delay, TimeUnit.SECONDS);
        }
    }
}
