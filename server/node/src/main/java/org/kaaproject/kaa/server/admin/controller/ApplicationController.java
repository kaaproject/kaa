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
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "Application", description = "Provides function for manage applications", basePath = "/kaaAdmin/rest")
@Controller
public class ApplicationController extends AbstractAdminController {

    /**
     * Gets all applications.
     *
     * @return the list application dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get all applications",
            notes = "Returns all applications for the current authorized user within the current tenant. The current user must have one of the following " +
                    "roles: TENANT_ADMIN, TENANT_DEVELOPER or TENANT_USER.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN, TENANT_DEVELOPER or TENANT_USER)"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "applications", method = RequestMethod.GET)
    @ResponseBody
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        return applicationService.getApplications();
    }

    /**
     * Gets the application by its application token.
     *
     * @param applicationToken the application token
     * @return the application dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get application based on application token",
            notes = "Returns an application by applicationToken. The current user must have one of the following roles: TENANT_ADMIN, TENANT_DEVELOPER or " +
                    "TENANT_USER.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN, TENANT_DEVELOPER, or TENANT_USER) or " +
                    "Tenant ID of the application does not match the Tenant ID of the user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "application/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public ApplicationDto getApplicationByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return applicationService.getApplicationByApplicationToken(applicationToken);
    }

    /**
     * Edits application to the list of all applications.
     *
     * @param application the application
     * @return the application dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit application",
            notes = "Creates or edits an application. To create an application you do not need to specify the application ID. To edit the application " +
                    "specify the application ID. If the application with the specified ID exists, it will be updated. Only users with the TENANT_ADMIN role " +
                    "are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN) or the Tenant ID of the application " +
                    "to be edited does not match the Tenant ID of the user"),
            @ApiResponse(code = 404, message = "An application to be edited with the specified application ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "application", method = RequestMethod.POST)
    @ResponseBody
    public ApplicationDto editApplication(
            @ApiParam(name = "application", value = "ApplicationDto body. Mandatory fields: name, tenantId", required = true)
            @RequestBody ApplicationDto application) throws KaaAdminServiceException {
        return applicationService.editApplication(application);
    }

}
