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

import org.apache.curator.framework.CuratorFramework;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * The Class ControlZkService.
 */
@Service
public class ControlZkService {


  private static final Logger LOG = LoggerFactory.getLogger(ControlZkService.class);

  @Autowired
  private KaaNodeServerConfig kaaNodeServerConfig;

  @Autowired
  private CuratorFramework zkClient;



  private ControlNode controlZkNode;

  /**
   * KaaNodeServerConfig getter.
   *
   * @return KaaNodeServerConfig
   */
  private KaaNodeServerConfig getNodeConfig() {
    return kaaNodeServerConfig;
  }

  /**
   * Start Zookeeper service.
   */
  public void start() {
    if (getNodeConfig().isZkEnabled()) {
      LOG.info("Control service starting ZooKepper connection to {}",
          getNodeConfig().getZkHostPortList());
      ControlNodeInfo nodeInfo = new ControlNodeInfo();
      ConnectionInfo connectionInfo = new ConnectionInfo(
          getNodeConfig().getThriftHost(), getNodeConfig().getThriftPort(), null);
      nodeInfo.setConnectionInfo(connectionInfo);
      controlZkNode = new ControlNode(nodeInfo, zkClient);
      try {
        controlZkNode.start();
      } catch (Exception ex) {
        if (getNodeConfig().isZkIgnoreErrors()) {
          LOG.info("Failed to register control in ZooKeeper", ex);
        } else {
          LOG.error("Failed to register control in ZooKeeper", ex);
          throw new RuntimeException(ex); // NOSONAR
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
        controlZkNode.close();
      } catch (IOException ex) {
        LOG.warn("Error closing ZK node", ex);
      }
    }
  }

  /**
   * Send endpoint notification.
   *
   * @param thriftNotification the thrift notification
   */
  public void sendEndpointNotification(final Notification thriftNotification) {
    if (getNodeConfig().isZkEnabled()) {
      List<OperationsNodeInfo> endpoints = controlZkNode.getCurrentOperationServerNodes();
      for (OperationsNodeInfo endpoint : endpoints) {
        String host = endpoint.getConnectionInfo().getThriftHost().toString();
        int port = endpoint.getConnectionInfo().getThriftPort();
        try {
          ThriftClient<OperationsThriftService.Client> thriftClient = new ThriftClient<>(host, port,
              KaaThriftService.OPERATIONS_SERVICE, OperationsThriftService.Client.class);
          thriftClient.setThriftActivity(new ThriftActivity<OperationsThriftService.Client>() {
            @Override
            public void isSuccess(boolean activitySuccess) {
              if (!activitySuccess) {
                LOG.error("Sending notification to endpoint server failed.");
              }
            }

            @Override
            public void doInTemplate(Client template) {
              try { // NOSONAR
                template.onNotification(thriftNotification);
              } catch (TException ex) {
                LOG.error("Unexpected error occurred while send notification to endpoint server",
                    ex);
              }
            }
          });
          ThriftExecutor.execute(thriftClient);
        } catch (NoSuchMethodException
            | SecurityException
            | InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException ex) {
          LOG.error("Unexpected error occurred while creating thrift connection"
              + " to send notification to endpoint server", ex);
        }
      }
    }
  }

  /**
   * Gets the control zk node.
   *
   * @return the control zk node
   */
  public ControlNode getControlZkNode() {
    return controlZkNode;
  }

  /**
   * Gets the current bootstrap nodes.
   *
   * @return the current bootstrap nodes
   */
  public List<BootstrapNodeInfo> getCurrentBootstrapNodes() {
    if (getNodeConfig().isZkEnabled()) {
      return controlZkNode.getCurrentBootstrapNodes();
    } else {
      return null; // NOSONAR
    }
  }

}
