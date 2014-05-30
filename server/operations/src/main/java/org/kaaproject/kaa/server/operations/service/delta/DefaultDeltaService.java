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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ConfigurationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.operations.pojo.Base64Util;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse;
import org.kaaproject.kaa.server.operations.pojo.GetDeltaResponse.GetDeltaResponseType;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.Computable;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheEntry;
import org.kaaproject.kaa.server.operations.service.cache.DeltaCacheKey;
import org.kaaproject.kaa.server.operations.service.delta.merge.ConfigurationMerger;
import org.kaaproject.kaa.server.operations.service.delta.merge.ConfigurationMergerFactory;
import org.kaaproject.kaa.server.operations.service.delta.merge.MergeException;
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
    /** The profile service. */
    @Autowired
    private EndpointService endpointService;

    /** The delta calculator factory. */
    @Autowired
    private DeltaCalculatorFactory deltaCalculatorFactory;

    /** The configuration merger factory. */
    @Autowired
    private ConfigurationMergerFactory configurationMergerFactory;
    
    /**
     * Instantiates a new default delta service.
     */
    public DefaultDeltaService() {
        super();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.delta.DeltaService#getDelta(org.kaaproject.kaa.server.operations.pojo.GetDeltaRequest, org.kaaproject.kaa.server.operations.service.delta.HistoryDelta, int)
     */
    @Override
    public GetDeltaResponse getDelta(GetDeltaRequest request, HistoryDelta historyDelta, int curAppSeqNumber) throws GetDeltaException {
        GetDeltaResponse response;
        EndpointProfileDto profile = request.getEndpointProfile();
        String endpointId = Base64Util.encode(profile);

        if (request.getSequenceNumber() == curAppSeqNumber) {
            LOG.debug("[{}] No changes to current application was maid -> no delta", endpointId);
            return new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
        }

        AppVersionKey appConfigVersionKey = new AppVersionKey(request.getApplicationToken(), profile.getConfigurationVersion());
        List<EndpointGroupStateDto> endpointGroups = historyDelta.getEndpointGroupStates();
        if (historyDelta.isConfigurationChanged()) {
            boolean resync = request.isFirstRequest();
            if(!resync && !request.getConfigurationHash().binaryEquals(profile.getConfigurationHash())){
                resync = true;
                String serverHash = "";
                String clientHash = "";
                if(profile.getConfigurationHash() != null){
                    serverHash = MessageEncoderDecoder.bytesToHex(profile.getConfigurationHash());
                }
                if(request.getConfigurationHash() != null){
                    clientHash = MessageEncoderDecoder.bytesToHex(request.getConfigurationHash().getData());                    
                }
                LOG.warn("[{}] Configuration hash mismatch! server {}, client {}", endpointId, serverHash, clientHash);
            }
            DeltaCacheKey deltaKey;
            if (resync) {
                deltaKey = new DeltaCacheKey(appConfigVersionKey, endpointGroups, null);
                LOG.debug("[{}] Building resync delta key {}", endpointId, deltaKey);
            } else {
                deltaKey = new DeltaCacheKey(appConfigVersionKey, endpointGroups, request.getConfigurationHash());
                LOG.debug("[{}] Building regular delta key {}", endpointId, deltaKey);
            }

            DeltaCacheEntry deltaCacheEntry = getDelta(endpointId, deltaKey);

            if (resync) {
                response = new GetDeltaResponse(GetDeltaResponseType.CONF_RESYNC, curAppSeqNumber, deltaCacheEntry.getDelta());
            } else {
                if (deltaCacheEntry.getDelta().hasChanges()) {
                    response = new GetDeltaResponse(GetDeltaResponseType.DELTA, curAppSeqNumber, deltaCacheEntry.getDelta());
                } else {
                    LOG.debug("[{}] Delta has no changes -> no delta", endpointId);
                    response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
                }
            }

            profile.setConfigurationHash(deltaCacheEntry.getHash().getData());
        } else {
            LOG.debug("[{}] No changes to current application group configurations was maid -> no delta", endpointId);
            response = new GetDeltaResponse(GetDeltaResponseType.NO_DELTA, curAppSeqNumber);
        }

//        if (request.isFetchSchema()) {
//            LOG.debug("[{}] Fetching delta schema for application {} and configuration schema version {}", endpointId, appConfigVersionKey.getApplicationToken(),
//                    appConfigVersionKey.getVersion());
//            ConfigurationSchemaDto latestConfiguration = cacheService.getConfSchemaByAppAndVersion(appConfigVersionKey);
//            response.setConfSchema(latestConfiguration.getProtocolSchema());
//        }

        profile.setSequenceNumber(curAppSeqNumber);

        LOG.debug("[{}] Response: {}", endpointId, response);
        return response;
    }

    /**
     * Calculate delta.
     *
     * @param deltaKey            the delta key
     * @return the delta cache entry
     * @throws GetDeltaException             the get delta exception
     */
    private DeltaCacheEntry getDelta(final String endpointId, DeltaCacheKey deltaKey) throws GetDeltaException {
        return cacheService.getDelta(deltaKey, new Computable<DeltaCacheKey, DeltaCacheEntry>() {
            @Override
            public DeltaCacheEntry compute(DeltaCacheKey deltaKey) {
                try {
                    LOG.debug("[{}] Calculating delta for {}", endpointId, deltaKey);
                    DeltaCacheEntry deltaCache;
                    ConfigurationSchemaDto latestConfigurationSchema = cacheService.getConfSchemaByAppAndVersion(deltaKey.getAppConfigVersionKey());
                    byte[] mergedConfiguration = getMergedConfiguration(endpointId, deltaKey.getEndpointGroups());

                    LOG.trace("[{}] Merged configuration {}", endpointId, new String(mergedConfiguration));
                    DeltaCalculator deltaCalculator = deltaCalculatorFactory.createDeltaCalculator(latestConfigurationSchema.getProtocolSchema(), latestConfigurationSchema.getBaseSchema());

                    if (deltaKey.getEndpointConfHash() == null) {
                        deltaCache = buildResyncDelta(endpointId, deltaCalculator, mergedConfiguration);
                    } else {
                        EndpointConfigurationDto endpointConfiguration = cacheService.getConfByHash(deltaKey.getEndpointConfHash());
                        deltaCache = calculateDelta(endpointId, deltaCalculator, endpointConfiguration, mergedConfiguration);
                    }
                    if (cacheService.getConfByHash(deltaCache.getHash()) == null) {
                        EndpointConfigurationDto newConfiguration = new EndpointConfigurationDto();
                        newConfiguration.setConfiguration(deltaCache.getConfiguration());
                        newConfiguration.setConfigurationHash(deltaCache.getHash().getData());
                        cacheService.putConfiguration(deltaCache.getHash(), newConfiguration);
                    }
                    
                    LOG.debug("[{}] Configuration hash for {} is {}", endpointId, deltaKey, MessageEncoderDecoder.bytesToHex(deltaCache.getHash().getData()));
                    return deltaCache;
                } catch (GetDeltaException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Gets the latest conf from cache.
     *
     * @param egsList the egs list
     * @return the latest conf from cache
     * @throws GetDeltaException the get delta exception
     */
    private byte[] getMergedConfiguration(final String endpointId, final List<EndpointGroupStateDto> egsList) throws GetDeltaException {
        byte[] mergedConfiguration = cacheService.getMergedConfiguration(egsList, new Computable<List<EndpointGroupStateDto>, byte[]>() {

            @Override
            public byte[] compute(List<EndpointGroupStateDto> key) {
                LOG.trace("[{}] getMergedConfiguration.compute begin", endpointId);
                try {
                    List<EndpointGroupDto> endpointGroups = new ArrayList<>();
                    List<ConfigurationDto> configurations = new ArrayList<>();
                    ConfigurationSchemaDto configurationSchema = null;
                    for (EndpointGroupStateDto egs : egsList) {
                        EndpointGroupDto endpointGroup = null;
                        if (!StringUtils.isBlank(egs.getEndpointGroupId())) {
                            endpointGroup = endpointService.findEndpointGroupById(egs.getEndpointGroupId());
                        }
                        if (endpointGroup != null) {
                            endpointGroups.add(endpointGroup);
                        }

                        ConfigurationDto configuration = null;
                        if (!StringUtils.isBlank(egs.getConfigurationId())) {
                            configuration = configurationService.findConfigurationById(egs.getConfigurationId());
                        }
                        if (configuration != null) {
                            configurations.add(configuration);
                        }

                        if (configurationSchema == null && configuration != null) {
                            configurationSchema = configurationService.findConfSchemaById(configuration.getSchemaId());
                        }
                    }

                    ConfigurationMerger configurationMerger = configurationMergerFactory.createConfigurationMerger();
                    return configurationMerger.merge(endpointGroups, configurations, configurationSchema);
                } catch (MergeException me) {
                    LOG.error("[{}] Unexpected exception occurred while merging configuration: ", endpointId, me);
                    throw new RuntimeException(me);
                } finally {
                    LOG.trace("[{}] getMergedConfiguration.compute end", endpointId);
                }
            }
        });
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
    private DeltaCacheEntry calculateDelta(final String endpointId, DeltaCalculator deltaCalculator, EndpointConfigurationDto endpointConfiguration, byte[] latestConfiguration)
            throws GetDeltaException {
        try {
            LOG.debug("[{}] Calculating partial delta. Old configuration: {}. New configuration: {}", endpointId, new String(endpointConfiguration.getConfiguration()), new String(latestConfiguration));
            RawBinaryDelta delta = deltaCalculator.calculate(endpointConfiguration.getConfiguration(), latestConfiguration);
            RawBinaryDelta fullResyncDelta = deltaCalculator.calculate(latestConfiguration);
            return new DeltaCacheEntry(latestConfiguration, delta, EndpointObjectHash.fromSHA1(fullResyncDelta.getData()));
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
    private DeltaCacheEntry buildResyncDelta(final String endpointId, DeltaCalculator deltaCalculator, byte[] mergedConfiguration) throws GetDeltaException {
        try {
            LOG.debug("[{}] Calculating full resync delta from configuration: {}", endpointId, mergedConfiguration);
            RawBinaryDelta delta = deltaCalculator.calculate(mergedConfiguration);
            return new DeltaCacheEntry(mergedConfiguration, delta, EndpointObjectHash.fromSHA1(delta.getData()));
        } catch (IOException | DeltaCalculatorException e) {
            throw new GetDeltaException(e);
        }
    }
}
