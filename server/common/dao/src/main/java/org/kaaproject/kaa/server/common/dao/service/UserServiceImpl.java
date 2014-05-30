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

package org.kaaproject.kaa.server.common.dao.service;

import static org.kaaproject.kaa.server.common.dao.DaoUtil.convertDtoList;
import static org.kaaproject.kaa.server.common.dao.DaoUtil.getDto;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidId;
import static org.kaaproject.kaa.server.common.dao.service.Validator.isValidObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.TenantDao;
import org.kaaproject.kaa.server.common.dao.UserDao;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.mongo.model.Tenant;
import org.kaaproject.kaa.server.common.dao.mongo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao<User> userDao;

    @Autowired
    private TenantDao<Tenant> tenantDao;

    @Autowired
    private ApplicationService applicationService;

    @Override
    public TenantDto saveTenant(TenantDto tenantDto) {
        TenantDto tenant = null;
        if (isValidObject(tenantDto)) {
            tenant = getDto(tenantDao.save(new Tenant(tenantDto)));
        }
        return tenant;
    }

    @Override
    public void removeTenantById(String tenantId) {
        if (isValidId(tenantId)) {
            removeCascadeTenant(tenantId);
        }
    }

    @Override
    public TenantDto findTenantByName(String name) {
        TenantDto tenantDto = null;
        if (StringUtils.isNotBlank(name)) {
            tenantDto = getDto(tenantDao.findByName(name));
        }
        return tenantDto;
    }

    @Override
    public TenantDto findTenantById(String id) {
        TenantDto tenantDto = null;
        if (isValidId(id)) {
            tenantDto = getDto(tenantDao.findById(id));
        }
        return tenantDto;
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        UserDto user = null;
        if (isValidObject(userDto)) {
            user = getDto(userDao.save(new User(userDto)));
        }
        return user;
    }

    @Override
    public void removeUserById(String id) {
        if (isValidId(id)) {
            userDao.removeById(id);
        }
    }

    @Override
    public UserDto findUserByExternalUid(String externalUid) {
        UserDto userDto = null;
        if (StringUtils.isNotBlank(externalUid)) {
            userDto = getDto(userDao.findByExternalUid(externalUid));
        }
        return userDto;
    }

    @Override
    public UserDto findUserById(String id) {
        UserDto userDto = null;
        if (isValidId(id)) {
            userDto = getDto(userDao.findById(id));
        }
        return userDto;
    }

    @Override
    public List<TenantDto> findAllTenants() {
        return convertDtoList(tenantDao.find());
    }

    @Override
    public List<UserDto> findAllUsers() {
        return convertDtoList(userDao.find());
    }

    private void removeCascadeTenant(String tenantId) {
        LOG.debug("Casscade remove by tenant id [{}]", tenantId);
        applicationService.removeAppsByTenantId(tenantId);
        userDao.removeByTenantId(tenantId);
        tenantDao.removeById(tenantId);
    }

    @Override
    public List<TenantAdminDto> findAllTenantAdmins() {
        LOG.debug("Try to find all admin tenants");
        List<TenantDto> tenants = findAllTenants();
        List<TenantAdminDto> tenantAdmins = new ArrayList<TenantAdminDto>(tenants.size());
        for (TenantDto tenant : tenants) {
            TenantAdminDto tenantAdmin = new TenantAdminDto();
            tenantAdmin.setId(tenant.getId());
            tenantAdmin.setName(tenant.getName());
            List<User> users = userDao.findByTenantIdAndAuthority(tenant.getId(), KaaAuthorityDto.TENANT_ADMIN.name());
            if (!users.isEmpty()) {
                tenantAdmin.setUserId(users.get(0).getId());
                tenantAdmin.setUsername(users.get(0).getUsername());
                tenantAdmin.setExternalUid(users.get(0).getExternalUid());
            }
            tenantAdmins.add(tenantAdmin);
        }
        return tenantAdmins;
    }

    @Override
    public TenantAdminDto saveTenantAdmin(TenantAdminDto tenantAdminDto) {
        TenantAdminDto tenantAdmin = new TenantAdminDto();
        if (isValidObject(tenantAdminDto)) {
            TenantDto tenant = new TenantDto();
            tenant.setId(tenantAdminDto.getId());
            tenant.setName(tenantAdminDto.getName());
            tenant = getDto(tenantDao.save(new Tenant(tenant)));
            tenantAdmin.setId(tenant.getId());
            tenantAdmin.setName(tenant.getName());
        }
        if (StringUtils.isEmpty(tenantAdminDto.getUserId()) || isValidId(tenantAdminDto.getUserId())) {
            UserDto user = new UserDto();
            user.setId(tenantAdminDto.getUserId());
            user.setUsername(tenantAdminDto.getUsername());
            user.setExternalUid(tenantAdminDto.getExternalUid());
            user.setTenantId(tenantAdmin.getId());
            user.setAuthority(KaaAuthorityDto.TENANT_ADMIN);
            user = getDto(userDao.save(new User(user)));
            tenantAdmin.setUserId(user.getId());
            tenantAdmin.setUsername(user.getUsername());
            tenantAdmin.setExternalUid(user.getExternalUid());
        }
        return tenantAdmin;
    }

    @Override
    public void removeTenantAdminById(String tenantId) {
        if (isValidId(tenantId)) {
            removeCascadeTenant(tenantId);
        }
    }

    @Override
    public TenantAdminDto findTenantAdminById(String id) {
        TenantAdminDto tenantAdminDto = null;
        if (isValidId(id)) {
            TenantDto tenantDto = getDto(tenantDao.findById(id));
            if (tenantDto != null) {
                tenantAdminDto = new TenantAdminDto();
                tenantAdminDto.setId(tenantDto.getId());
                tenantAdminDto.setName(tenantDto.getName());
                List<User> users = userDao.findByTenantIdAndAuthority(id, KaaAuthorityDto.TENANT_ADMIN.name());
                if (!users.isEmpty()) {
                    tenantAdminDto.setUserId(users.get(0).getId());
                    tenantAdminDto.setUsername(users.get(0).getUsername());
                    tenantAdminDto.setExternalUid(users.get(0).getExternalUid());
                }
            }
        }
        return tenantAdminDto;
    }

    @Override
    public List<UserDto> findAllTenantUsers(String tenantId) {
        if (isValidId(tenantId)) {
            return convertDtoList(userDao.findByTenantIdAndAuthorities(tenantId,
                    KaaAuthorityDto.TENANT_DEVELOPER.name(),
                    KaaAuthorityDto.TENANT_USER.name()));
        } else {
            return null;
        }
    }

}
