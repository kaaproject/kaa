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

package org.kaaproject.kaa.server.control.service.zk;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.KaaThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.OperationsThriftService.Client;
import org.kaaproject.kaa.server.common.thrift.util.ThriftActivity;
import org.kaaproject.kaa.server.common.thrift.util.ThriftClient;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.kaaproject.kaa.server.common.zk.control.ControlNode;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ControlZkService.
 */
@Service
public class ControlZkService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlZkService.class);

    @Autowired
    private KaaNodeServerConfig kaaNodeServerConfig;

    /** The control Zookeeper node. */
    private ControlNode controlZKNode;

    /**
     * KaaNodeServerConfig getter
     *
     * @return KaaNodeServerConfig
     */
    private KaaNodeServerConfig getNodeConfig() {
        return kaaNodeServerConfig;
    }

    /**
     * Start Zookeeper Service.
     */
    public void start() {
        if (getNodeConfig().isZkEnabled()) {
            LOG.info("Control service starting ZooKepper connection to {}", getNodeConfig().getZkHostPortList());
            ControlNodeInfo nodeInfo = new ControlNodeInfo();
            ConnectionInfo connectionInfo = new ConnectionInfo(getNodeConfig().getThriftHost(), getNodeConfig().getThriftPort(), null);
            nodeInfo.setConnectionInfo(connectionInfo);
            controlZKNode = new ControlNode(nodeInfo, getNodeConfig().getZkHostPortList(), 60 * 1000, 3 * 1000, new RetryUntilElapsed(
                    getNodeConfig().getZkMaxRetryTime(), getNodeConfig().getZkSleepTime()));
            try {
                controlZKNode.start();
            } catch (Exception e) {
                if (getNodeConfig().isZkIgnoreErrors()) {
                    LOG.info("Failed to register control in ZooKeeper", e);
                } else {
                    LOG.error("Failed to register control in ZooKeeper", e);
                    throw new RuntimeException(e); // NOSONAR
                }
            }
        }
    }

    /**
     * Stop Zookeeper Service.
     */
    public void stop() {
        if (getNodeConfig().isZkEnabled()) {
            try {
                controlZKNode.close();
            } catch (IOException e) {
                LOG.warn("Error closing ZK node", e);
            }
        }
    }

    /**
     * Send endpoint notification.
     * 
     * @param thriftNotification
     *            the thrift notification
     */
    public void sendEndpointNotification(final Notification thriftNotification) {
        if (getNodeConfig().isZkEnabled()) {
            List<OperationsNodeInfo> endpoints = controlZKNode.getCurrentOperationServerNodes();
            for (OperationsNodeInfo endpoint : endpoints) {
                String host = endpoint.getConnectionInfo().getThriftHost().toString();
                int port = endpoint.getConnectionInfo().getThriftPort();
                try {
                    ThriftClient<OperationsThriftService.Client> client = new ThriftClient<>(host, port,
                            KaaThriftService.OPERATIONS_SERVICE, OperationsThriftService.Client.class);
                    client.setThriftActivity(new ThriftActivity<OperationsThriftService.Client>() {
                        @Override
                        public void isSuccess(boolean activitySuccess) {
                            if (!activitySuccess) {
                                LOG.error("Sending notification to endpoint server failed.");
                            }
                        }

                        @Override
                        public void doInTemplate(Client t) {
                            try { // NOSONAR
                                t.onNotification(thriftNotification);
                            } catch (TException e) {
                                LOG.error("Unexpected error occurred while send notification to endpoint server", e);
                            }
                        }
                    });
                    ThriftExecutor.execute(client);
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    LOG.error("Unexpected error occurred while creating thrift connection to send notification to endpoint server", e);
                }
            }
        }
    }

    /**
     * Gets the control zk node.
     *
     * @return the control zk node
     */
    public ControlNode getControlZKNode() {
        return controlZKNode;
    }

    /**
     * Gets the current bootstrap nodes.
     *
     * @return the current bootstrap nodes
     */
    public List<BootstrapNodeInfo> getCurrentBootstrapNodes() {
        if (getNodeConfig().isZkEnabled()) {
            return controlZKNode.getCurrentBootstrapNodes();
        } else {
            return null; // NOSONAR
        }
    }

}
