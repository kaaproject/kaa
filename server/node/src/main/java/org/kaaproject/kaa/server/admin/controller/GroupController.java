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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(value = "Grouping", description = "Provides function for manage groups", basePath = "/kaaAdmin/rest")
@Controller
public class GroupController extends AbstractAdminController {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GroupController.class);

    /**
     * The Constant HTTPS_PORT.
     */
    public static final int HTTPS_PORT = 443;

    /**
     * The Constant HTTP_PORT.
     */
    public static final int HTTP_PORT = 80;

    /**
     * The Constant DEFAULT_LIMIT.
     */
    private static final String DEFAULT_LIMIT = "20";

    /**
     * The Constant DEFAULT_OFFSET.
     */
    private static final String DEFAULT_OFFSET = "0";

    /**
     * Gets all endpoint groups by application token.
     *
     * @param applicationToken the application token
     * @return the list endpoint group dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get endpoint groups based on application token",
            notes = "Returns all endpoint groups for the specified application based on application token. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointGroups/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<EndpointGroupDto> getEndpointGroupsByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return groupService.getEndpointGroupsByApplicationToken(applicationToken);
    }

    /**
     * Gets the endpoint group by its id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the endpoint group dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get endpoint group based on endpoint group id",
            notes = "Returns an endpoint group by Endpoint Group ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this " +
                    "information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointGroup/{endpointGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public EndpointGroupDto getEndpointGroup(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return groupService.getEndpointGroup(endpointGroupId);
    }

    /**
     * Edits endpoint group to the list of all endpoint groups.
     *
     * @param endpointGroup the endpoint group
     * @return the endpoint group dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit endpoint group",
            notes = "Creates or edits an endpoint group. To create endpoint group you do not need to specify the endpoint group ID. To edit the endpoint group" +
                    " specify the endpoint group ID. If an endpoint group with the specified id exists, it will be updated. Only users with the " +
                    "TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group to be edited with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointGroup", method = RequestMethod.POST)
    @ResponseBody
    public EndpointGroupDto editEndpointGroup(
            @ApiParam(name = "endpointGroup", value = "EndpointGroupDto body. Mandatory fields: name, applicationId, weight", required = true)
            @RequestBody EndpointGroupDto endpointGroup) throws KaaAdminServiceException {
        return groupService.editEndpointGroup(endpointGroup);
    }

    /**
     * Delete endpoint group by its id.
     *
     * @param endpointGroupId the endpoint group id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete endpoint group",
            notes = "Deletes an endpoint group specified by endpoint group ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to " +
                    "perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delEndpointGroup", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteEndpointGroup(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        groupService.deleteEndpointGroup(endpointGroupId);
    }

    /**
     * Gets the profile filter records by endpoint group id.
     *
     * @param endpointGroupId   the endpoint group id
     * @param includeDeprecated the include deprecated
     * @return the list profile filter record dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get profile filter records",
            notes = "Returns all profile filter records for the specified endpoint group. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId/includeDeprecated supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "profileFilterRecords", method = RequestMethod.GET)
    @ResponseBody
    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @ApiParam(name = "includeDeprecated", value = "[“true” or ”false”] if “true”, all profile filter records will be returned, including deprecated " +
                    "ones. If “false”, only active and inactive profile filter records will be returned", required = true)
            @RequestParam(value = "includeDeprecated") boolean includeDeprecated) throws KaaAdminServiceException {
        return groupService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated);
    }

    /**
     * Gets the profile filter record by schema id and endpoint group id.
     *
     * @param endpointProfileSchemaId the endpoint profile schema id
     * @param serverProfileSchemaId   the server profile schema id
     * @param endpointGroupId         the endpoint group id
     * @return the profile filter record dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get profile filter record",
            notes = "Returns the profile filter record for the specified endpoint group and profile schema. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to request this information. You need to provide either endpointProfileSchemaId or serverProfileSchemaId " +
                    "parameter or use both of them with endpointGroupId parameter.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or endpoint group does not have the " +
                    "specified profile filter record"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "profileFilterRecord", method = RequestMethod.GET)
    @ResponseBody
    public ProfileFilterRecordDto getProfileFilterRecord(
            @ApiParam(name = "endpointProfileSchemaId", value = "The client-side endpoint profile schema ID", required = false)
            @RequestParam(value = "endpointProfileSchemaId", required = false) String endpointProfileSchemaId,
            @ApiParam(name = "serverProfileSchemaId", value = "The server-side endpoint profile schema ID", required = false)
            @RequestParam(value = "serverProfileSchemaId", required = false) String serverProfileSchemaId,
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return groupService.getProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId);
    }

    /**
     * Gets the vacant profile schemas by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get vacant profile schemas",
            notes = "Returns all vacant (not being used by endpoint group profile filters) profile schemas for the specified endpoint group. Only users " +
                    "with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "vacantProfileSchemas/{endpointGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @PathVariable String endpointGroupId)
            throws KaaAdminServiceException {
        return groupService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Edits profile filter to the list of all profile filters.
     *
     * @param profileFilter the profile filter
     * @return the profile filter dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/edit profile filter",
            notes = "Creates or updates a profile filter. To create profile filter you do not need to specify the profile filter ID, createUsername field " +
                    "will be set to the name of the user who has created it. To edit the profile filter specify the profile filer ID, if a profile filter " +
                    "with the specified ID exists, the profile filter will be updated and its modifyUsername will be set to the user who has submitted the " +
                    "request. Only users with the  TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or existing profile filter with the " +
                    "specified profileFilterId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "profileFilter", method = RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto editProfileFilter(
            @ApiParam(name = "profileFilter", value = "ProfileFilterDto body. Mandatory fields: applicationId, body, either endpointProfileSchemaId or " +
                    "serverProfileSchemaId or both of them", required = true)
            @RequestBody ProfileFilterDto profileFilter) throws KaaAdminServiceException {
        return groupService.editProfileFilter(profileFilter);
    }

    /**
     * Activate profile filter by his id.
     *
     * @param profileFilterId the profile filter id
     * @return the profile filter dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Activate profile filter",
            notes = "Activates a profile filter. Sets the status field of the profile filter to ACTIVE, increments the sequenceNumber field value by 1, " +
                    "and sets the activateUsername and activatedTime fields to the name of the user who submitted the request and the activation time " +
                    "respectively. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Profile filter with the specified profileFilterId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "activateProfileFilter", method = RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto activateProfileFilter(
            @ApiParam(name = "profileFilterId", value = "A unique profile filter identifier", required = true)
            @RequestBody String profileFilterId) throws KaaAdminServiceException {
        return groupService.activateProfileFilter(profileFilterId);
    }

    /**
     * Deactivate profile filter by his id.
     *
     * @param profileFilterId the profile filter id
     * @return the profile filter dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Deactivate profile filter",
            notes = "Deactivates a profile filter. Sets the status field of the profile filter to DEPRECATED, sets the deactivatedTime field value to the " +
                    "time of the deactivation request, and sets the deactivateUsername field to the name of the user who has submitted the request. " +
                    "Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Profile filter with the specified profileFilterId not found"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "deactivateProfileFilter", method = RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto deactivateProfileFilter(
            @ApiParam(name = "profileFilterId", value = "A unique profile filter identifier", required = true)
            @RequestBody String profileFilterId) throws KaaAdminServiceException {
        return groupService.deactivateProfileFilter(profileFilterId);
    }

    /**
     * Delete profile filter record by schema ids and endpoin group id.
     *
     * @param endpointProfileSchemaId the endpoint profile schema id
     * @param serverProfileSchemaId   the server profile schema id
     * @param endpointGroupId         the endpoint group id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete profile filter record",
            notes = "Deletes a profile filter record based on client-side endpoint profile schema ID, server-side endpoint profile schema ID and " +
                    "endpoint group ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation." +
                    "You need to provide either endpointProfileSchemaId or serverProfileSchemaId parameter or use both of them with endpointGroupId parameter.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId/endpointProfileSchemaId/serverProfileSchemaId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist or the endpoint group does not have " +
                    "the specified profile filter record"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delProfileFilterRecord", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteProfileFilterRecord(
            @ApiParam(name = "endpointProfileSchemaId", value = "The client-side endpoint profile schema ID")
            @RequestParam(value = "endpointProfileSchemaId", required = false) String endpointProfileSchemaId,
            @ApiParam(name = "serverProfileSchemaId", value = "The server-side endpoint profile schema ID")
            @RequestParam(value = "serverProfileSchemaId", required = false) String serverProfileSchemaId,
            @ApiParam(name = "endpointGroupId", value = "The ID of the endpoint group.", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        groupService.deleteProfileFilterRecord(endpointProfileSchemaId, serverProfileSchemaId, endpointGroupId);
    }

    /**
     * Gets the endpoint profile by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param limit           the limit
     * @param offset          the offset
     * @param request         the request
     * @return the endpoint profiles page dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get endpoint profiles based on endpoint group id",
            notes = "Returns the endpoint profiles based on endpoint group id. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to request this information. Default limit value equals \"20\", default offset value equals \"0\". Maximum limit value is \"500\".")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId/limit/offset supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointProfileByGroupId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "The id of the endpoint group.", required = true)
            @RequestParam("endpointGroupId") String endpointGroupId,
            @ApiParam(name = "limit", value = "The maximum number of shown profiles. (optional parameter)", defaultValue = DEFAULT_LIMIT, required = false)
            @RequestParam(value = "limit", defaultValue = DEFAULT_LIMIT, required = false) String limit,
            @ApiParam(name = "offset", value = "The offset from beginning of profiles list. (Optional parameter)", defaultValue = DEFAULT_OFFSET,
                    required = false)
            @RequestParam(value = "offset", defaultValue = DEFAULT_OFFSET, required = false) String offset,
            HttpServletRequest request) throws KaaAdminServiceException {
        EndpointProfilesPageDto endpointProfilesPageDto = groupService.getEndpointProfileByEndpointGroupId(endpointGroupId, limit,
                offset);
        if (endpointProfilesPageDto.hasEndpointProfiles()) {
            PageLinkDto pageLinkDto = createNext(endpointProfilesPageDto.getPageLinkDto(), request);
            endpointProfilesPageDto.setNext(pageLinkDto.getNext());
        }
        return endpointProfilesPageDto;
    }

    /**
     * Gets the endpoint profile body by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @param limit           the limit
     * @param offset          the offset
     * @param request         the request
     * @return the endpoint profiles body dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get endpoint profiles bodies based on endpoint group id",
            notes = "Returns the endpoint profiles bodies based on endpoint group id. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to request this information. Default limit value equals \"20\", default offset value equals \"0\". Maximum limit value is \"500\".")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId/limit/offset supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "Endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "endpointProfileBodyByGroupId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "The id of the endpoint group.", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @ApiParam(name = "limit", value = "The maximum number of shown profiles. (Optional parameter)", defaultValue = DEFAULT_LIMIT, required = false)
            @RequestParam(value = "limit", defaultValue = DEFAULT_LIMIT, required = false) String limit,
            @ApiParam(name = "offset", value = "The offset from beginning of profiles list. (Optional parameter)", defaultValue = DEFAULT_OFFSET,
                    required = false)
            @RequestParam(value = "offset", defaultValue = DEFAULT_OFFSET, required = false) String offset,
            HttpServletRequest request)
            throws KaaAdminServiceException {
        EndpointProfilesBodyDto endpointProfilesBodyDto = groupService.getEndpointProfileBodyByEndpointGroupId(endpointGroupId, limit,
                offset);
        if (endpointProfilesBodyDto.hasEndpointBodies()) {
            PageLinkDto pageLinkDto = createNext(endpointProfilesBodyDto.getPageLinkDto(), request);
            endpointProfilesBodyDto.setNext(pageLinkDto.getNext());
        }
        return endpointProfilesBodyDto;
    }

    private PageLinkDto createNext(PageLinkDto pageLink, HttpServletRequest request) {
        if (pageLink != null && pageLink.getNext() == null) {
            StringBuilder nextUrl = new StringBuilder();
            nextUrl.append(request.getScheme()).append("://").append(request.getServerName());
            int port = request.getServerPort();
            if (HTTP_PORT != port && HTTPS_PORT != port) {
                nextUrl.append(":").append(port);
            }
            String next = nextUrl.append(request.getRequestURI()).append("?").append(pageLink.getNextUrlPart()).toString();
            pageLink.setNext(next);
            LOG.debug("Generated next url {}", next);
        }
        return pageLink;
    }
}
