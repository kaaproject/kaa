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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.EndpointCountRebalancer;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.OperationsServerLoadHistory;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.Rebalancer;

/**
 * @author Andrey Panasenko
 *
 */
public class EndpointCountRebalancerTest {

    private static final long MAX_HISTORY_TIME_LIVE = 300000;

    /**
     * Check rebalance() operation with empty history Test method for
     * {@link org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.EndpointCountRebalancer#recalculate(java.util.Map)}
     * .
     */
    @Test
    public void testRecalculateEmptyHistory() {
        Rebalancer rebalancer = new EndpointCountRebalancer();
        assertNotNull(rebalancer);

        Integer server1 = "dns1".hashCode();
        OperationsServerLoadHistory server1History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);

        Integer server2 = "dns2".hashCode();
        OperationsServerLoadHistory server2History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);

        Map<Integer, OperationsServerLoadHistory> serversHistory = new HashMap<>();
        serversHistory.put(server1, server1History);
        serversHistory.put(server2, server2History);
        Map<Integer, List<RedirectionRule>> rules = rebalancer.recalculate(serversHistory);
        assertNotNull(rules);
        assertEquals(0, rules.size());
    }

    /**
     * Tests redirection rule generation. There are 3 servers dns1,dns2,dns3
     * With load 10,30,55 - average will be 31 its more than default minimum 10
     * recalculate should generate rule for dns3 to drop connection to dns1 with
     * probability 0.2 Test method for
     * {@link org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.EndpointCountRebalancer#recalculate(java.util.Map)}
     * .
     */
    @Test
    public void testRecalculate() {
        Rebalancer rebalancer = new EndpointCountRebalancer();
        assertNotNull(rebalancer);

        Integer server1 = "dns1".hashCode();
        OperationsServerLoadHistory server1History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server1History.addOpsServerLoad(new LoadInfo(100000, 1.0));

        Integer server2 = "dns2".hashCode();
        OperationsServerLoadHistory server2History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server2History.addOpsServerLoad(new LoadInfo(40000, 1.0));

        Integer server3 = "dns3".hashCode();
        OperationsServerLoadHistory server3History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server3History.addOpsServerLoad(new LoadInfo(10000, 1.0));
        
        Integer server4 = "dns4".hashCode();
        OperationsServerLoadHistory server4History = new OperationsServerLoadHistory(MAX_HISTORY_TIME_LIVE);
        server4History.addOpsServerLoad(new LoadInfo(10000, 1.0));


        Map<Integer, OperationsServerLoadHistory> serversHistory = new LinkedHashMap<Integer, OperationsServerLoadHistory>();

        serversHistory.put(server1, server1History);
        serversHistory.put(server2, server2History);
        serversHistory.put(server3, server3History);
        serversHistory.put(server4, server4History);
        Map<Integer, List<RedirectionRule>> rules = rebalancer.recalculate(serversHistory);
        assertNotNull(rules);

        assertEquals(1, rules.size());

        assertNotNull(rules.get(server1));
        
        assertEquals(2, rules.get(server1).size());

        assertEquals(server3.intValue(), rules.get(server1).get(0).getAccessPointId());
        assertEquals(0.375, rules.get(server1).get(0).getInitRedirectProbability(), 0.0);
        assertEquals(0.0, rules.get(server1).get(0).getSessionRedirectProbability(), 0.0);
        assertEquals(server4.intValue(), rules.get(server1).get(1).getAccessPointId());
        assertEquals(0.375, rules.get(server1).get(1).getInitRedirectProbability(), 0.0);
        assertEquals(0.0, rules.get(server1).get(1).getSessionRedirectProbability(), 0.0);
    }
}
