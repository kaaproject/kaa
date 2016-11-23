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

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

import org.apache.commons.lang3.StringUtils;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.common.dto.admin.UserProfileUpdateDto;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("userService")
public class UserServiceImpl extends AbstractAdminService implements UserService {

  @Override
  public org.kaaproject.kaa.common.dto.admin.UserDto getUserProfile()
      throws KaaAdminServiceException {
    try {
      return toUser(getCurrentUser());
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public void editUserProfile(UserProfileUpdateDto userDto)
      throws KaaAdminServiceException {
    try {
      User user = userFacade.findById(Long.valueOf(getCurrentUser().getExternalUid()));
      if (!isEmpty(userDto.getFirstName())) {
        user.setFirstName(userDto.getFirstName());
      }
      if (!isEmpty(userDto.getLastName())) {
        user.setLastName(userDto.getLastName());
      }
      if (!isEmpty(userDto.getMail())) {
        user.setMail(userDto.getMail());
      }
      userFacade.save(user);
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public List<org.kaaproject.kaa.common.dto.admin.UserDto> getUsers()
      throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
    try {
      List<org.kaaproject.kaa.common.dto.UserDto> users = controlService.getTenantUsers(
          getTenantId());
      List<org.kaaproject.kaa.common.dto.admin.UserDto> tenantUsers = new ArrayList<>(
          users.size());
      for (org.kaaproject.kaa.common.dto.UserDto user : users) {
        org.kaaproject.kaa.common.dto.admin.UserDto tenantUser = toUser(user);
        tenantUsers.add(tenantUser);
      }
      return tenantUsers;
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public org.kaaproject.kaa.common.dto.admin.UserDto getUser(String userId)
      throws KaaAdminServiceException {
    try {
      UserDto user = controlService.getUser(userId);
      Utils.checkNotNull(user);
      if (user.getAuthority().equals(KaaAuthorityDto.TENANT_ADMIN)) {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
      } else {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        checkTenantId(user.getTenantId());
      }
      return toUser(user);
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public org.kaaproject.kaa.common.dto.admin.UserDto editUser(
      org.kaaproject.kaa.common.dto.admin.UserDto user, boolean doSendTempPassword)
      throws KaaAdminServiceException {
    try {
      boolean createNewUser = (user.getId() == null);

      String tempPassword = null;
      if (createNewUser) {
        checkCreateUserPermission(user);
        tempPassword = createNewUser(user, doSendTempPassword);
      } else {
        checkEditUserPermission(user);
        editUserFacadeUser(user);
      }

      org.kaaproject.kaa.common.dto.admin.UserDto editedUser = editControlServiceUser(user);
      if (StringUtils.isNotBlank(tempPassword)) {
        editedUser.setTempPassword(tempPassword);
      }
      return editedUser;
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public void deleteUser(String userId) throws KaaAdminServiceException {
    try {
      UserDto user = controlService.getUser(userId);
      Utils.checkNotNull(user);
      if (user.getAuthority().equals(KaaAuthorityDto.TENANT_ADMIN)) {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
      } else {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        checkTenantId(user.getTenantId());
      }
      userFacade.deleteUser(Long.valueOf(user.getExternalUid()));
      controlService.deleteUser(user.getId());
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
  }

  @Override
  public List<org.kaaproject.kaa.common.dto.admin.UserDto> findAllTenantAdminsByTenantId(
      String tenantId) throws KaaAdminServiceException {
    checkAuthority(KaaAuthorityDto.KAA_ADMIN);
    List<org.kaaproject.kaa.common.dto.admin.UserDto> tenantAdminList = new ArrayList<>();
    try {
      List<UserDto> userDtoList = controlService.findAllTenantAdminsByTenantId(tenantId);
      if (userDtoList != null) {
        for (UserDto userDto : userDtoList) {
          tenantAdminList.add(toUser(userDto));
        }
      }
    } catch (Exception ex) {
      throw Utils.handleException(ex);
    }
    return tenantAdminList;
  }

}
