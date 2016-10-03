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

package org.kaaproject.kaa.client.channel.impl;

import org.kaaproject.kaa.client.FailureListener;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.KaaInvalidChannelException;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.channel.impl.sync.SyncTask;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultChannelManager implements KaaInternalChannelManager {

  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(DefaultChannelManager.class);
  private final List<KaaDataChannel> channels = new LinkedList<>();
  private final Map<TransportType, KaaDataChannel> upChannels = new HashMap<>();
  private final BootstrapManager bootstrapManager;
  private final Map<TransportProtocolId, TransportConnectionInfo> lastServers = new HashMap<>();
  private final Map<TransportProtocolId, List<TransportConnectionInfo>> bootststrapServers;
  private final Map<TransportProtocolId, TransportConnectionInfo> lastBsServers = new HashMap<>();
  private final Map<String, BlockingQueue<SyncTask>> syncTaskQueueMap = new ConcurrentHashMap<>();
  private final Map<String, SyncWorker> syncWorkers = new HashMap<>();
  private FailureListener failureListener;
  private FailoverManager failoverManager;
  private ExecutorContext executorContext;

  private ConnectivityChecker connectivityChecker;
  private boolean isShutdown = false;
  private boolean isPaused = false;

  private KaaDataMultiplexer operationsMultiplexer;
  private KaaDataDemultiplexer operationsDemultiplexer;
  private KaaDataMultiplexer bootstrapMultiplexer;
  private KaaDataDemultiplexer bootstrapDemultiplexer;

  /**
   * All-args constructor.
   */
  public DefaultChannelManager(BootstrapManager manager, Map<TransportProtocolId,
      List<TransportConnectionInfo>> bootststrapServers, ExecutorContext executorContext,
                               FailureListener failureListener) {
    if (manager == null || bootststrapServers == null || bootststrapServers.isEmpty()) {
      throw new ChannelRuntimeException("Failed to create channel manager");
    }
    this.bootstrapManager = manager;
    this.bootststrapServers = bootststrapServers;
    this.executorContext = executorContext;
    this.failureListener = failureListener;
  }

  private boolean useChannelForType(KaaDataChannel channel, TransportType type) {
    ChannelDirection direction = channel.getSupportedTransportTypes().get(type);
    if (direction != null && (direction.equals(ChannelDirection.BIDIRECTIONAL)
            || direction.equals(ChannelDirection.UP))) {
      upChannels.put(type, channel);
      return true;
    }
    return false;
  }

  private void useNewChannelForType(TransportType type) {
    for (KaaDataChannel channel : channels) {
      if (useChannelForType(channel, type)) {
        return;
      }
    }
    upChannels.put(type, null);
  }

  private void applyNewChannel(KaaDataChannel channel) {
    for (TransportType type : channel.getSupportedTransportTypes().keySet()) {
      useChannelForType(channel, type);
    }
  }

  private void replaceAndRemoveChannel(KaaDataChannel channel) {
    channels.remove(channel);
    for (Map.Entry<TransportType, KaaDataChannel> entry : upChannels.entrySet()) {
      if (entry.getValue() == channel) {
        useNewChannelForType(entry.getKey());
      }
    }
    stopWorker(channel);
    channel.shutdown();
  }

  private void addChannelToList(KaaDataChannel channel) {
    if (!channels.contains(channel)) {
      channel.setConnectivityChecker(connectivityChecker);
      channels.add(channel);
      startWorker(channel);
      TransportConnectionInfo server;
      if (channel.getServerType() == ServerType.BOOTSTRAP) {
        server = getCurrentBootstrapServer(channel.getTransportProtocolId());
      } else {
        server = lastServers.get(channel.getTransportProtocolId());
      }
      if (server != null) {
        LOG.debug("Applying server {} for channel [{}] type {}",
                server, channel.getId(), channel.getTransportProtocolId());
        channel.setServer(server);
        if (failoverManager != null) {
          failoverManager.onServerChanged(server);
        } else {
          LOG.warn("Failover manager isn't set: null");
        }
      } else {
        if (lastServers != null && lastServers.isEmpty()) {
          if (channel.getServerType() == ServerType.BOOTSTRAP) {
            LOG.warn("Failed to find bootstrap service for channel [{}] type {}", channel.getId(),
                channel.getTransportProtocolId());
          } else {
            LOG.info("Failed to find operations service for channel [{}] type {}", channel.getId(),
                channel.getTransportProtocolId());
          }
        } else {
          LOG.debug("list of services is empty for channel [{}] type {}",
                  channel.getId(), channel.getTransportProtocolId());
        }
      }
    }
  }

  @Override
  public synchronized void setChannel(TransportType transport, KaaDataChannel channel)
          throws KaaInvalidChannelException {
    if (isShutdown) {
      LOG.warn("Can't set a channel. Channel manager is down");
      return;
    }
    if (channel != null) {
      if (!useChannelForType(channel, transport)) {
        throw new KaaInvalidChannelException("Unsupported transport type " + transport.toString()
                + " for channel \"" + channel.getId() + "\"");
      }
      if (isPaused) {
        channel.pause();
      }
      addChannelToList(channel);
    }
  }

  @Override
  public synchronized void addChannel(KaaDataChannel channel) {
    if (isShutdown) {
      LOG.warn("Can't add a channel. Channel manager is down");
      return;
    }
    if (channel != null) {
      if (ServerType.BOOTSTRAP == channel.getServerType()) {
        channel.setMultiplexer(bootstrapMultiplexer);
        channel.setDemultiplexer(bootstrapDemultiplexer);
      } else {
        channel.setMultiplexer(operationsMultiplexer);
        channel.setDemultiplexer(operationsDemultiplexer);
      }
      if (isPaused) {
        channel.pause();
      }
      addChannelToList(channel);
      applyNewChannel(channel);
    }
  }

  @Override
  public synchronized void removeChannel(KaaDataChannel channel) {
    replaceAndRemoveChannel(channel);
  }

  @Override
  public synchronized void removeChannel(String id) {
    for (KaaDataChannel channel : channels) {
      if (channel.getId().equals(id)) {
        replaceAndRemoveChannel(channel);
        return;
      }
    }
  }

  @Override
  public synchronized List<KaaDataChannel> getChannels() {
    return new LinkedList<>(channels);
  }

  @Override
  public TransportConnectionInfo getActiveServer(TransportType type) {
    KaaDataChannel channel = upChannels.get(type);
    if (channel == null) {
      return null;
    }
    return channel.getServer();
  }

  private KaaDataChannel getChannel(TransportType type) {
    KaaDataChannel result = upChannels.get(type);
    if (result == null) {
      LOG.error("Failed to find channel for transport {}", type);
      throw new ChannelRuntimeException("Failed to find channel for transport " + type.toString());
    }
    return result;
  }

  @Override
  public synchronized KaaDataChannel getChannel(String id) {
    for (KaaDataChannel channel : channels) {
      if (channel.getId().equals(id)) {
        return channel;
      }
    }
    return null;
  }


  @Override
  public void sync(TransportType type) {
    sync(type, false, false);
  }

  private void sync(TransportType type, boolean ack, boolean all) {
    LOG.debug("Lookup channel by type {}", type);
    KaaDataChannel channel = getChannel(type);
    BlockingQueue<SyncTask> queue = syncTaskQueueMap.get(channel.getId());
    if (queue != null) {
      queue.offer(new SyncTask(type, ack, all));
    } else {
      LOG.warn("Can't find queue for channel [{}]", channel.getId());
    }
  }

  @Override
  public void syncAck(TransportType type) {
    sync(type, true, false);
  }

  @Override
  public void syncAll(TransportType type) {
    sync(type, false, true);
  }


  @Override
  public synchronized void onTransportConnectionInfoUpdated(TransportConnectionInfo newServer) {
    LOG.debug("Transport connection info updated for server: {}", newServer);

    if (isShutdown) {
      LOG.warn("Can't process server update. Channel manager is down");
      return;
    }
    if (newServer.getServerType() == ServerType.OPERATIONS) {
      LOG.info("Adding new operations service: {}", newServer);
      lastServers.put(newServer.getTransportId(), newServer);
    }

    for (KaaDataChannel channel : channels) {
      if (channel.getServerType() == newServer.getServerType()
          && channel.getTransportProtocolId().equals(newServer.getTransportId())) {
        LOG.debug("Applying server {} for channel [{}] type {}",
            newServer, channel.getId(), channel.getTransportProtocolId());
        channel.setServer(newServer);
        if (failoverManager != null) {
          failoverManager.onServerChanged(newServer);
        } else {
          LOG.warn("Failover manager isn't set: null");
        }
      }
    }
  }

  @Override
  public synchronized void onServerFailed(final TransportConnectionInfo server,
                                          FailoverStatus status) {
    if (isShutdown) {
      LOG.warn("Can't process server failure. Channel manager is down");
      return;
    }

    if (server.getServerType() == ServerType.BOOTSTRAP) {
      final TransportConnectionInfo nextConnectionInfo = getNextBootstrapServer(server);
      if (nextConnectionInfo != null) {
        LOG.trace("Using next bootstrap service");
        FailoverDecision decision = failoverManager.onFailover(
                FailoverStatus.CURRENT_BOOTSTRAP_SERVER_NA);
        switch (decision.getAction()) {
          case NOOP:
            LOG.warn("No operation is performed according to failover strategy decision");
            break;
          case RETRY:
            long retryPeriod = decision.getRetryPeriod();
            LOG.warn("Attempt to reconnect to the current bootstrap service will be made in {} ms, "
                    + "according to failover strategy decision", retryPeriod);
            executorContext.getScheduledExecutor().schedule(new Runnable() {
              @Override
              public void run() {
                onTransportConnectionInfoUpdated(server);
              }
            }, retryPeriod, TimeUnit.MILLISECONDS);
            break;
          case USE_NEXT_BOOTSTRAP:
            retryPeriod = decision.getRetryPeriod();
            LOG.warn("Attempt to connect to the next bootstrap service will be made in {} ms, "
                    + "according to failover strategy decision", retryPeriod);
            executorContext.getScheduledExecutor().schedule(new Runnable() {
              @Override
              public void run() {
                onTransportConnectionInfoUpdated(nextConnectionInfo);
              }
            }, retryPeriod, TimeUnit.MILLISECONDS);
            break;
          case FAILURE:
            LOG.warn("Calling failure listener according to failover strategy decision!");
            failureListener.onFailure();
            break;
          default:
            break;
        }
      } else {
        LOG.trace("Can't find next bootstrap service");
        FailoverDecision decision = failoverManager.onFailover(status);
        switch (decision.getAction()) {
          case NOOP:
            LOG.warn("No operation is performed according to failover strategy decision");
            break;
          case RETRY:
            long retryPeriod = decision.getRetryPeriod();
            LOG.warn("Attempt to reconnect to first bootstrap service will be made in {} ms, "
                    + "according to failover strategy decision", retryPeriod);
            executorContext.getScheduledExecutor().schedule(new Runnable() {
              @Override
              public void run() {
                onTransportConnectionInfoUpdated(server);
              }
            }, retryPeriod, TimeUnit.MILLISECONDS);
            break;
          case FAILURE:
            LOG.warn("Calling failure listener according to failover strategy decision!");
            failureListener.onFailure();
            break;
          default:
            break;
        }
      }
    } else {
      bootstrapManager.useNextOperationsServer(server.getTransportId(), status);
    }
  }

  @Override
  public synchronized void clearChannelList() {
    channels.clear();
    upChannels.clear();
  }

  private TransportConnectionInfo getCurrentBootstrapServer(TransportProtocolId type) {
    TransportConnectionInfo bsi = lastBsServers.get(type);
    if (bsi == null) {
      List<TransportConnectionInfo> serverList = bootststrapServers.get(type);
      if (serverList != null && !serverList.isEmpty()) {
        bsi = serverList.get(0);
        lastBsServers.put(type, bsi);
      }
    }

    return bsi;
  }

  private TransportConnectionInfo getNextBootstrapServer(TransportConnectionInfo currentServer) {
    TransportConnectionInfo bsi = null;

    List<TransportConnectionInfo> serverList = bootststrapServers.get(
            currentServer.getTransportId());
    int serverIndex = serverList.indexOf(currentServer);

    if (serverIndex >= 0) {
      if (++serverIndex == serverList.size()) {
        serverIndex = 0;
      }
      bsi = serverList.get(serverIndex);
      lastBsServers.put(currentServer.getTransportId(), bsi);
    }

    return bsi;
  }

  @Override
  public void setConnectivityChecker(ConnectivityChecker checker) {
    if (isShutdown) {
      LOG.warn("Can't set connectivity checker. Channel manager is down");
      return;
    }
    connectivityChecker = checker;
    for (KaaDataChannel channel : channels) {
      channel.setConnectivityChecker(connectivityChecker);
    }
  }

  @Override
  public synchronized void shutdown() {
    if (!isShutdown) {
      isShutdown = true;
      for (KaaDataChannel channel : channels) {
        channel.shutdown();
      }
      for (SyncWorker worker : syncWorkers.values()) {
        worker.shutdown();
      }
    }
  }

  @Override
  public synchronized void pause() {
    if (isShutdown) {
      LOG.warn("Can't pause. Channel manager is down");
      return;
    }
    if (!isPaused) {
      isPaused = true;
      for (KaaDataChannel channel : upChannels.values()) {
        channel.pause();
      }
    }
  }

  @Override
  public synchronized void resume() {
    if (isShutdown) {
      LOG.warn("Can't resume. Channel manager is down");
      return;
    }
    if (isPaused) {
      isPaused = false;
      for (KaaDataChannel channel : upChannels.values()) {
        channel.resume();
      }
    }
  }

  @Override
  public void setOperationMultiplexer(KaaDataMultiplexer multiplexer) {
    this.operationsMultiplexer = multiplexer;
  }

  @Override
  public void setOperationDemultiplexer(KaaDataDemultiplexer demultiplexer) {
    this.operationsDemultiplexer = demultiplexer;
  }

  @Override
  public void setBootstrapMultiplexer(KaaDataMultiplexer multiplexer) {
    this.bootstrapMultiplexer = multiplexer;
  }

  @Override
  public void setBootstrapDemultiplexer(KaaDataDemultiplexer demultiplexer) {
    this.bootstrapDemultiplexer = demultiplexer;
  }


  private void startWorker(KaaDataChannel channel) {
    stopWorker(channel);
    SyncWorker worker = new SyncWorker(channel);
    syncTaskQueueMap.put(channel.getId(), new LinkedBlockingQueue<SyncTask>());
    syncWorkers.put(channel.getId(), worker);
    worker.start();
  }

  private void stopWorker(KaaDataChannel channel) {
    BlockingQueue<SyncTask> skippedTasks = syncTaskQueueMap.remove(channel.getId());
    if (skippedTasks != null) {
      for (SyncTask task : skippedTasks) {
        LOG.info("Task skipped due to worker shutdown: {}", task);
      }
    }
    SyncWorker worker = syncWorkers.remove(channel.getId());
    if (worker != null) {
      LOG.debug("[{}] stopping worker", channel.getId());
      worker.shutdown();
    }
  }

  public void setFailoverManager(FailoverManager failoverManager) {
    this.failoverManager = failoverManager;
  }

  private class SyncWorker extends Thread {
    private final KaaDataChannel channel;
    private volatile boolean stop;

    private SyncWorker(KaaDataChannel channel) {
      super();
      this.channel = channel;
    }

    @Override
    public void run() {
      LOG.debug("[{}] Worker started", channel.getId());
      while (!stop) {
        try {
          BlockingQueue<SyncTask> taskQueue = syncTaskQueueMap.get(channel.getId());
          SyncTask task = taskQueue.take();
          List<SyncTask> additionalTasks = new ArrayList<SyncTask>();
          if (taskQueue.drainTo(additionalTasks) > 0) {
            LOG.debug("[{}] Merging task {} with {}", channel.getId(), task, additionalTasks);
            task = SyncTask.merge(task, additionalTasks);
          }
          if (task.isAll()) {
            LOG.debug("[{}] Going to invoke syncAll method for types {}",
                    channel.getId(), task.getTypes());
            channel.syncAll();
          } else if (task.isAckOnly()) {
            LOG.debug("[{}] Going to invoke syncAck method for types {}",
                    channel.getId(), task.getTypes());
            channel.syncAck(task.getTypes());
          } else {
            LOG.debug("[{}] Going to invoke sync method", channel.getId());
            channel.sync(task.getTypes());
          }
        } catch (InterruptedException ex) {
          if (stop) {
            LOG.debug("[{}] Worker is interrupted.", channel.getId());
          } else {
            LOG.warn("[{}] Worker is interrupted.", channel.getId(), ex);
          }
        }
      }
      LOG.debug("[{}] Worker stopped", channel.getId());
    }

    public void shutdown() {
      this.stop = true;
      this.interrupt();
    }
  }
}
