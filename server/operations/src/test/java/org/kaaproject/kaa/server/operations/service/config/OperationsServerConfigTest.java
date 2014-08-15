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
package org.kaaproject.kaa.server.operations.service.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Andrey Panasenko
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/config-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class OperationsServerConfigTest {

    protected static final Logger LOG = LoggerFactory.getLogger(OperationsServerConfigTest.class);

    @Autowired
    private OperationsServerConfig config;

    @Test
    public void ConfigFillParamsTest() {
        assertNotNull(config);

        assertEquals(300, config.getStatisticsCalculationWindow());
        assertEquals(60, config.getStatisticsUpdateTimes());
        assertEquals("localhost", config.getThriftHost());
        assertEquals(10091, config.getThriftPort());
        assertEquals(false, config.isZkEnabled());
        assertEquals("localhost:2181", config.getZkHostPortList());
        assertEquals(3000, config.getZkMaxRetryTime());
        assertEquals(1000, config.getZkSleepTime());
        assertEquals(true, config.isZkIgnoreErrors());
    }
}
