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
package org.kaaproject.kaa.server.bootstrap.service.config;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Andrey Panasenko
 *
 */
public class OperationsServerConfigTest {

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getStatisticsCalculationWindow()}.
     */
    @Test
    public void testGetStatisticsCalculationWindow() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setStatisticsCalculationWindow(111);
        assertEquals(111, config.getStatisticsCalculationWindow());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getStatisticsUpdateTimes()}.
     */
    @Test
    public void testGetStatisticsUpdateTimes() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setStatisticsUpdateTimes(222);
        assertEquals(222,config.getStatisticsUpdateTimes());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getThriftHost()}.
     */
    @Test
    public void testGetThriftHost() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setThriftHost("asdcasdc");
        assertEquals("asdcasdc", config.getThriftHost());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getThriftPort()}.
     */
    @Test
    public void testGetThriftPort() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setThriftPort(2323);
        assertEquals(2323, config.getThriftPort());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#isZkEnabled()}.
     */
    @Test
    public void testIsZkEnabled() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setZkEnabled(true);
        assertEquals(true, config.isZkEnabled());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getZkHostPortList()}.
     */
    @Test
    public void testGetZkHostPortList() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setZkHostPortList("asdcasdc");
        assertEquals("asdcasdc", config.getZkHostPortList());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getZkMaxRetryTime()}.
     */
    @Test
    public void testGetZkMaxRetryTime() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setZkMaxRetryTime(111);
        assertEquals(111,config.getZkMaxRetryTime());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#getZkSleepTime()}.
     */
    @Test
    public void testGetZkSleepTime() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setZkSleepTime(222);
        assertEquals(222,config.getZkSleepTime());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.config.OperationsServerConfig#isZkIgnoreErrors()}.
     */
    @Test
    public void testIsZkIgnoreErrors() {
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        config.setZkIgnoreErrors(true);
        assertEquals(true, config.isZkIgnoreErrors());
    }

}
