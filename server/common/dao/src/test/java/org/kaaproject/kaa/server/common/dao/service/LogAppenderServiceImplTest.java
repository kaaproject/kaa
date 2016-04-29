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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class LogAppenderServiceImplTest extends AbstractTest {

    private ApplicationDto application;

    @Before
    public void beforeTest() {
        application = generateApplicationDto();
    }

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void findAllAppendersByAppIdTest() {
        LogAppenderDto logAppender1 = generateLogAppenderDto(application.getId(), null);
        Assert.assertNotNull(logAppender1);
        LogAppenderDto logAppender2 = generateLogAppenderDto(application.getId(), null);
        Assert.assertNotNull(logAppender2);
        List<LogAppenderDto> appenders = logAppendersService.findAllAppendersByAppId(application.getId());
        Assert.assertNotNull(appenders);
        Assert.assertEquals(2, appenders.size());
    }

    @Test
    public void findLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppenderDto(application.getId(), null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
        Assert.assertArrayEquals(LogHeaderStructureDto.values(), found.getHeaderStructure().toArray(new LogHeaderStructureDto[]{}));
    }

    @Test
    public void removeLogAppenderByIdTest() {
        LogAppenderDto appender = generateLogAppenderDto(application.getId(), null);
        LogAppenderDto found = logAppendersService.findLogAppenderById(appender.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(appender, found);
        logAppendersService.removeLogAppenderById(appender.getId());
    }
}
