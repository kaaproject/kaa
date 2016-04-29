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

package org.kaaproject.kaa.server.operations.service.loadbalance;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.operations.OperationsNode;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaServiceStatus;
import org.kaaproject.kaa.server.operations.service.akka.AkkaStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultLoadBalancingService implements LoadBalancingService {

    private static final long DEFAULT_STATS_UPDATE_FREQUENCY = 10 * 1000;

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLoadBalancingService.class);

    /**
     * The delta service.
     */
    @Autowired
    private AkkaService akkaService;

    @Value("#{properties[load_stats_update_frequency]}")
    private long loadStatsUpdateFrequency = DEFAULT_STATS_UPDATE_FREQUENCY;

    private ExecutorService pool = Executors.newSingleThreadExecutor();

    private OperationsNode operationsNode;

    @Override
    public void start(OperationsNode operationsNode) {
        LOG.info("Starting service using {} update frequency", loadStatsUpdateFrequency);
        this.operationsNode = operationsNode;
        akkaService.setStatusListener(new AkkaStatusListener() {

            @Override
            public void onStatusUpdate(final AkkaServiceStatus status) {
                pool.submit(new Runnable() {

                    @Override
                    public void run() {
                        DefaultLoadBalancingService.this.onStatusUpdate(status);
                    }
                });
            }
        }, loadStatsUpdateFrequency);
    }

    @Override
    public void stop() {
        LOG.info("Stopping service");
        akkaService.removeStatusListener();
        pool.shutdown();
        try {
            pool.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Failed to terminate service", e);
        }
    }

    @Override
    public void onStatusUpdate(AkkaServiceStatus status) {
        try {
            OperationsNodeInfo nodeInfo = operationsNode.getNodeInfo();
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            nodeInfo.setLoadInfo(new LoadInfo(status.getEndpointCount(), operatingSystemMXBean.getSystemLoadAverage()));
            operationsNode.updateNodeData(nodeInfo);
            LOG.info("Updated load info: {}", nodeInfo.getLoadInfo());
        } catch (Exception e) {
            LOG.error("Failed to report status update to control server", e);
        }
    }
}
