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
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
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

@Api(value = "Profiling", description = "Provides function for manage profiles", basePath = "/kaaAdmin/rest")
@Controller
public class ProfileController extends AbstractAdminController {

    /**
     * Gets the profile schemas by application token.
     *
     * @param applicationToken the application token
     * @return the list of endpoint profile schema dto objects
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get client-side endpoint profile schemas",
            notes = "Returns the client-side endpoint profile schemas for an application. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information. A users can only request schemas for applications associated with the same user.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "profileSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<EndpointProfileSchemaDto> getProfileSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken)
            throws KaaAdminServiceException {
        return profileService.getProfileSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the profile schema by its id.
     *
     * @param profileSchemaId the profile schema id
     * @return the endpoint profile schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get client-side endpoint profile schema",
            notes = "Returns the client-side endpoint profile schema by Profile Schema ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid profileSchemaId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The client-side endpoint profile schema with the specified profileSchemaId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "profileSchema/{profileSchemaId}", method = RequestMethod.GET)
    @ResponseBody
    public EndpointProfileSchemaDto getProfileSchema(
            @ApiParam(name = "profileSchemaId", value = "A unique client-side endpoint profile schema identifier", required = true)
            @PathVariable String profileSchemaId) throws KaaAdminServiceException {
        return profileService.getProfileSchema(profileSchemaId);
    }

    /**
     * Saves profile schema.
     *
     * @param profileSchema the profile schema
     * @return the profile schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create client-side endpoint profile schema",
            notes = "Uploads a client-side endpoint profile schema. A unique version number will be generated (incrementally) for the uploaded schema, " +
                    "and the createdUsername field of the schema will be set to the name of the user who uploaded it. Only users with the TENANT_DEVELOPER " +
                    "or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Specified request body is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Application or referenced CT schema is not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "saveProfileSchema", method = RequestMethod.POST)
    @ResponseBody
    public EndpointProfileSchemaDto saveProfileSchema(
            @ApiParam(name = "profileSchema", value = "EndpointProfileSchemaDto body. Mandatory fields: applicationId, ctlSchemaId, name", required = true)
            @RequestBody EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException {
        return profileService.saveProfileSchema(profileSchema);
    }

    /**
     * Gets the server profile schemas by application token.
     *
     * @param applicationToken the application token
     * @return the list profile schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get server-side endpoint profile schemas",
            notes = "Returns the server-side endpoint profile schemas for an application. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information. A users can only request schemas for applications associated with the same user.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "serverProfileSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken)
            throws KaaAdminServiceException {
        return profileService.getServerProfileSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the server profile schema by its id.
     *
     * @param serverProfileSchemaId the profile schema id
     * @return the profile schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get server-side endpoint profile schema",
            notes = "Returns the server-side endpoint profile schema by Server Profile Schema ID. Only users with the TENANT_DEVELOPER or TENANT_USER role " +
                    "are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid serverProfileSchemaId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Server-side endpoint profile schema with the specified serverProfileSchemaId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "serverProfileSchema/{serverProfileSchemaId}", method = RequestMethod.GET)
    @ResponseBody
    public ServerProfileSchemaDto getServerProfileSchema(
            @ApiParam(name = "serverProfileSchemaId", value = "A unique server-side endpoint profile schema identifier", required = true)
            @PathVariable String serverProfileSchemaId) throws KaaAdminServiceException {
        return profileService.getServerProfileSchema(serverProfileSchemaId);
    }

    /**
     * Saves server profile schema.
     *
     * @param serverProfileSchema the profile schema
     * @return the server profile schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create server-side endpoint profile schema",
            notes = "Uploads a server-side endpoint profile schema. A unique version number will be generated (incrementally) for the uploaded schema, " +
                    "and the createdUsername field of the schema will be set to the name of the user who uploaded it. Only users with the TENANT_DEVELOPER " +
                    "or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Specified request body is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Application or referenced CT schema is not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "saveServerProfileSchema", method = RequestMethod.POST)
    @ResponseBody
    public ServerProfileSchemaDto saveServerProfileSchema(
            @ApiParam(name = "serverProfileSchema", value = "ServerProfileSchemaDto body. Mandatory fields: applicationId, ctlSchemaId, name", required = true)
            @RequestBody ServerProfileSchemaDto serverProfileSchema)
            throws KaaAdminServiceException {
        return profileService.saveServerProfileSchema(serverProfileSchema);
    }

    /**
     * Update server profile of endpoint.
     *
     * @param endpointProfileKey the endpoint profile key
     * @param version            the version
     * @param serverProfileBody  the server profile body
     * @return the endpoint profile dto
     * @throws Exception the exception
     */
    @ApiOperation(value = "Update server-side endpoint profile",
            notes = "Update a server-side endpoint profile. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Specified server-side endpoint profile body is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER)"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "updateServerProfile", method = RequestMethod.POST)
    @ResponseBody
    public EndpointProfileDto updateServerProfile(
            @ApiParam(name = "endpointProfileKey", value = "The key hash of the endpoint in Base64 URL safe format", required = true)
            @RequestParam(value = "endpointProfileKey") String endpointProfileKey,
            @ApiParam(name = "version", value = "The version of the server-side endpoint profile schema", required = true)
            @RequestParam(value = "version") int version,
            @ApiParam(name = "serverProfileBody", value = "The server-side endpoint profile body", required = true)
            @RequestParam(value = "serverProfileBody") String serverProfileBody)
            throws Exception {
        return profileService.updateServerProfile(endpointProfileKey, version, serverProfileBody);
    }

