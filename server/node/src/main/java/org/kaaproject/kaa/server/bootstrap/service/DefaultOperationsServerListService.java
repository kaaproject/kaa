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

package org.kaaproject.kaa.server.bootstrap.service;

import static org.kaaproject.kaa.server.common.zk.ServerNameUtil.getNameFromConnectionInfo;

import org.kaaproject.kaa.server.common.zk.ServerNameUtil;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolVersionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * OperationsServerListService Class. Receive new Operations Servers list form
 * Thrift service and create AVRP object EndPointServerList
 *
 * @author Andrey Panasenko
 * @author Andrey Shvayka
 */
@Service
public class DefaultOperationsServerListService
    implements OperationsServerListService, OperationsNodeListener {
  private static final Logger LOG =
      LoggerFactory.getLogger(DefaultOperationsServerListService.class);
  private Map<String, OperationsNodeInfo> opsMap;
  private Memorizer<List<ProtocolVersionId>, Set<ProtocolConnectionData>> cache;
  private Object listenerLock = new Object();

  /**
   * Default constructor.
   */
  public DefaultOperationsServerListService() {
    opsMap = new ConcurrentHashMap<String, OperationsNodeInfo>();
    cache = new Memorizer<List<ProtocolVersionId>, Set<ProtocolConnectionData>>(
        new Computable<List<ProtocolVersionId>, Set<ProtocolConnectionData>>() {

          @Override
          public Set<ProtocolConnectionData> compute(List<ProtocolVersionId> protocolVersions)
              throws InterruptedException {
            return filterProtocolInstances(protocolVersions);
          }

        });
  }

  /**
   * Bean init-method.
   *
   * @param zkNode the bootstrap node
   */
  public void init(BootstrapNode zkNode) {
    LOG.info("Initializing with {}", zkNode);
    opsMap.clear();
    synchronized (listenerLock) {
      LOG.info("Registering as listener to ZK updates");
      zkNode.addListener(this);
      LOG.info("Adding existing nodes");
      for (OperationsNodeInfo info : zkNode.getCurrentOperationServerNodes()) {
        addNode(info);
      }
      LOG.info("Added existing nodes");
    }
  }

  @Override
  public Set<ProtocolConnectionData> filter(List<ProtocolVersionId> keys) {
    try {
      return cache.compute(keys);
    } catch (InterruptedException ex) {
      LOG.info("Failed to filter protocols", ex);
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void onNodeAdded(OperationsNodeInfo nodeInfo) {
    synchronized (listenerLock) {
      addNode(nodeInfo);
    }
  }

  @Override
  public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
    synchronized (listenerLock) {
      addNode(nodeInfo);
    }
  }

  @Override
  public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
    synchronized (listenerLock) {
      removeNode(nodeInfo);
    }
  }

  private void addNode(OperationsNodeInfo info) {
    LOG.info("Add/Update node {}", info);
    opsMap.put(getNameFromConnectionInfo(info.getConnectionInfo()), info);
    LOG.info("Cleanup cached responses");
    cache.clear();
  }

  private void removeNode(OperationsNodeInfo info) {
    if (opsMap.remove(getNameFromConnectionInfo(info.getConnectionInfo())) != null) {
      LOG.info("Removed node {}", info);
    } else {
      LOG.warn("Failed to remove node {}", info);
    }
    LOG.info("Cleanup cached responses");
    cache.clear();
  }

  protected Set<ProtocolConnectionData> filterProtocolInstances(List<ProtocolVersionId> keys) {
    Set<ProtocolConnectionData> result = new HashSet<>();
    for (ProtocolVersionId key : keys) {
      for (OperationsNodeInfo node : opsMap.values()) {
        for (TransportMetaData md : node.getTransports()) {
          if (md.getId() == key.getProtocolId() && md.getMinSupportedVersion() <= key.getVersion()
              && key.getVersion() <= md.getMaxSupportedVersion()) {
            result.addAll(toProtocolConnectionData(node, md, key.getVersion()));
          }
        }
      }
    }
    return result;
  }

  private Set<ProtocolConnectionData> toProtocolConnectionData(OperationsNodeInfo node,
                                                               TransportMetaData md,
                                                               int version) {
    byte[] connectionData = null;
    Set<ProtocolConnectionData> result = new HashSet<>();
    for (VersionConnectionInfoPair pair : md.getConnectionInfo()) {
      if (version == pair.getVersion()) {
        result.add(new ProtocolConnectionData(ServerNameUtil.crc32(node.getConnectionInfo()),
            new ProtocolVersionId(md.getId(), version),
            pair.getConenctionInfo().array())
        );

      }
    }

    return result;
  }


  public interface Computable<A, V> {
    V compute(A arg) throws InterruptedException;
  }

  public class Memorizer<A, V> implements Computable<A, V> {
    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
    private final Computable<A, V> computable;

    public Memorizer(Computable<A, V> computable) {
      this.computable = computable;
    }

    /**
     * Compute argument by field <code>compute</code>.
     *
     * @param arg the argument
     * @return computing result
     */
    public V compute(final A arg) throws InterruptedException {
      while (true) {
        Future<V> future = cache.get(arg);
        if (future == null) {
          Callable<V> eval = new Callable<V>() {
            public V call() throws InterruptedException {
              return computable.compute(arg);
            }
          };
          FutureTask<V> ft = new FutureTask<V>(eval);
          future = cache.putIfAbsent(arg, ft);
          if (future == null) {
            future = ft;
            ft.run();
          }
        }
        try {
          return future.get();
        } catch (CancellationException ex) {
          LOG.error("Cancellation exception exception ", ex);
          cache.remove(arg, future);
        } catch (ExecutionException ex) {
          LOG.error("Cache execution exception ", ex);
        }
      }
    }

    public void clear() {
      cache.clear();
    }
  }

}
