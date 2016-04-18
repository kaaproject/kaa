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

package org.kaaproject.kaa.server.appenders.flume.appender.client;

import java.util.Arrays;

import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.event.EventBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.appenders.flume.config.gen.PrioritizedFlumeNode;
import org.kaaproject.kaa.server.appenders.flume.config.gen.PrioritizedFlumeNodes;

public class PriorityFlumeClientManagerTest extends FlumeClientManagerTest<PrioritizedFlumeNodes> {

    @Before
    public final void before() throws Exception {
        flumeNodes = PrioritizedFlumeNodes
                .newBuilder()
                .setFlumeNodes(
                        Arrays.asList(new PrioritizedFlumeNode("localhost", 12121, 1),
                                      new PrioritizedFlumeNode("localhost", 12122, 2))).build();
        configuration.setHostsBalancing(flumeNodes);
        configuration.setCallbackThreadPoolSize(2);
        configuration.setClientsThreadPoolSize(2);
        configuration.setExecutorThreadPoolSize(2);
    }

    @After
    public final void after() throws Exception {
    }

    @Test(expected = FlumeException.class)
    public void initFlumeClientWithoutFlumeAgentTest() {
        FlumeClientManager.getInstance(configuration);
    }

    @Test
    public void initFlumeClientWithFlumeAgentTest() throws Exception {
        flumeSourceRunner.startFlumeSource("agent", "localhost", 12121);
        clientManager = FlumeClientManager.getInstance(configuration);
        clientManager.sendEventToFlume(EventBuilder.withBody(testEventBody));
    }
    
    @Test
    public void initFlumeClientWithFlumeAgentAsyncTest() throws Exception {
        flumeSourceRunner.startFlumeSource("agent", "localhost", 12121);
        clientManager = FlumeClientManager.getInstance(configuration);
        clientManager.sendEventToFlumeAsync(EventBuilder.withBody(testEventBody));
    }

    @Test(expected = EventDeliveryException.class)
    public void initFlumeClientWithFlumeAgentAndEmptyEventTest() throws Exception {
        flumeSourceRunner.startFlumeSource("agent", "localhost", 12121);
        clientManager = new PriorityFlumeClientManager();
        clientManager.init(flumeNodes);
        clientManager.sendEventToFlume(null);
    }

}
