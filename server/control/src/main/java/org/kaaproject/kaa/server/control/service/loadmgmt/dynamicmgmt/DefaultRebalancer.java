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
 * Default Rebalance Class.
 * Reacts only on processedRequestCount
 * If processedRequestCount more than 20 and number of opsServers more than 1
 * Calculate average load for all opsServers. Set as 100% load for the highest load opsServers.
 * If there are opsServers loaded less than 20%, get one with the lowest load.
 * Set redirection rule to the highest load opsServer with probability 0.2 and redirection to the lowest load.
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

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.Rebalancer#recalculate(java.util.Map)
     */
    @Override
    public Map<String,RedirectionRule> recalculate(Map<String, OperationsServerLoadHistory> opsServerLoadHistory) {
        LOG.info("DefaultRebalancer recalculate Operations servers balance...");
        Map<String,RedirectionRule> rules = new HashMap<String, RedirectionRule>();
        if (opsServerLoadHistory.size() > 1) {
            String highestLoadName = "";
            int highestLoadValue = 0;
            String lowestLoadName = "";
            int lowestLoadValue = Integer.MAX_VALUE;
            int averageLoad = 0;
            for(String opsServer : opsServerLoadHistory.keySet()) {
                if (opsServerLoadHistory.get(opsServer) != null) {
                    List<OperationsServerLoad> load = opsServerLoadHistory.get(opsServer).getHistory();
                    int p = load.get(load.size()).getProcessedRequestCount();
                    if (p > highestLoadValue) {
                        highestLoadValue = p;
                        highestLoadName = opsServer;
                    }
                    if (p < lowestLoadValue) {
                        lowestLoadName = opsServer;
                        lowestLoadValue = p;
                    }
                    averageLoad += p;
                }

            }
            averageLoad = averageLoad / opsServerLoadHistory.size();
            if (averageLoad > minRequestCount) {
                if (((highestLoadValue * 20)/100) > lowestLoadValue) { //NOSONAR
                    //Start redirection
                    RedirectionRule rule = new RedirectionRule();
                    rule.setDnsName(lowestLoadName);
                    rule.setRedirectionProbability(0.2);
                    rule.setRuleTTL(300000);
                    rules.put(highestLoadName, rule);
                }
            }
        }

        return rules;
    }

}
