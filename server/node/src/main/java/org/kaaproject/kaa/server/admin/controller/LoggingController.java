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
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.servlet.ServletUtils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Logging", description = "Provides function for manage logging", basePath = "/kaaAdmin/rest")
@Controller
public class LoggingController extends AbstractAdminController {

    /**
     * The Constant BUFFER.
     */
    private static final int BUFFER = 1024 * 100;

    /**
     * The cache service.
     */
    @Autowired
    private CacheService cacheService;

    /**
     * Gets all log schemas by application token.
     *
     * @param applicationToken the application Token
     * @return the list log schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get log schemas",
            notes = "Returns all log schemas for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request " +
                    "this information. The Tenant ID value of the application must match Tenant ID of the request submitter.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<LogSchemaDto> getLogSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return loggingService.getLogSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the log schema by its id.
     *
     * @param logSchemaId the log schema id
     * @return the log schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get log schema",
            notes = "Returns a log schema by log schema ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A log schema with the specified logSchemaId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logSchema/{logSchemaId}", method = RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchema(
            @ApiParam(name = "logSchemaId", value = "A unique log schema identifier", required = true)
            @PathVariable String logSchemaId) throws KaaAdminServiceException {
        return loggingService.getLogSchema(logSchemaId);
    }

    /**
     * Gets the log schema by application token and schema version.
     *
     * @param applicationToken the application token
     * @param schemaVersion    the schema version
     * @return the log schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get log schema by application token and schema version",
            notes = "Returns a log schema for the specified schema version and application token. Only users with the TENANT_DEVELOPER or TENANT_USER role " +
                    "are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group to be edited with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logSchema/{applicationToken}/{schemaVersion}", method = RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken,
            @ApiParam(name = "schemaVersion", value = "The version of the requested log schema", required = true)
            @PathVariable int schemaVersion)
            throws KaaAdminServiceException {
        return loggingService.getLogSchemaByApplicationTokenAndVersion(applicationToken, schemaVersion);
    }

    /**
     * Adds log schema to the list of all log schemas.
     *
     * @param logSchema
     *            the log schema
     * @return the log schema dto
     * @throws KaaAdminServiceException
     *             the kaa admin service exception
     */
    @RequestMapping(value = "createLogSchema", method = RequestMethod.POST)
    @ResponseBody
    public LogSchemaDto createLogSchema(@RequestBody LogSchemaDto logSchema)
            throws KaaAdminServiceException {
        return loggingService.saveLogSchema(logSchema);
    }

    /**
     * Edits existing log schema.
     *
     * @param logSchema the log schema
     * @return the log schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit log schema",
            notes = "Creates or updates a log schema. If a log schema with the specified ID does not exist, then it will be created and its createUsername " +
                    "field of the schema will be set to the name of the user who has uploaded it, a unique version number will be generated (incrementally) " +
                    "for this schema. If a configuration with the specified ID exists, the configuration will be updated. Only users with the " +
                    "TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "saveLogSchema", method = RequestMethod.POST)
    @ResponseBody
    public LogSchemaDto saveLogSchema(
            @ApiParam(name = "logSchema", value = "LogSchemaDto body.", required = true)
            @RequestBody LogSchemaDto logSchema) throws KaaAdminServiceException {
        return loggingService.saveLogSchema(logSchema);
    }

    /**
     * Gets all log appenders by application token.
     *
     * @param applicationToken the application token
     * @return the list log appender dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get log appenders",
            notes = "Returns all log appenders for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to " +
                    "request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logAppenders/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<LogAppenderDto> getLogAppendersByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return loggingService.getRestLogAppendersByApplicationToken(applicationToken);
    }

    /**
     * Gets the log appender by its id.
     *
     * @param logAppenderId the log appender id
     * @return the log appender dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get log appender",
            notes = "Returns a log appender by log appender ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this " +
                    "information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A log appender with the specified logAppenderId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logAppender/{logAppenderId}", method = RequestMethod.GET)
    @ResponseBody
    public LogAppenderDto getLogAppender(
            @ApiParam(name = "logAppenderId", value = "A unique log appender identifier", required = true)
            @PathVariable String logAppenderId) throws KaaAdminServiceException {
        return loggingService.getRestLogAppender(logAppenderId);
    }

    /**
     * Edits log appender.
     *
     * @param logAppender the log appender
     * @return the log appender dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit log appender",
            notes = "Creates or edits a log appender. If a log appender with the specified ID does not exist, it will be created. If a log appender with the " +
                    "specified ID exists, it will be updated. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A log appender to be edited with the specified id does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "logAppender", method = RequestMethod.POST)
    @ResponseBody
    public LogAppenderDto editLogAppender(
            @ApiParam(name = "logAppender", value = "LogAppenderDto body", required = true)
            @RequestBody LogAppenderDto logAppender) throws KaaAdminServiceException {
        return loggingService.editRestLogAppender(logAppender);
    }

    /**
     * Delete log appender by its id.
     *
     * @param logAppenderId the log appender id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete log appender",
            notes = "Deletes a log appender by log appender ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A log appender with the specified logAppenderId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delLogAppender", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteLogAppender(
            @ApiParam(name = "logAppenderId", value = "A unique log appender identifier", required = true)
            @RequestParam(value = "logAppenderId") String logAppenderId) throws KaaAdminServiceException {
        loggingService.deleteLogAppender(logAppenderId);
    }

    /**
     * Generate log library by record key.
     *
     * @param key      the key
     * @param request  the request
     * @param response the response
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "logLibrary", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getRecordLibrary(
            @RequestBody RecordKey key, HttpServletRequest request, HttpServletResponse response)
            throws KaaAdminServiceException {
        try {
            FileData file = cacheService.getRecordLibrary(key);
            response.setContentType("application/java-archive");
            ServletUtils.prepareDisposition(request, response, file.getFileName());
            response.setContentLength(file.getFileData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(file.getFileData());
            response.flushBuffer();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    /**
     * Get log record schema with header and log schema inside by record key.
     *
     * @param key      the key
     * @param request  the request
     * @param response the response
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "logRecordSchema", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getRecordSchema(
            @RequestBody RecordKey key, HttpServletRequest request, HttpServletResponse response)
            throws KaaAdminServiceException {
        try {
            FileData file = cacheService.getRecordSchema(key);
            response.setContentType("text/plain");
            ServletUtils.prepareDisposition(request, response, file.getFileName());
            response.setContentLength(file.getFileData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(file.getFileData());
            response.flushBuffer();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

}
