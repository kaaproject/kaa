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

package org.kaaproject.kaa.server.common.dao;


import java.util.List;

import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;

/**
 * The interface User service.
 */
public interface UserService {

    /**
     * Save tenant.
     *
     * @param tenantDto the tenant dto
     * @return the tenant dto
     */
    TenantDto saveTenant(TenantDto tenantDto);

    /**
     * Remove tenant by id.
     *
     * @param tenantId the tenant id
     */
    void removeTenantById(String tenantId);

    /**
     * Find tenant by name.
     *
     * @param name the name
     * @return the tenant dto
     */
    TenantDto findTenantByName(String name);

    /**
     * Find tenant by id.
     *
     * @param id the id
     * @return the tenant dto
     */
    TenantDto findTenantById(String id);

    /**
     * Save user.
     *
     * @param userDto the user dto
     * @return the user dto
     */
    UserDto saveUser(UserDto userDto);

    /**
     * Remove user by id.
     *
     * @param userId the user id
     */
    void removeUserById(String userId);

    /**
     * Find user by external Uid.
     *
     * @param externalUid the external user id
     * @return the user dto
     */
    UserDto findUserByExternalUid(String externalUid);

    /**
     * Find user by id.
     *
     * @param id the id
     * @return the user dto
     */
    UserDto findUserById(String id);



    /**
     * remove tenant admin.
     *
     * @param tenantId the tenant id
     */
    void removeTenantAdminById(String tenantId);

    /**
     * Find all tenant admin by tenant id.
     *
     * @param tenantId the tenant id
     * @return the UserDto list
     */
    List<UserDto> findAllTenantAdminsByTenantId(String tenantId);

    /**
     * Find all tenants.
     *
     * @return the list of tenants
     */
    List<TenantDto> findAllTenants();

    /**
     * Find all users.
     *
     * @return the list of users.
     */
    List<UserDto> findAllUsers();


    /**
     * Find all tenant users.
     *
     * @param tenantId the id of tenant the users belong to
     * @return the list of tenant users
     */
    List<UserDto> findAllTenantUsers(String tenantId);
}
