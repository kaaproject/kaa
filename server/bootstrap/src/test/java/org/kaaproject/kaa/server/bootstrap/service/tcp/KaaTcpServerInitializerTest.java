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
package org.kaaproject.kaa.server.bootstrap.service.tcp;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;

/**
 * @author Andrey Panasenko
 *
 */
public class KaaTcpServerInitializerTest {

    private static EventExecutorGroup executor;
    
    @BeforeClass
    public static void initClass() {
        executor = new DefaultEventExecutorGroup(3);
    }
    
    @AfterClass
    public static void deInitClass() {
        try {
            Future<? extends Object> f = executor.shutdownGracefully(250, 1000, TimeUnit.MILLISECONDS);
            f.await();
        } catch (InterruptedException e) {
            fail(e.toString() + " Error: ");
        } finally {
            executor = null;
        }
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.KaaTcpServerInitializer#getExecutor()}.
     */
    @Test
    public void testGetExecutor() {
        KaaTcpServerInitializer init = new KaaTcpServerInitializer();
        assertNotNull(init);
        init.setExecutor(executor);
        assertEquals(executor, init.getExecutor());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.KaaTcpServerInitializer#setExecutor(io.netty.util.concurrent.EventExecutorGroup)}.
     */
    @Test
    public void testSetExecutor() {
        KaaTcpServerInitializer init = new KaaTcpServerInitializer();
        assertNotNull(init);
        init.setExecutor(executor);
        assertEquals(executor, init.getExecutor());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.KaaTcpServerInitializer#getOperatonsServerListService()}.
     */
    @Test
    public void testGetOperatonsServerListService() {
        KaaTcpServerInitializer init = new KaaTcpServerInitializer();
        assertNotNull(init);
        OperationsServerListService opListService = new OperationsServerListService();
        init.setOperatonsServerListService(opListService );
        assertEquals(opListService, init.getOperatonsServerListService());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.KaaTcpServerInitializer#setOperatonsServerListService(org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService)}.
     */
    @Test
    public void testSetOperatonsServerListService() {
        KaaTcpServerInitializer init = new KaaTcpServerInitializer();
        assertNotNull(init);
        OperationsServerListService opListService = new OperationsServerListService();
        init.setOperatonsServerListService(opListService );
        assertEquals(opListService, init.getOperatonsServerListService());
    }

}
