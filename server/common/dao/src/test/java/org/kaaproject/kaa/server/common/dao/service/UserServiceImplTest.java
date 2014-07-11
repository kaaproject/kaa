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

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserServiceImplTest extends AbstractTest {

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws Exception {
        clearDBData();
    }

    @Test
    public void findAllTenantUsersTest() {
        TenantDto tenant = generateTenant();
        List<UserDto> expectedUsers = new ArrayList<>();
        List<UserDto> devUsers = generateUsers(tenant.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 2);
        List<UserDto> users = generateUsers(tenant.getId(), KaaAuthorityDto.TENANT_USER, 3);
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
        TenantDto tenant = generateTenant();
        TenantDto savedTenant = userService.findTenantById(tenant.getId());
        Assert.assertEquals(tenant, savedTenant);
    }

    @Test
    public void removeTenantByIdTest() {
        //TODO : implmenet me
    }

    @Test
    public void findTenantByNameTest() {
        TenantDto tenantDto = generateTenant();
        TenantDto foundTenant = userService.findTenantByName(tenantDto.getName());
        Assert.assertEquals(tenantDto, foundTenant);
    }

    @Test
    public void saveUserTest() {
        TenantDto tenantDto = generateTenant();
        List<UserDto> users = generateUsers(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        UserDto foundUser = userService.findUserById(user.getId());
        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void removeUserByIdTest() {
        TenantDto tenantDto = generateTenant();
        List<UserDto> users = generateUsers(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        userService.removeUserById(user.getId());
        UserDto foundUser = userService.findUserById(user.getId());
        Assert.assertNull(foundUser);
    }

    @Test
    public void findUserByExternalUidTest() {
        TenantDto tenantDto = generateTenant();
        List<UserDto> users = generateUsers(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 1);
        Assert.assertFalse(users.isEmpty());
        UserDto user = users.get(0);
        UserDto foundUser = userService.findUserByExternalUid(user.getExternalUid());
        Assert.assertEquals(user, foundUser);
    }

    @Test
    public void findAllTenantsTest() {
        TenantDto tenantDto = generateTenant();
        List<TenantDto> saved = new ArrayList<>(1);
        saved.add(tenantDto);
        List<TenantDto> tenants = userService.findAllTenants();
        Assert.assertEquals(saved, tenants);
    }

    @Test
    public void findAllUsersTest() {
        TenantDto tenantDto = generateTenant();
        List<UserDto> users = generateUsers(tenantDto.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 7);
        List<UserDto> foundUsers = userService.findAllUsers();
        assertUsersListsEqual(users, foundUsers);
    }


    @Test
    public void findAllTenantAdminsTest() {
        TenantAdminDto tenantAdminDto = generateTenantAdmin(null, null);
        List<TenantAdminDto> admins = userService.findAllTenantAdmins();
        Assert.assertEquals(1, admins.size());
        Assert.assertEquals(tenantAdminDto, admins.get(0));
    }

    @Test
    public void removeTenantAdminByIdTest() {
        TenantAdminDto tenantAdminDto = generateTenantAdmin(null, null);
        userService.removeTenantAdminById(tenantAdminDto.getId());
        TenantAdminDto found = userService.findTenantAdminById(tenantAdminDto.getId());
        Assert.assertNull(found);
    }

    @Test
    public void findTenantAdminByIdTest() {
        TenantAdminDto tenantAdminDto = generateTenantAdmin(null, null);
        TenantAdminDto found = userService.findTenantAdminById(tenantAdminDto.getId());
        Assert.assertEquals(tenantAdminDto, found);
    }

}
