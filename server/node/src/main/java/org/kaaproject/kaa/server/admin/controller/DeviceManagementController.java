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
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Api(value = "Device management", description = "Provides function for device management", basePath = "/kaaAdmin/rest")
@Controller
public class DeviceManagementController extends AbstractAdminController {

    /**
     * Provides security credentials, allowing an endpoint that uses them to
     * interact with the specified application.
     *
     * @param applicationToken The application Token to allow interaction with
     * @param credentialsBody  The security credentials to save
     * @return The security credentials saved
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Provision security credentials",
            notes = " Using this credentials endpoint can interact with specified application. Only users with the TENANT_ADMIN role are allowed to submit " +
                    "this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Parameter conditions \"applicationToken, credentialsBody\" not met for actual request parameters"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 404, message = "The requested item was not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "provisionCredentials", params = {"applicationToken", "credentialsBody"}, method = RequestMethod.POST)
    @ResponseBody
    public CredentialsDto provisionCredentials(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @RequestParam String applicationToken,
            @ApiParam(name = "credentialsBody", value = "The public key hash of the endpoint in Base64 URL safe format", required = true)
            @RequestParam String credentialsBody)
            throws KaaAdminServiceException {
        return this.deviceManagementService.provisionCredentials(applicationToken, credentialsBody);
    }

    /**
     * Provides the status of given credentials.
     *
     * @param applicationToken     The application Token
     * @param credentialsId The ID of the credentials
     * @return Credentials status
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Provides the status of given credentials",
            notes = " Only users with the TENANT_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Parameter conditions \"applicationToken, credentialsId\" not met for actual request parameters"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 404, message = "The requested item was not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "credentialsStatus", params = {"applicationToken", "credentialsId"}, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CredentialsStatus getCredentialsStatus(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @RequestParam String applicationToken,
            @ApiParam(name = "credentialsId", value = "A unique credentials identifier", required = true)
            @RequestParam String credentialsId)
            throws KaaAdminServiceException {
        return this.deviceManagementService.getCredentialsStatus(applicationToken, credentialsId);
    }

    /**
     * Binds credentials to the server-side endpoint profile specified.
     *
     * @param applicationToken     The application Token
     * @param credentialsId        The ID of the credentials to bind
     * @param serverProfileVersion The server-side endpoint profile version
     * @param serverProfileBody    The server-side endpoint profile body
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Bind specified endpoint profile to the credentials",
            notes = " Only users with the TENANT_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "provisionRegistration", params = {"applicationToken", "credentialsId", "serverProfileVersion", "serverProfileBody"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void provisionRegistration(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @RequestParam String applicationToken,
            @ApiParam(name = "credentialsId", value = "A unique credentials identifier", required = true)
            @RequestParam String credentialsId,
            @ApiParam(name = "serverProfileVersion", value = "The version number of server-side endpoint profile", required = true)
            @RequestParam Integer serverProfileVersion,
            @ApiParam(name = "serverProfileBody", value = "The body of server-side endpoint profile", required = true)
            @RequestParam String serverProfileBody)
            throws KaaAdminServiceException {
        this.deviceManagementService.provisionRegistration(applicationToken, credentialsId, serverProfileVersion, serverProfileBody);
    }

    /**
     * Revokes security credentials from the corresponding credentials storage.
     * Also launches an asynchronous process to terminate all active sessions of
     * the endpoint that uses these credentials.
     *
     * @param applicationToken The application Token
     * @param credentialsId    The credentials ID
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Revoke security credentials from the corresponding credentials storage",
            notes = "Launches an asynchronous process to terminate all active sessions of the endpoint that uses these credentials. Only users with the " +
                    "TENANT_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "revokeCredentials", params = {"applicationToken", "credentialsId"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void revokeCredentials(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @RequestParam String applicationToken,
            @ApiParam(name = "credentialsId", value = "A unique credentials identifier", required = true)
            @RequestParam String credentialsId) throws KaaAdminServiceException {
        this.deviceManagementService.revokeCredentials(applicationToken, credentialsId);
    }

    /**
     * Notifies the Kaa cluster about security credentials revocation. If an
     * endpoint is already registered with the specified credentials, this API
     * call launches an asynchronous process to terminate all active sessions of
     * the corresponding endpoint.
     *
     * @param applicationToken The application Token
     * @param credentialsId    The credentials ID
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Notify the Kaa cluster about security credentials revocation",
            notes = "If an endpoint is already registered with the specified credentials, this API all launches an asynchronous process to terminate all " +
                    "active sessions of  the corresponding endpoint. Only users with the TENANT_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 404, message = "An endpoint group to be edited with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "notifyRevoked", params = {"applicationToken", "credentialsId"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void onCredentialsRevoked(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @RequestParam String applicationToken,
            @ApiParam(name = "credentialsId", value = "A unique credentials identifier", required = true)
            @RequestParam String credentialsId) throws KaaAdminServiceException {
        this.deviceManagementService.onCredentialsRevoked(applicationToken, credentialsId);
    }

}
