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

package org.kaaproject.kaa.server.control.service.loadmgmt;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.OperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Client;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.thrift.util.ThriftActivity;
import org.kaaproject.kaa.server.common.thrift.util.ThriftClient;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.OperationsServerLoadHistory;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.Rebalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DynamicLoadManager Class.
 *
 * @author Andrey Panasenko
 *
 */
public class DynamicLoadManager implements OperationsNodeListener,
        BootstrapNodeListener {

    class OperationsServerMeta {
        /** The Operations Server */
        public OperationsServer opsServer;

        /** The history. */
        public OperationsServerLoadHistory history;

        /** The node info. */
        public OperationsNodeInfo nodeInfo;

        /**
         * The Constructor.
         *
         * @param opsServer the Operations Server
         * @param nodeInfo the node info
         */
        public OperationsServerMeta(OperationsServer opsServer, OperationsNodeInfo nodeInfo) {
            this.opsServer = opsServer;
            this.nodeInfo = nodeInfo;
            history = new OperationsServerLoadHistory();
            history.setMaxHistoryTimeLive(getOpsServerHistoryTTL());
        }
    }

    /** The Constant DEFAULT_PRIORITY. */
    private static final int DEFAULT_PRIORITY = 10;

    /**  Delimiter in DNS name host:port. */
    private static final String HOST_PORT_DELIMITER = ":";

    private static final Logger LOG = LoggerFactory.getLogger(DynamicLoadManager.class);

    /**  LoadDistributeonServiceImpl. */
    private LoadDistributionService loadDistributionService;

    /**  Map to store Operations servers, key - DNS name host:port. */
    private final Map<String, OperationsServerMeta> opsServersMap;

    /**  Map to store bootstrap servers, key - DNS name host:port. */
    private final Map<String, BootstrapNodeInfo> bootstrapsMap;

    /** The last bootstrap servers update failed. */
    private boolean lastBootstrapServersUpdateFailed = false;

    /** The dynamic_mgmt. */
    private Rebalancer dynamicMgmt;

    /**  Time to live of Operations server load history, in ms. */
    private long opsLoadHistoryTTL = 600000;

    /**
     * The Constructor.
     *
     * @param loadDistributionService the load distribution service
     */
    public DynamicLoadManager(LoadDistributionService loadDistributionService) {
        setLoadDistributionService(loadDistributionService);
        opsServersMap = new Hashtable<String,OperationsServerMeta>();
        bootstrapsMap = new Hashtable<String,BootstrapNodeInfo>();
        opsLoadHistoryTTL = loadDistributionService.getOpsServerHistoryTTL()*1000; //Translate seconds to ms
        try {
            Class<?> dynamicMgmtClass = Class.forName(getLoadDistributionService().getDynamicMgmtClass());
            Object obj = dynamicMgmtClass.newInstance();
            if (obj instanceof Rebalancer) {
                dynamicMgmt = (Rebalancer) obj;
            } else {
                LOG.error("Error initializing dynamic load management class instance (not a instance of rebalance)");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.error("Error initializing dynamic management class!", e);
        }
    }

    /**
     * Run recalculate process for Operations server Load optimization.
     */
    public void recalculate() {
        LOG.info("DynamicLoadManager recalculate() started... lastBootstrapServersUpdateFailed {}", lastBootstrapServersUpdateFailed);
        if (lastBootstrapServersUpdateFailed) {
            LOG.trace("Registred {} Bootstrap servers", bootstrapsMap.size());
            lastBootstrapServersUpdateFailed = false;
            for(BootstrapNodeInfo bootstrapNodeInfo : bootstrapsMap.values()) {
                updateBootstrap(bootstrapNodeInfo);
            }
        }
        if (dynamicMgmt != null) {
            Map<String,OperationsServerLoadHistory> opsServerHistory = new Hashtable<String,OperationsServerLoadHistory>();
            for(String dnsName : opsServersMap.keySet()) {
                opsServerHistory.put(dnsName, opsServersMap.get(dnsName).history);
            }
            Map<String,RedirectionRule> rules = dynamicMgmt.recalculate(opsServerHistory);
            LOG.trace("DynamicLoadManager recalculate() got {} redirection rules", rules.size());
            for(String dnsName : rules.keySet()) {
                if (opsServersMap.containsKey(dnsName)) {
                    sendRedirectionRule(dnsName, opsServersMap.get(dnsName).nodeInfo, rules.get(dnsName));
                } else {
                  LOG.error("Operations server {} redirection rule exist, but NO server available, skip setting rule.", dnsName);
                }
            }
        }
    }

    /**
     * Register listeners for Operations server nodes updates and Bootstrap nodes updates.
     */
    public void registerListeners() {
        LOG.trace("DynamicLoadManager register listeners...");
        ControlNode pm = getLoadDistributionService().getZkService().getControlZKNode();
        pm.addListener((OperationsNodeListener)this);
        pm.addListener((BootstrapNodeListener)this);
    }

    /**
     * Deregister listeners for Operations server nodes updates and Bootstrap nodes updates.
     */
    public void deregisterListeners() {
        LOG.trace("DynamicLoadManager deregister listeners...");
        ControlNode pm = getLoadDistributionService().getZkService().getControlZKNode();
        pm.removeListener((OperationsNodeListener)this);
        pm.removeListener((BootstrapNodeListener)this);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
     */
    @Override
    public void onNodeAdded(BootstrapNodeInfo nodeInfo) {
        final String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.info("Bootstrap server {} added", dnsName);
        bootstrapsMap.put(dnsName, nodeInfo);

        updateBootstrap(nodeInfo);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
     */
    @Override
    public void onNodeUpdated(BootstrapNodeInfo nodeInfo) {
        String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.info("Bootstrap server {} updated", dnsName);
        bootstrapsMap.put(dnsName, nodeInfo);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
     */
    @Override
    public void onNodeRemoved(BootstrapNodeInfo nodeInfo) {
        String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.info("Bootstrap server {} removed", dnsName);
        bootstrapsMap.remove(dnsName);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeAdded(OperationsNodeInfo nodeInfo) {
        String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        OperationsServer operations = new OperationsServer(DEFAULT_PRIORITY, nodeInfo.getConnectionInfo().getPublicKey());
        opsServersMap.put(dnsName, new OperationsServerMeta(operations, nodeInfo));

        LOG.info("Operations server {} added. Updating {} Bootstrap servers", dnsName, bootstrapsMap.size());
        for(BootstrapNodeInfo bootstrapNodeInfo : bootstrapsMap.values()) {
            updateBootstrap(bootstrapNodeInfo);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
        String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.info("Operations server {} update", dnsName);
        if (opsServersMap.containsKey(dnsName)) {
            opsServersMap.get(dnsName).opsServer.setPublicKey(nodeInfo.getConnectionInfo().getPublicKey());

            opsServersMap.get(dnsName).history.addOpsServerLoad(nodeInfo.getRegisteredUsersCount(),
                    nodeInfo.getProcessedRequestCount(),
                    nodeInfo.getDeltaCalculationCount());

        } else {
            OperationsServer opsServer = new OperationsServer(DEFAULT_PRIORITY,nodeInfo.getConnectionInfo().getPublicKey());
            opsServersMap.put(dnsName, new OperationsServerMeta(opsServer, nodeInfo));
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
        String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        opsServersMap.remove(dnsName);

        LOG.info("Operations server {} removed. Updating {} Bootstrap servers", dnsName, bootstrapsMap.size());
        for(BootstrapNodeInfo bootstrapNodeInfo : bootstrapsMap.values()) {
            updateBootstrap(bootstrapNodeInfo);
        }
    }

    /**
     * Gets the load distribution service.
     *
     * @return the loadDistributionService
     */
    public LoadDistributionService getLoadDistributionService() {
        return loadDistributionService;
    }

    /**
     * Sets the load distribution service.
     *
     * @param loadDistributionService the loadDistributionService to set
     */
    public void setLoadDistributionService(
            LoadDistributionService loadDistributionService) {
        this.loadDistributionService = loadDistributionService;
    }

    /**
     * Gets the dns name from connection info.
     *
     * @param connectionInfo the connection info
     * @return the DNS name from connection info
     */
    private String getDNSNameFromConnectionInfo(ConnectionInfo connectionInfo) {
        StringBuffer name = new StringBuffer();
        name.append(connectionInfo.getHttpHost());
        name.append(HOST_PORT_DELIMITER);
        name.append(connectionInfo.getHttpPort());
        return name.toString();
    }

    /**
     * Update bootstrap.
     *
     * @param nodeInfo the node info
     */
    private void updateBootstrap(BootstrapNodeInfo nodeInfo) {
        final String dnsName = getDNSNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.debug("Update bootstrap server: "+dnsName+" Thrift: "+nodeInfo.getConnectionInfo().getThriftHost().toString()+":"+nodeInfo.getConnectionInfo().getThriftPort());
        try {
            ThriftClient<BootstrapThriftService.Client> client = new ThriftClient<BootstrapThriftService.Client>(
                            nodeInfo.getConnectionInfo().getThriftHost().toString(),
                            nodeInfo.getConnectionInfo().getThriftPort(),
                            BootstrapThriftService.Client.class);
            client.setThriftActivity(new ThriftActivity<BootstrapThriftService.Client>() {

                @Override
                public void isSuccess(boolean activitySuccess) {
                    lastBootstrapServersUpdateFailed = !activitySuccess;
                    LOG.info("Bootstrap {}: Operations servers list updated {}", dnsName, activitySuccess ? "successfully" : "unsuccessfully");
                }

                @Override
                public void doInTemplate(Client t) {
                    try {
                        Map<String,OperationsServer> update = new Hashtable<String, OperationsServer>();
                        for(String dnsName : opsServersMap.keySet()) {
                            update.put(dnsName, opsServersMap.get(dnsName).opsServer);
                        }
                        t.onOperationsServerListUpdate(update);
                        LOG.info("Bootstrap "+dnsName+" Operations servers list updated.");
                    } catch (TException e) {
                        lastBootstrapServersUpdateFailed = true;
                        LOG.error("Bootstrap "+dnsName+" Operations servers list updated failed: "+e.toString());
                    }
                }
            });

            ThriftExecutor.execute(client);
        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            lastBootstrapServersUpdateFailed = true;
            LOG.error("Bootstrap " + dnsName + " Operations servers list execute updated failed: "+e.toString());
        }
    }

    /**
     * Send redirection rule.
     *
     * @param dnsName the dns name
     * @param nodeInfo the node info
     * @param rule the rule
     */
    private void sendRedirectionRule(final String dnsName, OperationsNodeInfo nodeInfo, final RedirectionRule rule) {
        LOG.trace("Set redirection rule for Operations server: {}; Thrift: {}:{}"
                , dnsName, nodeInfo.getConnectionInfo().getThriftHost().toString(), nodeInfo.getConnectionInfo().getThriftPort());
        try {
            ThriftClient<OperationsThriftService.Client> client = new ThriftClient<OperationsThriftService.Client>(
                            nodeInfo.getConnectionInfo().getThriftHost().toString(),
                            nodeInfo.getConnectionInfo().getThriftPort(),
                            OperationsThriftService.Client.class);
            client.setThriftActivity(new ThriftActivity<OperationsThriftService.Client>() {

                @Override
                public void isSuccess(boolean activitySuccess) {
                    LOG.info("Operations server {} redirection rule set {}", dnsName, activitySuccess ? "successfully" : "unsuccessfully");
                }

                @Override
                public void doInTemplate(org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Client t) {
                    try {
                        t.setRedirectionRule(rule);
                        LOG.info("Operations {} set redirection rule: {} <> {}"
                                , dnsName, rule.getDnsName(), rule.getRedirectionProbability());
                    } catch (TException e) {
                        LOG.error("Operations server {} set redirection rule failed", dnsName, e);
                    }
                }
            });

            ThriftExecutor.execute(client);
        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            LOG.error("Operations server {} set redirection rule failed", dnsName, e);
        }
    }

    /**
     * Gets the operations Server history TTL.
     *
     * @return the opsLoadHistoryTTL
     */
    public long getOpsServerHistoryTTL() {
        return opsLoadHistoryTTL;
    }


    /**
     * Sets the operations history TTL.
     *
     * @param opsServerHistoryTTL the opsLoadHistoryTTL to set
     */
    public void setOpsServerHistoryTTL(long opsServerHistoryTTL) {
        this.opsLoadHistoryTTL = opsServerHistoryTTL;
    }

    /**
     * Dynamic rebalancer getter.
     * @return Rebalancer instance.
     */
    public Rebalancer getDynamicRebalancer() {
        return dynamicMgmt;
    }
}
