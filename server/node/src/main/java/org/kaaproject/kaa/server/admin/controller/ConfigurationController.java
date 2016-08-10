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

package org.kaaproject.kaa.server.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.charset.Charset;
import java.util.List;

@Api(value = "Configuration", description = "Provides function for manage configuration", basePath = "/kaaAdmin/rest")
@Controller
public class ConfigurationController extends AbstractAdminController {

    private static final Charset DECODING_CHARSET = Charset.forName("ISO-8859-1");

    /**
     * Gets the configuration schemas by application token.
     *
     * @param applicationToken the application token
     * @return the сonfiguration schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get configuration schemas",
            notes = "Returns all configuration schemas for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to perform this operation. The application must be associated with the user who has submitted the request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Application with the specified applicationId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configurationSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken)
            throws KaaAdminServiceException {
        return configurationService.getConfigurationSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the vacant configuration schemas by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get vacant configuration schemas",
            notes = "Returns all vacant (not being used by endpoint group configurations) configuration schemas for the specified endpoint group. Only users " +
                    "with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group to be edited with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "vacantConfigurationSchemas/{endpointGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public List<VersionDto> getVacantConfigurationSchemasByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @PathVariable String endpointGroupId)
            throws KaaAdminServiceException {
        return configurationService.getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets the configuration schema by her id.
     *
     * @param configurationSchemaId the сonfiguration schema id
     * @return the сonfiguration schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get configuration schema",
            notes = "Returns a configuration schema by configuration schema ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The configuration schema with the specified configurationSchemaId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configurationSchema/{configurationSchemaId}", method = RequestMethod.GET)
    @ResponseBody
    public ConfigurationSchemaDto getConfigurationSchema(
            @ApiParam(name = "configurationSchemaId", value = "A unique configuration schema identifier", required = true)
            @PathVariable String configurationSchemaId) throws KaaAdminServiceException {
        return configurationService.getConfigurationSchema(configurationSchemaId);
    }

    /**
     * Adds configuration schema to the list of all configuration schemas.
     *
     * @param configurationSchema the сonfiguration schema
     * @return the сonfiguration schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create configuration schema",
            notes = "Uploads a configuration schema. A unique version number will be generated (incrementally) for the uploaded schema, and the " +
                    "createUsername field of the schema will be set to the name of the user who uploaded it. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Specified configuration schema is not a valid avro schema"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "saveConfigurationSchema", method = RequestMethod.POST)
    @ResponseBody
    public ConfigurationSchemaDto saveConfigurationSchema(
            @ApiParam(name = "configurationSchema", value = "ConfigurationSchemaDto body", required = true)
            @RequestBody  ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException {
        return configurationService.saveConfigurationSchema(configurationSchema);
    }

    /**
     * Gets the configuration records by endpoint group id.
     *
     * @param endpointGroupId   the endpoint group id
     * @param includeDeprecated the include deprecated
     * @return the list configuration record dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get configuration records",
            notes = "Returns all configuration records for the specified endpoint group. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configurationRecords", method = RequestMethod.GET)
    @ResponseBody
    public List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @ApiParam(name = "includeDeprecated", value = "[“true” or ”false”] if “true”, all configuration records will be returned, including deprecated " +
                    "ones. If “false”, only active and inactive configuration records will be returned", required = true)
            @RequestParam(value = "includeDeprecated") boolean includeDeprecated) throws KaaAdminServiceException {
        return configurationService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
    }

    /**
     * Gets the configuration record by schema id and endpoint group id.
     *
     * @param schemaId        the schema id
     * @param endpointGroupId the endpoint group id
     * @return the configuration record dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get configuration record",
            notes = "Returns the configuration record for the specified endpoint group and configuration schema. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or the endpoint group does not have the " +
                    "requested configuration record"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configurationRecord", method = RequestMethod.GET)
    @ResponseBody
    public ConfigurationRecordDto getConfigurationRecord(
            @ApiParam(name = "schemaId", value = "A unique configuration schema identifier", required = true)
            @RequestParam(value = "schemaId") String schemaId,
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return configurationService.getConfigurationRecord(schemaId, endpointGroupId);
    }


    @ApiOperation(value = "Get configuration record body",
            notes = "Returns the configuration record string body for the specified endpoint group and configuration schema. Only users with the " +
                    "TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or the endpoint group does not have the " +
                    "requested configuration record"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configurationRecordBody", params = {"schemaId", "endpointGroupId"}, method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getConfigurationRecordBody(
            @ApiParam(name = "schemaId", value = "A unique configuration schema identifier", required = true)
            @RequestParam(value = "schemaId") String schemaId,
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam("endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        String response = configurationService.getConfigurationRecord(schemaId, endpointGroupId).getActiveStructureDto().getBody();
        String decodedResponse = new String(response.getBytes(), DECODING_CHARSET);
        return decodedResponse;
    }

    /**
     * Edits the configuration to the list of all configurations.
     *
     * @param configuration the configuration
     * @return the configuration dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit configuration",
            notes = "Creates or updates a configuration. To create configuration you do not need to specify the configuration ID, createUsername field will " +
                    "be set to the name of the user who has uploaded it. To edit the configuration specify the configuration id. If a configuration with the " +
                    "specified ID exists, the configuration will be updated and its modifyUsername will be set to the user who has submitted the request. " +
                    "Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or a configuration with the specified " +
                    "configurationId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "configuration", method = RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto editConfiguration(
            @ApiParam(name = "configuration", value = "ConfigurationDto body. Mandatory fields: applicationId, schemaId, endpointGroupId, body", required = true)
            @RequestBody ConfigurationDto configuration) throws KaaAdminServiceException {
        return configurationService.editConfiguration(configuration);
    }

    /**
     * Creates or updates a configuration for the specific user under the application.
     *
     * @param endpointUserConfiguration the endpoint user configuration
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit user configuration",
            notes = "Creates or updates a configuration for the specific user under the application. TIf a configuration with the specified user ID does not " +
                    "exist, then it will be created. If a configuration with the specified user ID exists, the configuration will be updated. Only users " +
                    "with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A user with the specified userId or application with the specified appToken does not exists"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userConfiguration", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void editUserConfiguration(
            @ApiParam(name = "endpointUserConfiguration", value = "EndpointUserConfigurationDto body. Mandatory fields: userId, appToken, schemaVersion, " +
                    "body", required = true)
            @RequestBody EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException {
        configurationService.editUserConfiguration(endpointUserConfiguration);
    }

    /**
     * Activate configuration by its id.
     *
     * @param configurationId the configuration id
     * @return the configuration dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Activate configuration",
            notes = "Activates a configuration. Sets the status field of the configuration to ACTIVE, increments the sequenceNumber field value by 1, and " +
                    "sets the activateUsername and activatedTime fields to the name of the user who has submitted the request and the activation time " +
                    "respectively. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Configuration with the specified configurationId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "activateConfiguration", method = RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto activateConfiguration(
            @ApiParam(name = "configurationId", value = "A unique configuration identifier", required = true)
            @RequestBody String configurationId) throws KaaAdminServiceException {
        return configurationService.activateConfiguration(configurationId);
    }

    /**
     * Deactivate configuration by its id.
     *
     * @param configurationId the configuration id
     * @return the configuration dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Deactivate configuration",
            notes = "Deactivates a configuration. Sets the status field of the configuration to DEPRECATED, sets the deactivatedTime field value to the " +
                    "time of the deactivation request, and sets the deactivateUsername field to the name of the user who has submitted the request. " +
                    "Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Configuration with the specified configurationId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "deactivateConfiguration", method = RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto deactivateConfiguration(
            @ApiParam(name = "configurationId", value = "A unique configuration identifier", required = true)
            @RequestBody String configurationId) throws KaaAdminServiceException {
        return configurationService.deactivateConfiguration(configurationId);
    }

    /**
     * Delete configuration record by schema id and endpoint group id.
     *
     * @param schemaId        the schema id
     * @param endpointGroupId the endpoint group id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete configuration record",
            notes = "Deletes a configuration record by configuration schema ID and endpoint group ID. Only users with the TENANT_DEVELOPER or TENANT_USER " +
                    "role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or the endpoint group does not have the " +
                    "requested configuration record"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delConfigurationRecord", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteConfigurationRecord(
            @ApiParam(name = "schemaId", value = "A unique configuration schema identifier", required = true)
            @RequestParam(value = "schemaId") String schemaId,
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        configurationService.deleteConfigurationRecord(schemaId, endpointGroupId);
    }

}
