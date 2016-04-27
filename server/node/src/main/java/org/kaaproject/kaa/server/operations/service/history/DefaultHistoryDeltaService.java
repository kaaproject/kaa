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

package org.kaaproject.kaa.server.operations.service.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey;
import org.kaaproject.kaa.server.operations.service.cache.HistoryKey;
import org.kaaproject.kaa.server.operations.service.delta.HistoryDelta;
import org.kaaproject.kaa.server.operations.service.filter.FilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class DefaultHistoryDeltaService.
 */
@Service
public class DefaultHistoryDeltaService implements HistoryDeltaService {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHistoryDeltaService.class);

    /** The cache service. */
    @Autowired
    private CacheService cacheService;

    /** The filter service. */
    @Autowired
    private FilterService filterService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.history.HistoryDeltaService
     * #getDelta(org.kaaproject.kaa.common.dto.EndpointProfileDto,
     * java.lang.String, int)
     */
    @Override
    public HistoryDelta getDelta(EndpointProfileDto profile, String applicationToken, int curAppSeqNumber) {
        String endpointId = Base64Util.encode(profile);
        ConfigurationIdKey confIdKey = new ConfigurationIdKey(applicationToken, curAppSeqNumber, profile.getConfigurationVersion());
        AppProfileVersionsKey appVersionsKey = new AppProfileVersionsKey(applicationToken, profile.getClientProfileVersion(),
                profile.getServerProfileVersion());
        List<ProfileFilterDto> filters = filterService.getAllMatchingFilters(appVersionsKey, profile);
        LOG.debug("[{}] Found {} matching filters", endpointId, filters.size());
        List<EndpointGroupStateDto> result = new ArrayList<>(1 + filters.size());

        EndpointGroupDto groupDto = cacheService.getDefaultGroup(applicationToken);

        EndpointGroupStateDto groupAllState = new EndpointGroupStateDto();
        groupAllState.setEndpointGroupId(groupDto.getId());
        groupAllState.setConfigurationId(cacheService.getConfIdByKey(confIdKey.copyWithNewEGId(groupDto.getId())));
        result.add(groupAllState);

        for (ProfileFilterDto filter : filters) {
            String confId = cacheService.getConfIdByKey(confIdKey.copyWithNewEGId(filter.getEndpointGroupId()));
            EndpointGroupStateDto endpointGroupState = new EndpointGroupStateDto();
            endpointGroupState.setEndpointGroupId(filter.getEndpointGroupId());
            endpointGroupState.setProfileFilterId(filter.getId());
            endpointGroupState.setConfigurationId(confId);
            result.add(endpointGroupState);
        }
        return new HistoryDelta(result, true, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.history.HistoryDeltaService
     * #getDelta(org.kaaproject.kaa.common.dto.EndpointProfileDto,
     * java.lang.String, int, int)
     */
    @Override
    public HistoryDelta getDelta(EndpointProfileDto profile, String applicationToken, int oldAppSeqNumber, int curAppSeqNumber) {
        String endpointId = Base64Util.encode(profile.getEndpointKeyHash());

        HistoryDelta historyDelta = new HistoryDelta();

        if (oldAppSeqNumber == curAppSeqNumber) {
            if (profile.getGroupState() != null && profile.getGroupState().size() > 0) {
                historyDelta.setEndpointGroupStates(profile.getGroupState());
            } else {
                historyDelta.setEndpointGroupStates(new ArrayList<EndpointGroupStateDto>());
            }
            return historyDelta;
        } else {
            historyDelta.setSeqNumberChanged(true);
        }

        HistoryKey historyKey = new HistoryKey(applicationToken, oldAppSeqNumber, curAppSeqNumber, profile.getConfigurationVersion(),
                profile.getClientProfileVersion(), profile.getServerProfileVersion());
        ConfigurationIdKey confIdKey = new ConfigurationIdKey(applicationToken, curAppSeqNumber, profile.getConfigurationVersion());

        List<EndpointGroupStateDto> endpointGroups;

        LOG.debug("[{}] Fetching changes from history. From seq number: {} to {}", endpointId, historyKey.getOldSeqNumber(),
                historyKey.getNewSeqNumber());

        Map<String, EndpointGroupStateDto> groupsMap = getOldGroupMap(profile);

        List<HistoryDto> updates = cacheService.getHistory(historyKey);

        for (HistoryDto update : updates) {
            ChangeDto change = update.getChange();
            ChangeType changeType = change.getType();
            String groupId = update.getChange().getEndpointGroupId();

            if (changeType == ChangeType.REMOVE_GROUP) {
                if (groupsMap.remove(groupId) != null) {
                    historyDelta.setConfigurationChanged(true);
                    historyDelta.setTopicListChanged(true);
                }
                continue;
            }

            EndpointGroupStateDto egs = groupsMap.get(groupId);

            if (egs != null) {
                if (changeType == ChangeType.REMOVE_TOPIC || changeType == ChangeType.ADD_TOPIC) {
                    LOG.trace("[{}] Detected {} for {} on group {} which means topic list change", endpointId, changeType,
                            change.getTopicId(), change.getEndpointGroupId());
                    historyDelta.setTopicListChanged(true);
                    continue;
                } else if (changeType == ChangeType.REMOVE_CONF || changeType == ChangeType.ADD_CONF) {
                    LOG.trace("[{}] Detected {} for {} on group {} which means configuration change", endpointId, changeType,
                            change.getConfigurationId(), change.getEndpointGroupId());
                    if (changeType == ChangeType.ADD_CONF) {
                        egs.setConfigurationId(change.getConfigurationId());
                    } else {
                        egs.setConfigurationId(null);
                    }
                    historyDelta.setConfigurationChanged(true);
                } else if (changeType == ChangeType.REMOVE_PROF) {
                    LOG.trace("[{}] Detected {} for {} on group {} which means configuration/topic list change", endpointId, changeType,
                            change.getProfileFilterId(), change.getEndpointGroupId());
                    groupsMap.remove(egs.getEndpointGroupId());
                    historyDelta.setAllChanged();
                } else if (changeType == ChangeType.ADD_PROF) {
                    LOG.trace("[{}] Detected {} for {} on group {}", endpointId, changeType, change.getProfileFilterId(),
                            change.getEndpointGroupId());
                    if (!filterService.matches(historyKey.getAppToken(), change.getProfileFilterId(), profile)) {
                        LOG.trace("[{}] Detected {} does not match current profile body which means configuration/topic list change",
                                endpointId, change.getProfileFilterId());
                        groupsMap.remove(egs.getEndpointGroupId());
                        historyDelta.setAllChanged();
                    } else {
                        egs.setProfileFilterId(change.getProfileFilterId());
                    }
                }
            } else {
                if (changeType == ChangeType.ADD_PROF) {
                    LOG.trace("[{}] Detected {} for {} on group {}", endpointId, changeType, change.getProfileFilterId(),
                            change.getEndpointGroupId());
                    if (filterService.matches(historyKey.getAppToken(), change.getProfileFilterId(), profile)) {
                        LOG.trace("[{}] Detected {} match current profile body which means possible configuration/topic list change",
                                endpointId, change.getProfileFilterId());
                        egs = new EndpointGroupStateDto(groupId, change.getProfileFilterId(), null);
                        groupsMap.put(groupId, egs);
                        historyDelta.setAllChanged();
                    }
                }
            }
        }

        endpointGroups = new ArrayList<>(groupsMap.values().size());

        for (Entry<String, EndpointGroupStateDto> entry : groupsMap.entrySet()) {
            if (entry.getValue().getConfigurationId() == null) {
                LOG.debug("[{}] Attempt to fetch configuration id for {}", endpointId, entry.getKey());
                String confId = cacheService.getConfIdByKey(confIdKey.copyWithNewEGId(entry.getKey()));
                if (confId != null) {
                    entry.getValue().setConfigurationId(confId);
                } else {
                    LOG.debug("[{}] Attempt failed. This is possibly group with topic list but without configuration", endpointId,
                            entry.getKey());
                }
            }
            endpointGroups.add(entry.getValue());
        }

        historyDelta.setEndpointGroupStates(endpointGroups);

        return historyDelta;
    }

    /**
     * Gets the old group map.
     *
     * @param profile
     *            the profile
     * @return the old group map
     */
    private Map<String, EndpointGroupStateDto> getOldGroupMap(EndpointProfileDto profile) {
        return profile.getGroupState().stream().collect(Collectors.toMap(EndpointGroupStateDto::getEndpointGroupId, Function.identity()));
    }
}
