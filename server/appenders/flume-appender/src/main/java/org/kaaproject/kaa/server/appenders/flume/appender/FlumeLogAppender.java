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

package org.kaaproject.kaa.server.appenders.flume.appender;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.flume.appender.client.FlumeClientManager;
import org.kaaproject.kaa.server.appenders.flume.appender.client.async.AppendBatchAsyncResultPojo;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class FlumeLogAppender extends AbstractLogAppender<FlumeConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppender.class);
    private static final int MAX_CALLBACK_THREAD_POOL_SIZE = 10;

    private ExecutorService executor;
    private ExecutorService callbackExecutor;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private AtomicInteger flumeSuccessLogCount = new AtomicInteger();
    private AtomicInteger flumeFailureLogCount = new AtomicInteger();
    private AtomicInteger inputLogCount = new AtomicInteger();

    private FlumeConfig configuration;

    private boolean closed = false;

    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager<?> flumeClientManager;

    public FlumeLogAppender() {
        super(FlumeConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                long second = System.currentTimeMillis() / 1000;
                LOG.info(
                        "[{}] Received {} log record count, {} success flume callbacks, {}  failure flume callbacks / second.",
                        second, inputLogCount.getAndSet(0), flumeSuccessLogCount.getAndSet(0),
                        flumeFailureLogCount.getAndSet(0));
            }
        }, 0L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public void doAppend(final LogEventPack logEventPack, final RecordHeader header, final LogDeliveryCallback listener) {
        if (!closed) {
            if (executor == null || callbackExecutor == null || flumeClientManager == null) {
                reinit();
            }
            if (executor == null || callbackExecutor == null || flumeClientManager == null){
                LOG.warn("Some of components haven't been initialized. Skipping append method");
                listener.onInternalError();
                return;
            }
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Event> events = flumeEventBuilder.generateEvents(logEventPack, header,
                                getApplicationToken());
                        try {
                            if (events != null && !events.isEmpty()) {
                                if (flumeClientManager != null) {
                                    int logCount = events.size();
                                    inputLogCount.getAndAdd(logCount);
                                    ListenableFuture<AppendBatchAsyncResultPojo> result = flumeClientManager
                                            .sendEventsToFlumeAsync(events);
                                    Futures.addCallback(result, new Callback(listener, flumeSuccessLogCount,
                                            flumeFailureLogCount, logCount), callbackExecutor);
                                    LOG.debug("Appended {} logs to flume", logEventPack.getEvents().size());
                                } else {
                                    LOG.warn("Flume client wasn't initialized. Invoke method init before.");
                                    listener.onInternalError();
                                }
                            } else {
                                LOG.warn("Unable to generate Flume events from log event pack!");
                                listener.onInternalError();
                            }
                        } catch (EventDeliveryException e) {
                            LOG.warn("Can't send flume event. ", e);
                            listener.onConnectionError();
                        }
                    } catch (Exception e) {
                        LOG.warn("Got exception. Can't process log events", e);
                        listener.onInternalError();
                    }
                }
            });
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onInternalError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, FlumeConfig configuration) {
        LOG.debug("Initializing new instance of Flume log appender");
        try {
            this.configuration = configuration;
            flumeEventBuilder = new FlumeAvroEventBuilder();
            flumeEventBuilder.init(configuration);
            int executorPoolSize = Math.min(configuration.getExecutorThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            int callbackPoolSize = Math.min(configuration.getCallbackThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            executor = Executors.newFixedThreadPool(executorPoolSize);
            callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);
            flumeClientManager = FlumeClientManager.getInstance(configuration);
        } catch (Exception e) {
            LOG.error("Failed to init Flume log appender: ", e);
        }
    }

    public void reinit() {
        if (configuration == null) {
            LOG.warn("Flume configuration wasn't initialized. Invoke method init with configuration before.");
            return;
        }
        if (flumeEventBuilder == null) {
            flumeEventBuilder = new FlumeAvroEventBuilder();
            flumeEventBuilder.init(configuration);
        }
        if (executor == null) {
            int executorPoolSize = Math.min(configuration.getExecutorThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            executor = Executors.newFixedThreadPool(executorPoolSize);
        }
        if (callbackExecutor == null) {
            int callbackPoolSize = Math.min(configuration.getCallbackThreadPoolSize(), MAX_CALLBACK_THREAD_POOL_SIZE);
            callbackExecutor = Executors.newFixedThreadPool(callbackPoolSize);
        }
        if (flumeClientManager == null) {
            flumeClientManager = FlumeClientManager.getInstance(configuration);
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (flumeClientManager != null) {
                flumeClientManager.cleanUp();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
            if (callbackExecutor != null) {
                callbackExecutor.shutdownNow();
            }
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            flumeClientManager = null;
        }
        LOG.debug("Stoped Flume log appender.");
    }

    private static final class Callback implements FutureCallback<AppendBatchAsyncResultPojo> {

        private final LogDeliveryCallback callback;
        private final AtomicInteger flumeSuccessLogCount;
        private final AtomicInteger flumeFailureLogCount;
        private final int size;

        private Callback(LogDeliveryCallback callback, AtomicInteger flumeSuccessLogCount,
                AtomicInteger flumeFailureLogCount, int size) {
            this.callback = callback;
            this.flumeSuccessLogCount = flumeSuccessLogCount;
            this.flumeFailureLogCount = flumeFailureLogCount;
            this.size = size;
        }

        @Override
        public void onSuccess(AppendBatchAsyncResultPojo result) {
            flumeSuccessLogCount.getAndAdd(size);
            callback.onSuccess();
        }

        @Override
        public void onFailure(Throwable t) {
            flumeFailureLogCount.getAndAdd(size);
            LOG.warn("Failed to store record", t);
            if (t instanceof IOException) {
                callback.onConnectionError();
            } else if (t instanceof EventDeliveryException) {
                callback.onRemoteError();
            } else {
                callback.onInternalError();
            }
        }
    }
}
