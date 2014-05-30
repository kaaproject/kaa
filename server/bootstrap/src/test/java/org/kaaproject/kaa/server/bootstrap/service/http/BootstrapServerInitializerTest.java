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
package org.kaaproject.kaa.server.bootstrap.service.http;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import io.netty.util.concurrent.EventExecutorGroup;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.server.common.http.server.CommandFactory;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class BootstrapServerInitializerTest {

    private static BootstrapConfig config;
    private static EventExecutorGroup executorMock;
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        config = new BootstrapConfig();
        executorMock = mock(EventExecutorGroup.class);
        List<String> cmdList = new LinkedList<String>();
        cmdList.add("org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand");
        config.setCommandList(cmdList);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.BootstrapServerInitializer#BootstrapServerInitializer(org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig, io.netty.util.concurrent.EventExecutorGroup)}.
     */
    @Test
    public void testBootstrapServerInitializerBootstrapConfigEventExecutorGroup() {
        BootstrapServerInitializer bsi = new BootstrapServerInitializer(config, executorMock);
        assertEquals(config, bsi.getConf());
        assertEquals(executorMock, bsi.getExecutor());
        try {
            assertNotNull(CommandFactory.getCommandProcessor(CommonBSConstants.BOOTSTRAP_RESOLVE_URI));
        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
