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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraExecuteRequestType;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ClusteringElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnMappingElement;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class CassandraLogAppender extends AbstractLogAppender<CassandraConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogAppender.class);
    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;

    private ExecutorService callbackExecutor;

    private LogEventDao logEventDao;
    private String tableName;
    private boolean closed = false;
    private CassandraExecuteRequestType executeRequestType;

    public CassandraLogAppender() {
        super(CassandraConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            try {
                LOG.debug("[{}] appending {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
                GenericAvroConverter<GenericRecord> eventConverter = getConverter(logEventPack.getLogSchema().getSchema());
                GenericAvroConverter<GenericRecord> headerConverter = getConverter(header.getSchema().toString());
                List<CassandraLogEventDto> dtoList = generateCassandraLogEvent(logEventPack, header, eventConverter);
                LOG.debug("[{}] saving {} objects", tableName, dtoList.size());
                if (!dtoList.isEmpty()) {
                    switch (executeRequestType) {
                    case ASYNC:
                        ListenableFuture<ResultSet> result = logEventDao.saveAsync(dtoList, tableName, eventConverter, headerConverter);
                        Futures.addCallback(result, new Callback(listener), callbackExecutor);
                        break;
                    case SYNC:
                        logEventDao.save(dtoList, tableName, eventConverter, headerConverter);
                        listener.onSuccess();
                        break;
                    }
                    LOG.debug("[{}] appended {} logs to cassandra collection", tableName, logEventPack.getEvents().size());
                } else {
                    listener.onInternalError();
                }
            } catch (IOException e) {
                LOG.warn("Got io exception. Can't generate log events", e);
                listener.onInternalError();
            } catch (RuntimeException e) {
                LOG.warn("Appender got runtime exception:", e);
                listener.onInternalError();
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onConnectionError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CassandraConfig configuration) {
        LOG.info("Initializing new appender instance using {}", configuration);
        try {
            trimConfigurationFields(configuration);
            setExecuteRequestType(configuration);
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

    private void trimConfigurationFields(CassandraConfig configuration) {
        for (ColumnMappingElement element : configuration.getColumnMapping()) {
            if (element.getColumnName() != null) {
                element.setColumnName(element.getColumnName().trim());
            }
            if (element.getValue() != null) {
                element.setValue(element.getValue().trim());
            }
        }
        if (configuration.getClusteringMapping() != null) {
            for (ClusteringElement element : configuration.getClusteringMapping()) {
                if(element.getColumnName() != null){
                    element.setColumnName(element.getColumnName().trim());
                }
            }
        }
    }

    private void createTable(String applicationToken) {
        tableName = logEventDao.createTable(applicationToken);
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
        }
        LOG.info("Cassandra log appender stoped.");
    }

    protected List<CassandraLogEventDto> generateCassandraLogEvent(LogEventPack logEventPack, RecordHeader header,
            GenericAvroConverter<GenericRecord> eventConverter) throws IOException {
        LOG.debug("Generate LogEventDto objects from LogEventPack [{}] and header [{}]", logEventPack, header);
        List<CassandraLogEventDto> events = new ArrayList<>(logEventPack.getEvents().size());
        try {
            for (LogEvent logEvent : logEventPack.getEvents()) {
                LOG.debug("Convert log events [{}] to dto objects.", logEvent);
                if (logEvent == null | logEvent.getLogData() == null) {
                    continue;
                }
                LOG.trace("Avro record converter [{}] with log data [{}]", eventConverter, logEvent.getLogData());
                GenericRecord decodedLog = eventConverter.decodeBinary(logEvent.getLogData());
                events.add(new CassandraLogEventDto(header, decodedLog));
            }
        } catch (IOException e) {
            LOG.error("Unexpected IOException while decoding LogEvents", e);
            throw e;
        }
        return events;
    }

    private void setExecuteRequestType(CassandraConfig configuration) {
        CassandraExecuteRequestType requestType = configuration.getCassandraExecuteRequestType();
        if (CassandraExecuteRequestType.ASYNC.equals(requestType)) {
            executeRequestType = CassandraExecuteRequestType.ASYNC;
        } else {
            executeRequestType = CassandraExecuteRequestType.SYNC;
        }
    }

    private static final class Callback implements FutureCallback<ResultSet> {

        private final LogDeliveryCallback callback;

        private Callback(LogDeliveryCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(ResultSet result) {
            callback.onSuccess();
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
