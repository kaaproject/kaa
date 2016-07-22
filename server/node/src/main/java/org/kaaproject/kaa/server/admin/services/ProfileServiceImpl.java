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

import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConverterType;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaReferenceDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.services.CtlService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ProfileService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.operations.service.filter.DefaultFilterEvaluator;
import org.kaaproject.kaa.server.operations.service.filter.el.GenericRecordPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("profileService")
public class ProfileServiceImpl extends AbstractAdminService implements ProfileService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Autowired
    CtlService ctlService;

    @Override
    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getProfileSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getProfileSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = controlService.getProfileSchema(profileSchemaId);
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(profileSchema.getId())) {
                profileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(profileSchema.getApplicationId());
            } else {
                EndpointProfileSchemaDto storedProfileSchema = controlService.getProfileSchema(profileSchema.getId());
                Utils.checkNotNull(storedProfileSchema);
                checkApplicationId(storedProfileSchema.getApplicationId());
            }
            return controlService.editProfileSchema(profileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        return getServerProfileSchemasByApplicationId(checkApplicationToken(applicationToken));
    }

    @Override
    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            return controlService.getServerProfileSchemasByApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto profileSchema = controlService.getServerProfileSchema(serverProfileSchemaId);
            Utils.checkNotNull(profileSchema);
            checkApplicationId(profileSchema.getApplicationId());
            return profileSchema;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(serverProfileSchema.getId())) {
                serverProfileSchema.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(serverProfileSchema.getApplicationId());
            } else {
                ServerProfileSchemaDto storedServerProfileSchema = controlService.getServerProfileSchema(serverProfileSchema.getId());
                Utils.checkNotNull(storedServerProfileSchema);
                checkApplicationId(storedServerProfileSchema.getApplicationId());
            }
            return controlService.saveServerProfileSchema(serverProfileSchema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileDto updateServerProfile(String endpointKeyHash,
                                                  int serverProfileVersion, String serverProfileBody)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto profileDto = controlService.getEndpointProfileByKeyHash(endpointKeyHash);
            Utils.checkNotNull(profileDto);
            checkApplicationId(profileDto.getApplicationId());
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchemaByApplicationIdAndVersion(
                    profileDto.getApplicationId(), serverProfileVersion);
            Utils.checkNotNull(serverProfileSchema);
            RecordField record;
            try {
                record = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(),
                        serverProfileBody);
            } catch (Exception e) {
                LOG.error("Provided server profile body is not valid: ", e);
                throw new KaaAdminServiceException("Provided server profile body is not valid: "
                        + e.getMessage(), ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
            if (!record.isValid()) {
                throw new KaaAdminServiceException("Provided server profile body is not valid!", ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
            profileDto = controlService.updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileBody);
            return profileDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto profileDto = controlService.getEndpointProfileByKeyHash(endpointProfileKeyHash);
            if (profileDto == null) {
                throw new KaaAdminServiceException("Requested item was not found!", ServiceErrorCode.ITEM_NOT_FOUND);
            }
            checkApplicationId(profileDto.getApplicationId());
            return profileDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileBodyDto profileBodyDto = controlService.getEndpointProfileBodyByKeyHash(endpointProfileKeyHash);
            Utils.checkNotNull(profileBodyDto);
            checkApplicationId(profileBodyDto.getAppId());
            return profileBodyDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<EndpointProfileDto> getEndpointProfilesByUserExternalId(String endpointUserExternalId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (StringUtils.isEmpty(endpointUserExternalId)) {
                String message = "The endpoint user external ID provided is empty!";
                throw new IllegalArgumentException(message);
            }
            return this.controlService.getEndpointProfilesByUserExternalIdAndTenantId(endpointUserExternalId, getCurrentUser().getTenantId());

        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void removeEndpointProfileByKeyHash(String endpointKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto endpointProfile = controlService.getEndpointProfileByKeyHash(endpointKeyHash);
            Utils.checkNotNull(endpointProfile);
            checkApplicationId(endpointProfile.getApplicationId());
            controlService.removeEndpointProfile(endpointProfile);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = profileSchemaView.getSchema();
            String applicationId = profileSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = profileSchema.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (profileSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = profileSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(),
                            metaInfo.getVersion(),
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    profileSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = ctlService.saveCTLSchemaForm(profileSchemaView.getCtlSchemaForm(), ConverterType.FORM_AVRO_CONVERTER);
                    profileSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            EndpointProfileSchemaDto savedProfileSchema = saveProfileSchema(profileSchema);
            return getProfileSchemaView(savedProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            EndpointProfileSchemaDto profileSchema = new EndpointProfileSchemaDto();
            profileSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            profileSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            profileSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = ctlService.saveCTLSchemaForm(ctlSchemaForm, ConverterType.FORM_AVRO_CONVERTER);
            profileSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            EndpointProfileSchemaDto savedProfileSchema = saveProfileSchema(profileSchema);
            return getProfileSchemaView(savedProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto profileSchema = getProfileSchema(profileSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(profileSchema.getCtlSchemaId());
            return new ProfileSchemaViewDto(profileSchema, toCtlSchemaForm(ctlSchemaDto, ConverterType.FORM_AVRO_CONVERTER));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getServerProfileSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            List<ServerProfileSchemaDto> serverProfileSchemas = controlService.getServerProfileSchemasByApplicationId(applicationId);
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(serverProfileSchemas.size());
            for (ServerProfileSchemaDto serverProfileSchema : serverProfileSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
                RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), null);
                schemaInfo.setSchemaName(serverProfileSchema.getName());
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<SchemaInfoDto> getServerProfileSchemaInfosByEndpointKey(String endpointKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto endpointProfile = getEndpointProfileByKeyHash(endpointKeyHash);
            List<ServerProfileSchemaDto> serverProfileSchemas = controlService.getServerProfileSchemasByApplicationId(endpointProfile.getApplicationId());
            List<SchemaInfoDto> schemaInfos = new ArrayList<>(serverProfileSchemas.size());
            for (ServerProfileSchemaDto serverProfileSchema : serverProfileSchemas) {
                SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
                String body = null;
                if (schemaInfo.getVersion() == endpointProfile.getServerProfileVersion()) {
                    body = endpointProfile.getServerProfileBody();
                }
                RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), body);
                schemaInfo.setSchemaName(serverProfileSchema.getName());
                schemaInfo.setSchemaForm(schemaForm);
                schemaInfos.add(schemaInfo);
            }
            Collections.sort(schemaInfos, Collections.reverseOrder());
            return schemaInfos;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaViewDto getServerProfileSchemaView(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = getServerProfileSchema(serverProfileSchemaId);
            CTLSchemaDto ctlSchemaDto = controlService.getCTLSchemaById(serverProfileSchema.getCtlSchemaId());
            return new ServerProfileSchemaViewDto(serverProfileSchema, toCtlSchemaForm(ctlSchemaDto, ConverterType.FORM_AVRO_CONVERTER));
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaViewDto saveServerProfileSchemaView(ServerProfileSchemaViewDto serverProfileSchemaView) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = serverProfileSchemaView.getSchema();
            String applicationId = serverProfileSchema.getApplicationId();
            checkApplicationId(applicationId);
            String ctlSchemaId = serverProfileSchema.getCtlSchemaId();
            if (isEmpty(ctlSchemaId)) {
                if (serverProfileSchemaView.useExistingCtlSchema()) {
                    CtlSchemaReferenceDto metaInfo = serverProfileSchemaView.getExistingMetaInfo();
                    CTLSchemaDto schema = ctlService.getCTLSchemaByFqnVersionTenantIdAndApplicationId(metaInfo.getMetaInfo().getFqn(),
                            metaInfo.getVersion(),
                            metaInfo.getMetaInfo().getTenantId(),
                            metaInfo.getMetaInfo().getApplicationId());
                    serverProfileSchema.setCtlSchemaId(schema.getId());
                } else {
                    CtlSchemaFormDto ctlSchemaForm = ctlService.saveCTLSchemaForm(serverProfileSchemaView.getCtlSchemaForm(), ConverterType.FORM_AVRO_CONVERTER);
                    serverProfileSchema.setCtlSchemaId(ctlSchemaForm.getId());
                }
            }
            ServerProfileSchemaDto savedServerProfileSchema = saveServerProfileSchema(serverProfileSchema);
            return getServerProfileSchemaView(savedServerProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ServerProfileSchemaViewDto createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            ServerProfileSchemaDto serverProfileSchema = new ServerProfileSchemaDto();
            serverProfileSchema.setApplicationId(ctlSchemaForm.getMetaInfo().getApplicationId());
            serverProfileSchema.setName(ctlSchemaForm.getSchema().getDisplayNameFieldValue());
            serverProfileSchema.setDescription(ctlSchemaForm.getSchema().getDescriptionFieldValue());
            CtlSchemaFormDto savedCtlSchemaForm = ctlService.saveCTLSchemaForm(ctlSchemaForm, ConverterType.FORM_AVRO_CONVERTER);
            serverProfileSchema.setCtlSchemaId(savedCtlSchemaForm.getId());
            ServerProfileSchemaDto savedServerProfileSchema = saveServerProfileSchema(serverProfileSchema);
            return getServerProfileSchemaView(savedServerProfileSchema.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SchemaInfoDto getEndpointProfileSchemaInfo(String endpointProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileSchemaDto endpointProfileSchema = controlService.getProfileSchema(endpointProfileSchemaId);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(endpointProfileSchema);
            RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(endpointProfileSchema.getCtlSchemaId(), null);
            schemaInfo.setSchemaName(endpointProfileSchema.getName());
            schemaInfo.setSchemaForm(schemaForm);
            return schemaInfo;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public SchemaInfoDto getServerProfileSchemaInfo(String serverProfileSchemaId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchema(serverProfileSchemaId);
            SchemaInfoDto schemaInfo = new SchemaInfoDto(serverProfileSchema);
            RecordField schemaForm = createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(), null);
            schemaInfo.setSchemaName(serverProfileSchema.getName());
            schemaInfo.setSchemaForm(schemaForm);
            return schemaInfo;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public boolean testProfileFilter(RecordField endpointProfile, RecordField serverProfile, String filterBody) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord endpointProfileRecord = null;
            GenericRecord serverProfileRecord = null;
            try {
                if (endpointProfile != null) {
                    endpointProfileRecord = FormAvroConverter.createGenericRecordFromRecordField(endpointProfile);
                }
                if (serverProfile != null) {
                    serverProfileRecord = FormAvroConverter.createGenericRecordFromRecordField(serverProfile);
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
                return expression.getValue(evaluationContext, Boolean.class);
            } catch (Exception e) {
                throw new KaaAdminServiceException("Invalid profile filter: " + e.getMessage(), e, ServiceErrorCode.BAD_REQUEST_PARAMS);
            }
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileViewDto getEndpointProfileViewByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            EndpointProfileDto endpointProfile = controlService.getEndpointProfileByKeyHash(endpointProfileKeyHash);
            Utils.checkNotNull(endpointProfile);
            checkApplicationId(endpointProfile.getApplicationId());
            EndpointProfileViewDto endpointProfileView = new EndpointProfileViewDto();
            endpointProfileView.setEndpointKeyHash(endpointProfile.getEndpointKeyHash());
            endpointProfileView.setSdkProfileDto(controlService.findSdkProfileByToken(endpointProfile.getSdkToken()));
            if (endpointProfile.getEndpointUserId() != null) {
                EndpointUserDto endpointUser = controlService.getEndpointUser(endpointProfile.getEndpointUserId());
                if (endpointUser != null) {
                    endpointProfileView.setUserId(endpointUser.getId());
                    endpointProfileView.setUserExternalId(endpointUser.getExternalId());
                }
            }
            EndpointProfileSchemaDto clientProfileSchema = controlService.getProfileSchemaByApplicationIdAndVersion(
                    endpointProfile.getApplicationId(),
                    endpointProfile.getClientProfileVersion());
            ServerProfileSchemaDto serverProfileSchema = controlService.getServerProfileSchemaByApplicationIdAndVersion(
                    endpointProfile.getApplicationId(),
                    endpointProfile.getServerProfileVersion());
            endpointProfileView.setProfileSchemaName(clientProfileSchema.getName());
            endpointProfileView.setProfileSchemaVersion(clientProfileSchema.toVersionDto());
            endpointProfileView.setServerProfileSchemaName(serverProfileSchema.getName());
            endpointProfileView.setServerProfileSchemaVersion(serverProfileSchema.toVersionDto());
            endpointProfileView.setProfileRecord(createRecordFieldFromCtlSchemaAndBody(clientProfileSchema.getCtlSchemaId(),
                    endpointProfile.getClientProfileBody()));
            endpointProfileView.setServerProfileRecord(createRecordFieldFromCtlSchemaAndBody(serverProfileSchema.getCtlSchemaId(),
                    endpointProfile.getServerProfileBody()));
            List<TopicDto> topics = new ArrayList<>();
            if (endpointProfile.getSubscriptions() != null) {
                for (String topicId : endpointProfile.getSubscriptions()) {
                    topics.add(controlService.getTopic(topicId));
                }
            }
            endpointProfileView.setTopics(topics);
            Set<EndpointGroupDto> endpointGroupsSet = new HashSet<>();
            if (endpointProfile.getGroupState() != null) {
                for (EndpointGroupStateDto endpointGroupState : endpointProfile.getGroupState()) {
                    endpointGroupsSet.add(controlService.getEndpointGroup(endpointGroupState.getEndpointGroupId()));
                }
            }
            List<EndpointGroupDto> endpointGroups = new ArrayList<EndpointGroupDto>(endpointGroupsSet);
            endpointProfileView.setEndpointGroups(endpointGroups);
            return endpointProfileView;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public EndpointProfileDto updateServerProfile(String endpointKeyHash,
                                                  int serverProfileVersion, RecordField serverProfileRecord)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            GenericRecord record = FormAvroConverter.createGenericRecordFromRecordField(serverProfileRecord);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(record.getSchema());
            String serverProfileBody = converter.encodeToJson(record);
            return updateServerProfile(endpointKeyHash, serverProfileVersion, serverProfileBody);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

}
