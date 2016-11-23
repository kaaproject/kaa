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

package org.kaaproject.kaa.client.channel.impl.channels;

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.IpTransportInfo;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.TransportProtocolIdConstants;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableCommandRunnable;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableRunnable;
import org.kaaproject.kaa.client.channel.impl.channels.polling.CancelableScheduledFuture;
import org.kaaproject.kaa.client.channel.impl.channels.polling.PollCommand;
import org.kaaproject.kaa.client.channel.impl.channels.polling.RawDataProcessor;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class DefaultOperationsChannel implements KaaDataChannel, RawDataProcessor {

  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(DefaultOperationsChannel.class);

  private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<>();
  private static final String CHANNEL_ID = "default_operations_long_poll_channel";

  static {
    SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.DOWN);
  }

  private final Object httpClientLock = new Object();
  private final Object httpClientSetLock = new Object();
  private final AbstractKaaClient client;
  private final KaaClientState state;
  private final FailoverManager failoverManager;
  private AbstractHttpClient httpClient;
  private KaaDataDemultiplexer demultiplexer;
  private KaaDataMultiplexer multiplexer;
  private IpTransportInfo currentServer;
  private ScheduledExecutorService scheduler;

  private volatile Future<?> pollFuture;
  private volatile boolean stopped = true;
  private volatile boolean processingResponse = false;
  private volatile boolean taskPosted = false;
  private final CancelableCommandRunnable task = new CancelableCommandRunnable() {

    @Override
    protected void executeCommand() {
      if (!stopped) {
        taskPosted = false;
        synchronized (httpClientSetLock) {
          while (httpClient == null && !stopped && !Thread.currentThread().isInterrupted()) {
            try {
              httpClientSetLock.wait();
            } catch (InterruptedException ex) {
              break;
            }
          }
        }
        if (!stopped) {
          currentCommand = new PollCommand(httpClient, DefaultOperationsChannel.this,
                  getSupportedTransportTypes(), currentServer);
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
    }
  };
  private volatile boolean isShutdown = false;
  private volatile boolean isPaused = false;

  /**
   * All-args constructor.
   */
  public DefaultOperationsChannel(AbstractKaaClient client, KaaClientState state,
                                  FailoverManager failoverManager) {
    this.client = client;
    this.state = state;
    this.failoverManager = failoverManager;
  }

  protected ScheduledExecutorService createExecutor() {
    LOG.info("Creating a new executor for channel [{}]", getId());
    return new ScheduledThreadPoolExecutor(1) {
      @Override
      protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
                                                            RunnableScheduledFuture<V> task) {
        if (runnable instanceof CancelableRunnable) {
          return new CancelableScheduledFuture<V>((CancelableRunnable) runnable, task);
        }
        return super.decorateTask(runnable, task);
      }
    };
  }

  private void stopPollScheduler(boolean forced) {
    if (!stopped) {
      stopped = true;
      if (!processingResponse && pollFuture != null) {
        LOG.info("Stopping poll future..");
        pollFuture.cancel(forced);
        if (forced) {
          task.waitUntilExecuted();
        }
        LOG.info("Poll scheduler stopped");
      }
    }
  }

  private void startPoll() {
    if (!stopped) {
      stopPollScheduler(true);
    }
    if (scheduler == null) {
      scheduler = createExecutor();
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
  public LinkedHashMap<String, byte[]> createRequest(
          Map<TransportType, ChannelDirection> types) { // NOSONAR
    LinkedHashMap<String, byte[]> request = null;
    try {
      byte[] requestBodyRaw = multiplexer.compileRequest(types);
      synchronized (httpClientLock) {
        request = HttpRequestCreator.createOperationHttpRequest(requestBodyRaw,
                httpClient.getEncoderDecoder());
      }
    } catch (Exception ex) {
      LOG.error("Failed to create request {}", ex);
    }
    return request;
  }

  @Override
  public void onResponse(byte[] response) {
    LOG.debug("Response for channel [{}] received", getId());
    byte[] decodedResponse;
    try {
      processingResponse = true;
      synchronized (httpClientLock) {
        decodedResponse = httpClient.getEncoderDecoder().decodeData(response);
      }
      demultiplexer.processResponse(decodedResponse);
      processingResponse = false;
      failoverManager.onServerConnected(currentServer);
    } catch (Exception ex) {
      LOG.error("Failed to process response {}", Arrays.toString(response));
      LOG.error("Exception stack trace: ", ex);
    }
  }

  @Override
  public void onServerError(TransportConnectionInfo info) {
    if (!stopped) {
      LOG.debug("Channel [{}] connection failed", getId());
      synchronized (this) {
        stopPollScheduler(false);
      }
      failoverManager.onServerFailed(info, FailoverStatus.NO_CONNECTIVITY);
    } else {
      LOG.debug("Channel [{}] connection aborted", getId());
    }
  }

  @Override
  public synchronized void sync(TransportType type) {
    sync(Collections.singleton(type));
  }

  @Override
  public synchronized void sync(Set<TransportType> types) {
    if (isShutdown) {
      LOG.info("Can't sync. Channel [{}] is down", getId());
      return;
    }
    if (isPaused) {
      LOG.info("Can't sync. Channel [{}] is paused", getId());
      return;
    }
    if (multiplexer == null) {
      LOG.warn("Can't sync. Channel {} multiplexer is not set", getId());
      return;
    }
    if (demultiplexer == null) {
      LOG.warn("Can't sync. Channel {} demultiplexer is not set", getId());
      return;
    }
    if (currentServer == null) {
      LOG.warn("Can't sync. Server is null");
    }
    for (TransportType type : types) {
      LOG.info("Processing sync {} for channel [{}]", type, getId());
      if (getSupportedTransportTypes().get(type) == null) {
        LOG.error("Unsupported type {} for channel [{}]", type, getId());
        return;
      }
    }
    stopPoll();
    startPoll();
  }

  @Override
  public synchronized void syncAll() {
    if (isShutdown) {
      LOG.info("Can't sync. Channel [{}] is down", getId());
      return;
    }
    if (isPaused) {
      LOG.info("Can't sync. Channel [{}] is paused", getId());
      return;
    }
    LOG.info("Processing sync all for channel [{}]", getId());
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
  public void syncAck(TransportType type) {
    syncAck(Collections.singleton(type));
  }

  @Override
  public void syncAck(Set<TransportType> types) {
    LOG.info("Sync ack message is ignored for Channel {}", getId());
  }

  @Override
  public String getId() {
    return CHANNEL_ID;
  }

  @Override
  public TransportProtocolId getTransportProtocolId() {
    return TransportProtocolIdConstants.HTTP_TRANSPORT_ID;
  }

  @Override
  public ServerType getServerType() {
    return ServerType.OPERATIONS;
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
  public TransportConnectionInfo getServer() {
    return currentServer;
  }

  // TODO: refactor this as part of KAA-126
  @Override
  public synchronized void setServer(TransportConnectionInfo server) {
    if (isShutdown) {
      LOG.info("Can't set server. Channel [{}] is down", getId());
      return;
    }
    if (server != null) {
      if (!isPaused) {
        stopPoll();
      }
      this.currentServer = new IpTransportInfo(server);
      synchronized (httpClientLock) {
        LOG.debug("Channel [{}]: creating HTTP client..", getId());
        this.httpClient = client.createHttpClient(currentServer.getUrl() + "/EP/LongSync",
                state.getPrivateKey(), state.getPublicKey(), currentServer.getPublicKey());
        synchronized (httpClientSetLock) {
          httpClientSetLock.notifyAll();
        }
        LOG.debug("Channel [{}]: HTTP client created", getId());
      }
      if (!isPaused) {
        startPoll();
      }
    }
  }

  @Override
  public void setConnectivityChecker(ConnectivityChecker checker) {
    // Do nothing
  }

  @Override
  public synchronized void shutdown() {
    if (!isShutdown) {
      isShutdown = true;
      stopPoll();
      if (scheduler != null) {
        scheduler.shutdownNow();
      }
    }
  }

  @Override
  public synchronized void pause() {
    if (isShutdown) {
      LOG.info("Can't pause channel. Channel [{}] is down", getId());
      return;
    }
    if (!isPaused) {
      isPaused = true;
      stopPoll();
      if (scheduler != null) {
        scheduler.shutdownNow();
        scheduler = null;
      }
    }
  }

  @Override
  public synchronized void resume() {
    if (isShutdown) {
      LOG.info("Can't resume channel. Channel [{}] is down", getId());
      return;
    }
    if (isPaused) {
      isPaused = false;
      startPoll();
    }
  }

  @Override
  public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
    return SUPPORTED_TYPES;
  }

}
