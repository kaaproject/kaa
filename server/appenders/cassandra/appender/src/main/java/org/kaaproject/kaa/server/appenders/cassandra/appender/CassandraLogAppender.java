/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.appenders.cassandra.appender;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraExecuteRequestType;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CassandraLogAppender extends AbstractLogAppender<CassandraConfig> {

    private static final String LOG_TABLE_PREFIX = "logs_";
    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogAppender.class);
    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;

    private ExecutorService callbackExecutor;

    private LogEventDao logEventDao;
    private String tableName;
    private boolean closed = false;
    private CassandraExecuteRequestType executeRequestType;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicLong inputLogCount = new AtomicLong();
    private final AtomicLong cassandraLogCount = new AtomicLong();

    public CassandraLogAppender() {
        super(CassandraConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                long second = System.currentTimeMillis() / 1000;
                LOG.info("[{}] Perf status. Received {} input log record count, and cassandra {} callbacks per second.",
                        second, inputLogCount.getAndSet(0), cassandraLogCount.getAndSet(0));
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            try {
                LOG.debug("[{}] appending {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
                List<LogEventDto> dtoList = generateLogEvent(logEventPack, header);
                LOG.debug("[{}] saving {} objects", tableName, dtoList.size());
                if (!dtoList.isEmpty()) {
                    int logCount = dtoList.size();
                    inputLogCount.getAndAdd(logCount);
                    switch (executeRequestType) {
                        case ASYNC:
                            ListenableFuture<ResultSet> result = logEventDao.saveAsync(dtoList, tableName);
                            Futures.addCallback(result, new Callback(listener, cassandraLogCount, logCount), callbackExecutor);
                            break;
                        case SYNC:
                            logEventDao.save(dtoList, tableName);
                            listener.onSuccess();
                            cassandraLogCount.getAndAdd(logCount);
                            break;
                    }
                    LOG.debug("[{}] appended {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
                } else {
                    listener.onInternalError();
                }
            } catch (IOException e) {
                LOG.warn("Got io exception. Can't generate log events", e);
                listener.onInternalError();
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onConnectionError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CassandraConfig configuration) {
        LOG.info("Initializing new instance of Cassandra log appender");
        try {
            checkExecuteRequestType(configuration);
            logEventDao = new CassandraLogEventDao(configuration);
            createTable(appender.getApplicationToken());
            Integer callbackPoolSize = configuration.getCallbackThreadPoolSize();
            if (callbackPoolSize != null) {
                callbackPoolSize = Math.min(callbackPoolSize, MAX_CALLBACK_THREAD_POOL_SIZE);
            } else {
                callbackPoolSize = MAX_CALLBACK_THREAD_POOL_SIZE;
            }
            callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);
            LOG.info("Cassandra log appender initialized");
        } catch (Exception e) {
            LOG.error("Failed to init cassandra log appender: ", e);
        }
    }

    private void createTable(String applicationToken) {
        if (tableName == null) {
            tableName = LOG_TABLE_PREFIX + applicationToken;
            logEventDao.createTable(tableName);
        } else {
            LOG.warn("Appender is already initialized..");
        }
    }

    @Override
    public void close() {
        LOG.info("Try to stop cassandra log appender...");
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
            }
            if (callbackExecutor != null) {
                callbackExecutor.shutdownNow();
            }
            if(scheduler != null) {
                scheduler.shutdownNow();
            }
        }
        LOG.info("Cassandra log appender stoped.");
    }

    private void checkExecuteRequestType(CassandraConfig configuration) {
        CassandraExecuteRequestType requestType = configuration.getCassandraExecuteRequestType();
        if (CassandraExecuteRequestType.ASYNC.equals(requestType)) {
            executeRequestType = CassandraExecuteRequestType.ASYNC;
        } else {
            executeRequestType = CassandraExecuteRequestType.SYNC;
        }
    }

    private static final class Callback implements FutureCallback<ResultSet> {

        private final LogDeliveryCallback callback;
        private final AtomicLong cassandraLogCount;
        private final int size;

        private Callback(LogDeliveryCallback callback, AtomicLong cassandraLogCount, int size) {
            this.callback = callback;
            this.cassandraLogCount = cassandraLogCount;
            this.size = size;
        }

        @Override
        public void onSuccess(ResultSet result) {
            callback.onSuccess();
            cassandraLogCount.getAndAdd(size);
        }

        @Override
        public void onFailure(Throwable t) {
            if (t instanceof UnsupportedFeatureException) {
                callback.onRemoteError();
            } else if (t instanceof IOException) {
                callback.onConnectionError();
            } else {
                callback.onInternalError();
            }
        }
    }
}
