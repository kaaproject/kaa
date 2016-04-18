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

package org.kaaproject.kaa.server.control.service.loadmgmt;

import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.Rebalancer;
import org.kaaproject.kaa.server.control.service.zk.ControlZkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Load Distribution Service startup Class.
 *
 * @author Andrey Panasenko
 */
@Service
public class LoadDistributionService extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(LoadDistributionService.class);

    /**  boolean used to control operation mode in Thread run() cycle. */
    private volatile boolean operate = false;

    /**  Synchronization object. */
    private final Object sync = new Object();

    /**  boolean which used to switch on/off recalculation, set try if Control Server is master. */
    private volatile boolean isMaster  = false;

    /**  ControlZkService. */
    private ControlZkService zkService;

    /**  DynamicLoadManager. */
    private DynamicLoadManager loadManager;

    /**  Load mgmt data recalculation period. */
    @Value("#{properties[recalculation_period]}")
    private int recalculationPeriod;

    /**  Time to live of Operations server load history, in sec. */
    @Value("#{properties[ops_server_history_ttl]}")
    private int opsServerHistoryTtl;
    
    /** The dynamic_mgmt. */
    @Autowired
    private Rebalancer rebalancer;

    /* (non-Javadoc)
     * @see java.lang.Thread#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nLoad Distribution Service properties:\n");
        sb.append("\trecalculation_period: "+recalculationPeriod+"\n");
        sb.append("\tops_server_history_ttl: "+opsServerHistoryTtl+"\n");
        return sb.toString();
    }

    /**
     * Starts LoadDistributionService.
     */
    @Override
    public void start() {
        LOG.info("Load distribution service starting...");
        LOG.info(this.toString());
        loadManager = new DynamicLoadManager(this);
        if (zkService != null) {
            loadManager.registerListeners();
            isMaster = zkService.getControlZKNode().isMaster();
            LOG.trace("Load Distribution Service isMaster "+isMaster);
            operate = true;
            super.start();
        } else {
            LOG.error("Load distribution service start failed, ZK Service not set.");
        }
    }

    /**
     * Stop LoadDistributionService.
     */
    public void shutdown() {
        LOG.info("Load distribution service shutdown...");
        operate = false;
        loadManager.deregisterListeners();
        synchronized (sync) {
            sync.notify();
        }
        try {
            this.join(10000);
        } catch (InterruptedException e) {
            LOG.trace("Load distribution service shutdown join() interrupted");
        } finally {
            LOG.info("Load distribution service shutdown complete");
            loadManager = null;
        }
    }

    /**
     * LoadDistributionService extends Thread
     * Main run() cycle.
     * Operation controlled by operate Boolean and recalculate server list and redirection rules.
     */
    @Override
    public void run() {
        while(operate) {
            synchronized (sync) {
                try {
                    LOG.info("Load distribution service recalculation started...");
                    loadManager.recalculate();
                    sync.wait(recalculationPeriod*1000);
                } catch (InterruptedException e) {
                    LOG.warn("Load distribution service interrupted, shutting down...");
                    operate = false;
                }
            }
        }
        LOG.info("Load distribution service stopped");
    }

    /**
     * Gets the recalculation_period.
     *
     * @return the recalculation_period
     */
    public int getRecalculationPeriod () {
        return recalculationPeriod;
    }

    /**
     * Sets the recalculation_period.
     *
     * @param recalculationPeriod the recalculation period to set
     */
    public void setRecalculationPeriod(int recalculationPeriod) {
        this.recalculationPeriod = recalculationPeriod;
    }

    /**
     * Checks if is master.
     *
     * @return boolean the isMaster
     */
    public boolean isMaster() {
        return isMaster;
    }

    /**
     * Boolean isMaster control if Load Distribution run load balancing recalculation.
     * If set to true - run load balancing recalculation.
     *
     * @param isMaster the master
     */
    public void setMaster(boolean isMaster) {
        if (this.isMaster != isMaster) {
            this.isMaster = isMaster;
            LOG.info("Load distribution service master state changed from {} to {}", this.isMaster, isMaster);
            synchronized (sync) {
                sync.notify();
            }
        }
    }

    public Rebalancer getRebalancer() {
        return rebalancer;
    }

    /**
     * Gets the zk service.
     *
     * @return the zkService
     */
    public ControlZkService getZkService() {
        return zkService;
    }

    /**
     * Sets the zk service.
     *
     * @param zkService the zkService to set
     */
    public void setZkService(ControlZkService zkService) {
        this.zkService = zkService;
    }

    /**
     * Gets the ops_server_history_ttl.
     *
     * @return the endpoint_history_ttl
     */
    public int getOpsServerHistoryTTL() {
        return opsServerHistoryTtl;
    }
}
