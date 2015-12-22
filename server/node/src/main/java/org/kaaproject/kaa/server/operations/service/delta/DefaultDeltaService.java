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

package org.kaaproject.kaa.server.operations.service.delta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.core.algorithms.delta.BaseBinaryDelta;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculatorException;
import org.kaaproject.kaa.server.common.core.algorithms.delta.DeltaCalculatorFactory;
import org.kaaproject.kaa.server.common.core.algorithms.delta.RawBinaryDelta;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.override.OverrideException;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;
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
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry;
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

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDeltaService.class);

    /** The cache service. */
    @Autowired
    private CacheService cacheService;
    /** The configuration service. */
    @Autowired
    private ConfigurationService configurationService;
    /** The user configuration service. */
    @Autowired
    private UserConfigurationService userConfigurationService;
    /** The profile service. */
    @Autowired
    private EndpointService endpointService;
    /** The delta calculator factory. */
    @Autowired
    private DeltaCalculatorFactory deltaCalculatorFactory;

    /** The configuration merger factory. */
    @Autowired
    private OverrideAlgorithmFactory configurationOverrideFactory;

    /** The Constant ENDPOINT_GROUP_COMPARATOR. */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.operations.service.delta.DeltaService#getDelta
     * (org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest,
     * org.kaaproject.kaa.server.operations.service.delta.HistoryDelta, int)
     */
    @Override
    public GetDeltaResponse getDelta(GetDeltaRequest request, HistoryDelta historyDelta, int curAppSeqNumber) throws GetDeltaException {
        GetDeltaResponse response;
        EndpointProfileDto profile = request.getEndpointProfile();
        String endpointId = "N/A";
        if (LOG.isDebugEnabled() && profile != null && profile.getEndpointKeyHash() != null) {
            endpointId = Base64Util.encode(profile.getEndpointKeyHash());
        }

        boolean hashMismatch = !request.isFirstRequest() && !request.getConfigurationHash().binaryEquals(profile.getConfigurationHash());

        AppVersionKey appConfigVersionKey = new AppVersionKey(request.getApplicationToken(), profile.getConfigurationVersion());
        List<EndpointGroupStateDto> endpointGroups = historyDelta.getEndpointGroupStates();
        if (isChangePossible(request, historyDelta, hashMismatch)) {
            boolean resync = request.isFirstRequest() || request.isResyncOnly();
            if (hashMismatch) {
                resync = true;
                logHashMismatch(request, profile, endpointId);
            }
            DeltaCacheKey deltaKey;
            if (resync) {
                deltaKey = new DeltaCacheKey(appConfigVersionKey, endpointGroups, EndpointObjectHash.fromBytes(request
                        .getUserConfigurationHash()), null, request.isResyncOnly());
                LOG.debug("[{}] Building resync delta key {}", endpointId, deltaKey);
            } else {
                deltaKey = new DeltaCacheKey(appConfigVersionKey, endpointGroups, EndpointObjectHash.fromBytes(request
                        .getUserConfigurationHash()), request.getConfigurationHash());
                LOG.debug("[{}] Building regular delta key {}", endpointId, deltaKey);
            }

            DeltaCacheEntry deltaCacheEntry = getDelta(endpointId, profile.getEndpointUserId(), deltaKey);
            byte[] configurationHash = deltaCacheEntry.getHash().getData();
            if (LOG.isTraceEnabled()) {
                LOG.trace("[{}] Result configuration hash is {} for delta key {}", endpointId, Arrays.toString(configurationHash), deltaKey);
            }

            if (isFirstRequestWithUpToDateConfiguration(request, profile, configurationHash)) {
                response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
            } else {
                if (resync) {
                    if (isConfigurationUpToDate(request, configurationHash)) {
                        response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
                    } else {
                        response = new GetDeltaResponse(GetDeltaResponseType.CONF_RESYNC, curAppSeqNumber, deltaCacheEntry.getDelta());
                    }
                } else {
                    if (deltaCacheEntry.getDelta().hasChanges()) {
                        response = new GetDeltaResponse(GetDeltaResponseType.DELTA, curAppSeqNumber, deltaCacheEntry.getDelta());
                    } else {
                        LOG.debug("[{}] Delta has no changes -> no delta", endpointId);
                        response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
                    }
                }
            }

            profile.setConfigurationHash(configurationHash);
            if (deltaCacheEntry.getUserConfigurationHash() != null) {
                profile.setUserConfigurationHash(deltaCacheEntry.getUserConfigurationHash().getData());
            } else {
                profile.setUserConfigurationHash(null);
            }
        } else {
            LOG.debug("[{}] No changes to current application group configurations was maid -> no delta", endpointId);
            response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
        }

        LOG.debug("[{}] Response: {}", endpointId, response);
        return response;
    }

    private boolean isChangePossible(GetDeltaRequest request, HistoryDelta historyDelta, boolean hashMismatch) {
        return historyDelta.isConfigurationChanged() || request.isUserConfigurationChanged() || hashMismatch;
    }

    private boolean isFirstRequestWithUpToDateConfiguration(GetDeltaRequest request, EndpointProfileDto profile, byte[] configurationHash) {
        return profile.getConfigurationHash() == null && isConfigurationUpToDate(request, configurationHash);
    }

    private boolean isConfigurationUpToDate(GetDeltaRequest request, byte[] configurationHash) {
        return request.getConfigurationHash() != null && request.getConfigurationHash().binaryEquals(configurationHash);
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
    private DeltaCacheEntry getDelta(final String endpointId, final String userId, DeltaCacheKey deltaKey) throws GetDeltaException {
        EndpointUserConfigurationDto userConfiguration = findLatestUserConfiguration(userId, deltaKey);

        final DeltaCacheKey newKey;
        if (userConfiguration != null) {
            newKey = new DeltaCacheKey(deltaKey.getAppConfigVersionKey(), deltaKey.getEndpointGroups(),
                    EndpointObjectHash.fromString(userConfiguration.getBody()), deltaKey.getEndpointConfHash(), deltaKey.isResyncOnly());
        } else {
            newKey = deltaKey;
        }

        DeltaCacheEntry deltaCacheEntry = cacheService.getDelta(newKey, new Computable<DeltaCacheKey, DeltaCacheEntry>() { // NOSONAR
                    @Override
                    public DeltaCacheEntry compute(DeltaCacheKey deltaKey) {
                        try {
                            LOG.debug("[{}] Calculating delta for {}", endpointId, deltaKey);
                            DeltaCacheEntry deltaCache;
                            ConfigurationSchemaDto latestConfigurationSchema = cacheService.getConfSchemaByAppAndVersion(deltaKey
                                    .getAppConfigVersionKey());

                            EndpointUserConfigurationDto userConfiguration = findLatestUserConfiguration(userId, deltaKey);

                            EndpointObjectHash userConfigurationHash = null;
                            if (userConfiguration != null) {
                                userConfigurationHash = EndpointObjectHash.fromString(userConfiguration.getBody());
                            }

                            BaseData mergedConfiguration = getMergedConfiguration(endpointId, userConfiguration, deltaKey,
                                    latestConfigurationSchema);

                            LOG.trace("[{}] Merged configuration {}", endpointId, mergedConfiguration.getRawData());

                            if (deltaKey.isResyncOnly()) {
                                deltaCache = buildBaseResyncDelta(endpointId, mergedConfiguration, userConfigurationHash);
                            } else {
                                ProtocolSchema protocolSchema = new ProtocolSchema(latestConfigurationSchema.getProtocolSchema());
                                BaseSchema baseSchema = new BaseSchema(latestConfigurationSchema.getBaseSchema());
                                DeltaCalculationAlgorithm deltaCalculator = deltaCalculatorFactory.createDeltaCalculator(protocolSchema,
                                        baseSchema);

                                if (deltaKey.getEndpointConfHash() == null) {
                                    deltaCache = buildResyncDelta(endpointId, deltaCalculator, mergedConfiguration, userConfigurationHash);
                                } else {
                                    EndpointConfigurationDto endpointConfiguration = cacheService.getConfByHash(deltaKey
                                            .getEndpointConfHash());
                                    deltaCache = calculateDelta(endpointId, deltaCalculator, endpointConfiguration, mergedConfiguration,
                                            userConfigurationHash);
                                }
                                if (cacheService.getConfByHash(deltaCache.getHash()) == null) {
                                    EndpointConfigurationDto newConfiguration = new EndpointConfigurationDto();
                                    newConfiguration.setConfiguration(deltaCache.getConfiguration());
                                    newConfiguration.setConfigurationHash(deltaCache.getHash().getData());
                                    cacheService.putConfiguration(deltaCache.getHash(), newConfiguration);
                                }
                            }

                            LOG.debug("[{}] Configuration hash for {} is {}", endpointId, deltaKey,
                                    MessageEncoderDecoder.bytesToHex(deltaCache.getHash().getData()));
                            return deltaCache;
                        } catch (GetDeltaException e) {
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
    private BaseData getMergedConfiguration(final String endpointId, final EndpointUserConfigurationDto userConfiguration,
            final DeltaCacheKey cacheKey, ConfigurationSchemaDto latestConfigurationSchema) throws GetDeltaException {
        final List<EndpointGroupStateDto> egsList = cacheKey.getEndpointGroups();
        BaseData mergedConfiguration = cacheService.getMergedConfiguration(egsList,
                new Computable<List<EndpointGroupStateDto>, BaseData>() {

                    @Override
                    public BaseData compute(List<EndpointGroupStateDto> key) {
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
                            return processEndpointGroups(endpointGroups, configurations, configurationSchema);
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
                mergedConfiguration = configurationMerger.override(mergedConfiguration,
                        Collections.singletonList(new OverrideData(overrideSchema, userConfiguration.getBody())));
            } catch (OverrideException | IOException oe) {
                LOG.error("[{}] Unexpected exception occurred while merging configuration: ", endpointId, oe);
                throw new GetDeltaException(oe);
            } finally {
                LOG.trace("[{}] getMergedConfiguration.compute end", endpointId);
            }
        }
        return mergedConfiguration;
    }

    /**
     * Calculate delta.
     *
     * @param deltaCalculator
     *            the delta calculator
     * @param endpointConfiguration
     *            the endpoint configuration
     * @param latestConfiguration
     *            the latest configuration
     * @return the delta cache entry
     * @throws GetDeltaException
     *             the get delta exception
     */
    private DeltaCacheEntry calculateDelta(final String endpointId, DeltaCalculationAlgorithm deltaCalculator,
            EndpointConfigurationDto endpointConfiguration, BaseData latestConfiguration, EndpointObjectHash userConfigurationHash)
            throws GetDeltaException {
        try {
            BaseData currentConfiguration = new BaseData(latestConfiguration.getSchema(), endpointConfiguration.getConfigurationAsString());
            LOG.debug("[{}] Calculating partial delta. Old configuration: {}. New configuration: {}", endpointId,
                    currentConfiguration.getRawData(), latestConfiguration.getRawData());
            RawBinaryDelta delta = deltaCalculator.calculate(currentConfiguration, latestConfiguration);
            RawBinaryDelta fullResyncDelta = deltaCalculator.calculate(latestConfiguration);
            return new DeltaCacheEntry(latestConfiguration.getRawData().getBytes(), delta, EndpointObjectHash.fromSHA1(fullResyncDelta
                    .getData()), userConfigurationHash);
        } catch (IOException | DeltaCalculatorException e) {
            throw new GetDeltaException(e);
        }
    }

    /**
     * Builds the resync delta.
     *
     * @param deltaCalculator
     *            the delta calculator
     * @param mergedConfiguration
     *            the merged configuration
     * @return the delta cache entry
     * @throws GetDeltaException
     *             the get delta exception
     */
    private DeltaCacheEntry buildResyncDelta(final String endpointId, DeltaCalculationAlgorithm deltaCalculator,
            BaseData mergedConfiguration, EndpointObjectHash userConfigurationHash) throws GetDeltaException {
        try {
            LOG.debug("[{}] Calculating full resync delta from configuration: {}", endpointId, mergedConfiguration);
            RawBinaryDelta delta = deltaCalculator.calculate(mergedConfiguration);
            return new DeltaCacheEntry(mergedConfiguration.getRawData().getBytes(), delta, EndpointObjectHash.fromSHA1(delta.getData()),
                    userConfigurationHash);
        } catch (IOException | DeltaCalculatorException e) {
            throw new GetDeltaException(e);
        }
    }

    private DeltaCacheEntry buildBaseResyncDelta(String endpointId, BaseData mergedConfiguration, EndpointObjectHash userConfigurationHash) {
        byte[] configuration = GenericAvroConverter.toRawData(mergedConfiguration.getRawData(), mergedConfiguration.getSchema()
                .getRawSchema());
        return new DeltaCacheEntry(configuration, new BaseBinaryDelta(configuration), EndpointObjectHash.fromSHA1(configuration),
                userConfigurationHash);
    }
}
