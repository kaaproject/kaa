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

package org.kaaproject.kaa.server.admin.services;

import net.iharder.Base64;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.SdkService;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.kaaproject.kaa.server.control.service.exception.ControlServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("sdkService")
public class SdkServiceImpl extends AbstractAdminService implements SdkService {

    @Autowired
    ApplicationService applicationService;

    @Override
    public SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(sdkProfile.getApplicationId());
            sdkProfile.setCreatedUsername(getCurrentUser().getUsername());
            sdkProfile.setCreatedTime(System.currentTimeMillis());

            ApplicationDto application = controlService.getApplication(sdkProfile.getApplicationId());
            if (application == null) {
                throw new NotFoundException("Application not found!");
            }
            sdkProfile.setApplicationToken(application.getApplicationToken());
            return controlService.saveSdkProfile(sdkProfile);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            SdkProfileDto sdkProfile = this.checkSdkProfileId(sdkProfileId);
            if (!controlService.isSdkProfileUsed(sdkProfile.getToken())) {
                controlService.deleteSdkProfile(sdkProfileId);
            } else {
                throw new IllegalArgumentException("Associated endpoint profiles have been found.");
            }
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkSdkProfileId(sdkProfileId);
            return controlService.getSdkProfile(sdkProfileId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getSdkProfilesByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            this.checkApplicationId(applicationId);
            return controlService.getSdkProfilesByApplicationId(applicationId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        try {
            return doGenerateSdk(sdkProfile, targetPlatform);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void flushSdkCache() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            List<ApplicationDto> applications = applicationService.getApplications();
            for (ApplicationDto application : applications) {
                for (CacheService.SdkKey key : cacheService.getCachedSdkKeys(application.getId())) {
                    cacheService.flushSdk(key);
                }
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        try {
            doGenerateSdk(sdkProfile, targetPlatform);
            return Base64.encodeObject(new CacheService.SdkKey(sdkProfile, targetPlatform), Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);

        try {
            SdkProfileViewDto viewDto = new SdkProfileViewDto();
            SdkProfileDto sdkProfile = controlService.getSdkProfile(sdkProfileId);
            viewDto.setSdkProfile(sdkProfile);

            String applicationId = sdkProfile.getApplicationId();
            List<ApplicationEventFamilyMapDto> aefDtoList = new ArrayList<>();
            List<ApplicationEventFamilyMapDto> aefMaps = controlService.
                    getApplicationEventFamilyMapsByApplicationId(applicationId);
            List<String> aefMapIds = sdkProfile.getAefMapIds();
            for (ApplicationEventFamilyMapDto aefDto : aefMaps) {
                if (aefMapIds.contains(aefDto.getId())) {
                    aefDtoList.add(aefDto);
                }
            }
            viewDto.setAefMapDtoList(aefDtoList);

            List<ConfigurationSchemaDto> configSchemas =
                    controlService.getConfigurationSchemasByApplicationId(applicationId);
            for (ConfigurationSchemaDto dto : configSchemas) {
                if (dto.getVersion() == sdkProfile.getConfigurationSchemaVersion()) {
                    viewDto.setConfigurationSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setConfigurationSchemaId(dto.getId());
                }
            }

            List<EndpointProfileSchemaDto> profileSchemas = controlService.getProfileSchemasByApplicationId(applicationId);
            for (EndpointProfileSchemaDto dto : profileSchemas) {
                if (dto.getVersion() == sdkProfile.getProfileSchemaVersion()) {
                    viewDto.setProfileSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setProfileSchemaId(dto.getId());
                }
            }

            List<NotificationSchemaDto> notificationSchemas =
                    controlService.getNotificationSchemasByAppId(applicationId);
            for (NotificationSchemaDto dto : notificationSchemas) {
                if (dto.getVersion() == sdkProfile.getNotificationSchemaVersion()) {
                    viewDto.setNotificationSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setNotificationSchemaId(dto.getId());
                }
            }

            List<LogSchemaDto> logSchemas = controlService.getLogSchemasByApplicationId(applicationId);
            for (LogSchemaDto dto : logSchemas) {
                if (dto.getVersion() == sdkProfile.getLogSchemaVersion()) {
                    viewDto.setLogSchemaName(dto.getName() + " (v." + dto.getVersion() + ")");
                    viewDto.setLogSchemaId(dto.getId());
                }
            }

            return viewDto;
        } catch (ControlServiceException e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getSchemaVersionsByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);

            SchemaVersions schemaVersions = new SchemaVersions();

            List<VersionDto> configurationSchemaVersions = controlService.getConfigurationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setConfigurationSchemaVersions(configurationSchemaVersions);

            List<VersionDto> profileSchemaVersions = controlService.getProfileSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setProfileSchemaVersions(profileSchemaVersions);

            List<VersionDto> notificationSchemaVersions = controlService.getNotificationSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setNotificationSchemaVersions(notificationSchemaVersions);

            List<VersionDto> logSchemaVersions = controlService.getLogSchemaVersionsByApplicationId(applicationId);
            schemaVersions.setLogSchemaVersions(logSchemaVersions);

            return schemaVersions;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private SdkProfileDto checkSdkProfileId(String sdkProfileId) throws KaaAdminServiceException {
        try {
            if (isEmpty(sdkProfileId)) {
                throw new IllegalArgumentException("The SDK profile identifier is empty!");
            }
            SdkProfileDto sdkProfile = controlService.getSdkProfile(sdkProfileId);
            Utils.checkNotNull(sdkProfile);
            return sdkProfile;
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private FileData doGenerateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(sdkProfile.getApplicationId());
            CacheService.SdkKey sdkKey = new CacheService.SdkKey(sdkProfile, targetPlatform);
            FileData sdkFile = cacheService.getSdk(sdkKey);
            if (sdkFile == null) {
                sdkFile = controlService.generateSdk(sdkProfile, targetPlatform);
                cacheService.putSdk(sdkKey, sdkFile);
            }
            return sdkFile;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

}
