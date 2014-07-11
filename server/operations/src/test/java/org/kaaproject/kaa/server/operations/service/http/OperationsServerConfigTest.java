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
package org.kaaproject.kaa.server.operations.service.http;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.bootstrap.DefaultOperationsBootstrapService;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class OperationsServerConfigTest {

    @Test
    public void test() {
        DefaultOperationsBootstrapService ebs = mock(DefaultOperationsBootstrapService.class);
        OperationsNode en = mock(OperationsNode.class);
        
        OperationsServerConfig config = new OperationsServerConfig();
        assertNotNull(config);
        
        config.setOperationsBootstrapService(ebs);
        assertEquals(ebs, config.getOperationsBootstrapService());
        
        config.setStatisticsCalculationWindow(10101);
        assertEquals(10101, config.getStatisticsCalculationWindow());
        
        config.setStatisticsUpdateTimes(10);
        assertEquals(10, config.getStatisticsUpdateTimes());
        
        config.setZkNode(en);
        assertEquals(en, config.getZkNode());
    }

}
