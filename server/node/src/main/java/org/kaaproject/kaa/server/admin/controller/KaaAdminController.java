/*
 * Copyright 2014 CyberVision, Inc.
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

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.TenantUserDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.event.AefMapInfoDto;
import org.kaaproject.kaa.common.dto.event.ApplicationEventFamilyMapDto;
import org.kaaproject.kaa.common.dto.event.EcfInfoDto;
import org.kaaproject.kaa.common.dto.event.EventClassDto;
import org.kaaproject.kaa.common.dto.event.EventClassFamilyDto;
import org.kaaproject.kaa.common.dto.event.EventClassType;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.plugin.PluginContractDto;
import org.kaaproject.kaa.common.dto.plugin.PluginDto;
import org.kaaproject.kaa.common.dto.plugin.PluginInstanceDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.dao.UserFacade;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.servlet.ServletUtils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.KaaAuthService;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring4gwt.server.SpringGwtRemoteServiceServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The Class KaaAdminController.
 */
@Controller
@RequestMapping("api")
public class KaaAdminController {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(KaaAdminController.class);

    /** The Constant BUFFER. */
    private static final int BUFFER = 1024 * 100;

    /** The Constant DEFAULT_LIMIT. */
    private static final String DEFAULT_LIMIT = "20";

    /** The Constant DEFAULT_OFFSET. */
    private static final String DEFAULT_OFFSET = "0";

    /** The Constant HTTPS_PORT. */
    public static final int HTTPS_PORT = 443;

    /** The Constant HTTP_PORT. */
    public static final int HTTP_PORT = 80;

    /** The kaa admin service. */
    @Autowired
    KaaAdminService kaaAdminService;

    /** The kaa auth service. */
    @Autowired
    KaaAuthService kaaAuthService;

    /** The user facade. */
    @Autowired
    UserFacade userFacade;

    /** The password encoder. */
    @Autowired @Qualifier("encoder")
    PasswordEncoder passwordEncoder;

    /** The cache service. */
    @Autowired
    private CacheService cacheService;

    @ExceptionHandler(KaaAdminServiceException.class)
    public void handleKaaAdminServiceException(KaaAdminServiceException ex, HttpServletResponse response) {
        try {
        ServiceErrorCode errorCode = ex.getErrorCode();
        switch (errorCode) {
        case NOT_AUTHORIZED:
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            break;
        case PERMISSION_DENIED:
            response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
            break;
        case INVALID_ARGUMENTS:
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            break;
        case INVALID_SCHEMA:
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            break;
        case FILE_NOT_FOUND:
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
            break;
        case ITEM_NOT_FOUND:
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
            break;
        case BAD_REQUEST_PARAMS:
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            break;
        case GENERAL_ERROR:
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            break;
        }
        } catch (IOException e) {
            logger.error("Can't handle exception", e);
        }
    }

    /**
     * Gets the endpoint profile by endpoint group id.
     *
     * @param   endpointGroupId   the endpoint group id
     * @param   limit             the limit
     * @param   offset            the offset
     * @param   request           the request
     * @return  the endpoint profiles page dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "endpointProfileByGroupId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @RequestParam(value = "limit", defaultValue = DEFAULT_LIMIT) String limit,
            @RequestParam(value = "offset", defaultValue = DEFAULT_OFFSET) String offset,
            HttpServletRequest request) throws KaaAdminServiceException {
        EndpointProfilesPageDto endpointProfilesPageDto = kaaAdminService.getEndpointProfileByEndpointGroupId(endpointGroupId, limit, offset);
        if (endpointProfilesPageDto.hasEndpointProfiles()) {
            PageLinkDto pageLinkDto = createNext(endpointProfilesPageDto.getPageLinkDto(), request);
            endpointProfilesPageDto.setNext(pageLinkDto.getNext());
        }
        return endpointProfilesPageDto;
    }

    /**
     * Gets the endpoint profile body by endpoint group id.
     *
     * @param   endpointGroupId   the endpoint group id
     * @param   limit             the limit
     * @param   offset            the offset
     * @param   request           the request
     * @return  the endpoint profiles body dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "endpointProfileBodyByGroupId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(
            @RequestParam(value = "endpointGroupId") String endpointGroupId,
            @RequestParam(value = "limit", defaultValue = DEFAULT_LIMIT) String limit,
            @RequestParam(value = "offset", defaultValue = DEFAULT_OFFSET) String offset,
            HttpServletRequest request) throws KaaAdminServiceException {
        EndpointProfilesBodyDto endpointProfilesBodyDto = kaaAdminService.getEndpointProfileBodyByEndpointGroupId(endpointGroupId, limit, offset);
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
            logger.debug("Generated next url {}", next);
        }
        return pageLink;
    }

    /**
     * Gets the endpoint profile by endpoint key.
     *
     * @param   endpointProfileKey the endpoint profile key
     * @return  the endpoint profile dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="endpointProfile/{endpointProfileKey}", method=RequestMethod.GET)
    @ResponseBody
    public EndpointProfileDto getEndpointProfileByKeyHash(@PathVariable String endpointProfileKey) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointProfileByKeyHash(endpointProfileKey);
    }

    /**
     * Gets the endpoint profile body by endpoint key.
     *
     * @param   endpointProfileKey the endpoint profile key
     * @return  the endpoint profile body dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="endpointProfileBody/{endpointProfileKey}", method=RequestMethod.GET)
    @ResponseBody
    public EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(@PathVariable String endpointProfileKey) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointProfileBodyByKeyHash(endpointProfileKey);
    }

    /**
     * Check auth of kaa admin.
     *
     * @param   request the request
     * @return  the auth result dto
     * @throws  Exception the exception
     */
    @RequestMapping(value="auth/checkAuth", method=RequestMethod.GET)
    @ResponseBody
    public AuthResultDto checkAuth(HttpServletRequest request) throws Exception {
        SpringGwtRemoteServiceServlet.setRequest(request);
        try {
            return kaaAuthService.checkAuth();
        } finally {
            SpringGwtRemoteServiceServlet.setRequest(null);
        }
    }

