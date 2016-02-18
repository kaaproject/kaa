/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.profile;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.dao.exception.KaaOptimisticLockingFailureException;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DefaultProfileService is a default implementation of
 * {@link ProfileService ProfileService}.
 *
 * @author ashvayka
 */
public class DefaultProfileService implements ProfileService {

    /** The LOG constant. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultProfileService.class);

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The profile service. */
    @Autowired
    private org.kaaproject.kaa.server.common.dao.ProfileService profileService;

    /** The endpoint service. */
    @Autowired
    private CacheService cacheService;

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.profile.ProfileService#
     * getClientProfileBody (org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public EndpointProfileDto getProfile(EndpointObjectHash endpointKey) {
        return endpointService.findEndpointProfileByKeyHash(endpointKey.getData());
    }

    @Override
    public EndpointProfileDto updateProfile(EndpointProfileDto profile,
            BiFunction<EndpointProfileDto, EndpointProfileDto, EndpointProfileDto> mergeFunction) {
        return updateProfile(profile, mergeFunction, 3);
    }

    private EndpointProfileDto updateProfile(EndpointProfileDto update,
            BiFunction<EndpointProfileDto, EndpointProfileDto, EndpointProfileDto> mergeFunction, int retryCount) {
        LOG.debug("Updating profile {} ", update);
        try {
            return endpointService.saveEndpointProfile(update);
        } catch (KaaOptimisticLockingFailureException ex) {
            LOG.warn("Failed to update profile {} ", update, ex);
            if (retryCount > 0) {
                EndpointProfileDto stored = endpointService.findEndpointProfileByKeyHash(update.getEndpointKeyHash());
                LOG.warn("Going to merge it with stored profile {}", stored);
                EndpointProfileDto merged = mergeFunction.apply(stored, update);
                LOG.warn("Merge result: {}", merged);
                return updateProfile(merged, mergeFunction, retryCount - 1);
            } else {
                throw ex;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.profile.ProfileService#
     * registerProfile
     * (org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest)
     */
    @Override
    public EndpointProfileDto registerProfile(RegisterProfileRequest request) {
        LOG.debug("Registering Profile for {}", request.getEndpointKey());
        LOG.trace("Lookup application by token: {}", request.getAppToken());

        AppSeqNumber appSeqNumber = cacheService.getAppSeqNumber(request.getAppToken());
        LOG.trace("Application by token: {} found: {}", request.getAppToken(), appSeqNumber);

        SdkProfileDto sdkProfile = cacheService.getSdkProfileBySdkToken(request.getSdkToken());
        LOG.trace("Sdk properties by sdk token: {} found: {}", request.getSdkToken(), sdkProfile);

        String profileJson = decodeProfile(request.getProfile(), appSeqNumber.getAppToken(), sdkProfile.getProfileSchemaVersion());

        EndpointObjectHash keyHash = EndpointObjectHash.fromSHA1(request.getEndpointKey());

        EndpointProfileDto dto = endpointService.findEndpointProfileByKeyHash(keyHash.getData());
        if (dto == null) {
            dto = new EndpointProfileDto();
            dto.setSdkToken(sdkProfile.getToken());
            dto.setApplicationId(appSeqNumber.getAppId());
            dto.setEndpointKey(request.getEndpointKey());
            dto.setEndpointKeyHash(keyHash.getData());
            dto.setClientProfileBody(profileJson);
            dto.setProfileHash(EndpointObjectHash.fromSHA1(request.getProfile()).getData());

            populateVersionStates(appSeqNumber.getTenantId(), dto, sdkProfile);

            if (request.getAccessToken() != null) {
                dto.setAccessToken(request.getAccessToken());
            }

            dto.setSequenceNumber(0);

            try {
                cacheService.putEndpointKey(keyHash, KeyUtil.getPublic(dto.getEndpointKey()));
            } catch (InvalidKeyException e) {
                LOG.error("Can't generate public key for endpoint key: {}. Reason: {}", dto.getEndpointKey(), e);
                throw new RuntimeException(e);
            }

            return endpointService.saveEndpointProfile(dto);
        } else {
            return updateProfile(new UpdateProfileRequest(request.getAppToken(), keyHash, request.getAccessToken(), request.getProfile(),
                    request.getSdkToken()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.operations.service.profile.ProfileService#
     * updateProfile
     * (org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest)
     */
    @Override
    public EndpointProfileDto updateProfile(UpdateProfileRequest request) {
        LOG.debug("Updating Profile for {}", request.getEndpointKeyHash());

        EndpointProfileDto dto = endpointService.findEndpointProfileByKeyHash(request.getEndpointKeyHash().getData());

        AppSeqNumber appSeqNumber = cacheService.getAppSeqNumber(request.getApplicationToken());

        SdkProfileDto sdkProfile = cacheService.getSdkProfileBySdkToken(request.getSdkToken());
        String profileJson = decodeProfile(request.getProfile(), appSeqNumber.getAppToken(), sdkProfile.getProfileSchemaVersion());

        Function<EndpointProfileDto, EndpointProfileDto> updateFunction = profile -> {
            if (request.getAccessToken() != null) {
                profile.setAccessToken(request.getAccessToken());
            }
            profile.setClientProfileBody(profileJson);
            profile.setProfileHash(EndpointObjectHash.fromSHA1(request.getProfile()).getData());

            populateVersionStates(appSeqNumber.getTenantId(), profile, sdkProfile);
            profile.setGroupState(new ArrayList<>());
            profile.setSequenceNumber(0);
            return profile;
        }; 
        return updateProfile(updateFunction.apply(dto), (storedProfile, newProfile) -> {
            return updateFunction.apply(storedProfile);
        });
    }

    protected void populateVersionStates(String tenantId, EndpointProfileDto dto, SdkProfileDto sdkProfile) {
        dto.setClientProfileVersion(sdkProfile.getProfileSchemaVersion());
        dto.setConfigurationVersion(sdkProfile.getConfigurationSchemaVersion());
        dto.setUserNfVersion(sdkProfile.getNotificationSchemaVersion());
        dto.setLogSchemaVersion(sdkProfile.getLogSchemaVersion());
        if (sdkProfile.getAefMapIds() != null) {
            List<ApplicationEventFamilyMapDto> aefMaps = cacheService.getApplicationEventFamilyMapsByIds(sdkProfile.getAefMapIds());
            List<EventClassFamilyVersionStateDto> ecfVersionStates = new ArrayList<>(aefMaps.size());
            for (ApplicationEventFamilyMapDto aefMap : aefMaps) {
                EventClassFamilyVersionStateDto ecfVersionDto = new EventClassFamilyVersionStateDto();
                String ecfId = cacheService.getEventClassFamilyIdByName(new EventClassFamilyIdKey(tenantId, aefMap.getEcfName()));
                if (ecfId != null) {
                    ecfVersionDto.setEcfId(ecfId);
                    ecfVersionDto.setVersion(aefMap.getVersion());
                    ecfVersionStates.add(ecfVersionDto);
                } else {
                    LOG.warn("Failed to add ecf version state for ecf name {} and version {}", aefMap.getEcfName(), aefMap.getVersion());
                }
            }
            dto.setEcfVersionStates(ecfVersionStates);
        }
    }

    /**
     * Decode profile.
     *
     * @param profileRaw
     *            the profile raw
     * @param appToken
     *            the app id
     * @param schemaVersion
     *            the schema version
     * @return the string
     */
    private String decodeProfile(byte[] profileRaw, String appToken, int schemaVersion) {
        LOG.trace("Lookup profileSchema by appToken: {} and version: {}", appToken, schemaVersion);

        EndpointProfileSchemaDto profileSchemaDto = cacheService
                .getProfileSchemaByAppAndVersion(new AppVersionKey(appToken, schemaVersion));
        String profileSchema = cacheService.getFlatCtlSchemaById(profileSchemaDto.getCtlSchemaId());

        LOG.trace("EndpointProfileSchema by appToken: {} and version: {} found: {}", appToken, schemaVersion, profileSchema);

        String profileJson = GenericAvroConverter.toJson(profileRaw, profileSchema);
        LOG.trace("Profile json : {} ", profileJson);

        return profileJson;
    }
}
