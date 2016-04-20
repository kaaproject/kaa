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

/**
 * 
 */
package org.kaaproject.kaa.server.control.service.loadmgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.EndpointCountRebalancer;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class DynamicLoadManagerTest {

    private static LoadDistributionService ldServiceMock;
    private static ControlZkService zkServiceMock;
    private static ControlNode pNodeMock;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ldServiceMock = mock(LoadDistributionService.class);
        zkServiceMock = mock(ControlZkService.class);
        pNodeMock = mock(ControlNode.class);
        when(ldServiceMock.getOpsServerHistoryTTL()).thenReturn(300);
        when(ldServiceMock.getRebalancer()).thenReturn(new EndpointCountRebalancer());
        when(ldServiceMock.getZkService()).thenReturn(zkServiceMock);
        when(zkServiceMock.getControlZKNode()).thenReturn(pNodeMock);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#DynamicLoadManager(org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService)}.
     */
    @Test
    public void testDynamicLoadManager() {
        DynamicLoadManager dm = new DynamicLoadManager(ldServiceMock);
        assertNotNull(dm);
        assertNotNull(dm.getLoadDistributionService());
        assertNotNull(dm.getDynamicRebalancer());
        verify(ldServiceMock, atLeast(1)).getOpsServerHistoryTTL();
        assertEquals(300000, dm.getOpsServerHistoryTTL());
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#recalculate()}.
     */
    @Test
    public void testRecalculate() {
        DynamicLoadManager dm = new DynamicLoadManager(ldServiceMock);
        assertNotNull(dm);
        dm.recalculate();
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#registerListeners()}.
     */
    @Test
    public void testRegisterListeners() {
        DynamicLoadManager dm = new DynamicLoadManager(ldServiceMock);
        assertNotNull(dm);
        dm.registerListeners();
        
        verify(pNodeMock, atLeast(1)).addListener((OperationsNodeListener)dm);
        //verify(pNodeMock, times(1)).addListener((BootstrapNodeListener)dm);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#deregisterListeners()}.
     */
    @Ignore
    @Test
    public void testDeregisterListeners() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeAddedBootstrapNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeUpdatedBootstrapNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeRemovedBootstrapNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.EndpointNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeAddedEndpointNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.EndpointNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeUpdatedEndpointNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.EndpointNodeInfo)}.
     */
    @Ignore
    @Test
    public void testOnNodeRemovedEndpointNodeInfo() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#getLoadDistributionService()}.
     */
    @Ignore
    @Test
    public void testGetLoadDistributionService() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#setLoadDistributionService(org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService)}.
     */
    @Ignore
    @Test
    public void testSetLoadDistributionService() {
        fail("Not yet implemented");
    }


    /**
     * Test methods for {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#setOpsServerHistoryTTL(long)}.
     * and {@link org.kaaproject.kaa.server.control.service.loadmgmt.DynamicLoadManager#getOpsServerHistoryTTL(long)}
     */
    @Test
    public void testEndpointHistoryTTL() {
        DynamicLoadManager dm = new DynamicLoadManager(ldServiceMock);
        assertNotNull(dm);
        long opsServerHistoryTTL = 123456;
        dm.setOpsServerHistoryTTL(opsServerHistoryTTL );
        assertEquals(opsServerHistoryTTL, dm.getOpsServerHistoryTTL());
    }

}
