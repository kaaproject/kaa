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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.OperationsServerLoadHistory.OperationsServerLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Rebalance Class. Reacts only on processedRequestCount If
 * processedRequestCount more than 20 and number of opsServers more than 1
 * Calculate average load for all opsServers. Set as 100% load for the highest
 * load opsServers. If there are opsServers loaded less than 20%, get one with
 * the lowest load. Set redirection rule to the highest load opsServer with
 * probability 0.2 and redirection to the lowest load.
 *
 * @author Andrey Panasenko
 *
 */
public class DefaultRebalancer implements Rebalancer {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRebalancer.class);

    /** Default value for minimum number of processed request count */
    public static final int DEFAULT_MIN_VALUE_PROCESSED_REQUEST = 20;

    /** value for minimum number of processed request count */
    private final int minRequestCount = DEFAULT_MIN_VALUE_PROCESSED_REQUEST;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.Rebalancer
     * #recalculate(java.util.Map)
     */
    @Override
    public Map<Integer, RedirectionRule> recalculate(Map<Integer, OperationsServerLoadHistory> opsServerLoadHistory) {
        LOG.info("DefaultRebalancer recalculate Operations servers balance...");
        Map<Integer, RedirectionRule> rules = new HashMap<Integer, RedirectionRule>();
        if (opsServerLoadHistory.size() > 1) {
            int highestLoadName = 0;
            int highestLoadValue = 0;
            int lowestLoadName = 0;
            int lowestLoadValue = Integer.MAX_VALUE;
            int averageLoad = 0;
            int loadCount = 0;
            for (Integer opsServer : opsServerLoadHistory.keySet()) {
                if (opsServerLoadHistory.get(opsServer) != null) {
                    int processedRequestCountIntegral = getLastProcessedRequestCountFromAllChannels(opsServerLoadHistory.get(opsServer));
                    if (processedRequestCountIntegral > 0) {
                        if (processedRequestCountIntegral > highestLoadValue) {
                            highestLoadValue = processedRequestCountIntegral;
                            highestLoadName = opsServer;
                        }
                        if (processedRequestCountIntegral < lowestLoadValue) {
                            lowestLoadName = opsServer;
                            lowestLoadValue = processedRequestCountIntegral;
                        }
                        averageLoad += processedRequestCountIntegral;
                        loadCount++;
                    }
                }

            }
            if (loadCount > 0) {
                averageLoad = averageLoad / loadCount;
            }
            if (averageLoad > minRequestCount) {
                if (((highestLoadValue * 20) / 100) > lowestLoadValue) { // NOSONAR
                    // Start redirection
                    RedirectionRule rule = new RedirectionRule();
                    rule.setAccessPointId(lowestLoadName);
                    rule.setRedirectionProbability(0.2);
                    rule.setRuleTTL(300000);
                    rules.put(highestLoadName, rule);
                }
            }
        }

        return rules;
    }

    /**
     * @param map
     * @return
     */
    private int getLastProcessedRequestCountFromAllChannels(OperationsServerLoadHistory history) {
        int totalLoad = 0;
        List<OperationsServerLoad> load = history.getHistory();
        if (!load.isEmpty()) {
            if (load.get(load.size() - 1).getLoadInfo() != null) {
                totalLoad += load.get(load.size() - 1).getLoadInfo().getLoadIndex();
            }
        }
        return totalLoad;
    }

}
