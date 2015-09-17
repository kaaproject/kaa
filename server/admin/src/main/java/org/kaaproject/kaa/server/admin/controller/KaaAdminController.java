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
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
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
     * Check auth of kaa admin.
     *
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
     */
    @RequestMapping(value="tenants", method=RequestMethod.GET)
    @ResponseBody
    public List<TenantUserDto> getTenants() throws KaaAdminServiceException {
        return kaaAdminService.getTenants();
    }

    /**
     * Gets the tenant by user id.
     *
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
     */
    @RequestMapping(value="delTenant", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTenant(@RequestParam(value="userId") String userId) throws KaaAdminServiceException {
        kaaAdminService.deleteTenant(userId);
    }

    /**
     * Gets all applications.
     *
     */
    @RequestMapping(value="applications", method=RequestMethod.GET)
    @ResponseBody
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        return kaaAdminService.getApplications();
    }

    /**
     * Gets the application by its id.
     *
     */
    @RequestMapping(value="application/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public ApplicationDto getApplication(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getApplication(applicationId);
    }

    /**
     * Gets the application by its id.
     *
     */
    @RequestMapping(value="application/token/{applicationToken}", method=RequestMethod.GET)
    @ResponseBody
    public ApplicationDto getApplicationByApplicationToken(@PathVariable String applicationToken) throws KaaAdminServiceException {
        return kaaAdminService.getApplicationByApplicationToken(applicationToken);
    }

    /**
     * Edits application to the list of all applications.
     *
     */
    @RequestMapping(value="application", method=RequestMethod.POST)
    @ResponseBody
    public ApplicationDto editApplication(@RequestBody ApplicationDto application) throws KaaAdminServiceException {
        return kaaAdminService.editApplication(application);
    }

    /**
     * Gets the user profile of current user.
     *
     */
    @RequestMapping(value="userProfile", method=RequestMethod.GET)
    @ResponseBody
    public UserDto getUserProfile() throws KaaAdminServiceException {
        return kaaAdminService.getUserProfile();
    }

    /**
     * Edits user profile to all user profiles.
     *
     */
    @RequestMapping(value="userProfile", method=RequestMethod.POST)
    @ResponseBody
    public UserDto editUserProfile(@RequestBody UserDto userDto) throws KaaAdminServiceException {
        return kaaAdminService.editUserProfile(userDto);
    }

    /**
     * Gets all users.
     *
     */
    @RequestMapping(value="users", method=RequestMethod.GET)
    @ResponseBody
    public List<UserDto> getUsers() throws KaaAdminServiceException {
        return kaaAdminService.getUsers();
    }

    /**
     * Gets the user by his id.
     *
     */
    @RequestMapping(value="user/{userId}", method=RequestMethod.GET)
    @ResponseBody
    public UserDto getUser(@PathVariable String userId) throws KaaAdminServiceException {
        return kaaAdminService.getUser(userId);
    }

    /**
     * Edits user to the list of all users.
     *
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
     * Gets the schema versions by application id.
     *
     */
    @RequestMapping(value="schemaVersions/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public SchemaVersions getSchemaVersionsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getSchemaVersionsByApplicationId(applicationId);
    }

    /**
     * Gets the sdk by sdk key.
     *
     */
    @RequestMapping(value="sdk", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSdk(@RequestBody SdkPropertiesDto key,
            HttpServletRequest request,
            HttpServletResponse response) throws KaaAdminServiceException {
        try {
            FileData sdkData = kaaAdminService.getSdk(key);
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
     */
    @RequestMapping(value="profileSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<ProfileSchemaDto> getProfileSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getProfileSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the profile schema by her id.
     *
     */
    @RequestMapping(value="profileSchema/{profileSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public ProfileSchemaDto getProfileSchema(@PathVariable String profileSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getProfileSchema(profileSchemaId);
    }

    /**
     * Edits profile schema to the list of all profile schemas.
     *
     */
    @RequestMapping(value="profileSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public ProfileSchemaDto editProfileSchema(@RequestPart("profileSchema") ProfileSchemaDto profileSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editProfileSchema(profileSchema, data);
    }

    /**
     * Gets the configuration schemas by application id.
     *
     */
    @RequestMapping(value="configurationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<ConfigurationSchemaDto> getConfigurationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getConfigurationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the configuration schema by her id.
     *
     */
    @RequestMapping(value="configurationSchema/{configurationSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public ConfigurationSchemaDto getConfigurationSchema(@PathVariable String configurationSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getConfigurationSchema(configurationSchemaId);
    }

    /**
     * Edits configuration schema to the list of all configuration schemas.
     *
     */
    @RequestMapping(value="configurationSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public ConfigurationSchemaDto editConfigurationSchema(@RequestPart("configurationSchema") ConfigurationSchemaDto configurationSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editConfigurationSchema(configurationSchema, data);
    }

    /**
     * Gets the notification schemas by application id.
     *
     */
    @RequestMapping(value="notificationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<NotificationSchemaDto> getNotificationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getNotificationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the user notification schemas by application id.
     *
     */
    @RequestMapping(value="userNotificationSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getUserNotificationSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getUserNotificationSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the notification schema by her id.
     *
     */
    @RequestMapping(value="notificationSchema/{notificationSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public NotificationSchemaDto getNotificationSchema(@PathVariable String notificationSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getNotificationSchema(notificationSchemaId);
    }

    /**
     * Edits notification schema to the list of all notification schemas.
     *
     */
    @RequestMapping(value="notificationSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public NotificationSchemaDto editNotificationSchema(@RequestPart("notificationSchema") NotificationSchemaDto notificationSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editNotificationSchema(notificationSchema, data);
    }

    /**
     * Gets all log schemas by application id.
     *
     */
    @RequestMapping(value="logSchemas/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<LogSchemaDto> getLogSchemasByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchemasByApplicationId(applicationId);
    }

    /**
     * Gets the log schema by its id.
     *
     */
    @RequestMapping(value="logSchema/{logSchemaId}", method=RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchema(@PathVariable String logSchemaId) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchema(logSchemaId);
    }

    /**
     * Gets the log schema by application token and schema version.
     *
     */
    @RequestMapping(value="logSchema/{applicationToken}/{schemaVersion}", method=RequestMethod.GET)
    @ResponseBody
    public LogSchemaDto getLogSchemaByApplicationTokenAndVersion(@PathVariable String applicationToken,
                                                                 @PathVariable int schemaVersion) throws KaaAdminServiceException {
        return kaaAdminService.getLogSchemaByApplicationTokenAndVersion(applicationToken, schemaVersion);
    }

    /**
     * Edits log schema to the list of all log schemas.
     *
     */
    @RequestMapping(value="logSchema", method=RequestMethod.POST, consumes = { "multipart/mixed", "multipart/form-data" })
    @ResponseBody
    public LogSchemaDto editLogSchema(@RequestPart("logSchema") LogSchemaDto logSchema,
            @RequestPart("file") MultipartFile file) throws KaaAdminServiceException {
        byte[] data = getFileContent(file);
        return kaaAdminService.editLogSchema(logSchema, data);
    }

    /**
     * Gets all log appenders by application id.
     *
     */
    @RequestMapping(value="logAppenders/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<LogAppenderDto> getLogAppendersByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getRestLogAppendersByApplicationId(applicationId);
    }

    /**
     * Gets the log appender by its id.
     *
     */
    @RequestMapping(value="logAppender/{logAppenderId}", method=RequestMethod.GET)
    @ResponseBody
    public LogAppenderDto getLogAppender(@PathVariable String logAppenderId) throws KaaAdminServiceException {
        return kaaAdminService.getRestLogAppender(logAppenderId);
    }

    /**
     * Edits log appender.
     *
     */
    @RequestMapping(value="logAppender", method=RequestMethod.POST)
    @ResponseBody
    public LogAppenderDto editLogAppender(@RequestBody LogAppenderDto logAppender) throws KaaAdminServiceException {
        return kaaAdminService.editRestLogAppender(logAppender);
    }

    /**
     * Delete log appender by its id.
     *
     */
    @RequestMapping(value="delLogAppender", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteLogAppender(@RequestParam(value="logAppenderId") String logAppenderId) throws KaaAdminServiceException {
        kaaAdminService.deleteLogAppender(logAppenderId);
    }
    
    /**
     * Gets all user verifiers by application id.
     *
     */
    @RequestMapping(value="userVerifiers/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<UserVerifierDto> getUserVerifiersByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getRestUserVerifiersByApplicationId(applicationId);
    }

    /**
     * Gets the user verifier by its id.
     *
     */
    @RequestMapping(value="userVerifier/{userVerifierId}", method=RequestMethod.GET)
    @ResponseBody
    public UserVerifierDto getUserVerifier(@PathVariable String userVerifierId) throws KaaAdminServiceException {
        return kaaAdminService.getRestUserVerifier(userVerifierId);
    }

    /**
     * Edits user verifier.
     *
     */
    @RequestMapping(value="userVerifier", method=RequestMethod.POST)
    @ResponseBody
    public UserVerifierDto editUserVerifier(@RequestBody UserVerifierDto userVerifier) throws KaaAdminServiceException {
        return kaaAdminService.editRestUserVerifier(userVerifier);
    }

    /**
     * Delete user verifier by its id.
     *
     */
    @RequestMapping(value="delUserVerifier", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUserVerifier(@RequestParam(value="userVerifierId") String userVerifierId) throws KaaAdminServiceException {
        kaaAdminService.deleteUserVerifier(userVerifierId);
    }    

    /**
     * Generate log library by record key.
     *
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
     */
    @RequestMapping(value="endpointGroups/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<EndpointGroupDto> getEndpointGroupsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointGroupsByApplicationId(applicationId);
    }

    /**
     * Gets the endpoint group by its id.
     *
     */
    @RequestMapping(value="endpointGroup/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public EndpointGroupDto getEndpointGroup(@PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getEndpointGroup(endpointGroupId);
    }

    /**
     * Edits endpoint group to the list of all endpoint groups.
     *
     */
    @RequestMapping(value="endpointGroup", method=RequestMethod.POST)
    @ResponseBody
    public EndpointGroupDto editEndpointGroup(@RequestBody EndpointGroupDto endpointGroup) throws KaaAdminServiceException {
        return kaaAdminService.editEndpointGroup(endpointGroup);
    }

    /**
     * Delete endpoint group by its id.
     *
     */
    @RequestMapping(value="delEndpointGroup", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteEndpointGroup(@RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        kaaAdminService.deleteEndpointGroup(endpointGroupId);
    }

    /**
     * Gets the profile filter records by endpoint group id.
     *
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
     */
    @RequestMapping(value="vacantProfileSchemas", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getVacantProfileSchemasByEndpointGroupId(
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantProfileSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Edits profile filter to the list of all profile filters.
     *
     */
    @RequestMapping(value="profileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto editProfileFilter(@RequestBody ProfileFilterDto profileFilter) throws KaaAdminServiceException {
        return kaaAdminService.editProfileFilter(profileFilter);
    }

    /**
     * Activate profile filter by his id.
     *
     */
    @RequestMapping(value="activateProfileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto activateProfileFilter(@RequestBody String profileFilterId) throws KaaAdminServiceException {
        return kaaAdminService.activateProfileFilter(profileFilterId);
    }

    /**
     * Deactivate profile filter by his id.
     *
     */
    @RequestMapping(value="deactivateProfileFilter", method=RequestMethod.POST)
    @ResponseBody
    public ProfileFilterDto deactivateProfileFilter(@RequestBody String profileFilterId) throws KaaAdminServiceException {
        return kaaAdminService.deactivateProfileFilter(profileFilterId);
    }

    /**
     * Delete profile filter record by schema id and endpoin group id.
     *
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
     */
    @RequestMapping(value="vacantConfigurationSchemas", method=RequestMethod.GET)
    @ResponseBody
    public List<SchemaDto> getVacantConfigurationSchemasByEndpointGroupId(
            @RequestParam(value="endpointGroupId") String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantConfigurationSchemasByEndpointGroupId(endpointGroupId);
    }

    /**
     * Edits the configuration to the list of all configurations.
     *
     */
    @RequestMapping(value="configuration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto editConfiguration(@RequestBody ConfigurationDto configuration) throws KaaAdminServiceException {
        return kaaAdminService.editConfiguration(configuration);
    }

    /**
     * Activate configuration by its id.
     *
     */
    @RequestMapping(value="activateConfiguration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto activateConfiguration(@RequestBody String configurationId) throws KaaAdminServiceException {
        return kaaAdminService.activateConfiguration(configurationId);
    }

    /**
     * Deactivate configuration by its id.
     *
     */
    @RequestMapping(value="deactivateConfiguration", method=RequestMethod.POST)
    @ResponseBody
    public ConfigurationDto deactivateConfiguration(@RequestBody String configurationId) throws KaaAdminServiceException {
        return kaaAdminService.deactivateConfiguration(configurationId);
    }

    /**
     * Delete configuration record by schema id and endpoint group id.
     *
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
     */
    @RequestMapping(value="topics/{applicationId}", method=RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getTopicsByApplicationId(@PathVariable String applicationId) throws KaaAdminServiceException {
        return kaaAdminService.getTopicsByApplicationId(applicationId);
    }

    /**
     * Gets all topics by endpoint group id.
     *
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
     */
    @RequestMapping(value="vacantTopics/{endpointGroupId}", method=RequestMethod.GET)
    @ResponseBody
    public List<TopicDto> getVacantTopicsByEndpointGroupId(@PathVariable String endpointGroupId) throws KaaAdminServiceException {
        return kaaAdminService.getVacantTopicsByEndpointGroupId(endpointGroupId);
    }

    /**
     * Gets the topic by his id.
     *
     */
    @RequestMapping(value="topic/{topicId}", method=RequestMethod.GET)
    @ResponseBody
    public TopicDto getTopic(@PathVariable String topicId) throws KaaAdminServiceException {
        return kaaAdminService.getTopic(topicId);
    }

    /**
     * Edits topic to the list of all topics.
     *
     */
    @RequestMapping(value="topic", method=RequestMethod.POST)
    @ResponseBody
    public TopicDto editTopic(@RequestBody TopicDto topic) throws KaaAdminServiceException {
        return kaaAdminService.editTopic(topic);
    }

    /**
     * Delete topic by his id.
     *
     */
    @RequestMapping(value="delTopic", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteTopic(@RequestParam(value="topicId") String topicId) throws KaaAdminServiceException {
        kaaAdminService.deleteTopic(topicId);
    }

    /**
     * Adds the topic with specific id to endpoint group with specific id.
     *
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
     */
    @RequestMapping(value="eventClassFamilies", method=RequestMethod.GET)
    @ResponseBody
    public List<EventClassFamilyDto> getEventClassFamilies() throws KaaAdminServiceException {
        return kaaAdminService.getEventClassFamilies();
    }

    /**
     * Gets the event class family by its id.
     *
     */
    @RequestMapping(value="eventClassFamily/{eventClassFamilyId}", method=RequestMethod.GET)
    @ResponseBody
    public EventClassFamilyDto getEventClassFamily(@PathVariable String eventClassFamilyId) throws KaaAdminServiceException {
        return kaaAdminService.getEventClassFamily(eventClassFamilyId);
    }

    /**
     * Edits event class family to the list of all event class families.
     *
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
     */
    @RequestMapping(value="userConfiguration", method=RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void editUserConfiguration(@RequestBody EndpointUserConfigurationDto endpointUserConfiguration) throws KaaAdminServiceException {
        kaaAdminService.editUserConfiguration(endpointUserConfiguration);
    }

    /**
     * Gets the file content.
     *
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

}
