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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Client;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftChannelType;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftCommunicationParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftIpParameters;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftSupportedChannel;
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
import org.kaaproject.kaa.server.common.zk.gen.SupportedChannel;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpLpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkHttpStatistics;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpComunicationParameters;
import org.kaaproject.kaa.server.common.zk.gen.ZkKaaTcpStatistics;
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
        public ThriftOperationsServer opsServer;

        /** The history. */
        public Map<ZkChannelType,OperationsServerLoadHistory> history;

        /** The node info. */
        public OperationsNodeInfo nodeInfo;

        /**
         * The Constructor.
         *
         * @param opsServer the Operations Server
         * @param nodeInfo the node info
         */
        public OperationsServerMeta(ThriftOperationsServer opsServer, OperationsNodeInfo nodeInfo) {
            this.opsServer = opsServer;
            this.nodeInfo = nodeInfo;
            history = new HashMap<>();
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
        //Translate seconds to ms
        opsLoadHistoryTTL = loadDistributionService.getOpsServerHistoryTTL()*1000;
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
            Map<String,Map<ZkChannelType,OperationsServerLoadHistory>> opsServerHistory = new Hashtable<String,Map<ZkChannelType,OperationsServerLoadHistory>>();
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
        final String dnsName = getBootstrapNameFromNodeInfo(nodeInfo);
        LOG.info("Bootstrap server {} added", dnsName);
        bootstrapsMap.put(dnsName, nodeInfo);

        updateBootstrap(nodeInfo);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
     */
    @Override
    public void onNodeUpdated(BootstrapNodeInfo nodeInfo) {
        String dnsName = getBootstrapNameFromNodeInfo(nodeInfo);
        LOG.info("Bootstrap server {} updated", dnsName);
        bootstrapsMap.put(dnsName, nodeInfo);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo)
     */
    @Override
    public void onNodeRemoved(BootstrapNodeInfo nodeInfo) {
        String dnsName = getBootstrapNameFromNodeInfo(nodeInfo);
        LOG.info("Bootstrap server {} removed", dnsName);
        bootstrapsMap.remove(dnsName);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeAdded(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeAdded(OperationsNodeInfo nodeInfo) {
        String dnsName = getNameFromConnectionInfo(nodeInfo.getConnectionInfo());

        addNewOperationsServer(dnsName, nodeInfo);

        LOG.info("Operations server {} added. Updating {} Bootstrap servers", dnsName, bootstrapsMap.size());
        for(BootstrapNodeInfo bootstrapNodeInfo : bootstrapsMap.values()) {
            updateBootstrap(bootstrapNodeInfo);
        }
    }

    /**
     * Transform Zk List of supported channels into bootstrap thrift list of supported channels
     * @param supportedChannelsArray
     * @return List<ThriftSupportedChannel>
     */
    private List<ThriftSupportedChannel> getThriftSupportedChannelsFromZkSupportedChannels(List<SupportedChannel> supportedChannelsArray, OperationsServerMeta meta) {
        List<ThriftSupportedChannel> thriftSuppChannels  = new ArrayList<>();
        for(SupportedChannel channel : supportedChannelsArray) {
            ThriftChannelType thriftType = null;
            ThriftCommunicationParameters communicationParams = new ThriftCommunicationParameters();
            switch (channel.getZkChannel().getChannelType()) {
            case HTTP:
                thriftType = ThriftChannelType.HTTP;
                ZkHttpComunicationParameters zkCommParams = (ZkHttpComunicationParameters) channel.getZkChannel().getCommunicationParameters();
                communicationParams.setHttpParams(new ThriftIpParameters(
                        zkCommParams.getZkComunicationParameters().getHostName().toString(),
                        zkCommParams.getZkComunicationParameters().getPort().intValue()));
                break;
            case HTTP_LP:
                thriftType = ThriftChannelType.HTTP_LP;
                ZkHttpLpComunicationParameters zkLpCommParams = (ZkHttpLpComunicationParameters) channel.getZkChannel().getCommunicationParameters();
                communicationParams.setHttpLpParams(new ThriftIpParameters(
                        zkLpCommParams.getZkComunicationParameters().getHostName().toString(),
                        zkLpCommParams.getZkComunicationParameters().getPort().intValue()));
                break;
            case KAATCP:
                thriftType = ThriftChannelType.KAATCP;
                ZkKaaTcpComunicationParameters zkTcpCommParams = (ZkKaaTcpComunicationParameters) channel.getZkChannel().getCommunicationParameters();
                communicationParams.setKaaTcpParams(new ThriftIpParameters(
                        zkTcpCommParams.getZkComunicationParameters().getHostName().toString(),
                        zkTcpCommParams.getZkComunicationParameters().getPort().intValue()));
                break;
            default:
                break;
            }
            if (thriftType != null) {
                meta.history.put(channel.getZkChannel().getChannelType(), new OperationsServerLoadHistory(opsLoadHistoryTTL));
                ThriftSupportedChannel thriftChannel = new ThriftSupportedChannel(thriftType, communicationParams);
                thriftSuppChannels.add(thriftChannel);
            }
        }
        return thriftSuppChannels;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeUpdated(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeUpdated(OperationsNodeInfo nodeInfo) {
        String dnsName = getNameFromConnectionInfo(nodeInfo.getConnectionInfo());
        LOG.info("Operations server {} update", dnsName);
        if (opsServersMap.containsKey(dnsName)) {
            opsServersMap.get(dnsName).opsServer.setPublicKey(nodeInfo.getConnectionInfo().getPublicKey());

            for(SupportedChannel channel : nodeInfo.getSupportedChannelsArray()) {
                if(opsServersMap.containsKey(dnsName)
                        && opsServersMap.get(dnsName).history.containsKey(channel.getZkChannel().getChannelType())) {

                    int registeredUsersCount = 0;
                    int processedRequestCount = 0;
                    int deltaCalculationCount = 0;
                    switch (channel.getZkChannel().getChannelType()) {
                    case HTTP:
                        ZkHttpStatistics httpStats = (ZkHttpStatistics) channel.getZkChannel().getChannelStatistics();
                        registeredUsersCount = httpStats.getZkStatistics().getRegisteredUsersCount();
                        processedRequestCount = httpStats.getZkStatistics().getProcessedRequestCount();
                        deltaCalculationCount = httpStats.getZkStatistics().getDeltaCalculationCount();
                        break;
                    case HTTP_LP:
                        ZkHttpLpStatistics httpLpStats = (ZkHttpLpStatistics) channel.getZkChannel().getChannelStatistics();
                        registeredUsersCount = httpLpStats.getZkStatistics().getRegisteredUsersCount();
                        processedRequestCount = httpLpStats.getZkStatistics().getProcessedRequestCount();
                        deltaCalculationCount = httpLpStats.getZkStatistics().getDeltaCalculationCount();
                        break;
                    case KAATCP:
                        ZkKaaTcpStatistics tcpStats = (ZkKaaTcpStatistics) channel.getZkChannel().getChannelStatistics();
                        registeredUsersCount = tcpStats.getZkStatistics().getRegisteredUsersCount();
                        processedRequestCount = tcpStats.getZkStatistics().getProcessedRequestCount();
                        deltaCalculationCount = tcpStats.getZkStatistics().getDeltaCalculationCount();
                        break;
                    default:
                        break;
                    }
                    opsServersMap.get(dnsName).history.get(channel.getZkChannel().getChannelType()).addOpsServerLoad(
                            registeredUsersCount,
                            processedRequestCount,
                            deltaCalculationCount);
                }
            }
        } else {
            addNewOperationsServer(dnsName, nodeInfo);
        }
    }

    /**
     * @param dnsName
     * @param nodeInfo
     */
    private void addNewOperationsServer(String dnsName, OperationsNodeInfo nodeInfo) {
        OperationsServerMeta meta = new OperationsServerMeta(null , nodeInfo);
        List<ThriftSupportedChannel> thriftSuppChannels = getThriftSupportedChannelsFromZkSupportedChannels(nodeInfo.getSupportedChannelsArray(), meta);
        ThriftOperationsServer operations = new ThriftOperationsServer(dnsName, DEFAULT_PRIORITY, nodeInfo.getConnectionInfo().getPublicKey(), thriftSuppChannels  );
        meta.opsServer = operations;
        opsServersMap.put(dnsName, meta);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.zk.operations.OperationsNodeListener#onNodeRemoved(org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo)
     */
    @Override
    public void onNodeRemoved(OperationsNodeInfo nodeInfo) {
        String dnsName = getNameFromConnectionInfo(nodeInfo.getConnectionInfo());
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
     * Gets the operations name from connection info.
     *
     * @param connectionInfo the connection info
     * @return the thrift host name and thrift port from connection info
     */
    private String getNameFromConnectionInfo(ConnectionInfo connectionInfo) {
        StringBuffer name = new StringBuffer();
        name.append(connectionInfo.getThriftHost());
        name.append(HOST_PORT_DELIMITER);
        name.append(connectionInfo.getThriftPort());
        return name.toString();
    }

    /**
     * Gets the bootstrap name from connection info.
     *
     * @param BootstrapNodeInfo
     * @return the bootstrap host name and bootstrap port from connection info
     */
    private String getBootstrapNameFromNodeInfo(BootstrapNodeInfo nodeInfo) {
        StringBuffer name = new StringBuffer();
        name.append(nodeInfo.getBootstrapHostName());
        name.append(HOST_PORT_DELIMITER);
        name.append(nodeInfo.getBootstrapPort());
        return name.toString();
    }

    /**
     * Update bootstrap.
     *
     * @param nodeInfo the node info
     */
    private void updateBootstrap(BootstrapNodeInfo nodeInfo) {
        final String dnsName = getBootstrapNameFromNodeInfo(nodeInfo);
        LOG.debug("Update bootstrap server: {}", dnsName);
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
                    try { //NOSONAR
                        List<ThriftOperationsServer> operationsServersList = new ArrayList<>();
                        for(String dnsName : opsServersMap.keySet()) {
                            operationsServersList.add(opsServersMap.get(dnsName).opsServer);
                            LOG.trace("Bootstrap {} server: {}", dnsName,opsServersMap.get(dnsName).opsServer.toString());
                        }
                        LOG.trace("Bootstrap {} Operations servers list size {} ready to updates", dnsName,operationsServersList.size());
                        t.onOperationsServerListUpdate(operationsServersList );
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
                    try { //NOSONAR
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
