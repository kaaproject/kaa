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

package org.kaaproject.kaa.client.channel.failover;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultFailoverManager implements FailoverManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultFailoverManager.class);

  private static final long DEFAULT_FAILURE_RESOLUTION_TIMEOUT = 10;
  private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
  private final KaaChannelManager channelManager;
  private final ExecutorContext context;
  private long failureResolutionTimeout;
  private TimeUnit timeUnit = DEFAULT_TIME_UNIT;
  private FailoverStrategy failoverStrategy;
  private Map<ServerType, AccessPointIdResolution> resolutionProgressMap = new HashMap<>();

  public DefaultFailoverManager(KaaChannelManager channelManager, ExecutorContext context) {
    this(channelManager, context, new DefaultFailoverStrategy(),
            DEFAULT_FAILURE_RESOLUTION_TIMEOUT, DEFAULT_TIME_UNIT);
  }

  /**
   * All-args constructor.
   */
  public DefaultFailoverManager(KaaChannelManager channelManager,
                                ExecutorContext context,
                                FailoverStrategy failoverStrategy,
                                long failureResolutionTimeout,
                                TimeUnit timeUnit) {
    this.channelManager = channelManager;
    this.context = context;
    this.failoverStrategy = failoverStrategy;
    this.failureResolutionTimeout = failureResolutionTimeout;
    this.timeUnit = timeUnit;
  }

  @Override
  public synchronized void onServerFailed(final TransportConnectionInfo connectionInfo,
                                          FailoverStatus status) {
    if (connectionInfo == null) {
      LOG.warn("Server failed, but connection info is null, can't resolve");
      return;
    } else {
      LOG.info("Server [{}, {}] failed", connectionInfo.getServerType(),
              connectionInfo.getAccessPointId());
    }

    long currentResolutionTime = -1;
    AccessPointIdResolution currentAccessPointIdResolution = resolutionProgressMap.get(
            connectionInfo.getServerType());
    if (currentAccessPointIdResolution != null) {
      currentResolutionTime = currentAccessPointIdResolution.getResolutionTime();
      if (currentAccessPointIdResolution.getAccessPointId() == connectionInfo.getAccessPointId()
          && currentAccessPointIdResolution.getCurResolution() != null
          && System.currentTimeMillis() < currentResolutionTime) {
        LOG.debug("Resolution is in progress for {} server", connectionInfo);
        return;
      } else {
        if (currentAccessPointIdResolution.getCurResolution() != null) {
          LOG.trace("Cancelling old resolution: {}",
                  currentAccessPointIdResolution.getCurResolution());
          cancelCurrentFailResolution(currentAccessPointIdResolution);
        }
      }
    }

    LOG.trace("Next fail resolution will be available in {} {}",
            failureResolutionTimeout, timeUnit.toString());

    Future<?> currentResolution = context.getScheduledExecutor().schedule(new Runnable() {
      @Override
      public void run() {
        LOG.debug("Removing server {} from resolution map for type: {}",
                connectionInfo, connectionInfo.getServerType());
        resolutionProgressMap.remove(connectionInfo.getServerType());
      }
    }, failureResolutionTimeout, timeUnit);

    channelManager.onServerFailed(connectionInfo, status);

    long updatedResolutionTime = currentAccessPointIdResolution
            != null ? currentAccessPointIdResolution.getResolutionTime() : currentResolutionTime;

    AccessPointIdResolution newAccessPointIdResolution =
        new AccessPointIdResolution(connectionInfo.getAccessPointId(), currentResolution);

    if (updatedResolutionTime != currentResolutionTime) {
      newAccessPointIdResolution.setResolutionTime(updatedResolutionTime);
    }

    resolutionProgressMap.put(connectionInfo.getServerType(), newAccessPointIdResolution);
  }

  @Override
  public synchronized void onServerChanged(TransportConnectionInfo connectionInfo) {
    if (connectionInfo == null) {
      LOG.warn("Server has changed, but its connection info is null, can't resolve");
      return;
    } else {
      LOG.trace("Server [{}, {}] has changed",
              connectionInfo.getServerType(), connectionInfo.getAccessPointId());
    }

    AccessPointIdResolution currentAccessPointIdResolution = resolutionProgressMap.get(
            connectionInfo.getServerType());
    if (currentAccessPointIdResolution == null) {
      AccessPointIdResolution newResolution = new AccessPointIdResolution(
              connectionInfo.getAccessPointId(), null);
      resolutionProgressMap.put(connectionInfo.getServerType(), newResolution);
    } else if (currentAccessPointIdResolution.getAccessPointId()
            != connectionInfo.getAccessPointId()) {
      if (currentAccessPointIdResolution.getCurResolution() != null) {
        LOG.trace("Cancelling fail resolution, as server [{}] has changed: {}",
                connectionInfo, currentAccessPointIdResolution);
        cancelCurrentFailResolution(currentAccessPointIdResolution);
      }
      AccessPointIdResolution newResolution = new AccessPointIdResolution(
              connectionInfo.getAccessPointId(), null);
      resolutionProgressMap.put(connectionInfo.getServerType(), newResolution);
    } else {
      LOG.debug("Same server [{}] is used, nothing has changed", connectionInfo);
    }
  }

  @Override
  public synchronized void onServerConnected(TransportConnectionInfo connectionInfo) {
    LOG.trace("Server {} has connected", connectionInfo);
    if (connectionInfo == null) {
      LOG.warn("Server connection info is null, can't resolve");
      return;
    }

    failoverStrategy.onRecover(connectionInfo);

    AccessPointIdResolution accessPointIdResolution = resolutionProgressMap.get(
            connectionInfo.getServerType());
    if (accessPointIdResolution == null) {
      LOG.trace("Server hasn't been set yet (failover resolution has happened), so a new server: "
              + "{} can't be connected", connectionInfo);
    } else if (accessPointIdResolution.getCurResolution() != null
        && connectionInfo.getAccessPointId() == accessPointIdResolution.getAccessPointId()) {

      LOG.trace("Cancelling fail resolution: {}", accessPointIdResolution);
      cancelCurrentFailResolution(accessPointIdResolution);
    } else if (accessPointIdResolution.getCurResolution() != null) {
      LOG.debug("Connection for outdated accessPointId: {} was received, ignoring. "
                      + "The new accessPointId is: {}",
          connectionInfo.getAccessPointId(), accessPointIdResolution.getAccessPointId());
    } else {
      LOG.trace("There is no current resolution in progress, connected to the same server: {}",
              connectionInfo);
    }
  }

  @Override
  public void setFailoverStrategy(FailoverStrategy failoverStrategy) {
    if (failoverStrategy == null) {
      throw new IllegalArgumentException("Failover strategy can't be null");
    }

    this.failoverStrategy = failoverStrategy;
  }

  @Override
  public synchronized FailoverDecision onFailover(FailoverStatus failoverStatus) {
    AccessPointIdResolution accessPointIdResolution = null;
    long resolutionTime = System.currentTimeMillis();
    switch (failoverStatus) {
      case BOOTSTRAP_SERVERS_NA:
      case CURRENT_BOOTSTRAP_SERVER_NA:
        accessPointIdResolution = resolutionProgressMap.get(ServerType.BOOTSTRAP);
        resolutionTime += failoverStrategy.getTimeUnit().toMillis(
                failoverStrategy.getBootstrapServersRetryPeriod());
        break;
      case NO_OPERATION_SERVERS_RECEIVED:
        accessPointIdResolution = resolutionProgressMap.get(ServerType.BOOTSTRAP);
        break;
      case OPERATION_SERVERS_NA:
        accessPointIdResolution = resolutionProgressMap.get(ServerType.OPERATIONS);
        resolutionTime += failoverStrategy.getTimeUnit().toMillis(
                failoverStrategy.getOperationServersRetryPeriod());
        break;
      default:
        break;
    }
    if (accessPointIdResolution != null) {
      accessPointIdResolution.setResolutionTime(resolutionTime);
    }

    return failoverStrategy.onFailover(failoverStatus);
  }

  private void cancelCurrentFailResolution(AccessPointIdResolution accessPointIdResolution) {
    if (accessPointIdResolution.getCurResolution() != null) {
      accessPointIdResolution.getCurResolution().cancel(true);
      accessPointIdResolution.setCurResolution(null);
    } else {
      LOG.trace("Current resolution is null, can't cancel");
    }
  }

  static class AccessPointIdResolution {
    private int accessPointId;
    private long resolutionTimeMillis;
    private Future<?> curResolution;

    public AccessPointIdResolution(int accessPointId, Future<?> curResolution) {
      this.accessPointId = accessPointId;
      this.curResolution = curResolution;
      this.resolutionTimeMillis = Long.MAX_VALUE;
    }

    public int getAccessPointId() {
      return accessPointId;
    }

    public Future<?> getCurResolution() {
      return curResolution;
    }

    public void setCurResolution(Future<?> curResolution) {
      this.curResolution = curResolution;
    }

    public long getResolutionTime() {
      return resolutionTimeMillis;
    }

    public void setResolutionTime(long resolutionTimeMillis) {
      this.resolutionTimeMillis = resolutionTimeMillis;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      AccessPointIdResolution that = (AccessPointIdResolution) obj;

      if (accessPointId != that.accessPointId) {
        return false;
      }
      if (curResolution != null ? !curResolution.equals(that.curResolution) : that.curResolution
              != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = accessPointId;
      result = 31 * result + (curResolution != null ? curResolution.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "AccessPointIdResolution{"
              + "accessPointId=" + accessPointId
              + ", resolutionTime=" + resolutionTimeMillis
              + ", curResolution=" + curResolution
              + '}';
    }
  }
}
