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
package org.kaaproject.kaa.server.bootstrap.service.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.bootstrap.service.DefaultOperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class BootstrapThriftServiceImplTest {

    private static DefaultOperationsServerListService operationsListMock;
    private static BootstrapInitializationService bootstrapInitializationService;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        operationsListMock = mock(DefaultOperationsServerListService.class);
        bootstrapInitializationService = mock(BootstrapInitializationService.class);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#getServerShortName()}.
     */
    @Test
    public void testGetServerShortName() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        assertNotNull(b);
        assertEquals("bootstrap", b.getServerShortName());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.thrift.BootstrapThriftServiceImpl#onOperationsServerListUpdate(java.util.Map)}.
     */

    @Test
    public void testShutdown() {
        BootstrapThriftServiceImpl b = new BootstrapThriftServiceImpl();
        ReflectionTestUtils.setField(b, "bootstrapInitializationService", bootstrapInitializationService);

        try {
            b.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Mockito.verify(bootstrapInitializationService, Mockito.timeout(10000)).stop();
    }

}
