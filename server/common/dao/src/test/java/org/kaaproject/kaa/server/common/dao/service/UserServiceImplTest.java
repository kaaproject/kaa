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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.common.dao.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UserServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImplTest.class);

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test
    public void findAllTenantUsersTest() {
        TenantDto tenant = generateTenant();
        List<UserDto> expectedUsers = new ArrayList();
        List<UserDto> devUsers = generateUsers(tenant.getId(), KaaAuthorityDto.TENANT_DEVELOPER, 2);
        List<UserDto> users = generateUsers(tenant.getId(), KaaAuthorityDto.TENANT_USER, 3);
        expectedUsers.addAll(devUsers);
        expectedUsers.addAll(users);
        List<UserDto> foundUsers = userService.findAllTenantUsers(tenant.getId());
        Assert.assertEquals(expectedUsers, foundUsers);
    }

    @Test
    public void saveTenantTest() {
        TenantDto tenant = generateTenant();
        TenantDto savedTenant = userService.findTenantById(tenant.getId());
        Assert.assertEquals(tenant, savedTenant);
    }

    @Test
    public void removeTenantByIdTest() {
    }

    @Test
    public void findTenantByNameTest() {
        TenantDto tenantDto = generateTenant();
        TenantDto foundTenant = userService.findTenantByName(TENANT_NAME);
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
        Assert.assertEquals(users, foundUsers);
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
