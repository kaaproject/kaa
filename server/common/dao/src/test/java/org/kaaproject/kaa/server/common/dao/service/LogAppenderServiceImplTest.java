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

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.model.sql.LogSchema;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class LogAppenderServiceImplTest extends AbstractTest {

    private ApplicationDto application;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() {
        application = generateApplication();
    }

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void findRegisteredLogAppendersByAppIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, LogAppenderTypeDto.FLUME, null);
        List<LogAppenderDto> appenders = logAppendersService.findRegisteredLogAppendersByAppId(application.getId());
        Assert.assertNotNull(appenders);
        Assert.assertEquals(1, appenders.size());
        Assert.assertEquals(appender, appenders.get(0));
    }

    @Test
    public void findAllAppendersByAppIdTest() {
        LogAppenderDto unreg = generateLogAppender(application.getId(), null, null, LogAppenderStatusDto.UNREGISTERED);
        Assert.assertNotNull(unreg);
        LogAppenderDto reg = generateLogAppender(application.getId(), null, null, LogAppenderStatusDto.REGISTERED);
        Assert.assertNotNull(reg);
        List<LogAppenderDto> appenders = logAppendersService.findAllAppendersByAppId(application.getId());
        Assert.assertNotNull(appenders);
        Assert.assertEquals(2, appenders.size());
        List<LogAppenderDto> regAppenders = logAppendersService.findRegisteredLogAppendersByAppId(application.getId());
        Assert.assertNotNull(regAppenders);
        Assert.assertEquals(1, regAppenders.size());
        Assert.assertEquals(reg, regAppenders.get(0));
    }

    @Test
    public void registerLogAppenderByAppIdAndAppenderIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null, LogAppenderStatusDto.UNREGISTERED);
        LogAppenderDto registered = logAppendersService.registerLogAppenderById(appender.getId());
        Assert.assertNotNull(registered);
        Assert.assertNotEquals(appender, registered);
        Assert.assertEquals(LogAppenderStatusDto.REGISTERED, registered.getStatus());
    }

    @Test
    public void unregisterLogAppenderByAppenderIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null, null);
        LogAppenderDto unreg = logAppendersService.unregisterLogAppenderById(appender.getId());
        Assert.assertNotNull(unreg);
        Assert.assertNotEquals(appender, unreg);
        Assert.assertEquals(LogAppenderStatusDto.UNREGISTERED, unreg.getStatus());
    }

    @Test
    public void findLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, LogAppenderTypeDto.FLUME, null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
    }

    @Test
    public void removeLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, LogAppenderTypeDto.FLUME, null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
        logAppendersService.removeLogAppenderById(appender.getId());
    }
}
