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

package org.kaaproject.kaa.server.operations.service.http;

import org.kaaproject.kaa.server.common.http.server.Config;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.bootstrap.DefaultOperationsBootstrapService;
import org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The Class OperationsServerConfig.
 */
public class OperationsServerConfig extends Config {

    /** The end point service. */
    @Autowired
    private OperationsBootstrapService operationsBootstrapService;

    /** The operations service. */
    @Autowired
    private OperationsService operationsService;

    /** The akka service. */
    @Autowired
    private AkkaService akkaService;

    /** ZK operations node */
    private OperationsNode zkNode;
    

    /** Statistics collect window in seconds */
    private long statisticsCalculationWindow;
    
    /** Number of statistics update during collect window */
    private int statisticsUpdateTimes;
    
    /**
     * Gets the end point service.
     * 
     * @return the end point service
     */
    public OperationsBootstrapService getOperationsBootstrapService() {
        return operationsBootstrapService;
    }

    /**
     * Sets the end point service.
     * 
     * @param operationsBootstrapService
     *            the new end point service
     */
    public void setOperationsBootstrapService(DefaultOperationsBootstrapService operationsBootstrapService) {
        this.operationsBootstrapService = operationsBootstrapService;
    }

    /**
     * Gets the operations service.
     * 
     * @return the operations service
     */
    public OperationsService getOperationsService() {
        return operationsService;
    }

    /**
     * Gets the akka service.
     * 
     * @return the akka service
     */
    public AkkaService getAkkaService() {
        return akkaService;
    }

    /**
     * @param akkaService the akkaService to set
     */
    public void setAkkaService(AkkaService akkaService) {
        this.akkaService = akkaService;
    }

    /**
     * Statistics collect window getter.
     * @return the statisticsCalculationWindow long in ms
     */
    public long getStatisticsCalculationWindow() {
        return statisticsCalculationWindow;
    }

    /**
     * Statistics collect window setter.
     * @param statisticsCalculationWindow long in ms
     */
    public void setStatisticsCalculationWindow(long statisticsCalculationWindow) {
        this.statisticsCalculationWindow = statisticsCalculationWindow;
    }

    /**
     * Number of statistics update during collect window getter.
     * @return the statisticsUpdateTimes int number
     */
    public int getStatisticsUpdateTimes() {
        return statisticsUpdateTimes;
    }

    /**
     * Number of statistics update during collect window setter.
     * @param statisticsUpdateTimes int number
     */
    public void setStatisticsUpdateTimes(int statisticsUpdateTimes) {
        this.statisticsUpdateTimes = statisticsUpdateTimes;
    }

    /**
     * @return the zkNode
     */
    public OperationsNode getZkNode() {
        return zkNode;
    }

    /**
     * @param zkNode the zkNode to set
     */
    public void setZkNode(OperationsNode zkNode) {
        this.zkNode = zkNode;
    }

}
