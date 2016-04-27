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

package org.kaaproject.kaa.server.appenders.kafka.appender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.kafka.config.gen.KafkaConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaLogAppender extends AbstractLogAppender<KafkaConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaLogAppender.class);
    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;

    private ExecutorService executor;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private AtomicInteger kafkaSuccessLogCount = new AtomicInteger();
    private AtomicInteger kafkaFailureLogCount = new AtomicInteger();
    private AtomicInteger inputLogCount = new AtomicInteger();

    private LogEventDao logEventDao;
    private String topicName;
    private boolean closed = false;

    private ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>> converters = new ThreadLocal<Map<String, GenericAvroConverter<GenericRecord>>>() {

        @Override
        protected Map<String, GenericAvroConverter<GenericRecord>> initialValue() {
            return new HashMap<String, GenericAvroConverter<GenericRecord>>();
        }

    };

    public KafkaLogAppender() {
        super(KafkaConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                long second = System.currentTimeMillis() / 1000;
                LOG.info(
                        "[{}] Received {} log record count, {} success kafka callbacks, {}  failure kafka callbacks / second.",
                        second, inputLogCount.getAndSet(0), kafkaSuccessLogCount.getAndSet(0),
                        kafkaFailureLogCount.getAndSet(0));
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        LOG.info("Try to stop kafka log appender...");
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
        }
        LOG.info("Kafka log appender stoped.");
    }

    @Override
    public void doAppend(final LogEventPack logEventPack, final RecordHeader header, final LogDeliveryCallback listener) {
        if (!closed) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOG.debug("[{}] appending {} logs to kafka collection", topicName, logEventPack.getEvents()
                                .size());
                        GenericAvroConverter<GenericRecord> eventConverter = getConverter(logEventPack.getLogSchema()
                                .getSchema());
                        GenericAvroConverter<GenericRecord> headerConverter = getConverter(header.getSchema()
                                .toString());
                        List<KafkaLogEventDto> dtoList = generateKafkaLogEvent(logEventPack, header, eventConverter);
                        LOG.debug("[{}] saving {} objects", topicName, dtoList.size());
                        if (!dtoList.isEmpty()) {
                            int logCount = dtoList.size();
                            inputLogCount.getAndAdd(logCount);
                            logEventDao.save(dtoList, eventConverter, headerConverter, new LogAppenderCallback(
                                    listener, kafkaSuccessLogCount, kafkaFailureLogCount));
                            LOG.debug("[{}] appended {} logs to kafka collection", topicName, logEventPack.getEvents()
                                    .size());
                        } else {
                            listener.onInternalError();
                        }
                    } catch (Exception e) {
                        LOG.warn("Got exception. Can't process log events", e);
                        listener.onInternalError();
                    }
                }
            });
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onConnectionError();
        }

    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, KafkaConfig configuration) {
        LOG.info("Initializing new appender instance using {}", configuration);
        try {
            logEventDao = new KafkaLogEventDao(configuration);
            int executorPoolSize = Math.min(configuration.getExecutorThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            executor = Executors.newFixedThreadPool(executorPoolSize);
            topicName = configuration.getTopic();
            LOG.info("Kafka log appender initialized");
        } catch (Exception e) {
            LOG.error("Failed to init kafka log appender: ", e);
        }

    }

    protected List<KafkaLogEventDto> generateKafkaLogEvent(LogEventPack logEventPack, RecordHeader header,
            GenericAvroConverter<GenericRecord> eventConverter) throws IOException {
        LOG.debug("Generate LogEventDto objects from LogEventPack [{}] and header [{}]", logEventPack, header);
        List<KafkaLogEventDto> events = new ArrayList<>(logEventPack.getEvents().size());
        try {
            for (LogEvent logEvent : logEventPack.getEvents()) {
                LOG.debug("Convert log events [{}] to dto objects.", logEvent);
                if (logEvent == null | logEvent.getLogData() == null) {
                    continue;
                }
                LOG.trace("Avro record converter [{}] with log data [{}]", eventConverter, logEvent.getLogData());
                GenericRecord decodedLog = eventConverter.decodeBinary(logEvent.getLogData());
                events.add(new KafkaLogEventDto(header, decodedLog));
            }
        } catch (IOException e) {
            LOG.error("Unexpected IOException while decoding LogEvents", e);
            throw e;
        }
        return events;
    }

    private static final class LogAppenderCallback implements Callback {

        private final LogDeliveryCallback callback;
        private final AtomicInteger kafkaSuccessLogCount;
        private final AtomicInteger kafkaFailureLogCount;
        private final int size;

        private LogAppenderCallback(LogDeliveryCallback callback, AtomicInteger kafkaSuccessLogCount,
                AtomicInteger kafkaFailureLogCount) {
            this.callback = callback;
            this.kafkaSuccessLogCount = kafkaSuccessLogCount;
            this.kafkaFailureLogCount = kafkaFailureLogCount;
            this.size = 1;
        }

        @Override
        public void onCompletion(RecordMetadata record, Exception e) {
            if (e == null) {
                kafkaSuccessLogCount.getAndAdd(size);
                callback.onSuccess();
            } else {
                kafkaFailureLogCount.getAndAdd(size);
                LOG.warn("Failed to store record", e);
                if (e instanceof IOException) {
                    callback.onConnectionError();
                } else {
                    callback.onInternalError();
                }
            }
        }
    }

    /**
     * Gets the converter.
     *
     * @param schema
     *            the schema
     * @return the converter
     */
    private GenericAvroConverter<GenericRecord> getConverter(String schema) {
        LOG.trace("Get converter for schema [{}]", schema);
        Map<String, GenericAvroConverter<GenericRecord>> converterMap = converters.get();
        GenericAvroConverter<GenericRecord> genAvroConverter = converterMap.get(schema);
        if (genAvroConverter == null) {
            LOG.trace("Create new converter for schema [{}]", schema);
            genAvroConverter = new GenericAvroConverter<GenericRecord>(schema);
            converterMap.put(schema, genAvroConverter);
            converters.set(converterMap);
        }
        LOG.trace("Get converter [{}] from map.", genAvroConverter);
        return genAvroConverter;
    }

}
