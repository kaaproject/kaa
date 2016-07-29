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

package org.kaaproject.kaa.server.operations.service.logs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.appenders.mongo.appender.LogEventDao;
import org.kaaproject.kaa.server.appenders.mongo.appender.MongoDbLogAppender;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.LogAppendersService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultLogAppenderServiceTest {

    private static final String APPENDER_NAME = "appender";

    private LogAppender mongoDBLogAppender;

    private Map<String, LogAppender> appenderMap;

    private DefaultLogAppenderBuilder logAppenderResolver;

    private static final String APPLICATION_ID = "application_id";
    private static final String APPENDER_ID = "appender_id";
    private static final String APPLICATION_TOKEN = "application_token";
    private static final String TENANT_ID = "tenant_id";

    private static final int LOG_SCHEMA_VERSION = 3;

    private LogAppenderService logAppenderService;
    private ApplicationService applicationService;
    private LogSchemaService logSchemaService;
    private LogEventDao logEventDao;
    private CTLService ctlService;

    private LogAppendersService logAppendersService;

    @Before
    public void beforeTest() throws IOException {
        mongoDBLogAppender = new MongoDbLogAppender();
        mongoDBLogAppender.setName(APPENDER_NAME);
        appenderMap = new HashMap<>();
        appenderMap.put(APPENDER_NAME, mongoDBLogAppender);
        logAppenderResolver = mock(DefaultLogAppenderBuilder.class);

        logAppenderService = new DefaultLogAppenderService();
        applicationService = mock(ApplicationService.class);
        logSchemaService = mock(LogSchemaService.class);
        ctlService = mock(CTLService.class);
        logEventDao = mock(LogEventDao.class);
        logAppendersService = mock(LogAppendersService.class);

        ReflectionTestUtils.setField(logAppenderService, "logSchemaService", logSchemaService);
        ReflectionTestUtils.setField(logAppenderService, "logAppenderResolver", logAppenderResolver);

        ReflectionTestUtils.setField(mongoDBLogAppender, "logEventDao", logEventDao);

        ReflectionTestUtils.setField(logAppenderService, "logAppendersService", logAppendersService);
    }

    @Test
    public void getApplicationAppendersNoAppendersTest() {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);

        when(applicationService.findAppById(APPLICATION_ID)).thenReturn(dto);

        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);

        Assert.assertEquals(0, appenders.size());
    }

    @Test
    public void getApplicationAppendersTest() throws ReflectiveOperationException {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);

        when(logAppendersService.findAllAppendersByAppId(APPLICATION_ID)).thenReturn(Arrays.asList(new LogAppenderDto()));
        when(logAppenderResolver.getAppender(any(LogAppenderDto.class))).thenReturn(mongoDBLogAppender);

        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);

        Assert.assertEquals(1, appenders.size());
        Assert.assertEquals(mongoDBLogAppender.getName(), appenders.get(0).getName());
    }
    
    @Test
    public void getErrorApplicationAppendersTest() throws ReflectiveOperationException {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);

        when(logAppendersService.findAllAppendersByAppId(APPLICATION_ID)).thenReturn(Arrays.asList(new LogAppenderDto()));
        when(logAppenderResolver.getAppender(any(LogAppenderDto.class))).thenThrow(new IllegalArgumentException());

        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);

        Assert.assertEquals(0, appenders.size());
    }

    @Test
    public void getApplicationAppendersWithErrorTest() {
        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);
        Assert.assertNotNull(appenders);
        Assert.assertEquals(0, appenders.size());
    }

    @Test
    public void getApplicationAppenderTest() throws ReflectiveOperationException {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);

        when(logAppendersService.findLogAppenderById(APPENDER_ID)).thenReturn(new LogAppenderDto());
        when(logAppenderResolver.getAppender(any(LogAppenderDto.class))).thenReturn(mongoDBLogAppender);

        LogAppender appender = logAppenderService.getApplicationAppender(APPENDER_ID);

        Assert.assertNotNull(appender);
        Assert.assertEquals(mongoDBLogAppender.getName(), appender.getName());
    }
    
    @Test
    public void getErrorApplicationAppenderTest() throws ReflectiveOperationException {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);

        when(logAppendersService.findLogAppenderById(APPENDER_ID)).thenReturn(new LogAppenderDto());
        when(logAppenderResolver.getAppender(any(LogAppenderDto.class))).thenThrow(new IllegalArgumentException());

        LogAppender appender = logAppenderService.getApplicationAppender(APPENDER_ID);

        Assert.assertNull(appender);
    }

    @Test
    public void getApplicationAppenderWithErrorTest() {
        LogAppender appender = logAppenderService.getApplicationAppender(APPENDER_ID);
        Assert.assertNull(appender);
    }

    @Test
    public void getLogSchemaTest() {
        LogSchemaDto dto = new LogSchemaDto();

        when(logSchemaService.findLogSchemaByAppIdAndVersion(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(dto);

        CTLSchemaDto ctlSchemaDto = new CTLSchemaDto();

        when(ctlService.findCTLSchemaById(dto.getCtlSchemaId())).thenReturn(ctlSchemaDto);

        LogSchema logSchema = new LogSchema(dto, ctlSchemaDto.getBody());

        Assert.assertEquals(dto, ReflectionTestUtils.getField(logSchema, "logSchemaDto"));
    }
}
