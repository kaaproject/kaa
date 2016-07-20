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

package org.kaaproject.kaa.server.operations.service.event;

import java.security.PublicKey;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.structure.Pair;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.cache.HistoryKey;
import org.kaaproject.kaa.server.operations.service.cache.TopicListCacheEntry;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestCacheService implements CacheService {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getAppSeqNumber(java.lang.String)
     */
    @Override
    public AppSeqNumber getAppSeqNumber(String applicationToken) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfIdByKey(org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey)
     */
    @Override
    public String getConfIdByKey(ConfigurationIdKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getHistory(org.kaaproject.kaa.server.operations.service.cache.HistoryKey)
     */
    @Override
    public List<HistoryDto> getHistory(HistoryKey historyKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getFilter(java.lang.String)
     */
    @Override
    public ProfileFilterDto getFilter(String profileFilterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfByHash(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public EndpointConfigurationDto getConfByHash(EndpointObjectHash hash) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfSchemaByAppAndVersion(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public ConfigurationSchemaDto getConfSchemaByAppAndVersion(AppVersionKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getProfileSchemaByAppAndVersion(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public EndpointProfileSchemaDto getProfileSchemaByAppAndVersion(AppVersionKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SdkProfileDto getSdkProfileBySdkToken(String key) {
        return null;
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getDelta(org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey, org.kaaproject.kaa.server.operations.service.cache.Computable)
     */
    @Override
    public ConfigurationCacheEntry getDelta(DeltaCacheKey deltaKey, Computable<DeltaCacheKey, ConfigurationCacheEntry> worker) throws GetDeltaException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setDelta(org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey, org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry)
     */
    @Override
    public ConfigurationCacheEntry setDelta(DeltaCacheKey deltaKey, ConfigurationCacheEntry delta) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public PublicKey getEndpointKey(EndpointObjectHash hash) {
        // TODO Auto-generated method stub
        return null;
    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getTenantIdByAppToken(java.lang.String)
     */
    @Override
    public String getTenantIdByAppToken(String appToken) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getEventClassFamilyIdByEventClassFqn(org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey)
     */
    @Override
    public String getEventClassFamilyIdByEventClassFqn(EventClassFqnKey fqn) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getRouteKeys(org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion)
     */
    @Override
    public Set<RouteTableKey> getRouteKeys(EventClassFqnVersion eventClassVersion) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash, java.security.PublicKey)
     */
    @Override
    public PublicKey putEndpointKey(EndpointObjectHash hash, PublicKey endpointKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetEndpointKey(EndpointObjectHash hash, PublicKey endpointKey) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setApplicationService(org.kaaproject.kaa.server.common.dao.ApplicationService)
     */
    @Override
    public void setApplicationService(ApplicationService applicationService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setConfigurationService(org.kaaproject.kaa.server.common.dao.ConfigurationService)
     */
    @Override
    public void setConfigurationService(ConfigurationService configurationService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setHistoryService(org.kaaproject.kaa.server.common.dao.HistoryService)
     */
    @Override
    public void setHistoryService(HistoryService historyService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setProfileService(org.kaaproject.kaa.server.common.dao.ProfileService)
     */
    @Override
    public void setProfileService(ProfileService profileService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setEndpointService(org.kaaproject.kaa.server.common.dao.EndpointService)
     */
    @Override
    public void setEndpointService(EndpointService endpointService) {
        // TODO Auto-generated method stub

    }


    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putProfileSchema(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey, org.kaaproject.kaa.common.dto.ProfileSchemaDto)
     */
    @Override
    public EndpointProfileSchemaDto putProfileSchema(AppVersionKey key, EndpointProfileSchemaDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfigurationSchema(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey, org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    public ConfigurationSchemaDto putConfigurationSchema(AppVersionKey key, ConfigurationSchemaDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfiguration(org.kaaproject.kaa.common.hash.EndpointObjectHash, org.kaaproject.kaa.common.dto.EndpointConfigurationDto)
     */
    @Override
    public EndpointConfigurationDto putConfiguration(EndpointObjectHash key, EndpointConfigurationDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putFilter(java.lang.String, org.kaaproject.kaa.common.dto.ProfileFilterDto)
     */
    @Override
    public ProfileFilterDto putFilter(String key, ProfileFilterDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putHistory(org.kaaproject.kaa.server.operations.service.cache.HistoryKey, java.util.List)
     */
    @Override
    public List<HistoryDto> putHistory(HistoryKey key, List<HistoryDto> value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfId(org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey, java.lang.String)
     */
    @Override
    public String putConfId(ConfigurationIdKey key, String value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putAppSeqNumber(java.lang.String, org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber)
     */
    @Override
    public AppSeqNumber putAppSeqNumber(String key, AppSeqNumber value) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getEventClassFamilyIdByName(org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey)
     */
    @Override
    public String getEventClassFamilyIdByName(EventClassFamilyIdKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setEventClassService(org.kaaproject.kaa.server.common.dao.EventClassService)
     */
    @Override
    public void setEventClassService(EventClassService eventClassService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setApplicationEventMapService(org.kaaproject.kaa.server.common.dao.ApplicationEventMapService)
     */
    @Override
    public void setApplicationEventMapService(ApplicationEventMapService applicationEventMapService) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setSdkKeyService(org.kaaproject.kaa.server.common.dao.SdkKeyService)
     */
    @Override
    public void setSdkProfileService(SdkProfileService sdkKeyService) {
        // TODO Auto-generated method stub
    }

    @Override
    public Pair<BaseData, RawData> getMergedConfiguration(List<EndpointGroupStateDto> egsList, Computable<List<EndpointGroupStateDto>, Pair<BaseData, RawData>> worker) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseData setMergedConfiguration(List<EndpointGroupStateDto> egsList, BaseData mergedConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndpointGroupDto getEndpointGroupById(String endpointGroupId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicDto getTopicById(String topicId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndpointGroupDto putEndpointGroup(String key, EndpointGroupDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicDto putTopic(String key, TopicDto value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetGroup(String key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getAppTokenBySdkToken(String sdkToken) {
        return null;
    }

    @Override
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByIds(List<String> key) {
        return null;
    }

    @Override
    public List<ApplicationEventFamilyMapDto> putApplicationEventFamilyMaps(List<String> key, List<ApplicationEventFamilyMapDto> value) {
        return null;
    }

    @Override
    public CTLSchemaDto getCtlSchemaById(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServerProfileSchemaDto getServerProfileSchemaByAppAndVersion(
            AppVersionKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFlatCtlSchemaById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ProfileFilterDto> getFilters(AppProfileVersionsKey key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetFilters(AppProfileVersionsKey key) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<ProfileFilterDto> putFilterList(AppProfileVersionsKey key, List<ProfileFilterDto> value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EndpointGroupDto getDefaultGroup(String applicationToken) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TopicListCacheEntry putTopicList(EndpointObjectHash key, TopicListCacheEntry entry) {
        return null;
    }

    @Override
    public TopicListCacheEntry getTopicListByHash(EndpointObjectHash hash) {
        return null;
    }

    @Override
    public String getApplicationIdByAppToken(String appToken) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationDto findAppById(String applicationId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetAppById(String applicationId) {
        // TODO Auto-generated method stub
        
    }
}
