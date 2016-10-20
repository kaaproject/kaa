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

package org.kaaproject.kaa.server.common.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.server.common.zk.control.ControlNodeListener;
import org.kaaproject.kaa.server.common.zk.gen.BootstrapNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.ControlNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The Class ControlNodeTracker.
 */
public abstract class ControlNodeTracker implements ControlNodeAware, Closeable {

  /**
   * The Constant CONTROL_SERVER_NODE_PATH.
   */
  protected static final String CONTROL_SERVER_NODE_PATH = "/controlServerNode";

  /**
   * The Constant OPERATIONS_SERVER_NODE_PATH.
   */
  protected static final String OPERATIONS_SERVER_NODE_PATH = "/operationsServerNodes";

  /**
   * The Constant BOOTSTRAP_SERVER_NODE_PATH.
   */
  protected static final String BOOTSTRAP_SERVER_NODE_PATH = "/bootstrapServerNodes";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory
      .getLogger(ControlNodeTracker.class);
  /**
   * The listeners.
   */
  private final List<ControlNodeListener> listeners;
  /**
   * The client.
   */
  protected CuratorFramework zkClient;
  /**
   * The node path.
   */
  protected String nodePath;
  /**
   * The control node avro converter.
   */
  protected ThreadLocal<AvroByteArrayConverter<ControlNodeInfo>> controlNodeAvroConverter =
          new ThreadLocal<AvroByteArrayConverter<ControlNodeInfo>>() {
    @Override
    protected AvroByteArrayConverter<ControlNodeInfo> initialValue() {
      return new AvroByteArrayConverter<ControlNodeInfo>(ControlNodeInfo.class);
    }
  };
  /**
   * The endpoint node avro converter.
   */
  protected ThreadLocal<AvroByteArrayConverter<OperationsNodeInfo>> operationsNodeAvroConverter =
          new ThreadLocal<AvroByteArrayConverter<OperationsNodeInfo>>() {
    @Override
    protected AvroByteArrayConverter<OperationsNodeInfo> initialValue() {
      return new AvroByteArrayConverter<OperationsNodeInfo>(OperationsNodeInfo.class);
    }
  };
  /**
   * The bootstrap node avro converter.
   */
  protected ThreadLocal<AvroByteArrayConverter<BootstrapNodeInfo>> bootstrapNodeAvroConverter =
          new ThreadLocal<AvroByteArrayConverter<BootstrapNodeInfo>>() {
    @Override
    protected AvroByteArrayConverter<BootstrapNodeInfo> initialValue() {
      return new AvroByteArrayConverter<BootstrapNodeInfo>(BootstrapNodeInfo.class);
    }
  };
  /**
   * The control cache.
   */
  private NodeCache controlCache;
  /**
   * The errors listener.
   */
  private final UnhandledErrorListener errorsListener = new UnhandledErrorListener() {
    @Override
    public void unhandledError(String message, Throwable ex) {
      LOG.error("Unrecoverable error: " + message, ex);
      try {
        close();
      } catch (IOException ioe) {
        LOG.warn("Exception when closing.", ioe);
      }
    }
  };

  /**
   * Instantiates a new control node tracker.
   */
  public ControlNodeTracker() {
    super();
    this.listeners = new CopyOnWriteArrayList<ControlNodeListener>();
  }

  /**
   * Start.
   *
   * @throws Exception the exception
   */
  public void start() throws Exception { //NOSONAR
    LOG.info("Starting node tracker");
    zkClient.getUnhandledErrorListenable().addListener(errorsListener);
    if (createZkNode()) {
      controlCache = new NodeCache(zkClient, CONTROL_SERVER_NODE_PATH);
      controlCache.getListenable().addListener(new NodeCacheListener() {

        @Override
        public void nodeChanged() throws Exception {
          ChildData currentData = controlCache.getCurrentData();
          if (currentData == null) {
            LOG.warn("Control service node died!");
            onNoMaster();
          } else {
            LOG.warn("Control service node changed!");
            onMasterChange(currentData);
          }
        }
      });
      controlCache.start();
    } else {
      LOG.warn("Failed to create ZK node!");
    }
  }

  public abstract boolean createZkNode() throws IOException;

  /**
   * On no master.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void onNoMaster() throws IOException {
    for (ControlNodeListener listener : listeners) {
      listener.onControlNodeDown();
    }
  }

  /**
   * On master change.
   *
   * @param currentData the current data
   */
  protected void onMasterChange(ChildData currentData) {
    ControlNodeInfo controlServerInfo = extractControlServerInfo(currentData);

    for (ControlNodeListener listener : listeners) {
      listener.onControlNodeChange(controlServerInfo);
    }
  }

  /**
   * Checks if is connected.
   *
   * @return true, if is connected
   */
  public boolean isConnected() {
    return zkClient.getZookeeperClient().isConnected();
  }

  /**
   * Adds the listener.
   *
   * @param listener the listener
   */
  public void addListener(ControlNodeListener listener) {
    LOG.debug("Listener registered: " + listener);
    listeners.add(listener);
  }

  /**
   * Removes the listener.
   *
   * @param listener the listener
   * @return true, if successful
   */
  public boolean removeListener(ControlNodeListener listener) {
    if (listeners.remove(listener)) {
      LOG.debug("Listener removed: " + listener);
      return true;
    } else {
      LOG.debug("Listener not found: " + listener);
      return false;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    LOG.info("Closing");
    listeners.clear();
    if (controlCache != null) {
      controlCache.close();
    }

    if (nodePath != null) {
      try {
        zkClient.delete().forPath(nodePath);
        LOG.debug("Node with path {} successfully deleted", nodePath);
      } catch (Exception ex) {
        LOG.debug("Failed to delete node", ex);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.kaaproject.kaa.server.common.zk.ControlNodeAware#getControlServerInfo
   * ()
   */
  @Override
  public ControlNodeInfo getControlServerInfo() {
    if (controlCache != null && controlCache.getCurrentData() != null) {
      return extractControlServerInfo(controlCache.getCurrentData());
    } else {
      return null;
    }
  }

  /**
   * Extract control service info.
   *
   * @param currentData the current data
   * @return the control node info
   */
  private ControlNodeInfo extractControlServerInfo(ChildData currentData) {
    ControlNodeInfo controlServerInfo = null;
    try {
      controlServerInfo = controlNodeAvroConverter.get().fromByteArray(currentData.getData(),
              controlServerInfo);
    } catch (IOException ex) {
      LOG.error("error reading control service info", ex);
    }
    return controlServerInfo;
  }

  public boolean doZkClientAction(ZkClientAction action) throws IOException {
    return doZkClientAction(action, false);
  }

  /**
   * Do Zookeeper client action.
   *
   * @param action            the Zookeeper client action
   * @param throwIoException  define throw or not IOException
   * @return boolean 'true' if doWithZkClient method works without exceptions
   * @throws IOException the IOException
   */
  public boolean doZkClientAction(ZkClientAction action, boolean throwIoException)
          throws IOException {
    try {
      action.doWithZkClient(zkClient);
      return true;
    } catch (Exception ex) {
      LOG.error("Unknown Error", ex);
      close();
      if (throwIoException) {
        throw new IOException(ex);
      } else {
        return false;
      }
    }
  }

  public static interface ZkClientAction {
    void doWithZkClient(CuratorFramework client) throws Exception; //NOSONAR
  }
}
