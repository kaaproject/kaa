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

package org.kaaproject.kaa.server.admin.services;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.kaaproject.kaa.server.admin.shared.services.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("userService")
public class UserServiceImpl extends AbstractAdminService implements UserService {

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto getUserProfile() throws KaaAdminServiceException {
        try {
            User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
            Utils.checkNotNull(user);
            org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                    user.getUsername(), user.getFirstName(), user.getLastName(), user.getMail(), KaaAuthorityDto.valueOf(user
                    .getAuthorities().iterator().next().getAuthority()));
            return result;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void editUserProfile(UserProfileUpdateDto userProfileUpdateDto)
            throws KaaAdminServiceException {
        try {
            checkUserProfile(userProfileUpdateDto);
            User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
            user.setFirstName(userProfileUpdateDto.getFirstName());
            user.setLastName(userProfileUpdateDto.getLastName());
            user.setMail(userProfileUpdateDto.getMail());
            userFacade.save(user);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<org.kaaproject.kaa.common.dto.admin.UserDto> getUsers() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            List<org.kaaproject.kaa.common.dto.UserDto> users = controlService.getTenantUsers(getTenantId());
            List<org.kaaproject.kaa.common.dto.admin.UserDto> tenantUsers = new ArrayList<>(users.size());
            for (org.kaaproject.kaa.common.dto.UserDto user : users) {
                org.kaaproject.kaa.common.dto.admin.UserDto tenantUser = toUser(user);
                tenantUsers.add(tenantUser);
            }
            return tenantUsers;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto getUser(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            org.kaaproject.kaa.common.dto.UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            checkTenantId(user.getTenantId());
            return toUser(user);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public org.kaaproject.kaa.common.dto.admin.UserDto editUser(org.kaaproject.kaa.common.dto.admin.UserDto user)
            throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(user.getId())) {
                org.kaaproject.kaa.common.dto.UserDto storedUser = controlService.getUser(user.getId());
                Utils.checkNotNull(storedUser);
                checkTenantId(storedUser.getTenantId());
            }
            Long userId = saveUser(user);
            org.kaaproject.kaa.common.dto.UserDto userDto = new org.kaaproject.kaa.common.dto.UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setExternalUid(userId.toString());
            userDto.setTenantId(getTenantId());
            userDto.setAuthority(user.getAuthority());
            org.kaaproject.kaa.common.dto.UserDto savedUser = controlService.editUser(userDto);
            return toUser(savedUser);

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteUser(String userId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            org.kaaproject.kaa.common.dto.UserDto user = controlService.getUser(userId);
            Utils.checkNotNull(user);
            checkTenantId(user.getTenantId());
            if (KaaAuthorityDto.KAA_ADMIN.equals(user.getAuthority()) || KaaAuthorityDto.TENANT_ADMIN.equals(user.getAuthority())) {
                throw new KaaAdminServiceException("Can't delete KAA admin or Tenant admin user!", ServiceErrorCode.PERMISSION_DENIED);
            }
            userFacade.deleteUser(Long.valueOf(user.getExternalUid()));
            controlService.deleteUser(user.getId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    private void checkUserProfile(UserProfileUpdateDto userProfileUpdateDto) throws KaaAdminServiceException {
        if (isEmpty(userProfileUpdateDto.getFirstName())) {
            throw new IllegalArgumentException("First name is not valid.");
        } else if (isEmpty(userProfileUpdateDto.getLastName())) {
            throw new IllegalArgumentException("Last name is not valid.");
        } else if (isEmpty(userProfileUpdateDto.getMail())) {
            throw new IllegalArgumentException("Mail is not valid.");
        }
    }

    private org.kaaproject.kaa.common.dto.admin.UserDto toUser(org.kaaproject.kaa.common.dto.UserDto tenantUser) {
        User user = userFacade.findById(Long.valueOf(tenantUser.getExternalUid()));
        org.kaaproject.kaa.common.dto.admin.UserDto result = new org.kaaproject.kaa.common.dto.admin.UserDto(user.getId().toString(),
                user.getUsername(), user.getFirstName(), user.getLastName(), user.getMail(), KaaAuthorityDto.valueOf(user.getAuthorities()
                .iterator().next().getAuthority()));
        result.setId(tenantUser.getId());
        result.setTenantId(tenantUser.getTenantId());
        return result;
    }

}
