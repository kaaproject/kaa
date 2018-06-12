package org.kaaproject.kaa.server.operations.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.node.service.config.KaaNodeServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;

@Service
public class ZookeeperClient {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClient.class);
    public static final String ZK_ENDPOINTS_BASE_PATH = "/endpoints";

    @Autowired
    private KaaNodeServerConfig kaaNodeServerConfig;

    private ZooKeeper zookeeper;

    @PostConstruct
    public void init() {
        String connStr = kaaNodeServerConfig.getZkHostPortList();
        try {
            LOG.info("Connecting zookeeper client to [{}]", connStr);
            zookeeper = new ZooKeeper(connStr, 100000, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    LOG.debug("Processing watched event [{}]", watchedEvent);
                }
            });
            waitUntilConnected(zookeeper);
            LOG.info("Zookeeper client connection established");
        } catch (Exception e) {
            LOG.error("Could not innit zookeeper client.", e);
        }
    }

    private static void waitUntilConnected(ZooKeeper zooKeeper) {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        Watcher watcher = new ConnectedWatcher(connectedLatch);
        zooKeeper.register(watcher);
        if (ZooKeeper.States.CONNECTING == zooKeeper.getState()) {
            try {
                LOG.info("Waiting for Zookeeper client connection.");
                connectedLatch.await();
            } catch (InterruptedException e) {
                LOG.error("Wait connection interrupted", e);
                throw new IllegalStateException(e);
            }
        }
    }

    private static class ConnectedWatcher implements Watcher {

        private CountDownLatch connectedLatch;

        ConnectedWatcher(CountDownLatch connectedLatch) {
            this.connectedLatch = connectedLatch;
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                connectedLatch.countDown();
            }
        }
    }

    public String obtainEndpointNodeAddress(byte[] endpointKeyHash) {
        LOG.debug("Obtaining endpoint node address for endpoint key hash: [{}]", endpointKeyHash);
        try {
            byte[] encodedEpKey = Base64.encodeBase64(endpointKeyHash);
            String encodedEpKeyStr = new String(encodedEpKey);
            String path = ZK_ENDPOINTS_BASE_PATH + "/" + encodedEpKeyStr;

            if (null == zookeeper.exists(path, false)) {
                return null;
            }
            return new String(zookeeper.getData(path, false,  new Stat()));

        } catch (Exception e) {
            LOG.error("Could not get data!", e);
            return null;
        }
    }


    public void saveEndpointNodeAddress(EndpointObjectHash endpointKey, String nodeAddress) {
        try {
            Stat endpointsStat = zookeeper.exists(ZK_ENDPOINTS_BASE_PATH, false);
            if (null == endpointsStat) {
                zookeeper.create(ZK_ENDPOINTS_BASE_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOG.info("Created path for endpoint base [{}]", ZK_ENDPOINTS_BASE_PATH);
            }
            byte[] encodedEpKey = Base64.encodeBase64(endpointKey.getData());
            String encodedEpKeyStr = new String(encodedEpKey);
            Stat epKeyStat = zookeeper.exists(ZK_ENDPOINTS_BASE_PATH + "/" + encodedEpKeyStr, false);
            if (null != epKeyStat) {
                LOG.info("Deleting old value for endpoint [{}]", encodedEpKeyStr);
                zookeeper.delete(ZK_ENDPOINTS_BASE_PATH + "/" + encodedEpKeyStr, epKeyStat.getVersion());
            }
            LOG.info("Saving node address [{}] for endpoint [{}]", nodeAddress, encodedEpKeyStr);
            zookeeper.create(ZK_ENDPOINTS_BASE_PATH + "/" + encodedEpKeyStr, nodeAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        } catch (Exception e) {
            LOG.error("Error data.", e);
        }
    }

}
