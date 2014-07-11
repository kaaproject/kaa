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

package org.kaaproject.kaa.client.channel.impl.channels;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.HttpLongPollServerInfo;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableCommandRunnable;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableRunnable;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableScheduledFuture;
import org.kaaproject.kaa.client.channel.impl.channels.polling.PollCommand;
import org.kaaproject.kaa.client.channel.impl.channels.polling.RawDataProcessor;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOperationsChannel implements KaaDataChannel, RawDataProcessor {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultOperationsChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.DOWN);
    }

    private static final String CHANNEL_ID = "default_operations_long_poll_channel";

    private AbstractHttpClient httpClient;
    private KaaDataDemultiplexer demultiplexer;
    private KaaDataMultiplexer multiplexer;

    private HttpLongPollServerInfo currentServer;
    private final AbstractKaaClient client;
    private final KaaClientState state;

    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1) {
        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(
                Runnable runnable, RunnableScheduledFuture<V> task) {
            if (runnable instanceof CancelableRunnable) {
                return new CancelableScheduledFuture<V>((CancelableRunnable)runnable, task);
            }
            return super.decorateTask(runnable, task);
        }
    };

    private volatile Future<?> pollFuture;
    private volatile boolean stopped = true;
    private volatile boolean processingResponse = false;
    private volatile boolean taskPosted = false;
    private volatile boolean isShutdown = false;

    private final CancelableCommandRunnable task = new CancelableCommandRunnable() {

        @Override
        protected void executeCommand() {
            if (!stopped) {
                taskPosted = false;
                currentCommand = new PollCommand(httpClient,
                        DefaultOperationsChannel.this,
                        SUPPORTED_TYPES,
                        currentServer);
                if (!Thread.currentThread().isInterrupted()) {
                    currentCommand.execute();
                }
                currentCommand = null;
                if (!taskPosted && !stopped && !Thread.currentThread().isInterrupted()) {
                    taskPosted = true;
                    pollFuture = scheduler.submit(task);
                }
            }
        }
    };

    public DefaultOperationsChannel(AbstractKaaClient client, KaaClientState state) {
        this.client = client;
        this.state = state;
    }

    private void stopPollScheduler(boolean forced) {
        if (!stopped) {
            stopped = true;
            if (!processingResponse) {
                if (pollFuture != null) {
                    LOG.info("Stopping poll future..");
                    pollFuture.cancel(forced);
                    if (forced) {
                        task.waitUntilExecuted();
                    }
                }
            }
        }
    }

    private void startPoll() {
        if (!stopped) {
            stopPollScheduler(true);
        }
        stopped = false;
        LOG.info("Starting poll scheduler..");
        taskPosted = true;
        pollFuture = scheduler.submit(task);
        LOG.info("Poll scheduler started");
    }

    private void stopPoll() {
        stopPollScheduler(true);
    }

    @Override
    public synchronized LinkedHashMap<String, byte[]> createRequest(Map<TransportType, ChannelDirection> types) {
        try {
            byte[] requestBodyRaw = multiplexer.compileRequest(types);
            return HttpRequestCreator.createOperationHttpRequest(requestBodyRaw, httpClient.getEncoderDecoder());
        } catch (Exception e) {
            LOG.error("Failed to create request {}", e);
        }
        return null;
    }

    @Override
    public synchronized void onResponse(byte [] response) {
        LOG.debug("Response for channel {} received", getId());
        byte[] decodedResponse;
        try {
            processingResponse = true;
            decodedResponse = httpClient.getEncoderDecoder().decodeData(response);
            demultiplexer.processResponse(decodedResponse);
            processingResponse = false;
        } catch (Exception e) {
            LOG.error("Failed to process response {}. Exception {}", Arrays.toString(response), e);
        }
    }

    @Override
    public void onServerError(ServerInfo info) {
        if (!stopped) {
            LOG.debug("Channel {} connection failed", getId());
            synchronized (this) {
                stopPollScheduler(false);
            }
            client.getChannelMananager().onServerFailed(info);
        } else {
            LOG.debug("Channel {} connection aborted", getId());
        }
    }

    @Override
    public synchronized void sync(TransportType type) {
        if (isShutdown) {
            LOG.info("Can't sync. Channel {} is down", getId());
            return;
        }
        LOG.info("Processing sync {} for channel {}", type, getId());
        if (multiplexer != null && demultiplexer != null) {
            if (currentServer != null) {
                if (SUPPORTED_TYPES.get(type) != null) {
                    stopPoll();
                    startPoll();
                } else {
                    LOG.error("Unsupported type {} for channel {}", type, getId());
                }
            } else {
                LOG.warn("Can't sync. Server is null");
            }
        }
    }

    @Override
    public synchronized void syncAll() {
        if (isShutdown) {
            LOG.info("Can't sync. Channel {} is down", getId());
            return;
        }
        LOG.info("Processing sync all for channel {}", getId());
        if (multiplexer != null && demultiplexer != null) {
            if (currentServer != null) {
                stopPoll();
                startPoll();
            } else {
                LOG.warn("Can't sync. Server is null");
            }
        }
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.HTTP_LP;
    }

    @Override
    public synchronized void setDemultiplexer(KaaDataDemultiplexer demultiplexer) {
        if (demultiplexer != null) {
            this.demultiplexer = demultiplexer;
        }
    }

    @Override
    public synchronized void setMultiplexer(KaaDataMultiplexer multiplexer) {
        if (multiplexer != null) {
            this.multiplexer = multiplexer;
        }
    }

    @Override
    public synchronized void setServer(ServerInfo server) {
        if (isShutdown) {
            LOG.info("Can't set server. Channel {} is down", getId());
            return;
        }
        if (server != null) {
            stopPoll();
            this.currentServer = (HttpLongPollServerInfo) server;
            this.httpClient = client.createHttpClient(currentServer.getURL(), state.getPrivateKey(), state.getPublicKey(), currentServer.getPublicKey());
            startPoll();
        }
    }

    public void shutdown() {
        isShutdown = true;
        stopPoll();
        scheduler.shutdownNow();
    }

    @Override
    public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
        return SUPPORTED_TYPES;
    }

}
