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

package org.kaaproject.kaa.server.operations.service.cache.concurrent;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeDto;
import org.kaaproject.kaa.common.dto.ChangeType;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.cache.HistoryKey;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.operations.service.event.EventClassFqnVersion;
import org.kaaproject.kaa.server.operations.service.event.RouteTableKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * The Class ConcurrentCacheService.
 */
@Service
public class ConcurrentCacheService implements CacheService {
    /** The Constant ALGORITHM. */
    private static final String ALGORITHM = "RSA";

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentCacheService.class);

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The configuration service. */
    @Autowired
    private ConfigurationService configurationService;
    //
    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;
    //
    /** The profile service. */
    @Autowired
    private ProfileService profileService;

    /** The history service. */
    @Autowired
    private HistoryService historyService;

    @Autowired
    private EventClassService eventClassService;

    @Autowired
    private ApplicationEventMapService applicationEventMapService;

    /** The app seq number memorizer. */
    private final CacheTemporaryMemorizer<String, AppSeqNumber> appSeqNumberMemorizer = new CacheTemporaryMemorizer<String, AppSeqNumber>();

    /** The cf id memorizer. */
    private final CacheTemporaryMemorizer<ConfigurationIdKey, String> cfIdMemorizer = new CacheTemporaryMemorizer<ConfigurationIdKey, String>();

    /** The history memorizer. */
    private final CacheTemporaryMemorizer<HistoryKey, List<HistoryDto>> historyMemorizer = new CacheTemporaryMemorizer<HistoryKey, List<HistoryDto>>();

    /** The filter lists memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, List<ProfileFilterDto>> filterListsMemorizer = new CacheTemporaryMemorizer<AppVersionKey, List<ProfileFilterDto>>();

    /** The filters memorizer. */
    private final CacheTemporaryMemorizer<String, ProfileFilterDto> filtersMemorizer = new CacheTemporaryMemorizer<String, ProfileFilterDto>();

    /** The cf memorizer. */
    private final CacheTemporaryMemorizer<EndpointObjectHash, EndpointConfigurationDto> cfMemorizer = new CacheTemporaryMemorizer<EndpointObjectHash, EndpointConfigurationDto>();

    /** The cf schema memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, ConfigurationSchemaDto> cfSchemaMemorizer = new CacheTemporaryMemorizer<AppVersionKey, ConfigurationSchemaDto>();

    /** The pf schema memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, ProfileSchemaDto> pfSchemaMemorizer = new CacheTemporaryMemorizer<AppVersionKey, ProfileSchemaDto>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EndpointObjectHash, PublicKey> endpointKeyMemorizer = new CacheTemporaryMemorizer<EndpointObjectHash, PublicKey>();

    /** The merged configuration memorizer. */
    private final CacheTemporaryMemorizer<List<EndpointGroupStateDto>, BaseData> mergedConfigurationMemorizer = new CacheTemporaryMemorizer();

    /** The delta memorizer. */
    private final CacheTemporaryMemorizer<DeltaCacheKey, DeltaCacheEntry> deltaMemorizer = new CacheTemporaryMemorizer<DeltaCacheKey, DeltaCacheEntry>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EventClassFamilyIdKey, String> ecfIdKeyMemorizer = new CacheTemporaryMemorizer<EventClassFamilyIdKey, String>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<String, String> tenantIdMemorizer = new CacheTemporaryMemorizer<String, String>();

    /** The history seq number comparator. */
    public static final Comparator<HistoryDto> HISTORY_SEQ_NUMBER_COMPARATOR = new Comparator<HistoryDto>() {
        @Override
        public int compare(HistoryDto o1, HistoryDto o2) {
            if (o1.getSequenceNumber() > o2.getSequenceNumber()) {
                return 1;
            } else {
                return o1.getSequenceNumber() == o2.getSequenceNumber() ? 0 : -1;
            }
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getAppSeqNumber(java.lang.String)
     */
    @Override
    @Cacheable(value = "appSeqNumbers", key = "#key")
    public AppSeqNumber getAppSeqNumber(String key) {
        return appSeqNumberMemorizer.compute(key, new Computable<String, AppSeqNumber>() {
            @Override
            public AppSeqNumber compute(String key) {
                LOG.debug("Fetching result for getAppSeqNumber");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key);
                AppSeqNumber appSeqNumber = new AppSeqNumber(appDto.getId(), appDto.getApplicationToken(), appDto.getSequenceNumber());
                putAppSeqNumber(key, appSeqNumber);
                return appSeqNumber;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * putAppSeqNumber(java.lang.String, java.lang.Integer)
     */
    @Override
    @CachePut(value = "appSeqNumbers", key = "#key")
    public AppSeqNumber putAppSeqNumber(String key, AppSeqNumber appSeqNumber) {
        return appSeqNumber;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getConfIdByKey
     * (org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey)
     */
    @Override
    @Cacheable("configurationIds")
    public String getConfIdByKey(ConfigurationIdKey key) {
        return cfIdMemorizer.compute(key, new Computable<ConfigurationIdKey, String>() {
            @Override
            public String compute(ConfigurationIdKey key) {
                LOG.debug("Fetching result for getConfIdByKey");
                String confId = null;
                List<ConfigurationDto> configurations = configurationService.findConfigurationsByEndpointGroupId(key.getEndpointGroupId());
                for (ConfigurationDto confDto : configurations) {
                    if (confDto.getMajorVersion() == key.getConfigSchemaVersion()) {
                        confId = confDto.getId();
                        putConfId(key, confId);
                        break;
                    }
                }
                return confId;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#putConfId
     * (org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey,
     * java.lang.String)
     */
    @Override
    @CachePut(value = "configurationIds", key = "#key")
    public String putConfId(ConfigurationIdKey key, String value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#getHistory
     * (org.kaaproject.kaa.server.operations.service.cache.HistoryKey)
     */
    @Override
    @Cacheable("history")
    public List<HistoryDto> getHistory(HistoryKey key) {
        return historyMemorizer.compute(key, new Computable<HistoryKey, List<HistoryDto>>() {
            @Override
            public List<HistoryDto> compute(HistoryKey key) {
                LOG.debug("Fetching result for getHistory");
                List<HistoryDto> relatedChanges = new ArrayList<HistoryDto>();

                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getAppToken());
                List<HistoryDto> fullHistoryList = historyService.findHistoriesBySeqNumberRange(appDto.getId(), key.getOldSeqNumber(), key.getNewSeqNumber());
                Collections.sort(fullHistoryList, ConcurrentCacheService.HISTORY_SEQ_NUMBER_COMPARATOR);

                for (HistoryDto historyDto : fullHistoryList) {
                    ChangeDto changeDto = historyDto.getChange();
                    ChangeType changeType = changeDto.getType();
                    if (!isSupported(changeType)) {
                        continue;
                    }
                    if (changeType == ChangeType.REMOVE_GROUP) {
                        relatedChanges.add(historyDto);
                    } else if (changeType == ChangeType.ADD_TOPIC || changeType == ChangeType.REMOVE_TOPIC) {
                        relatedChanges.add(historyDto);
                    } else if (changeType == ChangeType.ADD_PROF || changeType == ChangeType.REMOVE_PROF) {
                        if (changeDto.getPfMajorVersion() == key.getProfileSchemaVersion()) {
                            relatedChanges.add(historyDto);
                        }
                    } else if (changeType == ChangeType.ADD_CONF || changeType == ChangeType.REMOVE_CONF) {
                        if (changeDto.getCfMajorVersion() == key.getConfSchemaVersion()) { // NOSONAR
                            relatedChanges.add(historyDto);
                        }
                    }
                }

                putHistory(key, relatedChanges);
                return relatedChanges;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#putHistory
     * (org.kaaproject.kaa.server.operations.service.cache.HistoryKey,
     * java.util.List)
     */
    @Override
    @CachePut(value = "history", key = "#key")
    public List<HistoryDto> putHistory(HistoryKey key, List<HistoryDto> value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#getFilters
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    @Cacheable("filterLists")
    public List<ProfileFilterDto> getFilters(AppVersionKey key) {
        return filterListsMemorizer.compute(key, new Computable<AppVersionKey, List<ProfileFilterDto>>() {

            @Override
            public List<ProfileFilterDto> compute(AppVersionKey key) {
                LOG.debug("Fetching result for getFilters");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                List<ProfileFilterDto> value = profileService.findProfileFilterByAppIdAndVersion(appDto.getId(), key.getVersion());
                putFilterList(key, value);
                return value;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#resetFilters
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    @CacheEvict(value = "filterLists", key = "#key")
    public void resetFilters(AppVersionKey key) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#putFilterList
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey,
     * java.util.List)
     */
    @Override
    @CachePut(value = "filterLists", key = "#key")
    public List<ProfileFilterDto> putFilterList(AppVersionKey key, List<ProfileFilterDto> value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#getFilter
     * (java.lang.String)
     */
    @Override
    @Cacheable("filters")
    public ProfileFilterDto getFilter(String key) {
        return filtersMemorizer.compute(key, new Computable<String, ProfileFilterDto>() {

            @Override
            public ProfileFilterDto compute(String key) {
                LOG.debug("Fetching result for getFilter");
                ProfileFilterDto value = profileService.findProfileFilterById(key);
                putFilter(key, value);
                return value;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#putFilter
     * (java.lang.String, org.kaaproject.kaa.common.dto.ProfileFilterDto)
     */
    @Override
    @CachePut(value = "filters", key = "#key")
    public ProfileFilterDto putFilter(String key, ProfileFilterDto value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#getConfByHash
     * (org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    @Cacheable("configurations")
    public EndpointConfigurationDto getConfByHash(EndpointObjectHash key) {
        return cfMemorizer.compute(key, new Computable<EndpointObjectHash, EndpointConfigurationDto>() {

            @Override
            public EndpointConfigurationDto compute(EndpointObjectHash key) {
                LOG.debug("Fetching result for getConfByHash");
                EndpointConfigurationDto value = endpointService.findEndpointConfigurationByHash(key.getData());
                putConfiguration(key, value);
                return value;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * putConfiguration(org.kaaproject.kaa.common.hash.EndpointObjectHash,
     * org.kaaproject.kaa.common.dto.EndpointConfigurationDto)
     */
    @Override
    @CachePut(value = "configurations", key = "#key")
    public EndpointConfigurationDto putConfiguration(EndpointObjectHash key, EndpointConfigurationDto value) {
        if (value != null && value.getId() == null) {
            LOG.debug("Fetching result for getConfByHash");
            value = endpointService.saveEndpointConfiguration(value);
        }
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getConfSchemaByAppAndVersion
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    @Cacheable("configurationSchemas")
    public ConfigurationSchemaDto getConfSchemaByAppAndVersion(AppVersionKey key) {
        return cfSchemaMemorizer.compute(key, new Computable<AppVersionKey, ConfigurationSchemaDto>() {

            @Override
            public ConfigurationSchemaDto compute(AppVersionKey key) {
                LOG.debug("Fetching result for getConfSchemaByAppAndVersion");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                ConfigurationSchemaDto value = configurationService.findConfSchemaByAppIdAndVersion(appDto.getId(), key.getVersion());
                putConfigurationSchema(key, value);
                return value;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * putConfigurationSchema
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey,
     * org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    @CachePut(value = "configurationSchemas", key = "#key")
    public ConfigurationSchemaDto putConfigurationSchema(AppVersionKey key, ConfigurationSchemaDto value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getProfileSchemaByAppAndVersion
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey)
     */
    @Override
    @Cacheable("profileSchemas")
    public ProfileSchemaDto getProfileSchemaByAppAndVersion(AppVersionKey key) {
        return pfSchemaMemorizer.compute(key, new Computable<AppVersionKey, ProfileSchemaDto>() {

            @Override
            public ProfileSchemaDto compute(AppVersionKey key) {
                LOG.debug("Fetching result for getProfileSchemaByAppAndVersion");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                ProfileSchemaDto value = profileService.findProfileSchemaByAppIdAndVersion(appDto.getId(), key.getVersion());
                putProfileSchema(key, value);
                return value;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * putProfileSchema
     * (org.kaaproject.kaa.server.operations.service.cache.AppVersionKey,
     * org.kaaproject.kaa.common.dto.ProfileSchemaDto)
     */
    @Override
    @CachePut(value = "profileSchemas", key = "#key")
    public ProfileSchemaDto putProfileSchema(AppVersionKey key, ProfileSchemaDto value) {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    @Cacheable("endpointKeys")
    public PublicKey getEndpointKey(EndpointObjectHash key) {
        return endpointKeyMemorizer.compute(key, new Computable<EndpointObjectHash, PublicKey>() {

            @Override
            public PublicKey compute(EndpointObjectHash key) {
                LOG.debug("Fetching result for getEndpointKey");
                PublicKey result = null;
                EndpointProfileDto endpointProfile = endpointService.findEndpointProfileByKeyHash(key.getData());
                if (endpointProfile != null) {
                    try {
                        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(endpointProfile.getEndpointKey());
                        KeyFactory keyFact = KeyFactory.getInstance(ALGORITHM);
                        result = keyFact.generatePublic(x509KeySpec);
                        setEndpointKey(key, result);
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        LOG.error("failed to decode key", e);
                    }
                } else {
                    LOG.error("failed to find key by hash {}", key);
                }
                return result;
            }
        });
    }

    @Override
    @Cacheable("ecfIds")
    public String getEventClassFamilyIdByName(EventClassFamilyIdKey key) {
        return ecfIdKeyMemorizer.compute(key, new Computable<EventClassFamilyIdKey, String>() {

            @Override
            public String compute(EventClassFamilyIdKey key) {
                LOG.debug("Fetching result for getEcfId using key {}", key);
                EventClassFamilyDto ecf = eventClassService.findEventClassFamilyByTenantIdAndName(key.getTenantId(), key.getName());
                if (ecf != null) {
                    return ecf.getId();
                } else {
                    LOG.error("failed to find ecf by tenantId [{}] and name {}", key.getTenantId(), key.getName());
                    return null;
                }
            }
        });
    }

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EventClassFqnKey, String> ecfIdFqnMemorizer = new CacheTemporaryMemorizer<EventClassFqnKey, String>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EventClassFqnVersion, Set<RouteTableKey>> routeKeysMemorizer = new CacheTemporaryMemorizer<EventClassFqnVersion, Set<RouteTableKey>>();

    @Override
    @Cacheable("ecfIds")
    public String getEventClassFamilyIdByEventClassFqn(EventClassFqnKey key) {
        return ecfIdFqnMemorizer.compute(key, new Computable<EventClassFqnKey, String>() {

            @Override
            public String compute(EventClassFqnKey key) {
                LOG.debug("Fetching result for getEventClassFamilyIdByEventClassFqn using key {}", key);
                List<EventClassDto> eventClasses = eventClassService.findEventClassByTenantIdAndFQN(key.getTenantId(), key.getFqn());
                if (eventClasses != null && !eventClasses.isEmpty()) {
                    return eventClasses.get(0).getEcfId();
                } else {
                    LOG.warn("Fetching result for getEcfId using key {} Failed!", key);
                    return null;
                }
            }

        });
    }

    @Override
    @Cacheable("routeKeys")
    public Set<RouteTableKey> getRouteKeys(EventClassFqnVersion key) {
        return routeKeysMemorizer.compute(key, new Computable<EventClassFqnVersion, Set<RouteTableKey>>() {

            @Override
            public Set<RouteTableKey> compute(EventClassFqnVersion key) {
                LOG.debug("Fetching result for getRouteKeys using key {}", key);
                Set<RouteTableKey> routeKeys = new HashSet<>();
                EventClassDto eventClass = eventClassService.findEventClassByTenantIdAndFQNAndVersion(key.getTenantId(), key.getFqn(), key.getVersion());

                String eventClassFamilyId = eventClass.getEcfId();

                List<ApplicationEventFamilyMapDto> mappingList = applicationEventMapService.findByEcfIdAndVersion(eventClassFamilyId, key.getVersion());
                for (ApplicationEventFamilyMapDto mapping : mappingList) {
                    String applicationId = mapping.getApplicationId();
                    ApplicationDto appDto = applicationService.findAppById(applicationId);
                    RouteTableKey routeTableKey = new RouteTableKey(appDto.getApplicationToken(), new EventClassFamilyVersion(eventClassFamilyId, key
                            .getVersion()));
                    if (!routeKeys.contains(routeTableKey)) {
                        for (ApplicationEventMapDto eventMap : mapping.getEventMaps()) {
                            if (eventMap.getEventClassId().equals(eventClass.getId()) && 
                                    (ApplicationEventAction.SINK == eventMap.getAction() || ApplicationEventAction.BOTH == eventMap.getAction())) {
                                routeKeys.add(routeTableKey);
                                break;
                            }
                        }
                    }
                }

                return routeKeys;
            }
        });
    }

    @Override
    @Cacheable("tenantIds")
    public String getTenantIdByAppToken(String key) {
        // TODO Auto-generated method stub
        return tenantIdMemorizer.compute(key, new Computable<String, String>() {

            @Override
            public String compute(String key) {
                LOG.debug("Fetching result for token id");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key);
                return appDto.getTenantId();
            }

        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash,
     * java.security.PublicKey)
     */
    @Override
    public void setEndpointKey(EndpointObjectHash key, PublicKey endpointKey) {
        putEndpointKey(key, endpointKey);
    }

    /**
     * Put endpoint key.
     *
     * @param key
     *            the key
     * @param endpointKey
     *            the endpoint key
     * @return the public key
     */
    @CachePut(value = "endpointKeys", key = "#key")
    public PublicKey putEndpointKey(EndpointObjectHash key, PublicKey endpointKey) {
        return endpointKey;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getMergedConfiguration(java.util.List,
     * org.kaaproject.kaa.server.operations.service.cache.Computable)
     */
    @Override
    @Cacheable(value = "mergedConfigurations", key = "#key")
    public BaseData getMergedConfiguration(final List<EndpointGroupStateDto> key, final Computable<List<EndpointGroupStateDto>, BaseData> worker) {
        return mergedConfigurationMemorizer.compute(key, new Computable<List<EndpointGroupStateDto>, BaseData>() {

            @Override
            public BaseData compute(List<EndpointGroupStateDto> key) {
                LOG.debug("Fetching result for getMergedConfiguration");
                BaseData result = worker.compute(key);
                setMergedConfiguration(key, result);
                return result;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setMergedConfiguration(java.util.List, java.lang.String)
     */
    @Override
    @CachePut(value = "mergedConfigurations", key = "#key")
    public BaseData setMergedConfiguration(List<EndpointGroupStateDto> key, BaseData mergedConfiguration) {
        return mergedConfiguration;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#getDelta
     * (org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey,
     * org.kaaproject.kaa.server.operations.service.cache.Computable)
     */
    @Override
    @Cacheable(value = "deltas", key = "#key")
    public DeltaCacheEntry getDelta(final DeltaCacheKey key, final Computable<DeltaCacheKey, DeltaCacheEntry> worker) throws GetDeltaException {
        DeltaCacheEntry deltaCacheEntry = deltaMemorizer.compute(key, new Computable<DeltaCacheKey, DeltaCacheEntry>() { //NOSONAR
            @Override
            public DeltaCacheEntry compute(DeltaCacheKey key) {
                LOG.debug("Fetching result for getMergedConfiguration");
                DeltaCacheEntry result = worker.compute(key);
                setDelta(key, result);
                return result;
            }
        });

        return deltaCacheEntry;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.cache.CacheService#setDelta
     * (org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey,
     * org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry)
     */
    @Override
    @CachePut(value = "deltas", key = "#key")
    public DeltaCacheEntry setDelta(DeltaCacheKey key, DeltaCacheEntry delta) {
        return delta;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setApplicationService
     * (org.kaaproject.kaa.server.common.dao.ApplicationService)
     */
    @Override
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setConfigurationService
     * (org.kaaproject.kaa.server.common.dao.ConfigurationService)
     */
    @Override
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setHistoryService(org.kaaproject.kaa.server.common.dao.HistoryService)
     */
    @Override
    public void setHistoryService(HistoryService historyService) {
        this.historyService = historyService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setProfileService(org.kaaproject.kaa.server.common.dao.ProfileService)
     */
    @Override
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * setEndpointService(org.kaaproject.kaa.server.common.dao.EndpointService)
     */
    @Override
    public void setEndpointService(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @Override
    public void setEventClassService(EventClassService eventClassService) {
        this.eventClassService = eventClassService;
    }

    @Override
    public void setApplicationEventMapService(ApplicationEventMapService applicationEventMapService) {
        this.applicationEventMapService = applicationEventMapService;
    }

    /**
     * Checks if is supported.
     *
     * @param changeType
     *            the change type
     * @return true, if is supported
     */
    public static boolean isSupported(ChangeType changeType) {
        switch (changeType) {
        case ADD_CONF:
        case ADD_PROF:
        case ADD_TOPIC:
        case REMOVE_CONF:
        case REMOVE_PROF:
        case REMOVE_TOPIC:
        case REMOVE_GROUP:
            return true;
        default:
            return false;
        }
    }
}
