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

package org.kaaproject.kaa.server.operations.service.logs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.logs.mongo.MongoDBLogAppender;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultLogAppenderServiceTest {
    
    private static final String APPENDER_NAME = "appender";
    
    private MongoDBLogAppender mongoDBLogAppender;
    
    private Map<String, LogAppender> appenderMap;
    
    private DefaultLogAppenderResolver logAppenderResolver;
    
    private static final String APPLICATION_ID = "application_id";
    private static final String APPLICATION_TOKEN = "application_token";
    private static final String TENANT_ID = "tenant_id";
    
    private static final int LOG_SCHEMA_VERSION = 3;
    
    private LogAppenderService logAppenderService;
    private ApplicationService applicationService;
    private LogSchemaService logSchemaService;
    private LogEventService logEventService;
    
    @Before
    public void beforeTest() throws IOException {
        mongoDBLogAppender = new MongoDBLogAppender(APPENDER_NAME);
        appenderMap = new HashMap<>();
        appenderMap.put(APPENDER_NAME, mongoDBLogAppender);
        logAppenderResolver = new DefaultLogAppenderResolver(appenderMap);
        
        logAppenderService = new DefaultLogAppenderService();
        applicationService = mock(ApplicationService.class);
        logSchemaService = mock(LogSchemaService.class);
        logEventService = mock(LogEventService.class);
        ReflectionTestUtils.setField(logAppenderService, "applicationService", applicationService);
        ReflectionTestUtils.setField(logAppenderService, "logSchemaService", logSchemaService);
        ReflectionTestUtils.setField(logAppenderService, "logAppenderResolver", logAppenderResolver);
        
        ReflectionTestUtils.setField(mongoDBLogAppender, "applicationService", applicationService);
        ReflectionTestUtils.setField(mongoDBLogAppender, "logEventService", logEventService);
    }
    
    @Test
    public void getApplicationAppendersNoAppendersTest() {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);
        dto.setLogAppendersNames(null);
        
        when(applicationService.findAppById(APPLICATION_ID)).thenReturn(dto);
        
        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);
        
        Assert.assertEquals(0, appenders.size());
    }
    
    @Test
    public void getApplicationAppendersTest() {
        ApplicationDto dto = new ApplicationDto();
        dto.setApplicationToken(APPLICATION_TOKEN);
        dto.setTenantId(TENANT_ID);
        dto.setLogAppendersNames(APPENDER_NAME);
        
        when(applicationService.findAppById(APPLICATION_ID)).thenReturn(dto);
        
        List<LogAppender> appenders = logAppenderService.getApplicationAppenders(APPLICATION_ID);
        
        Assert.assertEquals(1, appenders.size());
        Assert.assertEquals(mongoDBLogAppender.getName(), appenders.get(0).getName());
    }
    
    @Test
    public void getLogSchemaTest() {
        LogSchemaDto dto = new LogSchemaDto();
        
        when(logSchemaService.findLogSchemaByAppIdAndVersion(APPLICATION_ID, LOG_SCHEMA_VERSION)).thenReturn(dto);
        
        LogSchema logSchema = logAppenderService.getLogSchema(APPLICATION_ID, LOG_SCHEMA_VERSION);
        
        Assert.assertEquals(dto, ReflectionTestUtils.getField(logSchema, "logSchemaDto"));
    }
}
