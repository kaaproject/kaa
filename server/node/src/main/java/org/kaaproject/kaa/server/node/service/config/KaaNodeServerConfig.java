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

package org.kaaproject.kaa.server.node.service.config;

/**
 * The Class KaaNodeServerConfig.
 */
public class KaaNodeServerConfig {
    
    /** Enable control service parameter. */
    private boolean controlServiceEnabled;

    /** Enable bootstrap service parameter. */
    private boolean bootstrapServiceEnabled;
    
    /** Enable operations service parameter. */
    private boolean operationsServiceEnabled;

    /** The thrift host. */
    private String thriftHost;

    /** The thrift port. */
    private int thriftPort;

    /** The zk enabled. */
    private boolean zkEnabled;

    /** The zk host port list. */
    private String zkHostPortList;

    /** Time to connect to ZK. */
    private int zkWaitConnectionTime;

    /** The zk max retry time. */
    private int zkMaxRetryTime;

    /** The zk sleep time. */
    private int zkSleepTime;

    /** The zk ignore errors. */
    private boolean zkIgnoreErrors;
    
    /**
     * @return true if Control service enabled
     */
    public boolean isControlServiceEnabled() {
        return controlServiceEnabled;
    }

    /**
     * @return true if Bootstrap server enabled
     */
    public boolean isBootstrapServiceEnabled() {
        return bootstrapServiceEnabled;
    }

    /**
     * @return true if Operations server enabled
     */
    public boolean isOperationsServiceEnabled() {
        return operationsServiceEnabled;
    }

    /**
     * The thrift host.
     * 
     * @return the thriftHost
     */
    public String getThriftHost() {
        return thriftHost;
    }

    /**
     * The thrift port.
     * 
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
     * The zk host port list.
     * 
     * @return the zkHostPortList
     */
    public String getZkHostPortList() {
        return zkHostPortList;
    }

    /**
     * Time to connect to ZK.
     *
     * @return the zkWaitConnectionTime
     */
    public int getZkWaitConnectionTime() {
        return zkWaitConnectionTime;
    }

    /**
     * The zk max retry time.
     * 
     * @return the zkMaxRetryTime
     */
    public int getZkMaxRetryTime() {
        return zkMaxRetryTime;
    }

    /**
     * The zk sleep time.
     * 
     * @return the zkSleepTime
     */
    public int getZkSleepTime() {
        return zkSleepTime;
    }

    /**
     * The zk ignore errors.
     * 
     * @return the zkIgnoreErrors
     */
    public boolean isZkIgnoreErrors() {
        return zkIgnoreErrors;
    }
    
    /**
     * @param controlServiceEnabled
     *            set flag to enable/disable Control service
     */
    public void setControlServiceEnabled(boolean controlServiceEnabled) {
        this.controlServiceEnabled = controlServiceEnabled;
    }

    /**
     * @param bootstrapServiceEnabled
     *            set flag to enable/disable Bootstrap service
     */
    public void setBootstrapServiceEnabled(boolean bootstrapServiceEnabled) {
        this.bootstrapServiceEnabled = bootstrapServiceEnabled;
    }

    /**
     * @param operationsServiceEnabled
     *            set flag to enable/disable Operations server
     */
    public void setOperationsServiceEnabled(boolean operationsServiceEnabled) {
        this.operationsServiceEnabled = operationsServiceEnabled;
    }

    /**
     * @param thriftHost
     *            the thriftHost to set
     */
    public void setThriftHost(String thriftHost) {
        this.thriftHost = thriftHost;
    }

    /**
     * @param thriftPort
     *            the thriftPort to set
     */
    public void setThriftPort(int thriftPort) {
        this.thriftPort = thriftPort;
    }

    /**
     * @param zkEnabled
     *            the zkEnabled to set
     */
    public void setZkEnabled(boolean zkEnabled) {
        this.zkEnabled = zkEnabled;
    }

    /**
     * @param zkHostPortList
     *            the zkHostPortList to set
     */
    public void setZkHostPortList(String zkHostPortList) {
        this.zkHostPortList = zkHostPortList;
    }

    /**
     * @param zkWaitConnectionTime
     *            the zkWaitConnectionTime to set
     */
    public void setZkWaitConnectionTime(int zkWaitConnectionTime) {
        this.zkWaitConnectionTime = zkWaitConnectionTime;
    }

    /**
     * @param zkMaxRetryTime
     *            the zkMaxRetryTime to set
     */
    public void setZkMaxRetryTime(int zkMaxRetryTime) {
        this.zkMaxRetryTime = zkMaxRetryTime;
    }

    /**
     * @param zkSleepTime
     *            the zkSleepTime to set
     */
    public void setZkSleepTime(int zkSleepTime) {
        this.zkSleepTime = zkSleepTime;
    }

    /**
     * @param zkIgnoreErrors
     *            the zkIgnoreErrors to set
     */
    public void setZkIgnoreErrors(boolean zkIgnoreErrors) {
        this.zkIgnoreErrors = zkIgnoreErrors;
    }
}
