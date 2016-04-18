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

package org.kaaproject.kaa.server.common.nosql.mongo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.dao.AbstractTest;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoEndpointUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/mongo-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointUserMongoDaoTest extends AbstractTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EndpointUserMongoDaoTest.class);

    protected static final String USER = "User";
    protected static final String PASSWORD = "Password";

    @Autowired
    private EndpointUserDao<MongoEndpointUser> endpointUserDao;

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
        MongoDataLoader.clearDBData();
    }

    @Test
    public void findById() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        MongoEndpointUser user = endpointUserDao.findById(dto.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals(dto, user.toDto());
    }

    @Test
    public void removeById() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        MongoEndpointUser user = endpointUserDao.findById(dto.getId());
        Assert.assertNotNull(user);

        endpointUserDao.removeById(user.getId());
        user = endpointUserDao.findById(user.getId());
        Assert.assertNull(user);
    }

    @Test
    public void findByExternalIdTest() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        Assert.assertNotNull(dto);
        MongoEndpointUser foundUser = endpointUserDao.findById(dto.getId());
        Assert.assertNotNull(foundUser);
        MongoEndpointUser found = endpointUserDao.findByExternalIdAndTenantId(foundUser.getExternalId(), foundUser.getTenantId().toString());
        Assert.assertEquals(foundUser, found);
    }

    @Test
    public void removeByExternalIdTest() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        Assert.assertNotNull(dto);
        MongoEndpointUser found = endpointUserDao.findById(dto.getId());
        Assert.assertNotNull(found);
        endpointUserDao.removeByExternalIdAndTenantId(found.getExternalId(), found.getTenantId());
        found = endpointUserDao.findById(dto.getId());
        Assert.assertNull(found);
    }

    @Test
    public void generateAccessTokenTest() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        Assert.assertNotNull(dto);
        String accessToken = endpointUserDao.generateAccessToken(dto.getExternalId(), dto.getTenantId().toString());
        MongoEndpointUser found = endpointUserDao.findById(dto.getId());
        Assert.assertEquals(accessToken, found.getAccessToken());
        Assert.assertTrue(endpointUserDao.checkAccessToken(found.getExternalId(), found.getTenantId().toString(), accessToken));
        Assert.assertFalse(endpointUserDao.checkAccessToken(found.getTenantId().toString(), found.getExternalId(), "invalid"));
    }

    @Test
    public void convertToDtoTest() {
        TenantDto tenant = generateTenantDto();
        EndpointUserDto dto = generateEndpointUserDto(tenant.getId());
        Assert.assertNotNull(dto);
        MongoEndpointUser user = endpointUserDao.findById(dto.getId());
        Assert.assertNotNull(user);
        dto = user.toDto();
        MongoEndpointUser converted = new MongoEndpointUser(dto);
        Assert.assertEquals(user, converted);
    }
}
