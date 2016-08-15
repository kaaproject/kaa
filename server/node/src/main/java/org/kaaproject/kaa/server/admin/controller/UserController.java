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
import org.apache.commons.lang3.StringUtils;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.ResultCode;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.server.admin.services.entity.CreateUserResult;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.spring4gwt.server.SpringGwtRemoteServiceServlet;
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

@Api(value = "User", description = "Provides function for manage users", basePath = "/kaaAdmin/rest")
@Controller
public class UserController extends AbstractAdminController {

    /**
     * Check auth of kaa admin.
     *
     * @param request the request
     * @return the auth result dto
     * @throws Exception the exception
     */
    @ApiOperation(value = "Get user authentication status",
            notes = "Returns information about the current authorized user. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "auth/checkAuth", method = RequestMethod.GET)
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
     * @param username the username
     * @param password the password
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create user with KAA_ADMIN role",
            notes = "Creates a user with the KAA_ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "A user with the KAA_ADMIN role already exists"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "auth/createKaaAdmin", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void createKaaAdmin(
            @ApiParam(name = "username", value = "A user name of the Kaa admin user", required = true)
            @RequestParam(value = "username") String username,
            @ApiParam(name = "password", value = "A user password of the Kaa admin user; it must be no shorter than 6 characters", required = true)
            @RequestParam(value = "password") String password)
            throws KaaAdminServiceException {
        if (!userFacade.isAuthorityExists(KaaAuthorityDto.KAA_ADMIN.name())) {
            kaaAuthService.createKaaAdmin(username, password);
        } else {
            throw new KaaAdminServiceException("Kaa admin already exists. Can't create more than one kaa admin users.",
                    ServiceErrorCode.PERMISSION_DENIED);
        }
    }

    /**
     * Change password of user with specific name if old password valid.
     *
     * @param username    the username
     * @param oldPassword the old password
     * @param newPassword the new password
     * @return the result code
     * @throws Exception the exception
     */
    @ApiOperation(value = "Change user password",
            notes = "Changes the password of a user. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified parameters are not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "auth/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public ResultCode changePassword(
            @ApiParam(name = "username", value = "A user name of the user whose password is to be changed", required = true)
            @RequestParam(value = "username") String username,
            @ApiParam(name = "oldPassword", value = "An old password of the user", required = true)
            @RequestParam(value = "oldPassword") String oldPassword,
            @ApiParam(name = "newPassword", value = "A new password of the user; it must be no shorter than 6 characters", required = true)
            @RequestParam(value = "newPassword") String newPassword)
            throws Exception {
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
     * Gets the user profile of current user.
     *
     * @return the user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get user profile",
            notes = "Returns the user profile of the current user. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userProfile", method = RequestMethod.GET)
    @ResponseBody
    public UserDto getUserProfile() throws KaaAdminServiceException {
        return userService.getUserProfile();
    }

    /**
     * Edits user profile to all user profiles.
     *
     * @param userProfileUpdateDto the user profile dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Edit user profile",
            notes = "Edits the user profile. Only authorized users are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "userProfile", method = RequestMethod.POST)
    @ResponseBody
    public void editUserProfile(
            @ApiParam(name = "userProfileUpdateDto", value = "UserProfileUpdateDto body.", required = true)
            @RequestBody UserProfileUpdateDto userProfileUpdateDto) throws KaaAdminServiceException {
        userService.editUserProfile(userProfileUpdateDto);
    }

    /**
     * Gets all users.
     *
     * @return the list user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get all users",
            notes = "Returns a list of all users associated with the current authorized user: users whose Tenant ID values match the Tenant ID of the " +
                    "request submitter. Only users with the TENANT_ADMIN role are allowed to submit this request.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN)"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "users", method = RequestMethod.GET)
    @ResponseBody
    public List<UserDto> getUsers() throws KaaAdminServiceException {
        return userService.getUsers();
    }

    /**
     * Gets the user by his id.
     *
     * @param userId the user id
     * @return the user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Get user",
            notes = " Returns a user. Only users with the TENANT_ADMIN role are allowed to submit this request. The Tenant ID of requested user must be " +
                    "identical to the Tenant ID of the submitter.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required role (TENANT_ADMIN) or the Tenant ID of the requested user " +
                    "does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The user with the specified userId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public UserDto getUser(
            @ApiParam(name = "userId", value = "A unique user identifier", required = true)
            @PathVariable String userId) throws KaaAdminServiceException {
        return userService.getUser(userId);
    }

    /**
     * Edits user to the list of all users.
     *
     * @param user the user
     * @return the user dto
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Create/Edit user",
            notes = "Creates or edits a user. To create a user you do not need to specify the user ID, its Tenant ID will be set to the Tenant ID of the " +
                    "request submitter. A random password will be generated and presented in the success response in the tempPassword field. To edit user " +
                    "specify the user ID. If a user with the specified ID exists, it will be updated. Only users with the TENANT_ADMIN role can perform " +
                    "this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required TENANT_ADMIN role or the Tenant ID of the editing user " +
                    "does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "user", method = RequestMethod.POST)
    @ResponseBody
    public UserDto editUser(
            @ApiParam(name = "user", value = "UserDto body. Mandatory fields: username, firstName, lastName, mail, authority", required = true)
            @RequestBody UserDto user) throws KaaAdminServiceException {
        try {
            CreateUserResult result = userFacade.saveUserDto(user, passwordEncoder);
            user.setExternalUid(result.getUserId().toString());
            UserDto userDto = userService.editUser(user);
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
     * @param userId the user id
     * @throws KaaAdminServiceException the kaa admin service exception
     */
    @ApiOperation(value = "Delete user",
            notes = "Deletes a user specified by user ID. Only users with the TENANT_ADMIN role are allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified userId is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required TENANT_ADMIN role or the Tenant ID of the editing user " +
                    "does not match the Tenant ID of the authenticated user"),
            @ApiResponse(code = 404, message = "The user with the specified userId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "delUser", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteUser(
            @ApiParam(name = "userId", value = "An old password of the user", required = true)
            @RequestParam(value = "userId") String userId) throws KaaAdminServiceException {
        userService.deleteUser(userId);
    }


    @ApiOperation(value = "Get tenant admins based on tenant id",
     notes="Gets the tenant admins by specified tenantId. Only user with KAA_ADMIN role is allowed to perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "The specified tenantId is not valid"),
            @ApiResponse(code = 401, message = "The user is not authenticated or invalid credentials were provided"),
            @ApiResponse(code = 403, message = "The authenticated user does not have the required KAA_ADMIN role"),
            @ApiResponse(code = 404, message = "The user with the specified tenantId does not exist"),
            @ApiResponse(code = 500, message = "An unexpected error occurred on the server side")})
    @RequestMapping(value = "admins/{tenantId}", method = RequestMethod.POST)
    @ResponseBody
    public List<UserDto> findAllTenantAdminsByTenantId(
            @ApiParam(name = "tenantId", value = "A unique tenant identifier", required = true)
            @PathVariable String tenantId) throws KaaAdminServiceException {
       return  userService.findAllTenantAdminsByTenantId(tenantId);
    }



}
