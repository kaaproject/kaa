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

package org.kaaproject.kaa.server.operations.service.cache;

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
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;


/**
 * The Interface CacheService is used to model cache service.
 * Service to efficiently cache some core items that are used during delta
 * calculation. Although many DBs provide efficient caching logic, we decided to
 * use or own layer because of following reasons:
 * 1) Minimize network load between Endpoint and DB nodes
 * 2) Minimize dependency on DB
 * 3) Avoid unnecessary data serde
 * This service also works like a proxy and fetch data
 * from DB in case it is not found in cache.
 *
 * @author ashvayka
 */
public interface CacheService {

    /**
     * Gets the app seq number.
     *
     * @param applicationToken the application token
     * @return the app seq number
     */
    AppSeqNumber getAppSeqNumber(String applicationToken);

    /**
     * Gets the conf id by key.
     *
     * @param key the key
     * @return the conf id by key
     */
    String getConfIdByKey(ConfigurationIdKey key);

    /**
     * Gets the history.
     *
     * @param historyKey the history key
     * @return the history
     */
    List<HistoryDto> getHistory(HistoryKey historyKey);

    /**
     * Gets the filters.
     *
     * @param key the key
     * @return the filters
     */
    List<ProfileFilterDto> getFilters(AppProfileVersionsKey key);

    /**
     * Gets application event family maps by their ids.
     *
     * @param key list of ids
     * @return list of application event family maps
     */
    List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByIds(List<String> key);

    /**
     * Gets the filter.
     *
     * @param profileFilterId the profile filter id
     * @return the filter
     */
    ProfileFilterDto getFilter(String profileFilterId);


    /**
     * Gets the conf by hash.
     *
     * @param hash the hash
     * @return the conf by hash
     */
    EndpointConfigurationDto getConfByHash(EndpointObjectHash hash);

    /**
     * Gets the conf schema by app.
     *
     * @param key the key
     * @return the conf schema by app
     */
    ConfigurationSchemaDto getConfSchemaByAppAndVersion(AppVersionKey key);

    /**
     * Gets the profile schema by app.
     *
     * @param key the key
     * @return the conf schema by app
     */
    EndpointProfileSchemaDto getProfileSchemaByAppAndVersion(AppVersionKey key);
    
    /**
     * Gets the server profile schema by app.
     *
     * @param key the key
     * @return the server schema by app
     */
    ServerProfileSchemaDto getServerProfileSchemaByAppAndVersion(AppVersionKey key);

    /**
     * Gets the sdk profile by sdk token.
     *
     * @param key the sdk token
     * @return sdk profile by sdk token
     */
    SdkProfileDto getSdkProfileBySdkToken(String key);

    /**
     * Gets the merged configuration.
     *
     * @param egsList the egs list
     * @param worker the worker
     * @return the merged configuration
     */
    Pair<BaseData, RawData> getMergedConfiguration(List<EndpointGroupStateDto> egsList, Computable<List<EndpointGroupStateDto>, Pair<BaseData, RawData>> worker);

    /**
     * Sets the merged configuration.
     *
     * @param egsList the egs list
     * @param mergedConfiguration the merged configuration
     * @return the string
     */
    BaseData setMergedConfiguration(List<EndpointGroupStateDto> egsList, BaseData mergedConfiguration);

    /**
     * Gets the delta.
     *
     * @param deltaKey the delta key
     * @param worker the worker
     * @return the delta
     * @throws GetDeltaException the get delta exception
     */
    ConfigurationCacheEntry getDelta(DeltaCacheKey deltaKey, Computable<DeltaCacheKey, ConfigurationCacheEntry> worker) throws GetDeltaException;

    /**
     * Sets the delta.
     *
     * @param deltaKey the delta key
     * @param delta the delta
     * @return the delta cache entry
     */
    ConfigurationCacheEntry setDelta(DeltaCacheKey deltaKey, ConfigurationCacheEntry delta);

    /**
     * Gets the endpoint key.
     *
     * @param hash the hash
     * @return the endpoint key
     */
    PublicKey getEndpointKey(EndpointObjectHash hash);

    /**
     * Gets the EndpointClassFamily Id using tenant Id and name;
     *
     * @param key the event class family id key
     * @return the EndpointClassFamily Id
     */
    String getEventClassFamilyIdByName(EventClassFamilyIdKey key);

    /**
     * Gets the Tenant Id by application token;
     *
     * @param appToken token of Application that belongs to Tenant
     * @return the Tenant Id
     */
    String getTenantIdByAppToken(String appToken);
    
    /**
     * Gets the {@link ApplicationDto} by application token;
     *
     * @param appToken token of Application
     * @return the Application
     */
    String getApplicationIdByAppToken(String appToken);

    /**
     * Gets the application token by the sdk token
     *
     * @param sdkToken the sdk token
     * @return application token for the specified sdk token
     */
    String getAppTokenBySdkToken(String sdkToken);

