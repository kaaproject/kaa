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
package org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.zk.gen.ZkChannelType;

/**
 * @author Andrey Panasenko
 *
 */
public class DefaultRebalancerTest {

    private static final long MAX_HISTORY_TIME_LIVE = 300000;

    /**
     * Check rebalance() operation with empty history
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.DefaultRebalancer#recalculate(java.util.Map)}.
     */
    @Test
    public void testRecalculateEmptyHistory() {
        DefaultRebalancer rebalancer = new DefaultRebalancer();
        assertNotNull(rebalancer);

        String server1 = "dns1";
        OperationsServerLoadHistory server1History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);

        String server2 = "dns2";
        OperationsServerLoadHistory server2History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);

        Map<String, Map<ZkChannelType, OperationsServerLoadHistory>> serversHistory = new HashMap<>();
        Map<ZkChannelType, OperationsServerLoadHistory> server1OpsHistory = new HashMap<>();
        server1OpsHistory.put(ZkChannelType.HTTP, server1History);
        serversHistory.put(server1, server1OpsHistory );

        Map<ZkChannelType, OperationsServerLoadHistory> server2OpsHistory = new HashMap<>();
        server2OpsHistory.put(ZkChannelType.HTTP, server2History);
        serversHistory.put(server2, server2OpsHistory  );
        Map<String,RedirectionRule> rules = rebalancer.recalculate(serversHistory );
        assertNotNull(rules);
        assertEquals(0, rules.size());
    }

    /**
     * Tests redirection rule generation.
     * There are 3 servers dns1,dns2,dns3
     * With load 10,30,55 - average will be 31 its more than default minimum 10
     * recalculate should generate rule for dns3 to drop connection to dns1 with probability 0.2
     * Test method for {@link org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.DefaultRebalancer#recalculate(java.util.Map)}.
     */
    @Test
    public void testRecalculate() {
        DefaultRebalancer rebalancer = new DefaultRebalancer();
        assertNotNull(rebalancer);

        String server1 = "dns1";
        OperationsServerLoadHistory server1History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server1History.addOpsServerLoad(0, 10, 0);
        Map<ZkChannelType, OperationsServerLoadHistory> server1OpsHistory = new HashMap<>();
        server1OpsHistory.put(ZkChannelType.HTTP, server1History);

        String server2 = "dns2";
        OperationsServerLoadHistory server2History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server2History.addOpsServerLoad(0, 30, 0);
        Map<ZkChannelType, OperationsServerLoadHistory> server2OpsHistory = new HashMap<>();
        server2OpsHistory.put(ZkChannelType.HTTP, server2History);

        String server3 = "dns3";
        OperationsServerLoadHistory server3History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server3History.addOpsServerLoad(0, 55, 0);
        Map<ZkChannelType, OperationsServerLoadHistory> server3OpsHistory = new HashMap<>();
        server3OpsHistory.put(ZkChannelType.HTTP, server3History);

        Map<String, Map<ZkChannelType, OperationsServerLoadHistory>> serversHistory = new HashMap<>();

        serversHistory.put(server1, server1OpsHistory);
        serversHistory.put(server2, server2OpsHistory);
        serversHistory.put(server3, server3OpsHistory);
        Map<String,RedirectionRule> rules = rebalancer.recalculate(serversHistory );
        assertNotNull(rules);

        assertEquals(1, rules.size());

        assertNotNull(rules.get(server3));

        assertEquals(server1, rules.get(server3).getDnsName());
        assertEquals(new Double(0.2), new Double(rules.get(server3).getRedirectionProbability()));
    }
}
