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

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.failover.strategies.DefaultFailoverStrategy;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultFailoverManagerTest {
    private static final int RESOLUTION_TIMEOUT_MS = 500;
    private static final int BOOTSTRAP_RETRY_PERIOD = 100;

    private KaaChannelManager channelManager;
    private Map<ServerType, DefaultFailoverManager.AccessPointIdResolution> resolutionProgressMap;
    private DefaultFailoverManager failoverManager;
    private ExecutorContext context;

    @Before
    public void setUp() {
        channelManager = Mockito.mock(KaaChannelManager.class);
        context = Mockito.mock(ExecutorContext.class);
        Mockito.when(context.getScheduledExecutor()).thenReturn(Executors.newScheduledThreadPool(1));
        FailoverStrategy failoverStrategy = new DefaultFailoverStrategy(BOOTSTRAP_RETRY_PERIOD, 1, 1, TimeUnit.MILLISECONDS);
        failoverManager = new DefaultFailoverManager(channelManager, context, failoverStrategy, RESOLUTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        resolutionProgressMap = Mockito.spy(new HashMap<ServerType, DefaultFailoverManager.AccessPointIdResolution>());
        ReflectionTestUtils.setField(failoverManager, "resolutionProgressMap", resolutionProgressMap);
    }

    @Test
    public void onServerConnectedTest() {
        TransportConnectionInfo transportConnectionInfo = mockForTransportConnectionInfo(1);
        failoverManager.onServerConnected(transportConnectionInfo);

        failoverManager.onServerChanged(transportConnectionInfo);
        failoverManager.onServerFailed(transportConnectionInfo, FailoverStatus.NO_CONNECTIVITY);
        DefaultFailoverManager.AccessPointIdResolution accessPointIdResolutionSpy = spyForResolutionMap(transportConnectionInfo);
        failoverManager.onServerConnected(transportConnectionInfo);
        Mockito.verify(accessPointIdResolutionSpy, Mockito.times(1)).setCurResolution(Mockito.any(Future.class));
    }

    @Test
    public void onServerChangedTest() {
        TransportConnectionInfo info = mockForTransportConnectionInfo(1);

        failoverManager.onServerChanged(null);
        Mockito.verify(resolutionProgressMap, Mockito.never()).put(Mockito.any(ServerType.class),
                Mockito.any(DefaultFailoverManager.AccessPointIdResolution.class));

        failoverManager.onServerChanged(info);
        Mockito.verify(resolutionProgressMap, Mockito.times(1)).put(info.getServerType(),
                new DefaultFailoverManager.AccessPointIdResolution(info.getAccessPointId(), null));

        TransportConnectionInfo info2 = mockForTransportConnectionInfo(2);
        failoverManager.onServerFailed(info, FailoverStatus.NO_CONNECTIVITY);
        DefaultFailoverManager.AccessPointIdResolution accessPointIdResolutionSpy = spyForResolutionMap(info);
        failoverManager.onServerChanged(info2);
        Mockito.verify(accessPointIdResolutionSpy, Mockito.times(1)).setCurResolution(null);
        Mockito.verify(resolutionProgressMap, Mockito.times(1)).put(info2.getServerType(),
                new DefaultFailoverManager.AccessPointIdResolution(info2.getAccessPointId(), null));
    }

    @Test
    public void onServerFailedTest() throws InterruptedException {
        TransportConnectionInfo info = mockForTransportConnectionInfo(1);

        failoverManager.onServerFailed(null, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(resolutionProgressMap, Mockito.never()).put(Mockito.any(ServerType.class),
                Mockito.any(DefaultFailoverManager.AccessPointIdResolution.class));

        failoverManager.onServerFailed(info, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(channelManager, Mockito.times(1)).onServerFailed(info, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(context, Mockito.times(1)).getScheduledExecutor();

        ArgumentCaptor<DefaultFailoverManager.AccessPointIdResolution> argument =
                ArgumentCaptor.forClass(DefaultFailoverManager.AccessPointIdResolution.class);
        Mockito.verify(resolutionProgressMap, Mockito.times(1)).put(Mockito.eq(info.getServerType()), argument.capture());
        assertEquals(argument.getValue().getAccessPointId(), info.getAccessPointId());
        assertNotNull(argument.getValue().getCurResolution());
        Mockito.verify(resolutionProgressMap, Mockito.timeout(RESOLUTION_TIMEOUT_MS * 2).times(1)).remove(info.getServerType());

        TransportConnectionInfo info2 = mockForTransportConnectionInfo(2, ServerType.BOOTSTRAP);
        failoverManager.onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);
        final DefaultFailoverManager.AccessPointIdResolution accessPointIdResolutionSpy = spyForResolutionMap(info2);
        failoverManager.onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(accessPointIdResolutionSpy, Mockito.never()).setCurResolution(null);

        info2 = mockForTransportConnectionInfo(3, ServerType.BOOTSTRAP);
        failoverManager.onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(channelManager, Mockito.times(1)).onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);

        Mockito.reset(accessPointIdResolutionSpy);
        failoverManager.onFailover(FailoverStatus.BOOTSTRAP_SERVERS_NA);
        failoverManager.onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);
        Mockito.verify(accessPointIdResolutionSpy, Mockito.never()).setCurResolution(null);
        Thread.sleep(BOOTSTRAP_RETRY_PERIOD * 2);
        failoverManager.onServerFailed(info2, FailoverStatus.NO_CONNECTIVITY);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Mockito.verify(accessPointIdResolutionSpy, Mockito.timeout(BOOTSTRAP_RETRY_PERIOD * 2).times(1)).setCurResolution(null);
            }
        }).start();
    }

    private TransportConnectionInfo mockForTransportConnectionInfo(int accessPointId) {
        return mocksForTransportConnectionInfo(ServerType.OPERATIONS, accessPointId)[0];
    }

    private TransportConnectionInfo mockForTransportConnectionInfo(int accessPointId, ServerType serverType) {
        return mocksForTransportConnectionInfo(serverType, accessPointId)[0];
    }

    private TransportConnectionInfo[] mocksForTransportConnectionInfo(ServerType serverType, int... accessPointIds) {
        TransportConnectionInfo[] transportConnections = new TransportConnectionInfo[accessPointIds.length];
        for (int i = 0; i < accessPointIds.length; i++) {
            TransportConnectionInfo connectionInfo = Mockito.mock(TransportConnectionInfo.class);
            Mockito.when(connectionInfo.getAccessPointId()).thenReturn(accessPointIds[i]);
            Mockito.when(connectionInfo.getServerType()).thenReturn(serverType);
            transportConnections[i] = connectionInfo;
        }
        return transportConnections;
    }

    private DefaultFailoverManager.AccessPointIdResolution spyForResolutionMap(TransportConnectionInfo info) {
        DefaultFailoverManager.AccessPointIdResolution simplePointIdResolution = resolutionProgressMap.get(info.getServerType());
        DefaultFailoverManager.AccessPointIdResolution spyPointIdResolution = Mockito.spy(simplePointIdResolution);
        resolutionProgressMap.put(info.getServerType(), spyPointIdResolution);
        return spyPointIdResolution;
    }
}
