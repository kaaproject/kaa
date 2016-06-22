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

package org.kaaproject.kaa.server.appenders.cassandra.appender;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.UnsupportedFeatureException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.CassandraConfig;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ClusteringElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.ColumnMappingElement;
import org.kaaproject.kaa.server.appenders.cassandra.config.gen.DataMappingElement;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class CassandraLogAppender extends AbstractLogAppender<CassandraConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraLogAppender.class);

    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;
    private static AtomicLong appenderCounter = new AtomicLong();
    private ExecutorService executor;
    private ExecutorService callbackExecutor;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private LongAdder cassandraSuccessLogCount = new LongAdder();
    private LongAdder cassandraFailureLogCount = new LongAdder();
    private LongAdder inputLogCount = new LongAdder();

    private volatile String appenderName;
    private LogEventDao logEventDao;
    private boolean closed = false;

    private ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>> converters = new ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>>() {
        @Override
        protected Map<String, GenericAvroConverter<GenericRecord>> initialValue() {
            return new HashMap<>();
        }
    };

    public CassandraLogAppender() {
        super(CassandraConfig.class);
        LOG.debug("Starting statistic request scheduler...");
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                long second = System.currentTimeMillis() / 1000;
                long inLogCount = inputLogCount.sumThenReset();
                long successLogCount = cassandraSuccessLogCount.sumThenReset();
                long failureLogCount = cassandraFailureLogCount.sumThenReset();
                if (inLogCount > 0L || successLogCount > 0 || failureLogCount > 0) {
                    LOG.info("Appender[{}]: [{}] Received {} log record count, {} success cassandra callbacks, {}  failure cassandra callbacks / second.",
                            appenderName, second, inLogCount, successLogCount, failureLogCount);
                }
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void doAppend(final LogEventPack logEventPack, final RecordHeader header, final LogDeliveryCallback listener) {
        if (!closed) {
            executor.submit((Runnable) () -> {
                LOG.debug("[{}] appending {} logs to cassandra collection", getName(), logEventPack.getEvents().size());
                GenericAvroConverter<GenericRecord> eventConverter = getConverter(logEventPack.getLogSchema().getSchema());
                GenericAvroConverter<GenericRecord> headerConverter = getConverter(header.getSchema().toString());

                ProfileData clientProfileData = getProfileData(logEventPack.getClientProfile());
                ProfileData serverProfileData = getProfileData(logEventPack.getServerProfile());

                int logCount = logEventPack.getEvents().size();
                if (logCount > 0) {
                    LOG.debug("[{}] saving {} objects", getName(), logCount);
                    inputLogCount.add(logCount);
                    try {
                        List<CassandraLogEventDto> logs = generateCassandraLogEvent(logEventPack, header, eventConverter);
                        ListenableFuture<List<ResultSet>> result = logEventDao.save(logs, eventConverter, headerConverter, clientProfileData.getConverter(), serverProfileData.getConverter(), clientProfileData.getJson(), serverProfileData.getJson());
                        Futures.addCallback(result, new Callback(listener, cassandraSuccessLogCount, cassandraFailureLogCount, logCount), callbackExecutor);
                        LOG.debug("[{}] appended {} logs to cassandra collection", getName(), logCount);
                    } catch (IOException e) {
                        LOG.warn("Got exception. Can't process log events", e);
                        listener.onInternalError();
                        cassandraFailureLogCount.add(logCount);
                    }
                } else {
                    LOG.error("Received empty log event pack");
                    listener.onInternalError();
                }
            });
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onConnectionError();
        }
    }

    private ProfileData getProfileData(ProfileInfo profileInfo) {
        ProfileData profileData = new ProfileData();
        if (profileInfo != null) {
            profileData.setConverter(getConverter(profileInfo.getSchema()));
            profileData.setJson(profileInfo.getBody());
        }
        return profileData;
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, CassandraConfig configuration) {
        LOG.info("Initializing new appender instance with configuration: {}", configuration);
        try {
            trimConfigurationFields(configuration);
            logEventDao = new CassandraLogEventDao(configuration);
            for (DataMappingElement mappingElement : configuration.getColumnMappingList()) {
                createTable(appender.getApplicationToken(), mappingElement);
            }
            int executorPoolSize = Math.min(configuration.getExecutorThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            int callbackPoolSize = Math.min(configuration.getCallbackThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            executor = Executors.newFixedThreadPool(executorPoolSize);
            callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);
            appenderName = appender.getName();
            LOG.info("Cassandra log appender initialized");
        } catch (Exception e) {
            LOG.error("Failed to init cassandra log appender with configuration: {}", configuration, e);
            close();
        }
    }

    private void trimConfigurationFields(CassandraConfig configuration) {
        LOG.debug("Trimming string configuration properties...");
        for (DataMappingElement dataMappingElement : configuration.getColumnMappingList()) {
            for (ColumnMappingElement element : dataMappingElement.getColumnMapping()) {
                if (element.getColumnName() != null) {
                    element.setColumnName(element.getColumnName().trim());
                }
                if (element.getValue() != null) {
                    element.setValue(element.getValue().trim());
                }
            }
            if (dataMappingElement.getClusteringMapping() != null) {
                for (ClusteringElement element : dataMappingElement.getClusteringMapping()) {
                    if (element.getColumnName() != null) {
                        element.setColumnName(element.getColumnName().trim());
                    }
                }
            }
        }
    }

    private void createTable(String applicationToken, DataMappingElement mappingElement) {
        LOG.trace("Creating table for data mapping configuration: {}", mappingElement);
        logEventDao.createTable(applicationToken, mappingElement);
    }

    @Override
    public void close() {
        LOG.info("Try to stop cassandra log appender...");
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
            }

            for (ExecutorService executorService : Arrays.asList(executor, callbackExecutor, scheduler)) {
                if (executorService != null) {
                    executorService.shutdownNow();
                }
            }
        }
        LOG.info("Cassandra log appender stopped.");
    }

    protected List<CassandraLogEventDto> generateCassandraLogEvent(LogEventPack logEventPack, RecordHeader header,
                                                                   GenericAvroConverter<GenericRecord> eventConverter) throws IOException {
        LOG.debug("Generate LogEventDto objects from LogEventPack [{}] and header [{}]", logEventPack, header);
        List<CassandraLogEventDto> events = new ArrayList<>(logEventPack.getEvents().size());
        try {
            for (LogEvent logEvent : logEventPack.getEvents()) {
                if (logEvent != null && logEvent.getLogData() != null) {
                    LOG.trace("Convert log events [{}] to dto objects.", logEvent);
                    LOG.trace("Avro record converter [{}] with log data [{}]", eventConverter, logEvent.getLogData());
                    GenericRecord decodedLog = eventConverter.decodeBinary(logEvent.getLogData());
                    events.add(new CassandraLogEventDto(header, decodedLog));
                }
            }
        } catch (IOException e) {
            LOG.error("Unexpected IOException while decoding LogEvents", e);
            throw e;
        }
        return events;
    }

    private GenericAvroConverter<GenericRecord> getConverter(String schema) {
        LOG.trace("Get converter for schema [{}]", schema);
        Map<String, GenericAvroConverter<GenericRecord>> converterMap = converters.get();
        if (!converterMap.containsKey(schema)) {
            LOG.trace("Create new converter for schema [{}]", schema);
            converterMap.put(schema, new GenericAvroConverter<>(schema));
        }
        GenericAvroConverter<GenericRecord> genericAvroConverter = converterMap.get(schema);
        LOG.trace("Get converter [{}] from map.", genericAvroConverter);
        return genericAvroConverter;
    }

    private static final class Callback implements FutureCallback<List<ResultSet>> {

        private final LogDeliveryCallback callback;
        private final LongAdder cassandraSuccessLogCount;
        private final LongAdder cassandraFailureLogCount;
        private final int size;

        private Callback(LogDeliveryCallback callback, LongAdder cassandraSuccessLogCount, LongAdder cassandraFailureLogCount, int size) {
            this.callback = callback;
            this.cassandraSuccessLogCount = cassandraSuccessLogCount;
            this.cassandraFailureLogCount = cassandraFailureLogCount;
            this.size = size;
        }

        @Override
        public void onSuccess(List<ResultSet> results) {
            cassandraSuccessLogCount.add(size);
            callback.onSuccess();
        }

        @Override
        public void onFailure(Throwable t) {
            cassandraFailureLogCount.add(size);
            LOG.warn("Failed to store record", t);
            if (t instanceof UnsupportedFeatureException) {
                callback.onRemoteError();
            } else if (t instanceof IOException) {
                callback.onConnectionError();
            } else {
                callback.onInternalError();
            }
        }
    }

    private static final class ProfileData {
        private GenericAvroConverter<GenericRecord> converter;
        private String json;

        public ProfileData(GenericAvroConverter<GenericRecord> converter) {
            this.converter = converter;
        }

        public ProfileData() {
        }

        public GenericAvroConverter<GenericRecord> getConverter() {

            return converter;
        }

        public void setConverter(GenericAvroConverter<GenericRecord> converter) {
            this.converter = converter;
        }

        public String getJson() {
            return json;
        }

        public void setJson(String json) {
            this.json = json;
        }
    }
}
