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
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNode;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeNodes;

public class BalancingFlumeClientManagerTest extends FlumeClientManagerTest<FlumeNodes> {

    @Before
    public final void before() {
        flumeNodes = FlumeNodes
                .newBuilder()
                .setFlumeNodes(
                        Arrays.asList(new FlumeNode("localhost", 12121),
                                new FlumeNode("localhost", 12122))).build();
        configuration.setHostsBalancing(flumeNodes);
        configuration.setCallbackThreadPoolSize(2);
        configuration.setClientsThreadPoolSize(2);
        configuration.setExecutorThreadPoolSize(2);
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
        clientManager = FlumeClientManager.getInstance(configuration);
        clientManager.sendEventToFlume(null);
    }

    @Test(expected = FlumeException.class)
    public void initFlumeClientWithFlumeAgentAndOneHostTest() throws Exception {
        flumeSourceRunner.startFlumeSource("agent", "localhost", 12121);
        flumeNodes = FlumeNodes
                .newBuilder()
                .setFlumeNodes(
                        Arrays.asList(new FlumeNode("localhost", 12121))).build();
        configuration.setHostsBalancing(flumeNodes);
        clientManager = FlumeClientManager.getInstance(configuration);
        clientManager.sendEventToFlume(EventBuilder.withBody(testEventBody));
    }

    @Test(expected = RuntimeException.class)
    public void currentClientNullTest() {
        clientManager.init(null);
    }
}
