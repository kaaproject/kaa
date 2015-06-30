/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import org.kaaproject.kaa.client.channel.*;
import org.kaaproject.kaa.client.channel.FailoverDecision.FailoverAction;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DefaultFailoverManager implements FailoverManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFailoverManager.class);

    // all timeout values are specified in seconds
    private static final long DEFAULT_FAILURE_RESOLUTION_TIMEOUT = 10;
    private static final long DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD = 2;
    private static final long DEFAULT_OPERATION_SERVERS_RETRY_PERIOD = 2;
    private static final long DEFAULT_NO_OPERATION_SERVERS_RETRY_PERIOD = 2;
    private static final long DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD = 5;
    private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

    private long failureResolutionTimeout;
    private long bootstrapServersRetryPeriod;
    private long operationsServersRetryPeriod;
    private long noOperationServersRetryPeriod;
    private long noConnectivityRetryPeriod;
    private TimeUnit timeUnit = DEFAULT_TIMEUNIT;

    private final KaaChannelManager channelManager;
    private final ExecutorContext context;
    private Map<ServerType, AccessPointIdResolution> resolutionProgressMap = new HashMap<>();

    public DefaultFailoverManager(KaaChannelManager channelManager, ExecutorContext context) {
        this(channelManager,
             context,
             DEFAULT_FAILURE_RESOLUTION_TIMEOUT,
             DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD,
             DEFAULT_OPERATION_SERVERS_RETRY_PERIOD,
             DEFAULT_NO_OPERATION_SERVERS_RETRY_PERIOD,
             DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD,
             DEFAULT_TIMEUNIT);
    }

    public DefaultFailoverManager(KaaChannelManager channelManager, ExecutorContext context,
                                  long failureResolutionTimeout, long bootstrapServersRetryPeriod,
                                  long operationsServersRetryPeriod, long noOperationServersRetryPeriod,
                                  long noConnectivityRetryPeriod, TimeUnit timeUnit) {
        this.channelManager = channelManager;
        this.context = context;
        this.failureResolutionTimeout = failureResolutionTimeout;
        this.bootstrapServersRetryPeriod = bootstrapServersRetryPeriod;
        this.operationsServersRetryPeriod = operationsServersRetryPeriod;
        this.noOperationServersRetryPeriod = noOperationServersRetryPeriod;
        this.noConnectivityRetryPeriod = noConnectivityRetryPeriod;
        this.timeUnit = timeUnit;
    }

    @Override
    public synchronized void onServerFailed(final TransportConnectionInfo connectionInfo) {
        LOG.trace("Server {} failed", connectionInfo);
        if (connectionInfo == null) {
            LOG.warn("Server connection info is null, can't resolve");
            return;
        }

        AccessPointIdResolution currentAccessPointIdResolution = resolutionProgressMap.get(connectionInfo.getServerType());
        if (currentAccessPointIdResolution != null) {
            if (currentAccessPointIdResolution.getAccessPointId() == connectionInfo.getAccessPointId()
                    && currentAccessPointIdResolution.getCurResolution() != null) {
                LOG.debug("Resolution is in progress for {} server", connectionInfo);
                return;
            } else {
                if (currentAccessPointIdResolution.getCurResolution() != null) {
                    LOG.trace("Cancelling old resolution: {}", currentAccessPointIdResolution.getCurResolution());
                    cancelCurrentFailResolution(currentAccessPointIdResolution);
                }
            }
        }

        LOG.trace("Next fail resolution will be available in {} s",
                        TimeUnit.SECONDS.convert(failureResolutionTimeout, timeUnit));

        Future<?> currentResolution = context.getScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                LOG.debug("Removing server {} from resolution map for type: {}", connectionInfo, connectionInfo.getServerType());
                resolutionProgressMap.remove(connectionInfo.getServerType());
            }
        }, failureResolutionTimeout, timeUnit);

        channelManager.onServerFailed(connectionInfo);

        AccessPointIdResolution newAccessPointIdResolution =
                new AccessPointIdResolution(connectionInfo.getAccessPointId(), currentResolution);
        resolutionProgressMap.put(connectionInfo.getServerType(), newAccessPointIdResolution);
    }

    @Override
    public synchronized void onServerChanged(TransportConnectionInfo connectionInfo) {
        LOG.trace("Server {} has changed", connectionInfo);
        if (connectionInfo == null) {
            LOG.warn("Server connection info is null, can't resolve");
            return;
        }

        AccessPointIdResolution currentAccessPointIdResolution = resolutionProgressMap.get(connectionInfo.getServerType());
        if (currentAccessPointIdResolution == null) {
            AccessPointIdResolution newResolution = new AccessPointIdResolution(connectionInfo.getAccessPointId(), null);
            resolutionProgressMap.put(connectionInfo.getServerType(), newResolution);
        } else if (currentAccessPointIdResolution.getAccessPointId() != connectionInfo.getAccessPointId()) {
            if (currentAccessPointIdResolution.getCurResolution() != null) {
                LOG.trace("Cancelling fail resolution: {}", currentAccessPointIdResolution);
                cancelCurrentFailResolution(currentAccessPointIdResolution);
            }
            AccessPointIdResolution newResolution = new AccessPointIdResolution(connectionInfo.getAccessPointId(), null);
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

        AccessPointIdResolution accessPointIdResolution = resolutionProgressMap.get(connectionInfo.getServerType());
        if (accessPointIdResolution == null) {
            LOG.warn("Server hasn't been set yet, so a new server: {} can't be connected", connectionInfo);
        } else if (accessPointIdResolution.getCurResolution() != null
                   && connectionInfo.getAccessPointId() == accessPointIdResolution.getAccessPointId()) {

            LOG.trace("Cancelling fail resolution: {}", accessPointIdResolution);
            cancelCurrentFailResolution(accessPointIdResolution);
        } else if (accessPointIdResolution.getCurResolution() != null) {
            LOG.debug("Connection for outdated accessPointId: {} was received, ignoring. The new accessPointId is: {}",
                    connectionInfo.getAccessPointId(), accessPointIdResolution.getAccessPointId());
        } else {
            LOG.trace("There is no current resolution in progress, connected to the same server: {}", connectionInfo);
        }
    }

    @Override
    public FailoverDecision onFailover(FailoverStatus failoverStatus) {
        switch(failoverStatus) {
            case NO_BOOTSTRAP_SERVERS:
                return new FailoverDecision(FailoverAction.RETRY, bootstrapServersRetryPeriod, timeUnit);
            case NO_OPERATION_SERVERS:
                return new FailoverDecision(FailoverAction.RETRY, noOperationServersRetryPeriod, timeUnit);
            case ALL_OPERATION_SERVERS_NA:
                return new FailoverDecision(FailoverAction.RETRY, operationsServersRetryPeriod, timeUnit);
            case NO_CONNECTIVITY:
                return new FailoverDecision(FailoverAction.RETRY, noConnectivityRetryPeriod, timeUnit);
            default:
                return new FailoverDecision(FailoverAction.NOOP);
        }
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
        public int accessPointId;
        public Future<?> curResolution;

        public AccessPointIdResolution(int accessPointId, Future<?> curResolution) {
            this.accessPointId = accessPointId;
            this.curResolution = curResolution;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AccessPointIdResolution that = (AccessPointIdResolution) o;

            if (accessPointId != that.accessPointId) {
                return false;
            }
            if (curResolution != null ? !curResolution.equals(that.curResolution) : that.curResolution != null) {
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
            return "AccessPointIdResolution{" +
                    "accessPointId=" + accessPointId +
                    ", curResolution=" + curResolution +
                    '}';
        }
    }
}
