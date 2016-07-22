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
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
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

import java.util.List;

@Api(value = "Verifiers", description = "Provides function for manage verifiers", basePath = "/kaaAdmin/rest")
@Controller
public class VerifierController extends AbstractAdminController {

    /**
     * Gets all user verifiers by application token.
     *
     * @param applicationToken the application token
     * @return the list user verifier dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get verifiers",
            notes = "Returns all verifiers for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request " +
                    "this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userVerifiers/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<UserVerifierDto> getUserVerifiersByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return verifierService.getRestUserVerifiersByApplicationToken(applicationToken);
    }

    /**
     * Gets the user verifier by its id.
     *
     * @param userVerifierId the user verifier id
     * @return the user verifier dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get verifier",
            notes = "Returns a verifier by it's ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An owner verifier with the specified userVerifierId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userVerifier/{userVerifierId}", method = RequestMethod.GET)
    @ResponseBody
    public UserVerifierDto getUserVerifier(
            @ApiParam(name = "userVerifierId", value = "A unique owner verifier identifier", required = true)
            @PathVariable String userVerifierId) throws KaaAdminServiceException {
        return verifierService.getRestUserVerifier(userVerifierId);
    }

    /**
     * Edits user verifier.
     *
     * @param userVerifier the user verifier
     * @return the user verifier dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/edit owner verifier",
            notes = "Creates or edits an owner verifier. If the owner verifier with the specified ID does not exist, it will be created. If the owner " +
                    "verifier with the specified ID exists, it will be updated. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to " +
                    "perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An owner verifier to be edited with the specified id does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userVerifier", method = RequestMethod.POST)
    @ResponseBody
    public UserVerifierDto editUserVerifier(
            @ApiParam(name = "userVerifier", value = "UserVerifierDto body. Mandatory fields: pluginClassName, pluginTypeName, applicationId, name, " +
                    "jsonConfiguration", required = true)
            @RequestBody UserVerifierDto userVerifier) throws KaaAdminServiceException {
        return verifierService.editRestUserVerifier(userVerifier);
    }

    /**
     * Delete user verifier by its id.
     *
     * @param userVerifierId the user verifier id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete owner verifier",
            notes = "Deletes an owner verifier by the owner verifier ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this " +
                    "operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid userVerifierId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An owner verifier with the specified userVerifierId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delUserVerifier", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUserVerifier(
            @ApiParam(name = "userVerifierId", value = "A unique owner verifier identifier", required = true)
            @RequestParam(value = "userVerifierId") String userVerifierId) throws KaaAdminServiceException {
        verifierService.deleteUserVerifier(userVerifierId);
    }

}
