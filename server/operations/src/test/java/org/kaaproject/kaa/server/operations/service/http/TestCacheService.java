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

package org.kaaproject.kaa.server.operations.service.http;

import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.kaaproject.kaa.server.operations.service.cache.HistoryKey;

/**
 * Test Cache Service. 
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class TestCacheService implements CacheService {
    
    /** HashMap to store public keys of Endpoints */
    private ConcurrentHashMap<EndpointObjectHash, PublicKey> publicKeys;
    
    public TestCacheService() {
        publicKeys = new ConcurrentHashMap<>();
    }

    public void addPublicKey(EndpointObjectHash hash, PublicKey key) {
        publicKeys.putIfAbsent(hash, key);
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getAppSeqNumber(java.lang.String)
     */
    @Override
    public int getAppSeqNumber(String applicationToken) {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfIdByKey(org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey)
     */
    @Override
    public String getConfIdByKey(ConfigurationIdKey key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getHistory(org.kaaproject.kaa.server.operations.service.cache.HistoryKey)
     */
    @Override
    public List<HistoryDto> getHistory(HistoryKey historyKey) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getFilters(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public List<ProfileFilterDto> getFilters(AppVersionKey key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getFilter(java.lang.String)
     */
    @Override
    public ProfileFilterDto getFilter(String profileFilterId) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfByHash(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public EndpointConfigurationDto getConfByHash(EndpointObjectHash hash) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfSchemaByAppAndVersion(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public ConfigurationSchemaDto getConfSchemaByAppAndVersion(AppVersionKey key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getProfileSchemaByAppAndVersion(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public ProfileSchemaDto getProfileSchemaByAppAndVersion(AppVersionKey key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getDelta(org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey, org.kaaproject.kaa.server.operations.service.cache.Computable)
     */
    @Override
    public DeltaCacheEntry getDelta(DeltaCacheKey deltaKey,
            Computable<DeltaCacheKey, DeltaCacheEntry> worker)
            throws GetDeltaException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setDelta(org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey, org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry)
     */
    @Override
    public DeltaCacheEntry setDelta(DeltaCacheKey deltaKey,
            DeltaCacheEntry delta) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#getEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public PublicKey getEndpointKey(EndpointObjectHash hash) {
        return publicKeys.get(hash);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash, java.security.PublicKey)
     */
    @Override
    public void setEndpointKey(EndpointObjectHash hash, PublicKey endpointKey) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setApplicationService(org.kaaproject.kaa.server.common.dao.ApplicationService)
     */
    @Override
    public void setApplicationService(ApplicationService applicationService) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setConfigurationService(org.kaaproject.kaa.server.common.dao.ConfigurationService)
     */
    @Override
    public void setConfigurationService(
            ConfigurationService configurationService) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setHistoryService(org.kaaproject.kaa.server.common.dao.HistoryService)
     */
    @Override
    public void setHistoryService(HistoryService historyService) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setProfileService(org.kaaproject.kaa.server.common.dao.ProfileService)
     */
    @Override
    public void setProfileService(ProfileService profileService) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#setEndpointService(org.kaaproject.kaa.server.common.dao.EndpointService)
     */
    @Override
    public void setEndpointService(EndpointService endpointService) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#resetFilters(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    public void resetFilters(AppVersionKey key) {

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putAppSeqNumber(java.lang.String, java.lang.Integer)
     */
    @Override
    public int putAppSeqNumber(String key, Integer value) {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putProfileSchema(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey, org.kaaproject.kaa.common.dto.ProfileSchemaDto)
     */
    @Override
    public ProfileSchemaDto putProfileSchema(AppVersionKey key,
            ProfileSchemaDto value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfigurationSchema(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey, org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    public ConfigurationSchemaDto putConfigurationSchema(AppVersionKey key,
            ConfigurationSchemaDto value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfiguration(org.kaaproject.kaa.common.hash.EndpointObjectHash, org.kaaproject.kaa.common.dto.EndpointConfigurationDto)
     */
    @Override
    public EndpointConfigurationDto putConfiguration(EndpointObjectHash key,
            EndpointConfigurationDto value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putFilter(java.lang.String, org.kaaproject.kaa.common.dto.ProfileFilterDto)
     */
    @Override
    public ProfileFilterDto putFilter(String key, ProfileFilterDto value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putFilterList(org.kaaproject.kaa.server.operations.service.cache.AppVersionKey, java.util.List)
     */
    @Override
    public List<ProfileFilterDto> putFilterList(AppVersionKey key,
            List<ProfileFilterDto> value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putHistory(org.kaaproject.kaa.server.operations.service.cache.HistoryKey, java.util.List)
     */
    @Override
    public List<HistoryDto> putHistory(HistoryKey key, List<HistoryDto> value) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfId(org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey, java.lang.String)
     */
    @Override
    public String putConfId(ConfigurationIdKey key, String value) {
        return null;
    }

    @Override
    public byte[] getMergedConfiguration(List<EndpointGroupStateDto> egsList, Computable<List<EndpointGroupStateDto>, byte[]> worker) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] setMergedConfiguration(List<EndpointGroupStateDto> egsList, byte[] mergedConfiguration) {
        // TODO Auto-generated method stub
        return null;
    }

}
