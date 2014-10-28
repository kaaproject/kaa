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
package org.kaaproject.kaa.server.common.server.statistics;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.server.common.http.server.NettyHttpServerIT;
import org.kaaproject.kaa.server.common.server.Config;
import org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater;
import org.kaaproject.kaa.server.common.server.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(StatisticsServiceTest.class);
    
    private static Config configMock;
    private static StatisticsNodeUpdater updaterMock;
    
    private static HashMap<ChannelType, StatisticsServiceContainer> statServices;
    
    public class Updater implements StatisticsNodeUpdater {

        public int updateCount = 0;
        public int averageProcessedRequests;
        public int averageOnlineSessions;
        public int averageDeltaSync;
        
        private Object sync = new Object();
        
        public Updater() {
            updateCount = 0;
            averageProcessedRequests = 0;
            averageOnlineSessions = 0;
            averageDeltaSync = 0;
        }
        /* (non-Javadoc)
         * @see org.kaaproject.kaa.server.common.server.StatisticsNodeUpdater#setStatistics(int, int, int)
         */
        @Override
        public void setStatistics(int averageProcessedRequests, int averageOnlineSessions, int averageDeltaSync) {
            this.averageDeltaSync = averageDeltaSync;
            this.averageOnlineSessions = averageOnlineSessions;
            this.averageProcessedRequests = averageProcessedRequests;
            this.updateCount++;
            synchronized (sync) {
                sync.notify();
            }
        }
        
        public void waitUpdate() {
            synchronized (sync) {
                try {
                    int uc = updateCount;
                    sync.wait(2000);
                    if (uc == updateCount) {
                        fail("Update wait failed, timeout exception");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail("Update wait failed");
                }
            }
        }
        
    }
    
    public class StatisticsServiceContainer {
        public ChannelType type;
        public StatisticsService service;
        public Updater updater;
        
        public StatisticsServiceContainer(ChannelType type, Config config) {
            this.type = type;
            this.updater = new Updater();
            this.service = new StatisticsService(type, config, updater);
            this.service.start();
        }
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        configMock = mock(Config.class);
        when(configMock.getStatisticsCalculationWindow()).thenReturn((long) 5);
        when(configMock.getStatisticsUpdateTimes()).thenReturn(5);
        statServices = new HashMap<>();
        
    }

    
    
    @After
    public void afterTest(){
        for(StatisticsServiceContainer serviceContainer : statServices.values()) {
            serviceContainer.service.shutdown();
            assertTrue(!serviceContainer.service.isAlive());
        }
        statServices.clear();
    }

    @Before
    public void beforeTest(){
        statServices.put(ChannelType.HTTP, new StatisticsServiceContainer(ChannelType.HTTP, configMock));
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#start()}.
     */
    @Test
    public void testStart() {
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        for(StatisticsServiceContainer serviceContainer : statServices.values()) {
            assertNotNull(serviceContainer.service);
            assertTrue(serviceContainer.service.isAlive());
            if (serviceContainer.updater.updateCount <= 0) {
                fail("Updater not work for service "+serviceContainer.type.toString());
            }
        }
    }


    /**
     * Test method for {@link org.kaaproject.kaa.server.operations.service.statistics.StatisticsService#newSession(java.util.UUID)}.
     */
    @Test
    public void testNewSession() {
        for(StatisticsServiceContainer serviceContainer : statServices.values()) {
            testSession(serviceContainer);
        }
    }


    private void testSession(StatisticsServiceContainer container) {
        Map<UUID, Map<Track, List<Integer>>> sessions = new HashMap<>();
        container.updater.waitUpdate();
        int initUpdateCount = container.updater.updateCount;
        logger.info("InitUpdateCount {}", initUpdateCount);
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i < 100; i++) {
                UUID uuid = UUID.randomUUID();
                Track track = container.service.newSession(uuid);
                Map<Track, List<Integer>> requests = new HashMap<>();
                requests.put(track, new LinkedList<Integer>());
                sessions.put(uuid, requests);
                for (int j = 0; j < 100; j++) {
                    int id = track.newRequest();
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
            
            container.updater.waitUpdate();
            
            logger.info("Updater: updateCount {}, averageDeltaSync {}, averageOnlineSessions {}, averageProcessedRequests {}.", 
                    container.updater.updateCount,
                    container.updater.averageDeltaSync,
                    container.updater.averageOnlineSessions,
                    container.updater.averageProcessedRequests);
            
            if (container.updater.updateCount == (1+initUpdateCount) ) {
                assertEquals(10, container.updater.averageDeltaSync);
                assertEquals(25, container.updater.averageOnlineSessions);
                assertEquals(5000, container.updater.averageProcessedRequests);
            } else if (container.updater.updateCount == (2+initUpdateCount)) {
                assertEquals(13, container.updater.averageDeltaSync);
                assertEquals(33, container.updater.averageOnlineSessions);
                assertEquals(6666, container.updater.averageProcessedRequests);
            } else if (container.updater.updateCount == (3+initUpdateCount)) {
                assertEquals(15, container.updater.averageDeltaSync);
                assertEquals(38, container.updater.averageOnlineSessions);
                assertEquals(7500, container.updater.averageProcessedRequests);
            } else if (container.updater.updateCount == (4+initUpdateCount)) {
                assertEquals(16, container.updater.averageDeltaSync);
                assertEquals(40, container.updater.averageOnlineSessions);
                assertEquals(8000, container.updater.averageProcessedRequests);
            } else if (container.updater.updateCount == (5+initUpdateCount)) {
                assertEquals(20, container.updater.averageDeltaSync);
                assertEquals(50, container.updater.averageOnlineSessions);
                assertEquals(10000, container.updater.averageProcessedRequests);
            } else {
                fail("Incorrect update value "+container.updater.updateCount);
            }
            
            for(UUID uuid : sessions.keySet()) {
                container.service.closeSession(uuid);
            }
    
            sessions.clear();
        }
    }
}
