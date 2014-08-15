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

package org.kaaproject.kaa.server.operations.service.logs.flume.client;

import org.apache.flume.EventDeliveryException;
import org.apache.flume.FlumeException;
import org.apache.flume.event.EventBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeBalancingTypeDto;

public class PriorityFlumeClientManagerTest extends FlumeClientManagerTest {

    @Before
    public final void before() throws Exception {
        parametersDto.setBalancingType(FlumeBalancingTypeDto.PRIORITIZED);
        parametersDto.setHosts(generateHosts(1));
    }

    @After
    public final void after() throws Exception {
    }

    @Test(expected = FlumeException.class)
    public void initFlumeClientWithoutFlumeAgentTest() {
        FlumeClientManager.getInstance(parametersDto);
    }

    @Test
    public void initFlumeClientWithFlumeAgentTest() throws Exception {
        install = StagedInstall.getInstance();
        install.startAgent("agent", CONFIG_FILE_PRCCLIENT_TEST);
        clientManager = FlumeClientManager.getInstance(parametersDto);
        clientManager.sendEventToFlume(EventBuilder.withBody(testEventBody));
    }

    @Test(expected = EventDeliveryException.class)
    public void initFlumeClientWithFlumeAgentAndEmptyEventTest() throws Exception {
        StagedInstall install = StagedInstall.getInstance();
        install.startAgent("agent", CONFIG_FILE_PRCCLIENT_TEST);
        clientManager = new PriorityFlumeClientManager();
        clientManager.init(parametersDto);
        clientManager.sendEventToFlume(null);
    }

}
