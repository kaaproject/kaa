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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.HistoryDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicListEntryDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventAction;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventMapDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.structure.Pair;
import org.kaaproject.kaa.server.common.dao.ApplicationEventMapService;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.EventClassService;
import org.kaaproject.kaa.server.common.dao.HistoryService;
import org.kaaproject.kaa.server.common.dao.ProfileService;
import org.kaaproject.kaa.server.common.dao.SdkProfileService;
import org.kaaproject.kaa.server.common.dao.ServerProfileService;
import org.kaaproject.kaa.server.common.dao.TopicService;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppProfileVersionsKey;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationIdKey;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFqnKey;
import org.kaaproject.kaa.server.operations.service.cache.HistoryKey;
import org.kaaproject.kaa.server.operations.service.cache.TopicListCacheEntry;
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

    /** The Constant LOG. */
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

    /** The topic service. */
    @Autowired
    private TopicService topicService;

    /** The profile service. */
    @Autowired
    private ProfileService profileService;

    /** The server profile service. */
    @Autowired
    private ServerProfileService serverProfileService;

    @Autowired
    private CTLService ctlService;

    /** The history service. */
    @Autowired
    private HistoryService historyService;

    @Autowired
    private EventClassService eventClassService;

    @Autowired
    private ApplicationEventMapService applicationEventMapService;

    @Autowired
    private SdkProfileService sdkProfileService;

    /** The app seq number memorizer. */
    private final CacheTemporaryMemorizer<String, AppSeqNumber> appSeqNumberMemorizer = new CacheTemporaryMemorizer<>();

    /** The cf id memorizer. */
    private final CacheTemporaryMemorizer<ConfigurationIdKey, String> cfIdMemorizer = new CacheTemporaryMemorizer<>();

    /** The history memorizer. */
    private final CacheTemporaryMemorizer<HistoryKey, List<HistoryDto>> historyMemorizer = new CacheTemporaryMemorizer<>();

    /** The filter lists memorizer. */
    private final CacheTemporaryMemorizer<AppProfileVersionsKey, List<ProfileFilterDto>> filterListsMemorizer = new CacheTemporaryMemorizer<>();

    /** The application event family maps memorizer. */
    private final CacheTemporaryMemorizer<List<String>, List<ApplicationEventFamilyMapDto>> aefmMemorizer = new CacheTemporaryMemorizer<>();

    /** The filters memorizer. */
    private final CacheTemporaryMemorizer<String, ProfileFilterDto> filtersMemorizer = new CacheTemporaryMemorizer<>();

    /** The cf memorizer. */
    private final CacheTemporaryMemorizer<EndpointObjectHash, EndpointConfigurationDto> cfMemorizer = new CacheTemporaryMemorizer<>();

    /** The cf schema memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, ConfigurationSchemaDto> cfSchemaMemorizer = new CacheTemporaryMemorizer<>();

    /** The pf schema memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, EndpointProfileSchemaDto> pfSchemaMemorizer = new CacheTemporaryMemorizer<>();

    /** The spf schema memorizer. */
    private final CacheTemporaryMemorizer<AppVersionKey, ServerProfileSchemaDto> spfSchemaMemorizer = new CacheTemporaryMemorizer<>();

    /** The ctl schema memorizer. */
    private final CacheTemporaryMemorizer<String, CTLSchemaDto> ctlSchemaMemorizer = new CacheTemporaryMemorizer<>();

    /** The ctl schema body memorizer. */
    private final CacheTemporaryMemorizer<String, String> ctlSchemaBodyMemorizer = new CacheTemporaryMemorizer<>();

    /** The SDK profile memorizer */
    private final CacheTemporaryMemorizer<String, SdkProfileDto> sdkProfileMemorizer = new CacheTemporaryMemorizer<>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EndpointObjectHash, PublicKey> endpointKeyMemorizer = new CacheTemporaryMemorizer<>();

    /** The merged configuration memorizer. */
    private final CacheTemporaryMemorizer<List<EndpointGroupStateDto>, Pair<BaseData, RawData>> mergedConfigurationMemorizer = new CacheTemporaryMemorizer<>();

    /** The delta memorizer. */
    private final CacheTemporaryMemorizer<DeltaCacheKey, ConfigurationCacheEntry> deltaMemorizer = new CacheTemporaryMemorizer<>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<EventClassFamilyIdKey, String> ecfIdKeyMemorizer = new CacheTemporaryMemorizer<>();

    /** The endpoint key memorizer. */
    private final CacheTemporaryMemorizer<String, String> tenantIdMemorizer = new CacheTemporaryMemorizer<>();

    /** The application token memorizer. */
    private final CacheTemporaryMemorizer<String, String> appTokenMemorizer = new CacheTemporaryMemorizer<>();

    /** The endpoint groups memorizer. */
    private final CacheTemporaryMemorizer<String, EndpointGroupDto> groupsMemorizer = new CacheTemporaryMemorizer<>();

    /** The topics memorizer. */
    private final CacheTemporaryMemorizer<String, TopicDto> topicsMemorizer = new CacheTemporaryMemorizer<>();

    /** The default group memorizer. */
    private final CacheTemporaryMemorizer<String, EndpointGroupDto> defaultGroupMemorizer = new CacheTemporaryMemorizer<>();

    /** The topic list memorizer. */
    private final CacheTemporaryMemorizer<EndpointObjectHash, TopicListCacheEntry> topicListMemorizer = new CacheTemporaryMemorizer<>();

    /** The history seq number comparator. */
    public static final Comparator<HistoryDto> HISTORY_SEQ_NUMBER_COMPARATOR = (o1, o2) -> {
        if (o1.getSequenceNumber() > o2.getSequenceNumber()) {
            return 1;
        } else {
            return o1.getSequenceNumber() == o2.getSequenceNumber() ? 0 : -1;
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
                AppSeqNumber appSeqNumber = new AppSeqNumber(appDto.getTenantId(), appDto.getId(), appDto.getApplicationToken(), appDto
                        .getSequenceNumber());
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
                    if (confDto.getSchemaVersion() == key.getConfigSchemaVersion()) {
                        confId = confDto.getId();
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
                List<HistoryDto> fullHistoryList = historyService.findHistoriesBySeqNumberRange(appDto.getId(), key.getOldSeqNumber(),
                        key.getNewSeqNumber());
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
                        ProfileFilterDto profileFilter = profileService.findProfileFilterById(changeDto.getProfileFilterId());
                        if (profileFilter != null && supports(profileFilter, key.getEndpointProfileSchemaVersion(), key.getServerProfileSchemaVersion())) {
                            relatedChanges.add(historyDto);
                        }
                    } else if (changeType == ChangeType.ADD_CONF || changeType == ChangeType.REMOVE_CONF) {
                        if (changeDto.getCfVersion() == key.getConfSchemaVersion()) { // NOSONAR
                            relatedChanges.add(historyDto);
                        }
                    }
                }

                return relatedChanges;
            }

            private boolean supports(ProfileFilterDto profileFilter, Integer endpointProfileSchemaVersion, Integer serverProfileSchemaVersion) {
                return (profileFilter.getEndpointProfileSchemaVersion() == null || profileFilter.getEndpointProfileSchemaVersion() == endpointProfileSchemaVersion)
                        && (profileFilter.getServerProfileSchemaVersion() == null || profileFilter.getServerProfileSchemaVersion() == serverProfileSchemaVersion);
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

    @Override
    @Cacheable("applicationEFMs")
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByIds(List<String> key) {
        return aefmMemorizer.compute(key, new Computable<List<String>, List<ApplicationEventFamilyMapDto>>() {
            @Override
            public List<ApplicationEventFamilyMapDto> compute(List<String> key) {
                LOG.debug("Fetching result for getApplicationEventFamilyMapsByIds");
                List<ApplicationEventFamilyMapDto> value = applicationEventMapService.findApplicationEventFamilyMapsByIds(key);
                putApplicationEventFamilyMaps(key, value);
                return value;
            }
        });
    }

    @Override
    @CachePut(value = "applicationEFMs", key = "#key")
    public List<ApplicationEventFamilyMapDto> putApplicationEventFamilyMaps(List<String> key, List<ApplicationEventFamilyMapDto> value) {
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
    public List<ProfileFilterDto> getFilters(AppProfileVersionsKey key) {
        return filterListsMemorizer.compute(key, new Computable<AppProfileVersionsKey, List<ProfileFilterDto>>() {

            @Override
            public List<ProfileFilterDto> compute(AppProfileVersionsKey key) {
                LOG.debug("Fetching result for getFilters");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                List<ProfileFilterDto> value = profileService.findProfileFiltersByAppIdAndVersionsCombination(appDto.getId(),
                        key.getEndpointProfileSchemaVersion(), key.getServerProfileSchemaVersion());
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
    public void resetFilters(AppProfileVersionsKey key) {
        // Do nothing
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
    public List<ProfileFilterDto> putFilterList(AppProfileVersionsKey key, List<ProfileFilterDto> value) {
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
                LOG.debug("Fetching result for getConfByHash {}", key);
                EndpointConfigurationDto value = endpointService.findEndpointConfigurationByHash(key.getData());
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
        if (value != null) {
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
    @Cacheable("endpointProfileSchemas")
    public EndpointProfileSchemaDto getProfileSchemaByAppAndVersion(AppVersionKey key) {
        return pfSchemaMemorizer.compute(key, new Computable<AppVersionKey, EndpointProfileSchemaDto>() {

            @Override
            public EndpointProfileSchemaDto compute(AppVersionKey key) {
                LOG.debug("Fetching result for getProfileSchemaByAppAndVersion");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                EndpointProfileSchemaDto value = profileService.findProfileSchemaByAppIdAndVersion(appDto.getId(), key.getVersion());
                return value;
            }
        });
    }

    @Override
    @Cacheable("serverProfileSchemas")
    public ServerProfileSchemaDto getServerProfileSchemaByAppAndVersion(AppVersionKey key) {
        return spfSchemaMemorizer.compute(key, new Computable<AppVersionKey, ServerProfileSchemaDto>() {

            @Override
            public ServerProfileSchemaDto compute(AppVersionKey key) {
                LOG.debug("Fetching result for getServerProfileSchemaByAppAndVersion");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key.getApplicationToken());
                ServerProfileSchemaDto value = serverProfileService.findServerProfileSchemaByAppIdAndVersion(appDto.getId(),
                        key.getVersion());
                return value;
            }
        });
    }

    @Override
    @Cacheable("ctlSchemas")
    public CTLSchemaDto getCtlSchemaById(String key) {
        return ctlSchemaMemorizer.compute(key, new Computable<String, CTLSchemaDto>() {
            @Override
            public CTLSchemaDto compute(String key) {
                LOG.debug("Fetching result for ctl schemas");
                return ctlService.findCTLSchemaById(key);
            }
        });
    }

    @Override
    @Cacheable("ctlSchemaBodies")
    public String getFlatCtlSchemaById(String key) {
        return ctlSchemaBodyMemorizer.compute(key, new Computable<String, String>() {
            @Override
            public String compute(String key) {
                LOG.debug("Fetching result for ctl schemas");
                CTLSchemaDto ctlSchema = ctlService.findCTLSchemaById(key);
                return ctlService.flatExportAsString(ctlSchema);
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
    @CachePut(value = "endpointProfileSchemas", key = "#key")
    public EndpointProfileSchemaDto putProfileSchema(AppVersionKey key, EndpointProfileSchemaDto value) {
        return value;
    }

    @Override
    @Cacheable(value = "sdkProfiles", unless="#result == null")
    public SdkProfileDto getSdkProfileBySdkToken(String key) {
        return sdkProfileMemorizer.compute(key, new Computable<String, SdkProfileDto>() {
            @Override
            public SdkProfileDto compute(String key) {
                LOG.debug("Fetching result for getSdkProfileBySdkToken");
                return sdkProfileService.findSdkProfileByToken(key);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.cache.CacheService#
     * getEndpointKey(org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    @Cacheable(value = "endpointKeys", unless = "#result == null")
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
                EventClassDto eventClass = eventClassService.findEventClassByTenantIdAndFQNAndVersion(key.getTenantId(), key.getFqn(),
                        key.getVersion());

                String eventClassFamilyId = eventClass.getEcfId();

                List<ApplicationEventFamilyMapDto> mappingList = applicationEventMapService.findByEcfIdAndVersion(eventClassFamilyId,
                        key.getVersion());
                for (ApplicationEventFamilyMapDto mapping : mappingList) {
                    String applicationId = mapping.getApplicationId();
                    ApplicationDto appDto = applicationService.findAppById(applicationId);
                    RouteTableKey routeTableKey = new RouteTableKey(appDto.getApplicationToken(), new EventClassFamilyVersion(
                            eventClassFamilyId, key.getVersion()));
                    if (!routeKeys.contains(routeTableKey)) {
                        for (ApplicationEventMapDto eventMap : mapping.getEventMaps()) {
                            if (eventMap.getEventClassId().equals(eventClass.getId())
                                    && (ApplicationEventAction.SINK == eventMap.getAction() || ApplicationEventAction.BOTH == eventMap
                                            .getAction())) {
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
        // TODO: throw exception instead of returning null
        return tenantIdMemorizer.compute(key, new Computable<String, String>() {

            @Override
            public String compute(String key) {
                LOG.debug("Fetching result for token id");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key);
                return appDto != null ? appDto.getTenantId() : null;
            }

        });
    }

    @Override
    @Cacheable("appTokens")
    public String getAppTokenBySdkToken(String key) {
        return appTokenMemorizer.compute(key, new Computable<String, String>() {

            @Override
            public String compute(String key) {
                LOG.debug("Fetching result for sdk token: {} to retrieve application token", key);
                SdkProfileDto sdkProfileDto = sdkProfileService.findSdkProfileByToken(key);
                String appToken = sdkProfileDto != null ? sdkProfileDto.getApplicationToken() : null;
                LOG.trace("Resolved application token: {}", appToken);
                return appToken;
            }

        });
    }
    
    @Override
    @Cacheable("apps")
    public ApplicationDto findAppById(String applicationId) {
        return applicationService.findAppById(applicationId);
    }

    @Override
    @CacheEvict(value = "apps", key = "#applicationId")
    public void resetAppById(String applicationId) {
        return;
    }
    
    @Override
    @Cacheable("appIds")
    public String getApplicationIdByAppToken(String key) {
        return appTokenMemorizer.compute(key, new Computable<String, String>() {

            @Override
            public String compute(String key) {
                LOG.debug("Fetching result for token id");
                ApplicationDto appDto = applicationService.findAppByApplicationToken(key);
                return appDto != null ? appDto.getId() : null;
            }
        });
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
    @Override
    @CachePut(value = "endpointKeys", key = "#key")
    public PublicKey putEndpointKey(EndpointObjectHash key, PublicKey endpointKey) {
        return endpointKey;
    }

    /**
     *
     * Remove key from hash
     *
     * @param hash
     * @param endpointKey
     */
    @Override
    @CacheEvict(value = "endpointKeys", key = "#key")
    public void resetEndpointKey(EndpointObjectHash hash, PublicKey endpointKey){
        // Do nothing
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
    public Pair<BaseData, RawData> getMergedConfiguration(final List<EndpointGroupStateDto> key,
                                                          final Computable<List<EndpointGroupStateDto>, Pair<BaseData, RawData>> worker) {
        return mergedConfigurationMemorizer.compute(key, new Computable<List<EndpointGroupStateDto>, Pair<BaseData, RawData>>() {
            @Override
            public Pair<BaseData, RawData> compute(List<EndpointGroupStateDto> key) {
                LOG.debug("Fetching result for getMergedConfiguration");
                Pair<BaseData, RawData> result = worker.compute(key);
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
    public ConfigurationCacheEntry getDelta(final DeltaCacheKey key, final Computable<DeltaCacheKey, ConfigurationCacheEntry> worker)
            throws GetDeltaException {
        ConfigurationCacheEntry deltaCacheEntry = deltaMemorizer.compute(key, new Computable<DeltaCacheKey, ConfigurationCacheEntry>() { // NOSONAR
                    @Override
                    public ConfigurationCacheEntry compute(DeltaCacheKey key) {
                        LOG.debug("Fetching result for getMergedConfiguration");
                        ConfigurationCacheEntry result = worker.compute(key);
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
    public ConfigurationCacheEntry setDelta(DeltaCacheKey key, ConfigurationCacheEntry delta) {
        return delta;
    }

    @Override
    @CacheEvict(value = "endpointGroups", key = "#key")
    public void resetGroup(String key) {
        // Do nothing
    }

    @Override
    @CachePut(value = "endpointGroups", key = "#key")
    public EndpointGroupDto putEndpointGroup(String key, EndpointGroupDto value) {
        return value;
    }

    @Override
    @Cacheable("endpointGroups")
    public EndpointGroupDto getEndpointGroupById(String endpointGroupId) {
        return groupsMemorizer.compute(endpointGroupId, new Computable<String, EndpointGroupDto>() {
            @Override
            public EndpointGroupDto compute(String key) {
                LOG.debug("Fetching result for token id");
                EndpointGroupDto groupDto = endpointService.findEndpointGroupById(key);
                return groupDto;
            }

        });
    }

    @Override
    @CachePut(value = "topics", key = "#key")
    public TopicDto putTopic(String key, TopicDto value) {
        return value;
    }

    @Override
    @Cacheable("topics")
    public TopicDto getTopicById(String topicId) {
        return topicsMemorizer.compute(topicId, new Computable<String, TopicDto>() {
            @Override
            public TopicDto compute(String key) {
                LOG.debug("Fetching result for token id");
                TopicDto topicDto = topicService.findTopicById(key);
                return topicDto;
            }
        });
    }

    @Override
    @Cacheable("defaultGroups")
    public EndpointGroupDto getDefaultGroup(String applicationToken) {
        return defaultGroupMemorizer.compute(applicationToken, applicationToken1 -> {
            LOG.debug("Fetching result for token id");
            ApplicationDto appDto = applicationService.findAppByApplicationToken(applicationToken1);
            return endpointService.findDefaultGroup(appDto.getId());
        });
    }

    @Override
    @CachePut(value = "topicListEntries", key = "#key")
    public TopicListCacheEntry putTopicList(EndpointObjectHash key, TopicListCacheEntry entry) {
        if (entry != null) {
            TopicListEntryDto entryDto = new TopicListEntryDto(entry.getSimpleHash(), entry.getHash().getData(), entry.getTopics());
            endpointService.saveTopicListEntry(entryDto);
        }
        return entry;
    }

    @Override
    @Cacheable("topicListEntries")
    public TopicListCacheEntry getTopicListByHash(EndpointObjectHash hash) {
        return topicListMemorizer.compute(hash, new Computable<EndpointObjectHash, TopicListCacheEntry>() {
            @Override
            public TopicListCacheEntry compute(EndpointObjectHash key) {
                LOG.debug("Fetching result for getTopicListByHash {}", key);
                TopicListEntryDto entryDto = endpointService.findTopicListEntryByHash(key.getData());
                if (entryDto != null) {
                    return new TopicListCacheEntry(entryDto.getSimpleHash(), EndpointObjectHash.fromBytes(entryDto.getHash()), entryDto.getTopics());
                } else {
                    return null;
                }
            }
        });
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

    @Override
    public void setSdkProfileService(SdkProfileService sdkProfileService) {
        this.sdkProfileService = sdkProfileService;
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
