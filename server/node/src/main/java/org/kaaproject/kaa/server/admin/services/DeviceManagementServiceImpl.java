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
import org.apache.commons.lang3.Validate;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.DeviceManagementService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.common.dao.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service("deviceManagementService")
public class DeviceManagementServiceImpl extends AbstractAdminService implements DeviceManagementService {

    @Override
    public CredentialsDto provisionCredentials(String applicationToken, String credentialsBody) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String applicationId = checkApplicationToken(applicationToken);
            return this.controlService.provisionCredentials(applicationId, credentialsBody);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void provisionRegistration(
            String applicationToken,
            String credentialsId,
            Integer serverProfileVersion,
            String serverProfileBody)
            throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String applicationId = checkApplicationToken(applicationToken);
            Optional<CredentialsDto> credentials = this.controlService.getCredentials(applicationId, credentialsId);
            Validate.isTrue(credentials.isPresent(), "No credentials with the given ID found!");
            Validate.isTrue(credentials.get().getStatus() != CredentialsStatus.REVOKED, "The credentials with the given ID are revoked!");
            if (serverProfileVersion != null && serverProfileBody != null) {
                ServerProfileSchemaDto serverProfileSchema = this.getServerProfileSchema(applicationId, serverProfileVersion);
                this.validateServerProfile(serverProfileSchema, serverProfileBody);
            } else if (serverProfileVersion != null || serverProfileBody != null) {
                String missingParameter = (serverProfileVersion == null ? "schema version" : "body");
                String message = MessageFormat.format("The server-side endpoint profile {0} provided is empty!", missingParameter);
                throw new IllegalArgumentException(message);
            }
            this.controlService.provisionRegistration(applicationId, credentialsId, serverProfileVersion, serverProfileBody);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void revokeCredentials(String applicationToken, String credentialsId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String applicationId = checkApplicationToken(applicationToken);
            Validate.isTrue(this.controlService.getCredentials(applicationId, credentialsId).isPresent(), "No credentials with the given ID found!");
            this.controlService.revokeCredentials(applicationId, credentialsId);
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public void onCredentialsRevoked(String applicationToken, String credentialsId) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String applicationId = checkApplicationToken(applicationToken);
            Optional<CredentialsDto> credentials = this.controlService.getCredentials(applicationId, credentialsId);
            Validate.isTrue(credentials.isPresent(), "No credentials with the given ID found!");
            Validate.isTrue(credentials.get().getStatus() == CredentialsStatus.REVOKED, "Credentails with the given ID are not revoked!");
            this.controlService.onCredentailsRevoked(applicationId, credentials.get().getId());
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public CredentialsStatus getCredentialsStatus(
            String applicationToken,
            String credentialsId
    ) throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            String applicationId = checkApplicationToken(applicationToken);
            Optional<CredentialsDto> credentials = this.controlService.getCredentials(applicationId, credentialsId);
            Validate.isTrue(credentials.isPresent(), "No credentials with the given ID found!");
            return credentials.get().getStatus();
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    @Override
    public List<String> getCredentialsServiceNames() throws KaaAdminServiceException {
        this.checkAuthority(KaaAuthorityDto.values());
        try {
            return this.controlService.getCredentialsServiceNames();
        } catch (Exception cause) {
            throw Utils.handleException(cause);
        }
    }

    private ServerProfileSchemaDto getServerProfileSchema(String applicationId, Integer serverProfileVersion) throws Exception {
        ServerProfileSchemaDto serverProfileSchema = this.controlService.getServerProfileSchemaByApplicationIdAndVersion(applicationId, serverProfileVersion);
        if (serverProfileSchema == null) {
            throw new NotFoundException("No server-side endpoint profile schema found!");
        }
        return serverProfileSchema;
    }

    private void validateServerProfile(ServerProfileSchemaDto serverProfileSchema, String serverProfileBody) throws Exception {
        CTLSchemaDto commonType = this.controlService.getCTLSchemaById(serverProfileSchema.getCtlSchemaId());
        Schema typeSchema = this.controlService.exportCTLSchemaFlatAsSchema(commonType);
        try {
            new GenericAvroConverter<GenericRecord>(typeSchema).decodeJson(serverProfileBody);
        } catch (Exception cause) {
            throw new IllegalArgumentException("Invalid server-side endpoint profile body provided!");
        }
    }

}
