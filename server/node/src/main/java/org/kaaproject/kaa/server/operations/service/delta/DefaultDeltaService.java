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

package org.kaaproject.kaa.server.operations.service.delta;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
import org.kaaproject.kaa.server.common.core.algorithms.delta.BaseBinaryDelta;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideException;
import org.kaaproject.kaa.server.common.core.configuration.AbstractKaaData;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideData;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.core.structure.Pair;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.UserConfigurationService;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse.GetDeltaResponseType;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.ConfigurationCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link DeltaService}. Delta calculation process is quite
 * resource consuming. In order to minimize amount of delta calculations,
 * certain caching logic is used.
 *
 * @author ashvayka
 *
 */
@Service
public class DefaultDeltaService implements DeltaService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDeltaService.class);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private UserConfigurationService userConfigurationService;

    @Autowired
    private EndpointService endpointService;


    @Autowired
    private OverrideAlgorithmFactory configurationOverrideFactory;


    private static final Comparator<EndpointGroupDto> ENDPOINT_GROUP_COMPARATOR = new Comparator<EndpointGroupDto>() {

        @Override
        public int compare(EndpointGroupDto o1, EndpointGroupDto o2) {
            if (o1.getWeight() < o2.getWeight()) {
                return -1;
            }
            if (o1.getWeight() > o2.getWeight()) {
                return 1;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    /**
     * Instantiates a new default delta service.
     */
    public DefaultDeltaService() {
        super();
    }

    @Override
    public ConfigurationCacheEntry getConfiguration(String appToken, String endpointId, EndpointProfileDto profile) throws GetDeltaException {
        LOG.debug("[{}][{}] Calculating new configuration", appToken, endpointId);
        AppVersionKey appConfigVersionKey = new AppVersionKey(appToken, profile.getConfigurationVersion());
        DeltaCacheKey deltaKey = new DeltaCacheKey(appConfigVersionKey, profile.getGroupState(), EndpointObjectHash.fromBytes(profile
                .getUserConfigurationHash()), null, profile.isUseConfigurationRawSchema(), true);
        LOG.debug("[{}][{}] Built resync delta key {}", appToken, endpointId, deltaKey);
        return getDelta(endpointId, profile.getEndpointUserId(), deltaKey, profile.isUseConfigurationRawSchema());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.delta.DeltaService#getDelta
     * (org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest,
     * org.kaaproject.kaa.server.operations.service.delta.HistoryDelta, int)
     */
    @Override
    public GetDeltaResponse getDelta(GetDeltaRequest request) throws GetDeltaException {
        GetDeltaResponse response;
        EndpointProfileDto profile = request.getEndpointProfile();
        String endpointId = "N/A";
        if (LOG.isDebugEnabled() && profile != null && profile.getEndpointKeyHash() != null) {
            endpointId = Base64Util.encode(profile.getEndpointKeyHash());
        }
        LOG.debug("[{}][{}] Processing configuration request", request.getApplicationToken(), endpointId);
        boolean resync = false;
        if (request.isFirstRequest()) {
            resync = true;
        } else if (!request.getConfigurationHash().binaryEquals(profile.getConfigurationHash())) {
            logHashMismatch(request, profile, endpointId);
            resync = true;
        }
        if (resync) {
            EndpointConfigurationDto configurationDto = cacheService.getConfByHash(EndpointObjectHash.fromBytes(profile
                    .getConfigurationHash()));
            response = new GetDeltaResponse(GetDeltaResponseType.CONF_RESYNC, new BaseBinaryDelta(configurationDto.getConfiguration()));
        } else {
            response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA);
        }
        LOG.debug("[{}][{}] Processed configuration request {}", request.getApplicationToken(), endpointId, response.getResponseType());
        return response;
    }

    private void logHashMismatch(GetDeltaRequest request, EndpointProfileDto profile, String endpointId) {
        if (profile.getConfigurationHash() != null && LOG.isWarnEnabled()) {
            String serverHash = "";
            String clientHash = "";
            if (profile.getConfigurationHash() != null) {
                serverHash = MessageEncoderDecoder.bytesToHex(profile.getConfigurationHash());
            }
            if (request.getConfigurationHash() != null) {
                clientHash = MessageEncoderDecoder.bytesToHex(request.getConfigurationHash().getData());
            }
            LOG.warn("[{}] Configuration hash mismatch! server {}, client {}", endpointId, serverHash, clientHash);
        }
    }

    /**
     * Calculate delta.
     *
     * @param deltaKey
     *            the delta key
     * @return the delta cache entry
     * @throws GetDeltaException
     *             the get delta exception
     */
    private ConfigurationCacheEntry getDelta(final String endpointId, final String userId, DeltaCacheKey deltaKey, boolean useConfigurationRawSchema) throws GetDeltaException {
        EndpointUserConfigurationDto userConfiguration = findLatestUserConfiguration(userId, deltaKey);

        final DeltaCacheKey newKey;
        if (userConfiguration != null) {
            newKey = new DeltaCacheKey(
                    deltaKey.getAppConfigVersionKey(),
                    deltaKey.getEndpointGroups(),
                    EndpointObjectHash.fromString(userConfiguration.getBody()),
                    deltaKey.getEndpointConfHash(),
                    useConfigurationRawSchema,
                    deltaKey.isResyncOnly()
            );
        } else {
            newKey = deltaKey;
        }

        ConfigurationCacheEntry deltaCacheEntry = cacheService.getDelta(newKey, new Computable<DeltaCacheKey, ConfigurationCacheEntry>() { // NOSONAR
                    @Override
                    public ConfigurationCacheEntry compute(DeltaCacheKey deltaKey) {
                        try {
                            LOG.debug("[{}] Calculating delta for {}", endpointId, deltaKey);
                            ConfigurationCacheEntry deltaCache;
                            AbstractKaaData<?> data;
                            ConfigurationSchemaDto latestConfigurationSchema = cacheService.getConfSchemaByAppAndVersion(deltaKey.getAppConfigVersionKey());

                            EndpointUserConfigurationDto userConfiguration = findLatestUserConfiguration(userId, deltaKey);

                            EndpointObjectHash userConfigurationHash = null;
                            if (userConfiguration != null) {
                                userConfigurationHash = EndpointObjectHash.fromString(userConfiguration.getBody());
                            }

                            Pair<BaseData, RawData> mergedConfiguration = getMergedConfiguration(endpointId, userConfiguration, deltaKey, latestConfigurationSchema);

                            if(useConfigurationRawSchema) {
                                data = mergedConfiguration.getV2();
                            } else {
                                data = mergedConfiguration.getV1();
                            }

                            LOG.trace("[{}] Merged configuration {}", endpointId, data.getRawData());
                            deltaCache = buildBaseResyncDelta(endpointId, data.getRawData(), data.getSchema().getRawSchema(), userConfigurationHash);
                            
                            if (cacheService.getConfByHash(deltaCache.getHash()) == null) {
                                EndpointConfigurationDto newConfiguration = new EndpointConfigurationDto();
                                newConfiguration.setConfiguration(deltaCache.getConfiguration());
                                newConfiguration.setConfigurationHash(deltaCache.getHash().getData());
                                cacheService.putConfiguration(deltaCache.getHash(), newConfiguration);
                            }

                            LOG.debug("[{}] Configuration hash for {} is {}", endpointId, deltaKey,
                                    MessageEncoderDecoder.bytesToHex(deltaCache.getHash().getData()));
                            return deltaCache;
                        } catch (GetDeltaException | IOException e) {
                            throw new RuntimeException(e); // NOSONAR
                        }
                    }
                });

        return deltaCacheEntry;
    }

    private EndpointUserConfigurationDto findLatestUserConfiguration(final String userId, DeltaCacheKey deltaKey) {
        EndpointUserConfigurationDto userConfiguration = null;
        if (userId != null) {
            userConfiguration = userConfigurationService.findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(userId, deltaKey
                    .getAppConfigVersionKey().getApplicationToken(), deltaKey.getAppConfigVersionKey().getVersion());
            if (userConfiguration != null) {
                LOG.debug("[{}] User specific configuration found", userId);
            } else {
                LOG.debug("[{}] No user configuration found ", userId);
            }
        }
        return userConfiguration;
    }

    private BaseData processEndpointGroups(List<EndpointGroupDto> endpointGroups, List<ConfigurationDto> configurations,
            ConfigurationSchemaDto configurationSchema) throws OverrideException, IOException {
        // create sorted map to store configurations sorted by endpoint group
        // weight
        // put all endpoint groups as keys into the map
        Collections.sort(endpointGroups, ENDPOINT_GROUP_COMPARATOR);
        List<OverrideData> overrideConfigs = new LinkedList<>();
        BaseData baseConfig = null;
        OverrideSchema overrideSchema = new OverrideSchema(configurationSchema.getOverrideSchema());
        BaseSchema baseSchema = new BaseSchema(configurationSchema.getBaseSchema());
        // put configurations into the map under corresponding endpoint group
        for (EndpointGroupDto endpointGroup : endpointGroups) {
            boolean endpointGroupFound = false;
            for (ConfigurationDto configuration : configurations) {
                if (configuration.getEndpointGroupId().equals(endpointGroup.getId())) {
                    if (endpointGroup.getWeight() != 0) {
                        overrideConfigs.add(new OverrideData(overrideSchema, configuration.getBody()));
                    } else {
                        baseConfig = new BaseData(baseSchema, configuration.getBody());
                    }
                    endpointGroupFound = true;
                    break;
                }
            }
            if (!endpointGroupFound) {
                LOG.debug("No Configuration found for Endpoint Group; Endpoint Group Id: {}", endpointGroup.getId());
            }
        }

        OverrideAlgorithm configurationMerger = configurationOverrideFactory.createConfigurationOverrideAlgorithm();
        return configurationMerger.override(baseConfig, overrideConfigs);
    }

    /**
     * Gets the latest conf from cache
     *
     * @param endpointId
     * @param userConfiguration
     * @param cacheKey
     * @return the latest conf from cache
     * @throws GetDeltaException
     */
    private Pair<BaseData, RawData> getMergedConfiguration(final String endpointId, final EndpointUserConfigurationDto userConfiguration,
                                            final DeltaCacheKey cacheKey, ConfigurationSchemaDto latestConfigurationSchema) throws GetDeltaException {

        final List<EndpointGroupStateDto> egsList = cacheKey.getEndpointGroups();
        // return Pair in order to cache both calculated configuration and optimize performance
        Pair<BaseData, RawData> mergedConfiguration = cacheService.getMergedConfiguration(egsList,
                new Computable<List<EndpointGroupStateDto>, Pair<BaseData, RawData>>() {

                    @Override
                    public  Pair<BaseData, RawData> compute(List<EndpointGroupStateDto> key) {
                        LOG.trace("[{}] getMergedConfiguration.compute begin", endpointId);
                        try {
                            List<EndpointGroupDto> endpointGroups = new ArrayList<>();
                            List<ConfigurationDto> configurations = new ArrayList<>();
                            ConfigurationSchemaDto configurationSchema = null;
                            for (EndpointGroupStateDto egs : egsList) {
                                EndpointGroupDto endpointGroup = null;
                                if (!StringUtils.isBlank(egs.getEndpointGroupId())) {
                                    endpointGroup = endpointService.findEndpointGroupById(egs.getEndpointGroupId());
                                    if (endpointGroup != null) {
                                        endpointGroups.add(endpointGroup);
                                    }
                                }

                                ConfigurationDto configuration = null;
                                if (!StringUtils.isBlank(egs.getConfigurationId())) {
                                    configuration = configurationService.findConfigurationById(egs.getConfigurationId());
                                    if (configuration != null) {
                                        configurations.add(configuration);
                                    }
                                }

                                if (configurationSchema == null && configuration != null) {
                                    configurationSchema = configurationService.findConfSchemaById(configuration.getSchemaId());
                                }
                            }
                            BaseData baseData = processEndpointGroups(endpointGroups, configurations, configurationSchema);

                            // converting merged base schema to raw schema
                            String ctlSchema = cacheService.getFlatCtlSchemaById(latestConfigurationSchema.getCtlSchemaId());
                            JsonNode json = new ObjectMapper().readTree(baseData.getRawData());
                            AvroUtils.removeUuids(json);
                            RawData rawData = new RawData(new RawSchema(ctlSchema), json.toString());

                            return new Pair<>(baseData, rawData);
                        } catch (OverrideException | IOException oe) {
                            LOG.error("[{}] Unexpected exception occurred while merging configuration: ", endpointId, oe);
                            throw new RuntimeException(oe); // NOSONAR
                        } finally {
                            LOG.trace("[{}] getMergedGroupConfiguration.compute end", endpointId);
                        }
                    }
                });

        if (userConfiguration != null) {
            OverrideAlgorithm configurationMerger = configurationOverrideFactory.createConfigurationOverrideAlgorithm();
            OverrideSchema overrideSchema = new OverrideSchema(latestConfigurationSchema.getOverrideSchema());
            try {
                LOG.trace("Merging group configuration with user configuration: {}", userConfiguration.getBody());
                BaseData baseData = configurationMerger.override(mergedConfiguration.getV1(),
                        Collections.singletonList(new OverrideData(overrideSchema, userConfiguration.getBody())));

                JsonNode json = new ObjectMapper().readTree(baseData.getRawData());
                AvroUtils.removeUuids(json);
                RawData rawData = new RawData(new RawSchema(mergedConfiguration.getV2().getSchema().getRawSchema()), baseData.getRawData());

                mergedConfiguration = new Pair<>(baseData, rawData);
            } catch (OverrideException | IOException oe) {
                LOG.error("[{}] Unexpected exception occurred while merging configuration: ", endpointId, oe);
                throw new GetDeltaException(oe);
            } finally {
                LOG.trace("[{}] getMergedConfiguration.compute end", endpointId);
            }
        }
        return mergedConfiguration;
    }

    private ConfigurationCacheEntry buildBaseResyncDelta(String endpointId, String jsonData, String schema, EndpointObjectHash userConfigurationHash) throws IOException {
        byte[] configuration = GenericAvroConverter.toRawData(jsonData, schema);
        return new ConfigurationCacheEntry(configuration, new BaseBinaryDelta(configuration), EndpointObjectHash.fromSHA1(configuration),
                userConfigurationHash);
    }

}
