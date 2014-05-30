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

package org.kaaproject.kaa.server.bootstrap.service.http;

import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.common.http.server.Config;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BootstrapConfig Class.
 * Used to store Bootstrap HTTP service configuration.
 *
 * @author Andrey Panasenko
 *
 */
public class BootstrapConfig extends Config {

    /** The bootstrap service. */
    @Autowired
    private BootstrapInitializationService bootstrapInitializationService;

    /** Operations servers list gather service. */

    private OperationsServerListService endpointListService;

    /** Bootstrap ZK service */
    private BootstrapNode bootstrapNode;

    /**
     * @return the bootstrapService
     */
    public BootstrapInitializationService getBootstrapInitializationService() {
        return bootstrapInitializationService;
    }

    /**
     * @param bootstrapService the bootstrapService to set
     */
    public void setBootstrapInitializationService(BootstrapInitializationService bootstrapService) {
        this.bootstrapInitializationService = bootstrapService;
    }

    /**
     * @return the operationsListService
     */
    public OperationsServerListService getOperationsServerListService() {
        return endpointListService;
    }

    /**
     * @param operationsListService the operationsListService to set
     */
    public void setOperationsServerListService(OperationsServerListService endpointListService) {
        this.endpointListService = endpointListService;
    }

    /**
     * BootstrapNode getter.
     * @return BootstrapNode
     */
    public BootstrapNode getBootstrapNode() {
        return bootstrapNode;
    }

    /**
     * BootstrapNode setter.
     *
     * @param bootstrapNode Bootstrap ZK service
     */
    public void setBootstrapNode(BootstrapNode bootstrapNode) {
        this.bootstrapNode = bootstrapNode;
    }

}