    /**
     * Gets the endpoint profile by endpoint key.
     *
     * @param endpointProfileKey the endpoint profile key
     * @return the endpoint profile dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get profile based on endpoint key",
            notes = "Returns the endpoint profile based on endpoint key hash. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The endpoint profile with the specified endpointProfileKey does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointProfile/{endpointProfileKey}", method = RequestMethod.GET)
    @ResponseBody
    public EndpointProfileDto getEndpointProfileByKeyHash(
            @ApiParam(name = "endpointProfileKey", value = "The key hash of the endpoint in Base64 URL safe format", required = true)
            @PathVariable String endpointProfileKey) throws KaaAdminServiceException {
        return profileService.getEndpointProfileByKeyHash(endpointProfileKey);
    }

    /**
     * Gets the endpoint profile body by endpoint key.
     *
     * @param endpointProfileKey the endpoint profile key
     * @return the endpoint profile body dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get client- and server-side endpoint profile bodies based on endpoint key",
            notes = "Returns the client- and server-side endpoint profile bodies based on endpoint key hash. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group to be edited with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointProfileBody/{endpointProfileKey}", method = RequestMethod.GET)
    @ResponseBody
    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(
            @ApiParam(name = "endpointProfileKey", value = "The key hash of the endpoint in Base64 URL safe format", required = true)
            @PathVariable String endpointProfileKey) throws KaaAdminServiceException {
        return profileService.getEndpointProfileBodyByKeyHash(endpointProfileKey);
    }

    /**
     * Returns a list of endpoint profiles attached to the endpoint user with
     * the given external ID.
     *
     * @param endpointUserExternalId The endpoint user external ID
     * @return A list of endpoint profiles for the user with the given external ID
     * @throws KaaAdminServiceException - if an exception occures.
     */
    @ApiOperation(value = "Get endpoint profiles by owner ID",
            notes = "Returns a list of endpoint profiles by the ID of the endpoint owner. (Requires TENANT_DEVELOPER or TENANT_USER to request this " +
                    "information).")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid userExternalId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The endpoint user with the given ID not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointProfiles", params = {"userExternalId"}, method = RequestMethod.GET)
    @ResponseBody
    public List<EndpointProfileDto> getEndpointProfilesByUserExternalId(
            @ApiParam(name = "userExternalId", value = "A unique endpoint owner identifier", required = true)
            @RequestParam("userExternalId") String endpointUserExternalId) throws KaaAdminServiceException {
        return this.profileService.getEndpointProfilesByUserExternalId(endpointUserExternalId);
    }

    /**
     * Remove the endpoint with specific profile key.
     *
     * @param endpointProfileKeyHash the endpoint profile key hash
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete endpoint",
            notes = "Delete endpoint profile based on endpoint key hash. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform " +
                    "this operation")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint profile to be deleted with the specified endpointProfileKeyHash does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "removeEndpointProfileByKeyHash", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void removeEndpointProfileByKeyHash(
            @ApiParam(name = "endpointProfileKeyHash", value = "The key hash of the endpoint in Base64 URL safe format", required = true)
            @RequestParam(value = "endpointProfileKeyHash") String endpointProfileKeyHash) throws KaaAdminServiceException {
        profileService.removeEndpointProfileByKeyHash(endpointProfileKeyHash);
    }

}
