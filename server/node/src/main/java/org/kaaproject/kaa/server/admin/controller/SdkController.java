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
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.servlet.ServletUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "SDK profiles", description = "Provides function for manage SDK profiles", basePath = "/kaaAdmin/rest")
@Controller
public class SdkController extends AbstractAdminController {

    /**
     * The Constant BUFFER.
     */
    private static final int BUFFER = 1024 * 100;

    /**
     * Stores a new SDK profile into the database.
     *
     * @param sdkProfile the sdk profile
     * @return the sdk profile dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create SDK profile",
            notes = "Creates an SDK profile for further endpoint SDK generation. (Requires TENANT_DEVELOPER or TENANT_USER to perform this operation).")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "createSdkProfile", method = RequestMethod.POST)
    @ResponseBody
    public SdkProfileDto createSdkProfile(
            @ApiParam(name = "sdkProfile", value = "SdkProfileDto body. Mandatory fields: applicationId, configurationSchemaVersion, logSchemaVersion, " +
                    "notificationSchemaVersion, profileSchemaVersion, name", required = true)
            @RequestBody SdkProfileDto sdkProfile) throws KaaAdminServiceException {
        return sdkService.createSdkProfile(sdkProfile);
    }

    /**
     * Deletes an SDK profile by its identifier.
     *
     * @param sdkProfileId the sdk profile id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete SDK profile",
            notes = "Deletes an SDK profile from the database. (Requires TENANT_DEVELOPER or TENANT_USER to perform this operation).")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid sdkProfileId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The SDK profile with the specified sdkProfileId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "deleteSdkProfile", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteSdkProfile(
            @ApiParam(name = "sdkProfileId", value = "A unique SDK profile identifier", required = true)
            @RequestParam(value = "sdkProfileId") String sdkProfileId) throws KaaAdminServiceException {
        sdkService.deleteSdkProfile(sdkProfileId);
    }

    /**
     * Returns a list of SDK profiles for the given application.
     *
     * @param applicationToken the application token
     * @return the list sdk profile dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get SDK profiles for application",
            notes = "Returns a list of SDK profiles for the given application. (Requires TENANT_DEVELOPER or TENANT_USER to request this information).")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The application with the specified ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "sdkProfiles/{applicationToken}")
    @ResponseBody
    public List<SdkProfileDto> getSdkProfilesByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return sdkService.getSdkProfilesByApplicationToken(applicationToken);
    }

    /**
     * Returns an SDK profile by its identifier.
     *
     * @param sdkProfileId the sdk profile id
     * @return the sdk profile dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get specific SDK profile",
            notes = "Returns the SDK profile by its identifier. (Requires TENANT_DEVELOPER or TENANT_USER to request this information).")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid sdkProfileId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The SDK profile with the specified ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "sdkProfile/{sdkProfileId}")
    @ResponseBody
    public SdkProfileDto getSdkProfile(
            @ApiParam(name = "sdkProfileId", value = "A unique SDK profile identifier", required = true)
            @PathVariable String sdkProfileId) throws KaaAdminServiceException {
        return sdkService.getSdkProfile(sdkProfileId);
    }

    /**
     * Generates an SDK for the specified target platform from an SDK profile .
     *
     * @param sdkProfileId   the sdk profile id
     * @param targetPlatform the target platform
     * @param request        the request
     * @param response       the response
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Generate endpoint SDK",
            notes = "Generates the endpoint SDK based on the SDK profile. (Requires TENANT_DEVELOPER or TENANT_USER to perform this operation). For android " +
                    "or java client file extension must be .jar, for other platforms extension must be .tar.gz.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "An unknown target platform was specified"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The SDK profile with the given ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "sdk", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSdk(
            @ApiParam(name = "sdkProfileId", value = "A unique SDK profile identifier", required = true)
            @RequestParam(value = "sdkProfileId") String sdkProfileId,
            @ApiParam(name = "targetPlatform", value = "The target platform's name (either JAVA, ANDROID, CPP, C or OBJC)", required = true)
            @RequestParam(value = "targetPlatform") String targetPlatform, HttpServletRequest request, HttpServletResponse response)
            throws KaaAdminServiceException {
        try {
            SdkProfileDto sdkProfile = sdkService.getSdkProfile(sdkProfileId);
            FileData sdkData = sdkService.getSdk(sdkProfile, SdkPlatform.valueOf(targetPlatform.toUpperCase()));
            response.setContentType(sdkData.getContentType());
            ServletUtils.prepareDisposition(request, response, sdkData.getFileName());
            response.setContentLength(sdkData.getFileData().length);
            response.setBufferSize(BUFFER);
            response.getOutputStream().write(sdkData.getFileData());
            response.flushBuffer();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    /**
     * Flushes all cached Sdks within tenant.
     *
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "flushSdkCache", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void flushSdkCache() throws KaaAdminServiceException {
        try {
            sdkService.flushSdkCache();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    /**
     * Gets the schema versions by application token.
     *
     * @param applicationToken the application token
     * @return the schema versions
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "schemaVersions/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public SchemaVersions getSchemaVersionsByApplicationToken(@PathVariable String applicationToken) throws KaaAdminServiceException {
        return sdkService.getSchemaVersionsByApplicationToken(applicationToken);
    }

}
