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

/**
 * 
 */
package org.kaaproject.kaa.server.operations.service.statistics;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.server.common.http.server.Track;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.http.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.http.commands.SyncCommand;
import org.kaaproject.kaa.server.operations.service.statistics.StatisticsService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class StatisticsServiceTest {

    private static OperationsServerConfig configMock;
    private static OperationsNode zkNodeMock;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configMock = mock(OperationsServerConfig.class);
        when(configMock.getStatisticsCalculationWindow()).thenReturn((long) 5);
        when(configMock.getStatisticsUpdateTimes()).thenReturn(5);
        
        zkNodeMock = mock(OperationsNode.class);
        when(configMock.getZkNode()).thenReturn(zkNodeMock);
        
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#start()}.
     */
    @Test
    public void testStart() {
        StatisticsService service = StatisticsService.getService();
        assertNotNull(service);
        StatisticsService.setConfig(configMock);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        assertTrue(service.isAlive());
        try {
            verify(zkNodeMock, atLeastOnce()).updateNodeStatsValues(0, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        service.shutdown();
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#shutdown()}.
     */
    @Test
    public void testShutdown() {
        StatisticsService service = StatisticsService.getService();
        assertNotNull(service);
        StatisticsService.setConfig(configMock);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        assertTrue(service.isAlive());
        service.shutdown();
        assertTrue(!service.isAlive());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#getService()}.
     */
    @Test
    public void testGetService() {
        StatisticsService service = StatisticsService.getService();
        assertNotNull(service);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#newSession(java.util.UUID)}.
     */
    @Test
    @Ignore("unstable")
    public void testNewSession() {
        StatisticsService service = StatisticsService.getService();
        assertNotNull(service);
        StatisticsService.setConfig(configMock);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        Map<UUID, Map<Track, List<Integer>>> sessions = new HashMap<>();
        for (int k = 0; k < 6; k++) {
            for (int i = 0; i < 100; i++) {
                UUID uuid = UUID.randomUUID();
                Track track = service.newSession(uuid);
                Map<Track, List<Integer>> requests = new HashMap<>();
                requests.put(track, new LinkedList<Integer>());
                sessions.put(uuid, requests);
                for (int j = 0; j < 100; j++) {
                    int id = track.newRequest(SyncCommand.getCommandName());
                    sessions.get(uuid).get(track).add(new Integer(id));
                }
            }
            
            for(UUID uuid : sessions.keySet()) {
                for(Track track : sessions.get(uuid).keySet()) {
                    for(Integer id : sessions.get(uuid).get(track)) {
                        track.setProcessTime(id.intValue(), 20);
                        track.closeRequest(id.intValue());
                    }
                }
                
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
            assertTrue(service.isAlive());
            
            for(UUID uuid : sessions.keySet()) {
                service.closeSession(uuid);
            }
            
            sessions.clear();
        }
        
        try {
            verify(zkNodeMock, atLeastOnce()).updateNodeStatsValues(0, 0, 0);
            verify(zkNodeMock, times(1)).updateNodeStatsValues(10, 5000, 50);
            verify(zkNodeMock, times(1)).updateNodeStatsValues(13, 6666, 66);
            verify(zkNodeMock, times(1)).updateNodeStatsValues(15, 7500, 75);
            verify(zkNodeMock, times(1)).updateNodeStatsValues(16, 8000, 80);
            verify(zkNodeMock, atLeastOnce()).updateNodeStatsValues(20, 10000, 100);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
        service.shutdown();
    }


}
