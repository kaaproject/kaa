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

package org.kaaproject.kaa.server.common.dao.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class UserServiceImplTest extends AbstractTest {

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findAllTenantUsersTest() {
        TenantDto tenant = generateTenantDto();
        List<UserDto> expectedUsers = new ArrayList<>();
        List<UserDto> devUsers = generateUsersDto(tenant.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 2);
        List<UserDto> users = generateUsersDto(tenant.getId(), KaaAuthorityDto.TENANT_USER, 3);
        expectedUsers.addAll(devUsers);
        expectedUsers.addAll(users);
        List<UserDto> foundUsers = userService.findAllTenantUsers(tenant.getId());
        assertUsersListsEqual(expectedUsers, foundUsers);
    }

    private void assertUsersListsEqual(List<UserDto> expectedUsers, List<UserDto> actualUsers) {
        Assert.assertNotNull(expectedUsers);
        Assert.assertNotNull(actualUsers);
        Assert.assertEquals(expectedUsers.size(), actualUsers.size());
        List<UserDto> notMatchedUsers = new ArrayList<>(actualUsers);
        for (UserDto expectedUser : expectedUsers) {
            boolean found = false;
            for (UserDto actualUser : actualUsers) {
                if (expectedUser.getId().equals(actualUser.getId())) {
                    Assert.assertEquals(expectedUser, actualUser);
                    found = true;
                    notMatchedUsers.remove(actualUser);
                }
            }
            Assert.assertTrue("User not found", found);
        }
        Assert.assertTrue(notMatchedUsers.isEmpty());
    }

    @Test
    public void saveTenantTest() {
        TenantDto tenant = generateTenantDto();
        TenantDto savedTenant = userService.findTenantById(tenant.getId());
        Assert.assertEquals(tenant, savedTenant);
    }

    @Test
    public void removeTenantByIdTest() {
        //TODO : implmenet me
    }

    @Test
    public void findTenantByNameTest() {
        TenantDto tenantDto = generateTenantDto();
        TenantDto foundTenant = userService.findTenantByName(tenantDto.getName());
        Assert.assertEquals(tenantDto, foundTenant);
    }

    @Test
    public void saveUserTest() {
        TenantDto tenantDto = generateTenantDto();
        List<UserDto> users = generateUsersDto(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        UserDto foundUser = userService.findUserById(user.getId());
        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void removeUserByIdTest() {
        TenantDto tenantDto = generateTenantDto();
        List<UserDto> users = generateUsersDto(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        userService.removeUserById(user.getId());
        UserDto foundUser = userService.findUserById(user.getId());
        Assert.assertNull(foundUser);
    }

    @Test
    public void findUserByExternalUidTest() {
        TenantDto tenantDto = generateTenantDto();
        List<UserDto> users = generateUsersDto(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        UserDto foundUser = userService.findUserByExternalUid(user.getExternalUid());
        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void findAllTenantsTest() {
        TenantDto tenantDto = generateTenantDto();
        List<TenantDto> saved = new ArrayList<>(1);
        saved.add(tenantDto);
        List<TenantDto> tenants = userService.findAllTenants();
        Assert.assertEquals(saved, tenants);
    }

    @Test
    public void findAllUsersTest() {
        TenantDto tenantDto = generateTenantDto();
        List<UserDto> users = generateUsersDto(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 7);
        List<UserDto> foundUsers = userService.findAllUsers();
        assertUsersListsEqual(users, foundUsers);
    }



    @Test
    public void removeTenantAdminByIdTest() {
        UserDto tenantAdminDto = generateTenantAdmin(null, null);
        userService.removeTenantAdminById(tenantAdminDto.getId());
        UserDto found = userService.findUserById(tenantAdminDto.getId());
        Assert.assertNull(found);
    }

    @Test
    public void findTenantAdminByIdTest() {
        UserDto tenantAdminDto = generateTenantAdmin(null, null);
        UserDto found  = userService.findUserById(tenantAdminDto.getId());
        Assert.assertEquals(tenantAdminDto, found);
    }

}
