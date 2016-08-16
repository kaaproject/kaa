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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/userService")
public interface UserService extends RemoteService {

    UserDto getUserProfile() throws KaaAdminServiceException;

    void editUserProfile(UserProfileUpdateDto userProfileUpdateDto) throws KaaAdminServiceException;

    List<UserDto> getUsers() throws KaaAdminServiceException;

    UserDto getUser(String userId) throws KaaAdminServiceException;

    UserDto editUser(UserDto user) throws KaaAdminServiceException;

    void deleteUser(String userId) throws KaaAdminServiceException;

    List<UserDto> findAllTenantAdminsByTenantId(String id) throws KaaAdminServiceException;
}
