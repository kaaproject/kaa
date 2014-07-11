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

package org.kaaproject.kaa.server.operations.service.config;

import java.util.List;

import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.bootstrap.DefaultOperationsBootstrapService;
import org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * The Class OperationsServerConfig.
 */
public class OperationsServerConfig {

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
    
    private List<ServiceChannelConfig> channelList;

    /** Statistics collect window in seconds */
    private long statisticsCalculationWindow;
    
    /** Number of statistics update during collect window */
    private int statisticsUpdateTimes;
    
    /** The thrift host. */
    private String thriftHost;

    /** The thrift port. */
    private int thriftPort;

    /** The zk enabled. */
    private boolean zkEnabled;

    /** The zk host port list. */
    private String zkHostPortList;

    /** The zk max retry time. */
    private int zkMaxRetryTime;

    /** The zk sleep time. */
    private int zkSleepTime;

    /** The zk ignore errors. */
    private boolean zkIgnoreErrors;

    
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

    /**
     * @return the channelList
     */
    public List<ServiceChannelConfig> getChannelList() {
        return channelList;
    }

    /**
     * @param channelList the channelList to set
     */
    public void setChannelList(List<ServiceChannelConfig> channelList) {
        this.channelList = channelList;
    }

    /**
     * @return the thriftHost
     */
    public String getThriftHost() {
        return thriftHost;
    }

    /**
     * @return the thriftPort
     */
    public int getThriftPort() {
        return thriftPort;
    }

    /**
     * @return the zkEnabled
     */
    public boolean isZkEnabled() {
        return zkEnabled;
    }

    /**
     * @return the zkHostPortList
     */
    public String getZkHostPortList() {
        return zkHostPortList;
    }

    /**
     * @return the zkMaxRetryTime
     */
    public int getZkMaxRetryTime() {
        return zkMaxRetryTime;
    }

    /**
     * @return the zkSleepTime
     */
    public int getZkSleepTime() {
        return zkSleepTime;
    }

    /**
     * @return the zkIgnoreErrors
     */
    public boolean isZkIgnoreErrors() {
        return zkIgnoreErrors;
    }

    /**
     * @param thriftHost the thriftHost to set
     */
    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    /**
     * @param thriftPort the thriftPort to set
     */
    public void setThriftPort(int thriftPort) {
        this.thriftPort = thriftPort;
    }

    /**
     * @param zkEnabled the zkEnabled to set
     */
    public void setZkEnabled(boolean zkEnabled) {
        this.zkEnabled = zkEnabled;
    }

    /**
     * @param zkHostPortList the zkHostPortList to set
     */
    public void setZkHostPortList(String zkHostPortList) {
        this.zkHostPortList = zkHostPortList;
    }

    /**
     * @param zkMaxRetryTime the zkMaxRetryTime to set
     */
    public void setZkMaxRetryTime(int zkMaxRetryTime) {
        this.zkMaxRetryTime = zkMaxRetryTime;
    }

    /**
     * @param zkSleepTime the zkSleepTime to set
     */
    public void setZkSleepTime(int zkSleepTime) {
        this.zkSleepTime = zkSleepTime;
    }

    /**
     * @param zkIgnoreErrors the zkIgnoreErrors to set
     */
    public void setZkIgnoreErrors(boolean zkIgnoreErrors) {
        this.zkIgnoreErrors = zkIgnoreErrors;
    }

}
