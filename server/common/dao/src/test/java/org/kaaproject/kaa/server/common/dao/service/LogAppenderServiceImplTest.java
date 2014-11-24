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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class LogAppenderServiceImplTest extends AbstractTest {

    private ApplicationDto application;

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
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null);
        List<LogAppenderDto> appenders = logAppendersService.findRegisteredLogAppendersByAppId(application.getId());
        Assert.assertNotNull(appenders);
        Assert.assertEquals(1, appenders.size());
        Assert.assertEquals(appender, appenders.get(0));
    }

    @Test
    public void findAllAppendersByAppIdTest() {
        LogAppenderDto unreg = generateLogAppender(application.getId(), null, LogAppenderStatusDto.UNREGISTERED);
        Assert.assertNotNull(unreg);
        LogAppenderDto reg = generateLogAppender(application.getId(), null, LogAppenderStatusDto.REGISTERED);
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
        LogAppenderDto appender = generateLogAppender(application.getId(), null, LogAppenderStatusDto.UNREGISTERED);
        LogAppenderDto registered = logAppendersService.registerLogAppenderById(appender.getId());
        Assert.assertNotNull(registered);
        Assert.assertNotEquals(appender, registered);
        Assert.assertEquals(LogAppenderStatusDto.REGISTERED, registered.getStatus());
    }

    @Test
    public void unregisterLogAppenderByAppenderIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null);
        LogAppenderDto unreg = logAppendersService.unregisterLogAppenderById(appender.getId());
        Assert.assertNotNull(unreg);
        Assert.assertNotEquals(appender, unreg);
        Assert.assertEquals(LogAppenderStatusDto.UNREGISTERED, unreg.getStatus());
    }

    @Test
    public void findLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
        Assert.assertArrayEquals(LogHeaderStructureDto.values(), found.getHeaderStructure().toArray(new LogHeaderStructureDto[]{}));
    }

    @Test
    public void removeLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppender(application.getId(), null, null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
        logAppendersService.removeLogAppenderById(appender.getId());
    }
}
