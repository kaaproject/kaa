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
package org.kaaproject.kaa.server.bootstrap.service.statistics;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.Test;

/**
 * @author Andrey Panasenko
 *
 */
public class StatisticsServiceTest {

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.statistics.StatisticsService#StatisticsService()}.
     */
    @Test
    public void testStatisticsService() {
        StatisticsService service = new StatisticsService();
        assertNotNull(service);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.statistics.StatisticsService#newSession(java.util.UUID)}.
     */
    @Test
    public void testNewSession() {
        StatisticsService service = new StatisticsService();
        assertNotNull(service);
        service.newSession(UUID.randomUUID());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.statistics.StatisticsService#closeSession(java.util.UUID)}.
     */
    @Test
    public void testCloseSession() {
        StatisticsService service = new StatisticsService();
        assertNotNull(service);
        service.closeSession(UUID.randomUUID());
    }

}
