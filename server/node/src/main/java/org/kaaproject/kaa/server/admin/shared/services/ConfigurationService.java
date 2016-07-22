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

package org.kaaproject.kaa.server.admin.shared.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordFormDto;
import org.kaaproject.kaa.server.admin.shared.config.ConfigurationRecordViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.ConfigurationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/configurationService")
public interface ConfigurationService extends RemoteService {

    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    ConfigurationSchemaDto getConfigurationSchema(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationSchemaDto saveConfigurationSchema(ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException;

    List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    ConfigurationRecordDto getConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    ConfigurationDto editConfiguration(ConfigurationDto configuration) throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException;

    ConfigurationDto activateConfiguration(String configurationId) throws KaaAdminServiceException;

    ConfigurationDto deactivateConfiguration(String configurationId) throws KaaAdminServiceException;

    void deleteConfigurationRecord(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    RecordField generateConfigurationSchemaForm(String fileItemName) throws KaaAdminServiceException;

    ConfigurationSchemaViewDto saveConfigurationSchemaView(ConfigurationSchemaViewDto confSchemaView) throws KaaAdminServiceException;

    ConfigurationSchemaViewDto getConfigurationSchemaView(String configurationSchemaId) throws KaaAdminServiceException;

    ConfigurationRecordViewDto getConfigurationRecordView(String schemaId, String endpointGroupId) throws KaaAdminServiceException;

    ConfigurationRecordFormDto editConfigurationRecordForm(ConfigurationRecordFormDto configuration) throws KaaAdminServiceException;

    ConfigurationRecordFormDto activateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    ConfigurationRecordFormDto deactivateConfigurationRecordForm(String configurationId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getVacantConfigurationSchemaInfosByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    RecordField getConfigurationRecordDataFromFile(String schema, String fileItemName) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserConfigurationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    void editUserConfiguration(EndpointUserConfigurationDto endpointUserConfiguration, String applicationId, RecordField configurationData) throws KaaAdminServiceException;

    ConfigurationSchemaViewDto createConfigurationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

}