    /**
     * Gets the Event Class Family Id by Event Class FQN
     *
     * @param fqn of one of the events that belong to target Event Class Family
     * @return the Event Class Family Id
     */
	String getEventClassFamilyIdByEventClassFqn(EventClassFqnKey fqn);

    /**
     * Gets all possible Event Class Family - Application keys that are interested in receiving
     * events for this particular Event Class
     *
     * @param eventClassVersion Event Class Id and Version pair
     * @return set of Event Class Family - Application keys
     */
	Set<RouteTableKey> getRouteKeys(EventClassFqnVersion eventClassVersion);

    /**
     * Sets the endpoint key.
     *
     * @param hash the hash
     * @param endpointKey the endpoint key
     *
     * @return cached endpoint key
     */
    PublicKey putEndpointKey(EndpointObjectHash hash, PublicKey endpointKey);

    /**
     *
     * Remove key from hash
     *
     * @param hash the hash
     * @param endpointKey the endpoint key
     */
    void resetEndpointKey(EndpointObjectHash hash, PublicKey endpointKey);

    /**
     * Setter for test purpose only.
     *
     * @param applicationService the new application service
     */
    void setApplicationService(ApplicationService applicationService);

    /**
     * Setter for test purpose only.
     *
     * @param configurationService the new configuration service
     */
    void setConfigurationService(ConfigurationService configurationService);

    /**
     * Setter for test purpose only.
     *
     * @param historyService the new history service
     */
    void setHistoryService(HistoryService historyService);

    /**
     * Setter for test purpose only.
     *
     * @param profileService the new profile service
     */
    void setProfileService(ProfileService profileService);

    /**
     * Setter for test purpose only.
     *
     * @param endpointService the new endpoint service
     */
    void setEndpointService(EndpointService endpointService);

    /**
     * Setter for test purposes only
     *
     * @param sdkProfileService the new sdk profile service
     */
    void setSdkProfileService(SdkProfileService sdkProfileService);

    /**
     * Cache invalidate method.
     *
     * @param key the key
     */
    void resetFilters(AppProfileVersionsKey key);

    /**
     * Cache invalidate method.
     *
     * @param key the key
     * @param value the value
     * @return the int
     */
    AppSeqNumber putAppSeqNumber(String key, AppSeqNumber value);

    /**
     * Put profile schema.
     *
     * @param key the key
     * @param value the value
     * @return the profile schema dto
     */
    EndpointProfileSchemaDto putProfileSchema(AppVersionKey key, EndpointProfileSchemaDto value);

    /**
     * Put configuration schema.
     *
     * @param key the key
     * @param value the value
     * @return the configuration schema dto
     */
    ConfigurationSchemaDto putConfigurationSchema(AppVersionKey key, ConfigurationSchemaDto value);

    /**
     * Put configuration.
     *
     * @param key the key
     * @param value the value
     * @return the endpoint configuration dto
     */
    EndpointConfigurationDto putConfiguration(EndpointObjectHash key, EndpointConfigurationDto value);

    /**
     * Put filter.
     *
     * @param key the key
     * @param value the value
     * @return the profile filter dto
     */
    ProfileFilterDto putFilter(String key, ProfileFilterDto value);

    /**
     * Put filter list.
     *
     * @param key the key
     * @param value the value
     * @return the list
     */
    List<ProfileFilterDto> putFilterList(AppProfileVersionsKey key, List<ProfileFilterDto> value);

    /**
     * Put history.
     *
     * @param key the key
     * @param value the value
     * @return the list
     */
    List<HistoryDto> putHistory(HistoryKey key, List<HistoryDto> value);

    /**
     * Put conf id.
     *
     * @param key the key
     * @param value the value
     * @return the string
     */
    String putConfId(ConfigurationIdKey key, String value);

    /**
     * Put application event family maps
     *
     * @param key list of event family maps ids
     * @param value list of event family maps
     * @return the list
     */
    public List<ApplicationEventFamilyMapDto> putApplicationEventFamilyMaps(List<String> key, List<ApplicationEventFamilyMapDto> value);

    void setEventClassService(EventClassService eventClassService);

    void setApplicationEventMapService(ApplicationEventMapService applicationEventMapService);

    EndpointGroupDto getEndpointGroupById(String endpointGroupId);

    TopicDto getTopicById(String topicId);

    EndpointGroupDto putEndpointGroup(String key, EndpointGroupDto value);

    TopicDto putTopic(String key, TopicDto value);

    void resetGroup(String key);

    CTLSchemaDto getCtlSchemaById(String id);

    String getFlatCtlSchemaById(String id);

    EndpointGroupDto getDefaultGroup(String applicationToken);

    TopicListCacheEntry putTopicList(EndpointObjectHash key, TopicListCacheEntry entry);

    TopicListCacheEntry getTopicListByHash(EndpointObjectHash hash);

    ApplicationDto findAppById(String applicationId);
    
    void resetAppById(String applicationId);
}