    /**
     * Creates the kaa admin with specific name and password.
     *
     * @param   username    the username
     * @param   password    the password
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="auth/createKaaAdmin", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void createKaaAdmin(@RequestParam(value="username") String username,
                               @RequestParam(value="password") String password) throws KaaAdminServiceException {
        if (!userFacade.isAuthorityExists(KaaAuthorityDto.KAA_ADMIN.name())) {
            kaaAuthService.createKaaAdmin(username, password);
        } else {
            throw new KaaAdminServiceException("Kaa admin already exists. Can't create more than one kaa admin users.", ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    /**
     * Change password of user with specific name if old password valid.
     *
     * @param   username      the username
     * @param   oldPassword   the old password
     * @param   newPassword   the new password
     * @return  the result code
     * @throws  Exception the exception
     */
    @RequestMapping(value="auth/changePassword", method=RequestMethod.POST)
    @ResponseBody
    public ResultCode changePassword(
            @RequestParam(value="username") String username,
            @RequestParam(value="oldPassword") String oldPassword,
            @RequestParam(value="newPassword") String newPassword) throws Exception {
        ResultCode resultCode = kaaAuthService.changePassword(username, oldPassword, newPassword);
        if (resultCode == ResultCode.USER_NOT_FOUND) {
            throw Utils.handleException(new IllegalArgumentException("User with specified username was not found."));
        } else if (resultCode == ResultCode.OLD_PASSWORD_MISMATCH) {
            throw Utils.handleException(new IllegalArgumentException("Current password is invalid."));
        } else if (resultCode == ResultCode.BAD_PASSWORD_STRENGTH) {
            throw Utils.handleException(new IllegalArgumentException("Password strength is insufficient."));
        }
        return resultCode;
    }

