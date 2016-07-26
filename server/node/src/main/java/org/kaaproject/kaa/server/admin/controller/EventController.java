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
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
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

import java.util.List;

@Api(value = "Events", description = "Provides function for manage events", basePath = "/kaaAdmin/rest")
@Controller
public class EventController extends AbstractAdminController {

    /**
     * Gets all event class families.
     *
     * @return the list event class family dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get event class families",
            notes = "Returns all event class families for the current authorized user within the current tenant. Only users with the TENANT_ADMIN role are " +
                    "allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the TENANT_ADMIN role"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "eventClassFamilies", method = RequestMethod.GET)
    @ResponseBody
    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException {
        return eventService.getEventClassFamilies();
    }

    /**
     * Gets the event class family by its id.
     *
     * @param eventClassFamilyId the event class family id
     * @return the event class family dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get event class family",
            notes = "Returns an event class family by event class family ID. Only users with the TENANT_ADMIN role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role TENANT_ADMIN or the Tenant ID of the event class " +
                    "family does not match the Tenant ID of the user"),
            @ApiResponse(code = 404, message = "An event class family with the specified eventClassFamilyId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "eventClassFamily/{eventClassFamilyId}", method = RequestMethod.GET)
    @ResponseBody
    public EventClassFamilyDto getEventClassFamily(
            @ApiParam(name = "eventClassFamilyId", value = "A unique event class family identifier", required = true)
            @PathVariable String eventClassFamilyId) throws KaaAdminServiceException {
        return eventService.getEventClassFamily(eventClassFamilyId);
    }

    /**
     * Edits event class family to the list of all event class families.
     *
     * @param eventClassFamily the event class family
     * @return the event class family dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit event class family",
            notes = "Creates or edits an event class family. To create event class family you do not need to specify the event class family ID. To edit the " +
                    "event class family specify the event class family ID. If an event class family with the specified ID exists, it will be updated. Only " +
                    "users with the TENANT_ADMIN role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN) or the Tenant ID of the editing event " +
                    "class family does not match the Tenant ID of the user"),
            @ApiResponse(code = 404, message = "An event class family to be edited with the specified eventClassFamilyId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "eventClassFamily", method = RequestMethod.POST)
    @ResponseBody
    public EventClassFamilyDto editEventClassFamily(
            @ApiParam(name = "eventClassFamily", value = "EventClassFamilyDto body. Mandatory fields: name, className, namespace", required = true)
            @RequestBody EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException {
        return eventService.editEventClassFamily(eventClassFamily);
    }

    /**
     * Adds the event class family schema to the event class family with
     * specific id. Current user will be marked as creator of schema.
     *
     * @param eventClassFamilyId the event class family id
     * @param file               the file
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Add event class family schema",
            notes = "Adds the event class family schema to the event class family with the specified ID. The current user will be marked as the creator of " +
                    "schema. Only users with the TENANT_ADMIN role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified event class family schema is not a valid avro schema"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN) or the Tenant ID of the specified " +
                    "event class family does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A file with the event class family schema was not found in the form data or an event class family with the " +
                    "specified ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "addEventClassFamilySchema", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    @ResponseStatus(value = HttpStatus.OK)
    public void addEventClassFamilySchema(
            @ApiParam(name = "eventClassFamilyId", value = "A unique event class family identifier", required = true)
            @RequestPart(value = "eventClassFamilyId") String eventClassFamilyId,
            @ApiParam(name = "file", value = "Event class family schema represented in json format", required = true)
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        eventService.addEventClassFamilySchema(eventClassFamilyId, data);
    }

    /**
     * Gets the event classes by family its id, version and type.
     *
     * @param eventClassFamilyId the event class family id
     * @param version            the version
     * @param type               the type
     * @return the list event class dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get event classes",
            notes = "Gets event classes by event class family ID, version and type. Only users with the TENANT_ADMIN, TENANT_DEVELOPER or TENANT_USER role " +
                    "are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "One of the url parameters is empty (eventClassFamilyId, version or type)"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN, TENANT_DEVELOPER or TENANT_USER) or " +
                    "the Tenant ID of the event class family does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An event class family with the specified eventClassFamilyId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "eventClasses", method = RequestMethod.GET)
    @ResponseBody
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(
            @ApiParam(name = "eventClassFamilyId", value = "A unique event class family identifier", required = true)
            @RequestParam(value = "eventClassFamilyId") String eventClassFamilyId,
            @ApiParam(name = "version", value = "The version of the event class family schema", required = true)
            @RequestParam(value = "version") int version,
            @ApiParam(name = "type", value = "The event classes type, one of [\"EVENT\", \"OBJECT\"]", required = true)
            @RequestParam(value = "type") EventClassType type) throws KaaAdminServiceException {
        return eventService.getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, type);
    }

    /**
     * Gets all application event family maps by application token.
     *
     * @param applicationToken the application token
     * @return list the application event family map dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get application event family maps",
            notes = "Returns all application event family maps for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "applicationEventMaps/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken)
            throws KaaAdminServiceException {
        return eventService.getApplicationEventFamilyMapsByApplicationToken(applicationToken);
    }

    /**
     * Gets the application event family map by its id.
     *
     * @param applicationEventFamilyMapId the application event family map
     * @return the application event family map dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get application event family map",
            notes = "Returns an application event family map by application event family map ID. Only users with the TENANT_DEVELOPER or TENANT_USER role " +
                    "are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application event family map with the specified applicationEventFamilyMapId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "applicationEventMap/{applicationEventFamilyMapId}", method = RequestMethod.GET)
    @ResponseBody
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(
            @ApiParam(name = "applicationEventFamilyMapId", value = "A unique application event family map identifier", required = true)
            @PathVariable String applicationEventFamilyMapId)
            throws KaaAdminServiceException {
        return eventService.getApplicationEventFamilyMap(applicationEventFamilyMapId);
    }

    /**
     * Edits application event family map to the list of all event family maps.
     *
     * @param applicationEventFamilyMap the application event family map
     * @return the application event family map dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit application event family map",
            notes = "Creates or edits an application event family map. If an application event family map with the specified ID does not exist, it will be " +
                    "created. If an application event family map with the specified ID exists, it will be updated. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application event family map to be edited with the specified applicationEventFamilyMapId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "applicationEventMap", method = RequestMethod.POST)
    @ResponseBody
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(
            @ApiParam(name = "applicationEventFamilyMap", value = "ApplicationEventFamilyMapDto body. Mandatory fields: applicationId, ecfId, ecfName, " +
                    "eventMaps", required = true)
            @RequestBody ApplicationEventFamilyMapDto applicationEventFamilyMap)
            throws KaaAdminServiceException {
        return eventService.editApplicationEventFamilyMap(applicationEventFamilyMap);
    }

    /**
     * Gets all vacant event class families by application token.
     *
     * @param applicationToken the application token
     * @return the list ecf info dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get vacant event class families",
            notes = "Returns all vacant (not being used by application event family maps) event class families for the specified application. Only users " +
                    "with the  TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "vacantEventClassFamilies/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return eventService.getVacantEventClassFamiliesByApplicationToken(applicationToken);
    }

    /**
     * Gets all event class families by application token.
     *
     * @param applicationToken the application token
     * @return the list aef map info dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get application event class families",
            notes = "Returns all event class families for the specified application (being used by application event family maps). Only users with the " +
                    "TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "eventClassFamilies/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return eventService.getEventClassFamiliesByApplicationToken(applicationToken);
    }

}
