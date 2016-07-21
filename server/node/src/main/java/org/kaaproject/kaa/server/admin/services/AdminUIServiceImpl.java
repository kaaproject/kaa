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
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.avro.ui.converter.FormAvroConverter;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.server.admin.services.entity.gen.GeneralProperties;
import org.kaaproject.kaa.server.admin.services.entity.gen.SmtpMailProperties;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.properties.PropertiesDto;
import org.kaaproject.kaa.server.admin.shared.services.AdminUIService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.stereotype.Service;

@Service("adminUIService")
public class AdminUIServiceImpl extends AbstractAdminService implements AdminUIService {

    @Override
    public PropertiesDto getMailProperties() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            return propertiesFacade.getPropertiesDto(SmtpMailProperties.class);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto editMailProperties(PropertiesDto mailPropertiesDto) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(mailPropertiesDto, SmtpMailProperties.class);
            messagingService.configureMailSender();
            return storedPropertiesDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto getGeneralProperties() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            return propertiesFacade.getPropertiesDto(GeneralProperties.class);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public PropertiesDto editGeneralProperties(PropertiesDto generalPropertiesDto) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            PropertiesDto storedPropertiesDto = propertiesFacade.editPropertiesDto(generalPropertiesDto, GeneralProperties.class);
            messagingService.configureMailSender();
            return storedPropertiesDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getRecordDataByApplicationIdAndSchemaVersion(String applicationId, int schemaVersion, RecordKey.RecordFiles file)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            RecordKey sdkKey = new RecordKey(applicationId, schemaVersion, file);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public String getRecordLibraryByApplicationIdAndSchemaVersion(String applicationId, int logSchemaVersion, RecordKey.RecordFiles file)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(applicationId);
            RecordKey sdkKey = new RecordKey(applicationId, logSchemaVersion, file);
            return Base64.encodeObject(sdkKey, Base64.URL_SAFE);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createSimpleEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return simpleSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField createCommonEmptySchemaForm() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return commonSchemaFormAvroConverter.getEmptySchemaFormInstance();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateSimpleSchemaForm(String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            validateRecordSchema(schema);
            return simpleSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField generateCommonSchemaForm(String fileItemName) throws KaaAdminServiceException {
        try {
            byte[] data = getFileContent(fileItemName);
            String avroSchema = new String(data);
            Schema schema = new Schema.Parser().parse(avroSchema);
            validateRecordSchema(schema);
            return commonSchemaFormAvroConverter.createSchemaFormFromSchema(schema);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public RecordField getRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            byte[] body = getFileContent(fileItemName);
            GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema);
            GenericRecord record = converter.decodeJson(body);
            RecordField recordData = FormAvroConverter.createRecordFieldFromGenericRecord(record);
            return recordData;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
}