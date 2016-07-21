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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.GroupService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ProfileService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.operations.service.filter.DefaultFilterEvaluator;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("groupService")
public class GroupServiceImpl extends AbstractAdminService implements GroupService {

    /**
     * The Constant MAX_LIMIT.
     */
    private static final int MAX_LIMIT = 500;

    @Autowired
    ProfileService profileService;

    @Override
    public List<EndpointGroupDto> getEndpointGroupsByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getEndpointGroupsByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getEndpointGroupsByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return checkEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupWeight(endpointGroup.getWeight());
            if (isEmpty(endpointGroup.getId())) {
                endpointGroup.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(endpointGroup.getApplicationId());
            } else {
                checkEndpointGroupId(endpointGroup.getId());
            }
            return controlService.editEndpointGroup(endpointGroup);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            controlService.deleteEndpointGroup(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId,
                                                                                 boolean includeDeprecated) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId,
                                                         String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            ProfileFilterRecordDto record = controlService.getProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId);
            return record;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkEndpointGroupId(endpointGroupId);
            return controlService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            String username = getCurrentUser().getUsername();
            if (isEmpty(profileFilter.getId())) {
                profileFilter.setCreatedUsername(username);
                checkEndpointGroupId(profileFilter.getEndpointGroupId());
            } else {
                profileFilter.setModifiedUsername(username);
                ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilter.getId());
                Utils.checkNotNull(storedProfileFilter);
                checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            }
            validateProfileFilterBody(profileFilter.getEndpointProfileSchemaId(),
                    profileFilter.getServerProfileSchemaId(),
                    profileFilter.getBody());
            return controlService.editProfileFilter(profileFilter);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.activateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterDto storedProfileFilter = controlService.getProfileFilter(profileFilterId);
            Utils.checkNotNull(storedProfileFilter);
            checkEndpointGroupId(storedProfileFilter.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            return controlService.deactivateProfileFilter(profileFilterId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ProfileFilterRecordDto record = controlService.getProfileFilterRecord(endpointProfileSchemaId,
                    serverProfileSchemaId, endpointGroupId);
            checkEndpointGroupId(record.getEndpointGroupId());
            String username = getCurrentUser().getUsername();
            controlService.deleteProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId, username);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (Integer.valueOf(limit) > MAX_LIMIT) {
                throw new IllegalArgumentException("Incorrect limit parameter. You must enter value not more than " + MAX_LIMIT);
            }
            EndpointGroupDto endpointGroupDto = getEndpointGroup(endpointGroupId);
            PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, limit, offset);
            if (isGroupAll(endpointGroupDto)) {
                pageLinkDto.setApplicationId(endpointGroupDto.getApplicationId());
            }
            return controlService.getEndpointProfileByEndpointGroupId(pageLinkDto);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (Integer.valueOf(limit) > MAX_LIMIT) {
                throw new IllegalArgumentException("Incorrect limit parameter. You must enter value not more than " + MAX_LIMIT);
            }
            EndpointGroupDto endpointGroupDto = getEndpointGroup(endpointGroupId);
            PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, limit, offset);
            if (isGroupAll(endpointGroupDto)) {
                pageLinkDto.setApplicationId(endpointGroupDto.getApplicationId());
            }
            return controlService.getEndpointProfileBodyByEndpointGroupId(pageLinkDto);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkEndpointGroupWeight(int weight) throws KaaAdminServiceException {
        if (weight < 0) {
            throw new IllegalArgumentException("The weight can't be negative number!");
        }
    }

    private void validateProfileFilterBody(String endpointProfileSchemaId, String serverProfileSchemaId,
                                           String filterBody) throws KaaAdminServiceException {
        GenericRecord endpointProfileRecord = null;
        GenericRecord serverProfileRecord = null;
        try {
            if (endpointProfileSchemaId != null) {
                EndpointProfileSchemaDto endpointProfileSchema = profileService.getProfileSchema(endpointProfileSchemaId);
                endpointProfileRecord = getDefaultRecordFromCtlSchema(endpointProfileSchema.getCtlSchemaId());
            }
            if (serverProfileSchemaId != null) {
                ServerProfileSchemaDto serverProfileSchema = profileService.getServerProfileSchema(serverProfileSchemaId);
                serverProfileRecord = getDefaultRecordFromCtlSchema(serverProfileSchema.getCtlSchemaId());
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
        try {
            Expression expression = new SpelExpressionParser().parseExpression(filterBody);
            StandardEvaluationContext evaluationContext;
            if (endpointProfileRecord != null) {
                evaluationContext = new StandardEvaluationContext(endpointProfileRecord);
                evaluationContext.setVariable(DefaultFilterEvaluator.CLIENT_PROFILE_VARIABLE_NAME, endpointProfileRecord);
            } else {
                evaluationContext = new StandardEvaluationContext();
            }
            evaluationContext.addPropertyAccessor(new GenericRecordPropertyAccessor());
            evaluationContext.setVariable(DefaultFilterEvaluator.EP_KEYHASH_VARIABLE_NAME, "test");
            if (serverProfileRecord != null) {
                evaluationContext.setVariable(DefaultFilterEvaluator.SERVER_PROFILE_VARIABLE_NAME, serverProfileRecord);
            }
            expression.getValue(evaluationContext, Boolean.class);
        } catch (Exception e) {
            throw new KaaAdminServiceException("Invalid profile filter body!", e, ServiceErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    private GenericRecord getDefaultRecordFromCtlSchema(String ctlSchemaId) throws Exception {
        CTLSchemaDto ctlSchema = controlService.getCTLSchemaById(ctlSchemaId);
        Schema schema = controlService.exportCTLSchemaFlatAsSchema(ctlSchema);
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
        GenericRecord defaultRecord = converter.decodeJson(ctlSchema.getDefaultRecord());
        return defaultRecord;
    }

}
