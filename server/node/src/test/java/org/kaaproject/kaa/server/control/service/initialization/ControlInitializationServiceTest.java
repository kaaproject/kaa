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

package org.kaaproject.kaa.server.control.service.initialization;

import org.junit.Test;
import org.kaaproject.kaa.server.control.service.admin.AdminInitializationService;
import org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.kaaproject.kaa.server.node.service.initialization.InitializationService;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class ControlInitializationServiceTest.
 */
public class ControlInitializationServiceTest {

    private ControlZkService zkService;
    private LoadDistributionService loadMgmtService;
    private InitializationService adminInitializationService;
    
    /**
     * Test control initialization service start.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlInitializationServiceStart() throws Exception {
        ControlInitializationService controlInitializationService = controlInitializationServiceSpy();
        controlInitializationService.start();
        
        Mockito.verify(zkService).start();
        Mockito.verify(loadMgmtService).start();
        Mockito.verify(adminInitializationService).start();
    }

    /**
     * Test control initialization service stop.
     *
     * @throws Exception the exception
     */
    @Test
    public void testControlInitializationServiceStop() throws Exception {
        ControlInitializationService controlInitializationService = controlInitializationServiceSpy();
        controlInitializationService.start();
        controlInitializationService.stop();
        
        Mockito.verify(zkService).start();
        Mockito.verify(loadMgmtService).start();
        Mockito.verify(adminInitializationService).start();

        Mockito.verify(zkService).stop();
        Mockito.verify(loadMgmtService).shutdown();
        Mockito.verify(adminInitializationService).stop();
    }
    
    /**
     * created stubbed control initialization service.
     *
     * @return the ControlInitializationService
     * @throws Exception the exception
     */
    private ControlInitializationService controlInitializationServiceSpy() throws Exception {
        
        ControlInitializationService controlInitializationService = Mockito.spy(new ControlInitializationService());
        
        KaaNodeServerConfig kaaNodeServerConfig = new KaaNodeServerConfig();
        kaaNodeServerConfig.setZkEnabled(true);
        ReflectionTestUtils.setField(controlInitializationService, "kaaNodeServerConfig", kaaNodeServerConfig);
        
        zkService = Mockito.mock(ControlZkService.class);
        loadMgmtService = Mockito.mock(LoadDistributionService.class);
        adminInitializationService = Mockito.mock(AdminInitializationService.class);
        
        ReflectionTestUtils.setField(controlInitializationService, "controlZkService", zkService);
        
        ReflectionTestUtils.setField(controlInitializationService, "loadMgmtService", loadMgmtService);
        
        ReflectionTestUtils.setField(controlInitializationService, "adminInitializationService", adminInitializationService);
        
        return controlInitializationService;
    }
}
