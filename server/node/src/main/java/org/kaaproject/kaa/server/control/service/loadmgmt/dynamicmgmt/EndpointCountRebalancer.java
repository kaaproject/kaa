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

package org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.common.zk.gen.LoadInfo;
import org.kaaproject.kaa.server.control.service.loadmgmt.dynamicmgmt.OperationsServerLoadHistory.OperationsServerLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EndpointCountRebalancer implements Rebalancer {
    private static final Logger LOG = LoggerFactory.getLogger(EndpointCountRebalancer.class);

    private static final int DEFAULT_MIN_DIFF = 5000;
    private static final double DEFAULT_MIN_INIT_REDIRECT = 0.75;
    private static final double DEFAULT_MIN_SESSION_REDIRECT = 0.0;

    /**
     * Minimum difference between amount of endpoints that need to be present in
     * order to trigger rebalancing
     */
    @Value("#{properties[loadmgmt_min_diff]}")
    private int minDiff = DEFAULT_MIN_DIFF;

    /** Maximum redirect probability for new sessions */
    @Value("#{properties[loadmgmt_max_init_redirect_probability]}")
    private double maxInitRedirectProbability = DEFAULT_MIN_INIT_REDIRECT;

    /** Maximum redirect probability for existing sessions */
    @Value("#{properties[loadmgmt_max_session_redirect_probability]}")
    private double maxSessionRedirectProbability = DEFAULT_MIN_SESSION_REDIRECT;

    /** Load mgmt data recalculation period. */
    @Value("#{properties[recalculation_period]}")
    private int recalculationPeriod;

    private final AtomicLong ruleIdSeq = new AtomicLong();

    @Override
    public Map<Integer, List<RedirectionRule>> recalculate(Map<Integer, OperationsServerLoadHistory> opsServerLoadHistory) {
        Map<Integer, List<RedirectionRule>> result = new HashMap<>();
        if (opsServerLoadHistory.isEmpty()) {
            LOG.debug("No ops server load history yet");
            return result;
        }
        if (opsServerLoadHistory.size() == 1) {
            LOG.debug("No rebalancing in standalone mode");
            return result;
        }
        for(Entry<Integer, OperationsServerLoadHistory> accessPointHistory : opsServerLoadHistory.entrySet()){
            LOG.debug("Access point: {} has {} history items", accessPointHistory.getKey(), accessPointHistory.getValue().getHistory().size());
            for (OperationsServerLoad load : accessPointHistory.getValue().getHistory()) {
                LOG.debug("History: {}", load);
            }
        }
        int minLoadedOpsServer = getMinLoadedOpsServer(opsServerLoadHistory);
        int minEndpointCount = getLastEndpointCount(opsServerLoadHistory.get(minLoadedOpsServer));
        int maxLoadedOpsServer = getMaxLoadedOpsServer(opsServerLoadHistory);
        int maxEndpointCount = getLastEndpointCount(opsServerLoadHistory.get(maxLoadedOpsServer));
        LOG.info("Min loaded ops server is {} with {} endpoints", minLoadedOpsServer, minEndpointCount);
        LOG.info("Max loaded ops server is {} with {} endpoints", maxLoadedOpsServer, maxEndpointCount);
        int maxDiff = maxEndpointCount - minEndpointCount;
        LOG.info("Max difference between endpoint counts is {}", maxDiff);
        if (maxDiff < minDiff) {
            LOG.debug("Max endpoint count difference is too small to trigger recalculation. Min required diff is {}", minDiff);
            return result;
        }

        int totalLoad = 0;
        for (Entry<Integer, OperationsServerLoadHistory> opsEntry : opsServerLoadHistory.entrySet()) {
            totalLoad += getLastEndpointCount(opsEntry.getValue());
        }

        int targetLoad = totalLoad / opsServerLoadHistory.size();
        LOG.debug("Target load is {}", targetLoad);

        Map<Integer, Double> weights = calculateWeights(opsServerLoadHistory, targetLoad);

        for (Entry<Integer, OperationsServerLoadHistory> opsEntry : opsServerLoadHistory.entrySet()) {
            double curWeight = weights.get(opsEntry.getKey());
            if (curWeight >= 0) {
                LOG.debug("No redirection rules for {}", targetLoad);
                continue;
            }
            List<RedirectionRule> redirectionRules = new ArrayList<>();
            for (Entry<Integer, Double> targetWeight : weights.entrySet()) {
                if (targetWeight.getValue() < 0) {
                    continue;
                }
                double initRedirectProbability = Math.abs(curWeight) * targetWeight.getValue() * maxInitRedirectProbability;
                double sessionRedirectProbability = targetWeight.getValue() * maxSessionRedirectProbability;
                if (initRedirectProbability > 0 || sessionRedirectProbability > 0) {
                    RedirectionRule rule = new RedirectionRule(targetWeight.getKey(), ruleIdSeq.getAndIncrement(), initRedirectProbability,
                            sessionRedirectProbability, recalculationPeriod * 1000L);
                    LOG.debug("Calculated new rule for accessPointId: {} -> {}", opsEntry.getKey(), rule);
                    redirectionRules.add(rule);
                }
            }
            result.put(opsEntry.getKey(), redirectionRules);
        }

        return result;
    }

    private Map<Integer, Double> calculateWeights(Map<Integer, OperationsServerLoadHistory> opsServerLoadHistory, int targetLoad) {
        Map<Integer, Double> weights = new LinkedHashMap<>();

        double totalPosWeight = 0;
        double totalNegWeight = 0;
        for (Entry<Integer, OperationsServerLoadHistory> opsEntry : opsServerLoadHistory.entrySet()) {
            int curOpsEndpointCount = getLastEndpointCount(opsEntry.getValue());
            double weight = targetLoad - curOpsEndpointCount;
            if (weight > 0) {
                totalPosWeight += weight;
            } else {
                totalNegWeight = Math.max(totalNegWeight, Math.abs(weight));
            }
            weights.put(opsEntry.getKey(), weight);
        }
        for (Entry<Integer, Double> weightEntry : weights.entrySet()) {
            double weight = weightEntry.getValue();
            if (weight > 0) {
                weightEntry.setValue(weightEntry.getValue() / totalPosWeight);
            } else {
                weightEntry.setValue(weightEntry.getValue() / totalNegWeight);
            }
            LOG.debug("Calculated redirection weight of {} is {}", weightEntry.getKey(), weightEntry.getValue());
        }
        return weights;
    }

    private int getMinLoadedOpsServer(Map<Integer, OperationsServerLoadHistory> opsServerLoadHistory) {
        int result = 0;
        int minCount = Integer.MAX_VALUE;
        for (Entry<Integer, OperationsServerLoadHistory> entry : opsServerLoadHistory.entrySet()) {
            int endpointCount = getLastEndpointCount(entry.getValue());
            if (endpointCount < minCount) {
                result = entry.getKey();
                minCount = endpointCount;
            }
        }
        return result;
    }

    private int getMaxLoadedOpsServer(Map<Integer, OperationsServerLoadHistory> opsServerLoadHistory) {
        int result = 0;
        int maxCount = Integer.MIN_VALUE;
        for (Entry<Integer, OperationsServerLoadHistory> entry : opsServerLoadHistory.entrySet()) {
            int endpointCount = getLastEndpointCount(entry.getValue());
            if (endpointCount > maxCount) {
                result = entry.getKey();
                maxCount = endpointCount;
            }
        }
        return result;
    }

    private int getLastEndpointCount(OperationsServerLoadHistory loadHistory) {
        List<OperationsServerLoad> history = loadHistory.getHistory();
        if (!history.isEmpty()) {
            LoadInfo loadInfo = history.get(history.size() - 1).getLoadInfo();
            if (loadInfo != null) {
                return loadInfo.getEndpointCount();
            }
        }
        return 0;
    }
}
