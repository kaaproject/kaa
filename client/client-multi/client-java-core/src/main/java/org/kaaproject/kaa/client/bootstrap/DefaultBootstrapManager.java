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

package org.kaaproject.kaa.client.bootstrap;

import org.kaaproject.kaa.client.FailureListener;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.GenericTransportInfo;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Default {@link BootstrapManager} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultBootstrapManager implements BootstrapManager {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapManager.class);
  private final Map<TransportProtocolId, List<ProtocolMetaData>> mappedOperationServerList =
          new HashMap<>();
  private final Map<TransportProtocolId, Iterator<ProtocolMetaData>> mappedIterators =
          new HashMap<>();
  private FailureListener failureListener;
  private BootstrapTransport transport;
  private List<ProtocolMetaData> operationsServerList;
  private KaaInternalChannelManager channelManager;
  private FailoverManager failoverManager;
  private Integer serverToApply;
  private ExecutorContext executorContext;

  /**
   * All-args constructor.
   */
  public DefaultBootstrapManager(BootstrapTransport transport, ExecutorContext executorContext,
                                 FailureListener failureListener) {
    this.transport = transport;
    this.executorContext = executorContext;
    this.failureListener = failureListener;
  }

  @Override
  public void receiveOperationsServerList() throws TransportException {
    LOG.debug("Going to invoke sync method of assigned transport");
    transport.sync();
  }

  @Override
  public void useNextOperationsServer(TransportProtocolId transportId, FailoverStatus status) {
    if (mappedOperationServerList != null && !mappedOperationServerList.isEmpty()) {
      if (mappedIterators.get(transportId).hasNext()) {
        ProtocolMetaData nextOperationsServer = mappedIterators.get(transportId).next();
        LOG.debug("New server [{}] will be user for [{}]",
            nextOperationsServer.getAccessPointId(), transportId);

        if (channelManager != null) {
          channelManager.onTransportConnectionInfoUpdated(
              new GenericTransportInfo(ServerType.OPERATIONS, nextOperationsServer));
        } else {
          LOG.error("Can not process server change. Channel manager was not specified");
        }
      } else {
        LOG.warn("Failed to find server for channel [{}]", transportId);
        resolveFailoverStatus(status);
      }
    } else {
      throw new BootstrapRuntimeException("Operations Server list is empty");
    }
  }

  @Override
  public synchronized void setTransport(BootstrapTransport transport) {
    this.transport = transport;
  }

  @Override
  public synchronized void useNextOperationsServerByAccessPointId(int accessPointId) {
    List<ProtocolMetaData> servers = getTransportsByAccessPointId(accessPointId);
    if (servers != null && !servers.isEmpty()) {
      notifyChannelManagerAboutServer(servers);
    } else {
      serverToApply = accessPointId;
      transport.sync();
    }
  }

  private void notifyChannelManagerAboutServer(List<ProtocolMetaData> transports) {
    for (ProtocolMetaData transport : transports) {
      LOG.debug("Applying new transport {}", transports);
      channelManager.onTransportConnectionInfoUpdated(new GenericTransportInfo(
              ServerType.OPERATIONS, transport));
    }
  }

  private List<ProtocolMetaData> getTransportsByAccessPointId(int accessPointId) {
    if (operationsServerList == null || operationsServerList.isEmpty()) {
      throw new BootstrapRuntimeException("Operations Server list is empty");
    }
    List<ProtocolMetaData> result = new ArrayList<>();
    for (ProtocolMetaData transport : operationsServerList) {
      if (transport.getAccessPointId().intValue() == accessPointId) {
        result.add(transport);
      }
    }
    return result;
  }

  @Override
  public synchronized void setChannelManager(KaaInternalChannelManager manager) {
    this.channelManager = manager;
  }

  @Override
  public synchronized void setFailoverManager(FailoverManager failoverManager) {
    this.failoverManager = failoverManager;
  }

  @Override
  public synchronized void onProtocolListUpdated(List<ProtocolMetaData> list) {
    LOG.trace("Protocol list was updated");
    operationsServerList = list;
    mappedOperationServerList.clear();
    mappedIterators.clear();

    if (operationsServerList == null || operationsServerList.isEmpty()) {
      LOG.trace("Received empty operations service list");
      resolveFailoverStatus(FailoverStatus.NO_OPERATION_SERVERS_RECEIVED);
      return;
    }

    for (ProtocolMetaData server : operationsServerList) {
      TransportProtocolId transportId = new TransportProtocolId(
              server.getProtocolVersionInfo().getId(),
              server.getProtocolVersionInfo().getVersion());
      List<ProtocolMetaData> servers = mappedOperationServerList.get(transportId);
      if (servers == null) {
        servers = new LinkedList<>();
        mappedOperationServerList.put(transportId, servers);
      }
      servers.add(server);
    }
    for (Map.Entry<TransportProtocolId, List<ProtocolMetaData>> entry : mappedOperationServerList
            .entrySet()) {
      Collections.shuffle(entry.getValue());
      mappedIterators.put(entry.getKey(), entry.getValue().iterator());
    }
    if (serverToApply != null) {
      List<ProtocolMetaData> servers = getTransportsByAccessPointId(serverToApply);
      if (servers != null && !servers.isEmpty()) {
        notifyChannelManagerAboutServer(servers);
        serverToApply = null;
      }
    } else {
      for (Map.Entry<TransportProtocolId, Iterator<ProtocolMetaData>> entry : mappedIterators
              .entrySet()) {
        TransportConnectionInfo info = new GenericTransportInfo(ServerType.OPERATIONS,
                entry.getValue().next());
        channelManager.onTransportConnectionInfoUpdated(info);
      }
    }
  }

  private void resolveFailoverStatus(FailoverStatus status) {
    FailoverDecision decision = failoverManager.onFailover(status);
    switch (decision.getAction()) {
      case NOOP:
        LOG.warn("No operation is performed according to failover strategy decision");
        break;
      case RETRY:
        long retryPeriod = decision.getRetryPeriod();
        LOG.warn("Attempt to receive operations service list will be made in {} ms, "
                 + "according to failover strategy decision", retryPeriod);
        executorContext.getScheduledExecutor().schedule(new Runnable() {
          @Override
          public void run() {
            try {
              receiveOperationsServerList();
            } catch (TransportException ex) {
              LOG.error("Error while receiving operations service list", ex);
            }
          }
        }, retryPeriod, TimeUnit.MILLISECONDS);
        break;
      case USE_NEXT_BOOTSTRAP:
        LOG.warn("Trying to switch to the next bootstrap service according to failover strategy "
                 + "decision");
        retryPeriod = decision.getRetryPeriod();
        failoverManager.onServerFailed(channelManager.getActiveServer(TransportType.BOOTSTRAP),
                status);
        executorContext.getScheduledExecutor().schedule(new Runnable() {
          @Override
          public void run() {
            try {
              receiveOperationsServerList();
            } catch (TransportException ex) {
              LOG.error("Error while receiving operations service list", ex);
            }
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
}