    /**
     * Gets all tenants.
     *
     * @return the list of tenant user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="tenants", method=RequestMethod.GET)
    @ResponseBody
    public List<TenantUserDto> getTenants() throws KaaAdminServiceException {
        return kaaAdminService.getTenants();
    }

    /**
     * Gets the tenant by user id.
     *
     * @param   userId the user id
     * @return  the tenant user dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="tenant/{userId}", method=RequestMethod.GET)
    @ResponseBody
    public TenantUserDto getTenant(
            @PathVariable("userId") String userId) throws KaaAdminServiceException {
        return kaaAdminService.getTenant(userId);
    }

    /**
     * Edits tenant to the list of all tenants.
     *
     * @param   tenantUser the tenant user
     * @return  the tenant user dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="tenant", method=RequestMethod.POST)
    @ResponseBody
    public TenantUserDto editTenant(@RequestBody TenantUserDto tenantUser) throws KaaAdminServiceException {
        try {
            CreateUserResult result = userFacade.saveUserDto(tenantUser, passwordEncoder);
            tenantUser.setExternalUid(result.getUserId().toString());
            TenantUserDto tenant =  kaaAdminService.editTenant(tenantUser);
            if (StringUtils.isNotBlank(result.getPassword())) {
                tenant.setTempPassword(result.getPassword());
            }
            return tenant;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    /**
     * Delete tenant by user id.
     *
     * @param   userId the user id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delTenant", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@RequestParam(value="userId") String userId) throws KaaAdminServiceException {
        kaaAdminService.deleteTenant(userId);
    }

    /**
     * Gets all applications.
     *
     * @return the list application dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="applications", method=RequestMethod.GET)
    @ResponseBody
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        return kaaAdminService.getApplications();
    }

    /**
     * Gets the application by its id.
     *
     * @param   applicationId the application id
     * @return  the application dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="application/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public ApplicationDto getApplication(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getApplication(applicationId);
    }

    /**
     * Gets the application by its id.
     *
     * @param   applicationToken the application token
     * @return  the application dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="application/token/{applicationToken}", method=RequestMethod.GET)
    @ResponseBody
    public ApplicationDto getApplicationByApplicationToken(@PathVariable String applicationToken) throws KaaAdminServiceException {
        return kaaAdminService.getApplicationByApplicationToken(applicationToken);
    }

    /**
     * Edits application to the list of all applications.
     *
     * @param   application the application
     * @return  the application dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="application", method=RequestMethod.POST)
    @ResponseBody
    public ApplicationDto editApplication(@RequestBody ApplicationDto application) throws KaaAdminServiceException {
        return kaaAdminService.editApplication(application);
    }
    
    /**
     * Delete application by application id.
     *
     * @param   applicationId the application id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delApplication", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteApplication(@RequestParam(value="applicationId") String applicationId) throws KaaAdminServiceException {
        kaaAdminService.deleteApplication(applicationId);
    }

    /**
     * Gets the user profile of current user.
     *
     * @return the user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userProfile", method=RequestMethod.GET)
    @ResponseBody
    public UserDto getUserProfile() throws KaaAdminServiceException {
        return kaaAdminService.getUserProfile();
    }

    /**
     * Edits user profile to all user profiles.
     *
     * @param   userDto the user dto
     * @return  the user dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userProfile", method=RequestMethod.POST)
    @ResponseBody
    public UserDto editUserProfile(@RequestBody UserDto userDto) throws KaaAdminServiceException {
        return kaaAdminService.editUserProfile(userDto);
    }

    /**
     * Gets all users.
     *
     * @return the list user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="users", method=RequestMethod.GET)
    @ResponseBody
    public List<UserDto> getUsers() throws KaaAdminServiceException {
        return kaaAdminService.getUsers();
    }

    /**
     * Gets the user by his id.
     *
     * @param   userId the user id
     * @return  the user dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="user/{userId}", method=RequestMethod.GET)
    @ResponseBody
    public UserDto getUser(@PathVariable String userId) throws KaaAdminServiceException {
        return kaaAdminService.getUser(userId);
    }

    /**
     * Edits user to the list of all users.
     *
     * @param   user the user
     * @return  the user dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="user", method=RequestMethod.POST)
    @ResponseBody
    public UserDto editUser(@RequestBody UserDto user) throws KaaAdminServiceException {
        try {
            CreateUserResult result = userFacade.saveUserDto(user, passwordEncoder);
            user.setExternalUid(result.getUserId().toString());
            UserDto userDto =  kaaAdminService.editUser(user);
            if (StringUtils.isNotBlank(result.getPassword())) {
                userDto.setTempPassword(result.getPassword());
            }
            return userDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }
    
    /**
     * Delete user by user id.
     *
     * @param   userId the user id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delUser", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUser(@RequestParam(value="userId") String userId) throws KaaAdminServiceException {
        kaaAdminService.deleteUser(userId);
    }

    /**
     * Gets the schema versions by application id.
     *
     * @param   applicationId the application id
     * @return  the schema versions
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="schemaVersions/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public SchemaVersions getSchemaVersionsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getSchemaVersionsByApplicationId(applicationId);
    }

    /**
     * Generates an SDK for the specified target platform from an SDK profile .
     *
     * @param   sdkProfileId    the sdk profile id
     * @param   targetPlatform  the target platform
     * @param   request         the request
     * @param   response        the response
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="sdk", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSdk(@RequestParam(value = "sdkProfileId") String sdkProfileId,
            @RequestParam(value = "targetPlatform") String targetPlatform,
            HttpServletRequest request,
            HttpServletResponse response) throws KaaAdminServiceException {
        try {
            SdkProfileDto sdkProfile = kaaAdminService.getSdkProfile(sdkProfileId);
            FileData sdkData = kaaAdminService.getSdk(sdkProfile, SdkPlatform.valueOf(targetPlatform.toUpperCase()));
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
     * Stores a new SDK profile into the database.
     *
     * @param   sdkProfile the sdk profile
     * @return  the sdk profile dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="addSdkProfile", method=RequestMethod.POST)
    @ResponseBody
    public SdkProfileDto addSdkProfile(@RequestBody SdkProfileDto sdkProfile) throws KaaAdminServiceException {
        return kaaAdminService.addSdkProfile(sdkProfile);
    }

    /**
     * Deletes an SDK profile by its identifier.
     *
     * @param   sdkProfileId the sdk profile id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="deleteSdkProfile", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteSdkProfile(@RequestParam(value = "sdkProfileId") String sdkProfileId) throws KaaAdminServiceException {
        kaaAdminService.deleteSdkProfile(sdkProfileId);
    }

    /**
     * Returns an SDK profile by its identifier.
     *
     * @param   sdkProfileId the sdk profile id
     * @return  the sdk profile dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "sdkProfile/{sdkProfileId}")
    @ResponseBody
    public SdkProfileDto getSdkProfile(@PathVariable String sdkProfileId) throws KaaAdminServiceException {
        return kaaAdminService.getSdkProfile(sdkProfileId);
    }

    /**
     * Returns a list of SDK profiles for the given application.
     *
     * @param   applicationId the application id
     * @return  the list sdk profile dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="sdkProfiles/{applicationId}")
    @ResponseBody
    public List<SdkProfileDto> getSdkProfilesByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getSdkProfilesByApplicationId(applicationId);
    }

    /**
     * Flushes all cached Sdks within tenant.
     *
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="flushSdkCache", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void flushSdkCache() throws KaaAdminServiceException {
        try {
            kaaAdminService.flushSdkCache();
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    /**
     * Gets the profile schemas by application id.
     *
     * @param   applicationId the application id
     * @return  the list profile schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="profileSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<ProfileSchemaDto> getProfileSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getProfileSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the profile schema by her id.
     *
     * @param   profileSchemaId the profile schema id
     * @return  the profile schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="profileSchema/{profileSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public ProfileSchemaDto getProfileSchema(@PathVariable String profileSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getProfileSchema(profileSchemaId);
    }

    /**
     * Adds profile schema to the list of all profile schemas.
     *
     * @param   profileSchema   the profile schema
     * @param   file            the file
     * @return  the profile schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="createProfileSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public ProfileSchemaDto createProfileSchema(@RequestPart("profileSchema") ProfileSchemaDto profileSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editProfileSchema(profileSchema, data);
    }
    
    /**
     * Edits existing profile schema.
     *
     * @param   profileSchema the profile schema
     * @return  the profile schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="editProfileSchema", method=RequestMethod.POST)
    @ResponseBody
    public ProfileSchemaDto editProfileSchema(@RequestBody ProfileSchemaDto profileSchema) throws KaaAdminServiceException {
        return kaaAdminService.editProfileSchema(profileSchema, null);
    }

    /**
     * Gets the configuration schemas by application id.
     *
     * @param   applicationId the application id
     * @return  the сonfiguration schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="configurationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getConfigurationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the configuration schema by her id.
     *
     * @param   configurationSchemaId the сonfiguration schema id
     * @return  the сonfiguration schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="configurationSchema/{configurationSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public ConfigurationSchemaDto getConfigurationSchema(@PathVariable String configurationSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getConfigurationSchema(configurationSchemaId);
    }

    /**
     * Adds configuration schema to the list of all configuration schemas.
     *
     * @param   configurationSchema   the сonfiguration schema
     * @param   file                  the file
     * @return  the сonfiguration schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="createConfigurationSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public ConfigurationSchemaDto createConfigurationSchema(@RequestPart("configurationSchema") ConfigurationSchemaDto configurationSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editConfigurationSchema(configurationSchema, data);
    }

    /**
     * Edits existing configuration schema.
     *
     * @param   configurationSchema the сonfiguration schema
     * @return  the сonfiguration schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="editConfigurationSchema", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationSchemaDto editConfigurationSchema(@RequestBody ConfigurationSchemaDto configurationSchema) throws KaaAdminServiceException {
        return kaaAdminService.editConfigurationSchema(configurationSchema, null);
    }

    /**
     * Gets the notification schemas by application id.
     *
     * @param   applicationId the application id
     * @return  the list notification schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="notificationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getNotificationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the user notification schemas by application id.
     *
     * @param   applicationId the application id
     * @return  the list schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userNotificationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getUserNotificationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getUserNotificationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the notification schema by her id.
     *
     * @param   notificationSchemaId the notification schema id
     * @return  the notification schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="notificationSchema/{notificationSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public NotificationSchemaDto getNotificationSchema(@PathVariable String notificationSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getNotificationSchema(notificationSchemaId);
    }

    /**
     * Adds notification schema to the list of all notification schemas.
     *
     * @param   notificationSchema  the notification schema
     * @param   file                the file
     * @return  the notification schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="createNotificationSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public NotificationSchemaDto createNotificationSchema(@RequestPart("notificationSchema") NotificationSchemaDto notificationSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editNotificationSchema(notificationSchema, data);
    }

    /**
     * Edits existing notification schema.
     *
     * @param   notificationSchema the notification schema
     * @return  the notification schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="editNotificationSchema", method=RequestMethod.POST)
    @ResponseBody
    public NotificationSchemaDto editNotificationSchema(@RequestBody NotificationSchemaDto notificationSchema) throws KaaAdminServiceException {
        return kaaAdminService.editNotificationSchema(notificationSchema, null);
    }

    /**
     * Gets all log schemas by application id.
     *
     * @param   applicationId the application id
     * @return  the list log schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<LogSchemaDto> getLogSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the log schema by its id.
     *
     * @param   logSchemaId the log schema id
     * @return  the log schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logSchema/{logSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchema(@PathVariable String logSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchema(logSchemaId);
    }

    /**
     * Gets the log schema by application token and schema version.
     *
     * @param   applicationToken    the application token
     * @param   schemaVersion       the schema version
     * @return  the log schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logSchema/{applicationToken}/{schemaVersion}", method=RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(@PathVariable String applicationToken,
                                                                 @PathVariable int schemaVersion) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchemaByApplicationTokenAndVersion(applicationToken, schemaVersion);
    }

    /**
     * Adds log schema to the list of all log schemas.
     *
     * @param   logSchema   the log schema
     * @param   file        the file
     * @return  the log schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="createLogSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public LogSchemaDto createLogSchema(@RequestPart("logSchema") LogSchemaDto logSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editLogSchema(logSchema, data);
    }

    /**
     * Edits existing log schema.
     *
     * @param   logSchema the log schema
     * @return  the log schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="editLogSchema", method=RequestMethod.POST)
    @ResponseBody
    public LogSchemaDto editLogSchema(@RequestBody LogSchemaDto logSchema) throws KaaAdminServiceException {
        return kaaAdminService.editLogSchema(logSchema, null);
    }

    /**
     * Gets all log appenders by application id.
     *
     * @param   applicationId the application id
     * @return  the list log appender dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logAppenders/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<LogAppenderDto> getLogAppendersByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getRestLogAppendersByApplicationId(applicationId);
    }

    /**
     * Gets the log appender by its id.
     *
     * @param   logAppenderId the log appender id
     * @return  the log appender dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logAppender/{logAppenderId}", method=RequestMethod.GET)
    @ResponseBody
    public LogAppenderDto getLogAppender(@PathVariable String logAppenderId) throws KaaAdminServiceException {
        return kaaAdminService.getRestLogAppender(logAppenderId);
    }

    /**
     * Edits log appender.
     *
     * @param   logAppender the log appender
     * @return  the log appender dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="logAppender", method=RequestMethod.POST)
    @ResponseBody
    public LogAppenderDto editLogAppender(@RequestBody LogAppenderDto logAppender) throws KaaAdminServiceException {
        return kaaAdminService.editRestLogAppender(logAppender);
    }

    /**
     * Delete log appender by its id.
     *
     * @param   logAppenderId the log appender id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delLogAppender", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteLogAppender(@RequestParam(value="logAppenderId") String logAppenderId) throws KaaAdminServiceException {
        kaaAdminService.deleteLogAppender(logAppenderId);
    }

    /**
     * Gets all user verifiers by application id.
     *
     * @param   applicationId the application id
     * @return  the list user verifier dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userVerifiers/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<UserVerifierDto> getUserVerifiersByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getRestUserVerifiersByApplicationId(applicationId);
    }

    /**
     * Gets the user verifier by its id.
     *
     * @param   userVerifierId the user verifier id
     * @return  the user verifier dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userVerifier/{userVerifierId}", method=RequestMethod.GET)
    @ResponseBody
    public UserVerifierDto getUserVerifier(@PathVariable String userVerifierId) throws KaaAdminServiceException {
        return kaaAdminService.getRestUserVerifier(userVerifierId);
    }

    /**
     * Edits user verifier.
     *
     * @param   userVerifier the user verifier
     * @return  the user verifier dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userVerifier", method=RequestMethod.POST)
    @ResponseBody
    public UserVerifierDto editUserVerifier(@RequestBody UserVerifierDto userVerifier) throws KaaAdminServiceException {
        return kaaAdminService.editRestUserVerifier(userVerifier);
    }

    /**
     * Delete user verifier by its id.
     *
     * @param   userVerifierId the user verifier id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delUserVerifier", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUserVerifier(@RequestParam(value="userVerifierId") String userVerifierId) throws KaaAdminServiceException {
        kaaAdminService.deleteUserVerifier(userVerifierId);
    }

    /**
     * Generate log library by record key.
     *
     * @param   key       the key
     * @param   request   the request
     * @param   response  the response
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "logLibrary", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getRecordLibrary(@RequestBody RecordKey key,
            HttpServletRequest request,
            HttpServletResponse response) throws KaaAdminServiceException {
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
     * @param   key         the key
     * @param   request     the request
     * @param   response    the response
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value = "logRecordSchema", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getRecordSchema(@RequestBody RecordKey key,
            HttpServletRequest request,
            HttpServletResponse response) throws KaaAdminServiceException {
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

     /**
     * Gets all endpoint groups by application id.
     *
     * @param   applicationId the application id
     * @return  the list endpoint group dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="endpointGroups/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointGroupsByApplicationId(applicationId);
    }

    /**
     * Gets the endpoint group by its id.
     *
     * @param   endpointGroupId the endpoint group id
     * @return  the endpoint group dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="endpointGroup/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public EndpointGroupDto getEndpointGroup(@PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointGroup(endpointGroupId);
    }

    /**
     * Edits endpoint group to the list of all endpoint groups.
     *
     * @param   endpointGroup the endpoint group
     * @return  the endpoint group dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="endpointGroup", method=RequestMethod.POST)
    @ResponseBody
    public EndpointGroupDto editEndpointGroup(@RequestBody EndpointGroupDto endpointGroup) throws KaaAdminServiceException {
        return kaaAdminService.editEndpointGroup(endpointGroup);
    }

    /**
     * Delete endpoint group by its id.
     *
     * @param   endpointGroupId the endpoint group id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delEndpointGroup", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteEndpointGroup(@RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        kaaAdminService.deleteEndpointGroup(endpointGroupId);
    }

    /**
     * Gets the profile filter records by endpoint group id.
     *
     * @param   endpointGroupId     the endpoint group id
     * @param   includeDeprecated   the include deprecated
     * @return  the list profile filter record dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="profileFilterRecords", method=RequestMethod.GET)
    @ResponseBody
    public List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(
            @RequestParam(value="endpointGroupId") String endpointGroupId,
            @RequestParam(value="includeDeprecated") boolean includeDeprecated) throws KaaAdminServiceException {
        return ProfileFilterRecordDto.formStructureRecords(kaaAdminService.getProfileFilterRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /**
     * Gets the profile filter record by schema id and endpoint group id.
     *
     * @param   schemaId        the schema id
     * @param   endpointGroupId the endpoint group id
     * @return  the profile filter record dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="profileFilterRecord", method=RequestMethod.GET)
    @ResponseBody
    public ProfileFilterRecordDto getProfileFilterRecord(
            @RequestParam(value="schemaId") String schemaId,
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return ProfileFilterRecordDto.fromStructureRecord(kaaAdminService.getProfileFilterRecord(schemaId, endpointGroupId));
    }

    /**
     * Gets the vacant profile schemas by endpoint group id.
     *
     * @param   endpointGroupId the endpoint group id
     * @return  the list schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="vacantProfileSchemas/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(@PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Edits profile filter to the list of all profile filters.
     *
     * @param   profileFilter the profile filter
     * @return  the profile filter dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="profileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto editProfileFilter(@RequestBody ProfileFilterDto profileFilter) throws KaaAdminServiceException {
        return kaaAdminService.editProfileFilter(profileFilter);
    }

    /**
     * Activate profile filter by his id.
     *
     * @param   profileFilterId the profile filter id
     * @return  the profile filter dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="activateProfileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto activateProfileFilter(@RequestBody String profileFilterId) throws KaaAdminServiceException {
        return kaaAdminService.activateProfileFilter(profileFilterId);
    }

    /**
     * Deactivate profile filter by his id.
     *
     * @param   profileFilterId the profile filter id
     * @return  the profile filter dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="deactivateProfileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto deactivateProfileFilter(@RequestBody String profileFilterId) throws KaaAdminServiceException {
        return kaaAdminService.deactivateProfileFilter(profileFilterId);
    }

    /**
     * Delete profile filter record by schema id and endpoin group id.
     *
     * @param   schemaId        the schema id
     * @param   endpointGroupId the endpoint group id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delProfileFilterRecord", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteProfileFilterRecord(
            @RequestParam(value="schemaId") String schemaId,
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        kaaAdminService.deleteProfileFilterRecord(schemaId, endpointGroupId);
    }

    /**
     * Gets the configuration records by endpoint group id.
     *
     * @param   endpointGroupId     the endpoint group id
     * @param   includeDeprecated   the include deprecated
     * @return  the list configuration record dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="configurationRecords", method=RequestMethod.GET)
    @ResponseBody
    public List<ConfigurationRecordDto> getConfigurationRecordsByEndpointGroupId(
            @RequestParam(value="endpointGroupId") String endpointGroupId,
            @RequestParam(value="includeDeprecated") boolean includeDeprecated) throws KaaAdminServiceException {
        return ConfigurationRecordDto.formStructureRecords(kaaAdminService.getConfigurationRecordsByEndpointGroupId(endpointGroupId, includeDeprecated));
    }

    /**
     * Gets the configuration record by schema id and endpoint group id.
     *
     * @param   schemaId        the schema id
     * @param   endpointGroupId the endpoint group id
     * @return  the configuration record dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="configurationRecord", method=RequestMethod.GET)
    @ResponseBody
    public ConfigurationRecordDto getConfigurationRecord(
            @RequestParam(value="schemaId") String schemaId,
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return ConfigurationRecordDto.fromStructureRecord(kaaAdminService.getConfigurationRecord(schemaId, endpointGroupId));
    }

    /**
     * Gets the vacant configuration schemas by endpoint group id.
     *
     * @param   endpointGroupId the endpoint group id
     * @return  the list schema dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="vacantConfigurationSchemas/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(
            @PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Edits the configuration to the list of all configurations.
     *
     * @param   configuration the configuration
     * @return  the configuration dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="configuration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto editConfiguration(@RequestBody ConfigurationDto configuration) throws KaaAdminServiceException {
        return kaaAdminService.editConfiguration(configuration);
    }

    /**
     * Activate configuration by its id.
     *
     * @param   configurationId the configuration id
     * @return  the configuration dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="activateConfiguration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto activateConfiguration(@RequestBody String configurationId) throws KaaAdminServiceException {
        return kaaAdminService.activateConfiguration(configurationId);
    }

    /**
     * Deactivate configuration by its id.
     *
     * @param   configurationId the configuration id
     * @return  the configuration dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="deactivateConfiguration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto deactivateConfiguration(@RequestBody String configurationId) throws KaaAdminServiceException {
        return kaaAdminService.deactivateConfiguration(configurationId);
    }

    /**
     * Delete configuration record by schema id and endpoint group id.
     *
     * @param   schemaId        the schema id
     * @param   endpointGroupId the endpoint group id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delConfigurationRecord", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteConfigurationRecord(
            @RequestParam(value="schemaId") String schemaId,
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        kaaAdminService.deleteConfigurationRecord(schemaId, endpointGroupId);
    }

    /**
     * Gets all topics by application id.
     *
     * @param   applicationId the application id
     * @return  the topic dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="topics/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getTopicsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getTopicsByApplicationId(applicationId);
    }

    /**
     * Gets all topics by endpoint group id.
     *
     * @param   endpointGroupId the endpoint group id
     * @return  the topic dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="topics", method=RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getTopicsByEndpointGroupId(
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getTopicsByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets all vacant topics by endpoint group id.
     *
     * @param   endpointGroupId the endpoint group id
     * @return  the topic dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="vacantTopics/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getVacantTopicsByEndpointGroupId(@PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantTopicsByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets the topic by his id.
     *
     * @param   topicId the topic id
     * @return  the topic dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="topic/{topicId}", method=RequestMethod.GET)
    @ResponseBody
    public TopicDto getTopic(@PathVariable String topicId) throws KaaAdminServiceException {
        return kaaAdminService.getTopic(topicId);
    }

    /**
     * Edits topic to the list of all topics.
     *
     * @param   topic the topic
     * @return  the topic dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="topic", method=RequestMethod.POST)
    @ResponseBody
    public TopicDto editTopic(@RequestBody TopicDto topic) throws KaaAdminServiceException {
        return kaaAdminService.editTopic(topic);
    }

    /**
     * Delete topic by his id.
     *
     * @param   topicId the topic id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="delTopic", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTopic(@RequestParam(value="topicId") String topicId) throws KaaAdminServiceException {
        kaaAdminService.deleteTopic(topicId);
    }

    /**
     * Adds the topic with specific id to endpoint group with specific id.
     *
     * @param   endpointGroupId the endpoint group id
     * @param   topicId         the topic id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="addTopicToEpGroup", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void addTopicToEndpointGroup(
            @RequestParam(value="endpointGroupId") String endpointGroupId,
            @RequestParam(value="topicId") String topicId) throws KaaAdminServiceException {
        kaaAdminService.addTopicToEndpointGroup(endpointGroupId, topicId);
    }

    /**
     * Removes the topic with specific id to endpoint group with specific id.
     *
     * @param   endpointGroupId the endpoint group id
     * @param   topicId         the topic id
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="removeTopicFromEpGroup", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void removeTopicFromEndpointGroup(
            @RequestParam(value="endpointGroupId") String endpointGroupId,
            @RequestParam(value="topicId") String topicId) throws KaaAdminServiceException {
        kaaAdminService.removeTopicFromEndpointGroup(endpointGroupId, topicId);
    }

    /**
     * Send notification, with information from specific file, to the client.
     *
     * @param   notification    the notification
     * @param   file            the file
     * @return  the notification dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="sendNotification", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public NotificationDto sendNotification(
            @RequestPart("notification") NotificationDto notification,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.sendNotification(notification, data);
    }

    /**
     * Send unicast notification, with information from specific file, to the client identified by clientKeyHash.
     *
     * @param   notification    the notification
     * @param   clientKeyHash   the client key hash
     * @param   file            the file
     * @return  the endpoint notification dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="sendUnicastNotification", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public EndpointNotificationDto sendUnicastNotification(
            @RequestPart("notification") NotificationDto notification,
            @RequestPart("clientKeyHash") String clientKeyHash,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.sendUnicastNotification(notification, clientKeyHash, data);
    }

    /**
     * Gets all event class families.
     *
     * @return  the list event class family dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="eventClassFamilies", method=RequestMethod.GET)
    @ResponseBody
    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException {
        return kaaAdminService.getEventClassFamilies();
    }

    /**
     * Gets the event class family by its id.
     *
     * @param   eventClassFamilyId the event class family id
     * @return  the event class family dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="eventClassFamily/{eventClassFamilyId}", method=RequestMethod.GET)
    @ResponseBody
    public EventClassFamilyDto getEventClassFamily(@PathVariable String eventClassFamilyId) throws KaaAdminServiceException {
        return kaaAdminService.getEventClassFamily(eventClassFamilyId);
    }

    /**
     * Edits event class family to the list of all event class families.
     *
     * @param   eventClassFamily the event class family
     * @return  the event class family dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="eventClassFamily", method=RequestMethod.POST)
    @ResponseBody
    public EventClassFamilyDto editEventClassFamily(@RequestBody EventClassFamilyDto eventClassFamily) throws KaaAdminServiceException {
        return kaaAdminService.editEventClassFamily(eventClassFamily);
    }

    /**
     * Adds the event class family schema to the event class family with specific id.
     * Current user will be marked as creator of schema.
     *
     * @param   eventClassFamilyId  the event class family id
     * @param   file                the file
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="addEventClassFamilySchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseStatus(value = HttpStatus.OK)
    public void addEventClassFamilySchema(
            @RequestPart(value="eventClassFamilyId") String eventClassFamilyId,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        kaaAdminService.addEventClassFamilySchema(eventClassFamilyId, data);
    }

    /**
     * Gets the event classes by family its id, version and type.
     *
     * @param   eventClassFamilyId  the event class family id
     * @param   version             the version
     * @param   type                the type
     * @return  the list event class dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="eventClasses", method=RequestMethod.GET)
    @ResponseBody
    public List<EventClassDto> getEventClassesByFamilyIdVersionAndType(
            @RequestParam(value="eventClassFamilyId") String eventClassFamilyId,
            @RequestParam(value="version") int version,
            @RequestParam(value="type") EventClassType type) throws KaaAdminServiceException {
        return kaaAdminService.getEventClassesByFamilyIdVersionAndType(eventClassFamilyId, version, type);
    }

    /**
     * Gets all application event family maps by application id.
     *
     * @param   applicationId the application id
     * @return  list the application event family map dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="applicationEventMaps/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<ApplicationEventFamilyMapDto> getApplicationEventFamilyMapsByApplicationId(
            @PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getApplicationEventFamilyMapsByApplicationId(applicationId);
    }

    /**
     * Gets the application event family map by its id.
     *
     * @param   applicationEventFamilyMapId the application event family map
     * @return  the application event family map dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="applicationEventMap/{applicationEventFamilyMapId}", method=RequestMethod.GET)
    @ResponseBody
    public ApplicationEventFamilyMapDto getApplicationEventFamilyMap(
            @PathVariable String applicationEventFamilyMapId) throws KaaAdminServiceException {
        return kaaAdminService.getApplicationEventFamilyMap(applicationEventFamilyMapId);
    }

    /**
     * Edits application event family map to the list of all event family maps.
     *
     * @param   applicationEventFamilyMap the application event family map
     * @return  the application event family map dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="applicationEventMap", method=RequestMethod.POST)
    @ResponseBody
    public ApplicationEventFamilyMapDto editApplicationEventFamilyMap(
            @RequestBody ApplicationEventFamilyMapDto applicationEventFamilyMap) throws KaaAdminServiceException {
        return kaaAdminService.editApplicationEventFamilyMap(applicationEventFamilyMap);
    }

    /**
     * Gets all vacant event class families by application id.
     *
     * @param   applicationId the application id
     * @return  the list ecf info dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="vacantEventClassFamilies/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<EcfInfoDto> getVacantEventClassFamiliesByApplicationId(
            @PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantEventClassFamiliesByApplicationId(applicationId);
    }

    /**
     * Gets all event class families by application id.
     *
     * @param   applicationId the application id
     * @return  the list aef map info dto
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="eventClassFamilies/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<AefMapInfoDto> getEventClassFamiliesByApplicationId(
            @PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getEventClassFamiliesByApplicationId(applicationId);
    }

    /**
     * Edits endpoint group to the list of all endpoint groups.
     *
     * @param   endpointUserConfiguration the endpoint user configuration
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    @RequestMapping(value="userConfiguration", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void editUserConfiguration(@RequestBody EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException {
        kaaAdminService.editUserConfiguration(endpointUserConfiguration);
    }

    /**
     * Gets the file content.
     *
     * @param   file the file
     * @return  the file content
     * @throws  KaaAdminServiceException the kaa admin service exception
     */
    private byte[] getFileContent(MultipartFile file) throws KaaAdminServiceException {
        if (!file.isEmpty()) {
            logger.debug("Uploading file with name '{}'", file.getOriginalFilename());
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw Utils.handleException(e);
            }
        } else {
            logger.error("No file found in post request!");
            throw new KaaAdminServiceException("No file found in post request!", ServiceErrorCode.FILE_NOT_FOUND);
        }
    }

    @RequestMapping(value = "plugins/getPlugins", method = RequestMethod.GET)
    @ResponseBody
    public List<PluginDto> getPlugins() throws KaaAdminServiceException {
        return kaaAdminService.getPlugins();
    }

    @RequestMapping(value = "plugins/getPlugin", params = { "id" }, method = RequestMethod.GET)
    @ResponseBody
    public PluginDto getPluginById(String id) throws KaaAdminServiceException {
        return kaaAdminService.getPluginById(id);
    }

    @RequestMapping(value = "plugins/getInstances", params = { "applicationId" }, method = RequestMethod.GET)
    @ResponseBody
    public List<PluginInstanceDto> getPluginInstances(String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getPluginInstances(applicationId);
    }

    @RequestMapping(value = "plugins/getInstance", params = { "id" }, method = RequestMethod.GET)
    @ResponseBody
    public PluginInstanceDto getPluginInstance(String id) throws KaaAdminServiceException {
        return kaaAdminService.getPluginInstanceById(id);
    }

    @RequestMapping(value = "plugins/createInstance", params = { "pluginId", "applicationId", "name", "configuration" }, method = RequestMethod.POST)
    @ResponseBody
    public PluginInstanceDto createPluginInstance(
            String pluginId,
            String applicationId,
            String name,
            String configuration)
                    throws KaaAdminServiceException {

        return kaaAdminService.createPluginInstance(pluginId, applicationId, name, configuration);
    }

    @RequestMapping(value = "plugins/deleteInstance", params = { "id" }, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deletePluginInstanceById(String id) throws KaaAdminServiceException {
        kaaAdminService.deletePluginInstanceById(id);
    }

    @RequestMapping(value = "plugins/setInstanceState", params = { "id", "state" }, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void setPluginInstanceState(String id, String state) throws KaaAdminServiceException {
        kaaAdminService.setPluginInstanceState(id, state);
    }

    @RequestMapping(value = "plugins/getPluginContracts", params = { "pluginInstanceId" }, method = RequestMethod.GET)
    @ResponseBody
    public List<PluginContractDto> getPluginContracts(String pluginInstanceId) throws KaaAdminServiceException {
        return kaaAdminService.getPluginContracts(pluginInstanceId);
    }

    @RequestMapping(value = "plugins/getPluginContract", params = { "id" }, method = RequestMethod.GET)
    @ResponseBody
    public PluginContractDto getPluginContractById(String id) throws KaaAdminServiceException {
        return kaaAdminService.getPluginContractById(id);
    }

    @RequestMapping(value = "plugins/editPluginContract", method = RequestMethod.POST)
    @ResponseBody
    public PluginContractDto editPluginContract(@RequestBody PluginContractDto pluginContract) throws KaaAdminServiceException {
        return kaaAdminService.editPluginContract(pluginContract);
    }

    @RequestMapping(value = "plugins/addPluginContract", params = { "pluginInstanceId" }, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void addPluginContractToPluginInstance(
            @RequestParam String pluginInstanceId,
            @RequestBody PluginContractDto pluginContract)
                    throws KaaAdminServiceException {

        kaaAdminService.addPluginContractToPluginInstance(pluginInstanceId, pluginContract);
    }

    @RequestMapping(value = "plugin/removePluginContract", params = { "pluginInstanceId", "pluginContractId" }, method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void removePluginContractFromPluginInstance(
            String pluginInstanceId,
            String pluginContractId)
                    throws KaaAdminServiceException {

        kaaAdminService.removePluginContractFromPluginInstance(pluginInstanceId, pluginContractId);
    }

}
