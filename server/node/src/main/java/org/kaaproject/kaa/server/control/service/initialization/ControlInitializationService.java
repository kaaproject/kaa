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

import org.kaaproject.kaa.server.control.service.loadmgmt.LoadDistributionService;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.kaaproject.kaa.server.node.service.initialization.AbstractInitializationService;
import org.kaaproject.kaa.server.node.service.initialization.InitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DefaultControlInitializationService.
 */
@Service
public class ControlInitializationService extends AbstractInitializationService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlInitializationService.class);

    /** The control zookeeper service. */
    @Autowired
    private ControlZkService controlZkService;

    /** Dynamic Load Distribution Service */
    @Autowired
    private LoadDistributionService loadMgmtService;
    
    /** The control zookeeper service. */
    @Autowired
    private InitializationService adminInitializationService;
    
    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.control.service.ControlService#start()
     */
    @Override
    public void start() {
        if (getNodeConfig().isZkEnabled()) {
            controlZkService.start();
            loadMgmtService.setZkService(controlZkService);
            loadMgmtService.start();
        }
        adminInitializationService.start();
        LOG.info("Control Service Started.");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.control.service.ControlService#stop()
     */
    @Override
    public void stop() {
        adminInitializationService.stop();
        if (getNodeConfig().isZkEnabled()) {
            loadMgmtService.shutdown();
            controlZkService.stop();
        }
        LOG.info("Control Service Stopped.");
    }

}
