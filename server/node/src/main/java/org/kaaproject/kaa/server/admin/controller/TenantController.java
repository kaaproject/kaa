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
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

@Api(value = "Tenant", description = "Provides function for manage tenants", basePath = "/kaaAdmin/rest")
@Controller
public class TenantController extends AbstractAdminController {

    /**
     * Gets all tenants.
     *
     * @return the list of tenant user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get all tenants",
            notes = "Returns all tenants existing in the system. Only users with the KAA_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the KAA_ADMIN role"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "tenants", method = RequestMethod.GET)
    @ResponseBody
    public List<TenantDto> getTenants() throws KaaAdminServiceException {
        return tenantService.getTenants();
    }

    /**
     * Gets the tenant by user id.
     *
     * @param userId the user id
     * @return the tenant user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get tenant",
            notes = "Returns tenant by associated userId. Only users with the KAA_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid userId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the KAA_ADMIN role"),
            @ApiResponse(code = 404, message = "A tenant with the specified userId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "tenant/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public TenantDto getTenant(
            @ApiParam(name = "userId", value = "A unique user identifier", required = true)
            @PathVariable("userId") String userId) throws KaaAdminServiceException {
        return tenantService.getTenant(userId);
    }

    /**
     * Edits tenant to the list of all tenants.
     *
     * @param tenantUser the tenant user
     * @return the tenant user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit tenant",
            notes = "Creates or edits a tenant. If a tenant with the specified ID does not exist, it will be created. If a tenant with the specified ID " +
                    "exists, it will be updated. Only users with the KAA_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the  KAA_ADMIN role"),
            @ApiResponse(code = 404, message = "A tenant with the specified user ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "tenant", method = RequestMethod.POST)
    @ResponseBody
    public TenantDto editTenant(@Valid @RequestBody TenantDto tenantUser) throws KaaAdminServiceException {
        try
        {
            TenantDto tenant = tenantService.editTenant(tenantUser);
            return tenant;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
}
