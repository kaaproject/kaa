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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LogSchemaServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSchemaServiceImplTest.class);
    
    private static final String CUSTOMER_ID = "customer id";
    private static final String APPLICATION_NAME = "application name";
    
    private ApplicationDto applicationDto;
    private TenantDto customer;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        customer = new TenantDto();
        customer.setName(CUSTOMER_ID);
        customer = userService.saveTenant(customer);
        Assert.assertNotNull(customer);
        Assert.assertNotNull(customer.getId());

        ApplicationDto applicationObject = new ApplicationDto();
        applicationObject.setTenantId(customer.getId());
        applicationObject.setName(APPLICATION_NAME);
        this.applicationDto = applicationService.saveApp(applicationObject);
        Assert.assertNotNull(applicationDto);
        Assert.assertNotNull(applicationDto.getId());
    }

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void removeLogSchemaByIdTest() {
        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        
        Assert.assertEquals(1, schemas.size());
        
        logSchemaService.removeLogSchemaById(schemas.get(0).getId());
        
        schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        
        Assert.assertEquals(0, schemas.size());
    }
    
    @Test
    public void removeLogSchemasByAppIdTest() {
        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        
        Assert.assertEquals(1, schemas.size());
        
        logSchemaService.removeLogSchemasByAppId(applicationDto.getId());
        
        schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        
        Assert.assertEquals(0, schemas.size());
    }
    
    @Test
    public void findLogSchemaByIdTest() {
        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        
        Assert.assertEquals(1, schemas.size());
        
        LogSchemaDto dto = null;
        
        dto = logSchemaService.findLogSchemaById(schemas.get(0).getId());
        
        Assert.assertNotNull(dto);
    }
    
    @Test
    public void findLogSchemaVersionsByApplicationIdTest() {
        List<SchemaDto> schemas = logSchemaService.findLogSchemaVersionsByApplicationId(applicationDto.getId());
        
        Assert.assertEquals(1, schemas.size());
    }
}
