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
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
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

@Api(value = "Notifications", description = "Provides function for manage notifications", basePath = "/kaaAdmin/rest")
@Controller
public class NotificationController extends AbstractAdminController {

    /**
     * Gets the notification schemas by application token.
     *
     * @param applicationToken the application token
     * @return the list notification schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get notification schemas",
            notes = "Returns notification schemas for an application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this " +
                    "information. The Tenant ID value of the application must match the Tenant ID of the request submitter.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "notificationSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken)
            throws KaaAdminServiceException {
        return notificationService.getNotificationSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the user notification schemas by application token.
     *
     * @param applicationToken the application token
     * @return the list schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get user notification schemas",
            notes = "Returns user notification schemas for an application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request " +
                    "this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userNotificationSchemas/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<VersionDto> getUserNotificationSchemasByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return notificationService.getUserNotificationSchemasByApplicationToken(applicationToken);
    }

    /**
     * Gets the notification schema by her id.
     *
     * @param notificationSchemaId the notification schema id
     * @return the notification schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get notification schema",
            notes = "Returns a notification schema by notification schema ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to " +
                    "request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A notification schema with the specified notificationSchemaId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "notificationSchema/{notificationSchemaId}", method = RequestMethod.GET)
    @ResponseBody
    public NotificationSchemaDto getNotificationSchema(
            @ApiParam(name = "notificationSchemaId", value = "A unique notification schema identifier", required = true)
            @PathVariable String notificationSchemaId) throws KaaAdminServiceException {
        return notificationService.getNotificationSchema(notificationSchemaId);
    }

    /**
     * Adds notification schema to the list of all notification schemas.
     *
     * @param notificationSchema the notification schema
     * @return the notification schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "createNotificationSchema", method = RequestMethod.POST)
    @ResponseBody
    public NotificationSchemaDto createNotificationSchema(@RequestBody NotificationSchemaDto notificationSchema) throws KaaAdminServiceException {
        return notificationService.saveNotificationSchema(notificationSchema);
    }

    /**
     * Edits existing notification schema.
     *
     * @param notificationSchema the notification schema
     * @return the notification schema dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit notification schema",
            notes = "Creates or updates a notification schema. To create notification schema you do not need to specify the notification schema ID, " +
                    "createUsername field of the schema will be set to the name of the user who has uploaded it, a unique version number will be generated " +
                    "(incrementally) for this schema. To edit the notification schema specify the notification schema ID. If a notification schema with the " +
                    "specified ID exists, the configuration will be updated. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform " +
                    "this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified notification schema is not a valid avro schema"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "saveNotificationSchema", method = RequestMethod.POST)
    @ResponseBody
    public NotificationSchemaDto saveNotificationSchema(
            @ApiParam(name = "notificationSchema", value = "NotificationSchemaDto body.", required = true)
            @RequestBody NotificationSchemaDto notificationSchema)
            throws KaaAdminServiceException {
        return notificationService.saveNotificationSchema(notificationSchema);
    }

    /**
     * Gets all topics by application token.
     *
     * @param applicationToken the application token
     * @return the topic dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get topics",
            notes = "Returns all topics for the specified application. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this " +
                    "information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An application with the specified applicationToken does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "topics/{applicationToken}", method = RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getTopicsByApplicationToken(
            @ApiParam(name = "applicationToken", value = "A unique auto-generated application identifier", required = true)
            @PathVariable String applicationToken) throws KaaAdminServiceException {
        return notificationService.getTopicsByApplicationToken(applicationToken);
    }

    /**
     * Gets all topics by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the topic dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get endpoint group topics",
            notes = "Returns all topics for the specified endpoint group. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request " +
                    "this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group to with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "topics", method = RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getTopicsByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId)
            throws KaaAdminServiceException {
        return notificationService.getTopicsByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets all vacant topics by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the topic dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get vacant topics",
            notes = "Returns all vacant (not present in the endpoint group) topics for the specified endpoint group. Only users with the TENANT_DEVELOPER or " +
                    "TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "An endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "vacantTopics/{endpointGroupId}", method = RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getVacantTopicsByEndpointGroupId(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return notificationService.getVacantTopicsByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets the topic by his id.
     *
     * @param topicId the topic id
     * @return the topic dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get topic",
            notes = "Returns a topic by topic ID. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to request this information.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid topicId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The topic with the specified topicId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "topic/{topicId}", method = RequestMethod.GET)
    @ResponseBody
    public TopicDto getTopic(
            @ApiParam(name = "topicId", value = "A unique topic identifier", required = true)
            @PathVariable String topicId) throws KaaAdminServiceException {
        return notificationService.getTopic(topicId);
    }

    /**
     * Edits topic to the list of all topics.
     *
     * @param topic the topic
     * @return the topic dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit topic",
            notes = "Creates or edits a topic. To create topic you do not need to specify the topic ID. To edit the topic specify the topic ID. If a topic " +
                    "with the specified ID exists, it will be updated. Only users with theTENANT_DEVELOPER or TENANT_USER role are allowed to perform " +
                    "this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A topic to be edited with the specified topicId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "topic", method = RequestMethod.POST)
    @ResponseBody
    public TopicDto editTopic(
            @ApiParam(name = "topic", value = "TopicDto body. Mandatory fields: applicationId, name, type", required = true)
            @RequestBody TopicDto topic) throws KaaAdminServiceException {
        return notificationService.editTopic(topic);
    }

    /**
     * Delete topic by his id.
     *
     * @param topicId the topic id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete topic",
            notes = "Deletes a topic. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid topicId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The topic with the specified topicId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delTopic", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTopic(
            @ApiParam(name = "topicId", value = "A unique topic identifier", required = true)
            @RequestParam(value = "topicId") String topicId) throws KaaAdminServiceException {
        notificationService.deleteTopic(topicId);
    }

    /**
     * Adds the topic with specific id to endpoint group with specific id.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId         the topic id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Add topic to endpoint group",
            notes = "Adds the specified topic to the specified endpoint group. Only users with the TENANT_DEVELOPER or TENANT_USER role are allowed to " +
                    "perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A topic with the specified topicId or an endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "addTopicToEpGroup", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void addTopicToEndpointGroup(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @ApiParam(name = "topicId", value = "A unique topic identifier", required = true)
            @RequestParam(value = "topicId") String topicId) throws KaaAdminServiceException {
        notificationService.addTopicToEndpointGroup(endpointGroupId, topicId);
    }

    /**
     * Removes the topic with specific id to endpoint group with specific id.
     *
     * @param endpointGroupId the endpoint group id
     * @param topicId         the topic id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Remove topic from endpoint group",
            notes = "Removes the specified topic from the specified endpoint group. Only users with the  TENANT_DEVELOPER or TENANT_USER role are allowed " +
                    "to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid endpointGroupId supplied"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A topic with the specified topicId or an endpoint group with the specified endpointGroupId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "removeTopicFromEpGroup", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void removeTopicFromEndpointGroup(
            @ApiParam(name = "endpointGroupId", value = "A unique endpoint group identifier", required = true)
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @ApiParam(name = "topicId", value = "A unique topic identifier", required = true)
            @RequestParam(value = "topicId") String topicId) throws KaaAdminServiceException {
        notificationService.removeTopicFromEndpointGroup(endpointGroupId, topicId);
    }

    /**
     * Send notification, with information from specific file, to the client.
     *
     * @param notification the notification
     * @param file         the file
     * @return the notification dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Send notification",
            notes = "Sends a notification with the notification body from the specified file. Only users with the TENANT_DEVELOPER or TENANT_USER role are " +
                    "allowed to perform this operation. If you want to set notification time to leave, you must set expiredAt field in the parameter " +
                    "notification. If your notification schema contains one field \"message\" with Union type, notification body from the specified file " +
                    "looks like below: " +
                    "```{" +
                    "  \"message\" : {" +
                    "    \"string\" : \"Hello world!\"" +
                    "  }" +
                    "}```. " +
                    "And for primitive string type of the field \"message\" notification body from the specified file looks like below: " +
                    "```{" +
                    "  \"message\" : \"Hello world!\"" +
                    "}```")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified notification is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A file with the notification body was not found in the form data or an application with the specified ID " +
                    "does not exist or a topic with the specified ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "sendNotification", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    @ResponseBody
    public NotificationDto sendNotification(
            @ApiParam(name = "notification", value = "NotificationDto body. Mandatory fields: applicationId, schemaId, topicId, type", required = true)
            @RequestPart("notification") NotificationDto notification,
            @ApiParam(name = "file", value = "A file with notification body according to the specified notification schema represented in json format",
                    required = true)
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return notificationService.sendNotification(notification, data);
    }

    /**
     * Send unicast notification, with information from specific file, to the
     * client identified by endpointKeyHash.
     *
     * @param notification  the notification
     * @param clientKeyHash the client key hash
     * @param file          the file
     * @return the endpoint notification dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Send unicast notification",
            notes = "Sends a unicast notification with the notification body from the specified file to the client identified by endpointKeyHash. Only users " +
                    "with the TENANT_DEVELOPER or TENANT_USER role are allowed to perform this operation. If you want to set notification time to leave, " +
                    "you must set expiredAt field in the parameter notification. If your notification schema contains one field \"message\" with Union type, " +
                    "notification body from the specified file looks like below: " +
                    "```{" +
                    "  \"message\" : {" +
                    "    \"string\" : \"Hello world!\"" +
                    "  }" +
                    "}```. " +
                    "And for primitive string type of the field \"message\" notification body from the specified file looks like below: " +
                    "```{" +
                    "  \"message\" : \"Hello world!\"" +
                    "}```")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified notification is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_DEVELOPER or TENANT_USER) or the Tenant ID " +
                    "of the application does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "A file with the notification body was not found in the form data or an application with the specified ID does " +
                    "not exist or a topic with the specified ID does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "sendUnicastNotification", method = RequestMethod.POST, consumes = {"multipart/mixed", "multipart/form-data"})
    @ResponseBody
    public EndpointNotificationDto sendUnicastNotification(
            @ApiParam(name = "notification", value = "NotificationDto body. Mandatory fields: applicationId, schemaId, topicId, type", required = true)
            @RequestPart("notification") NotificationDto notification,
            @ApiParam(name = "endpointKeyHash", value = "The key hash of the endpoint in Base64 URL safe format", required = true)
            @RequestPart("endpointKeyHash") String clientKeyHash,
            @ApiParam(name = "file", value = "A file with notification body according to the specified notification schema represented in json format",
                    required = true)
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return notificationService.sendUnicastNotification(notification, clientKeyHash, data);
    }

}
