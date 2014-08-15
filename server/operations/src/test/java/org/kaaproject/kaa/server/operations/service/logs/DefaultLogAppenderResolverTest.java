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
import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.operations.service.logs.mongo.MongoDBLogAppender;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultLogAppenderResolverTest {

    private static final String APPENDER_NAME = "appender";

    private static final MongoDBLogAppender MONGO_DB_LOG_APPENDER = new MongoDBLogAppender(APPENDER_NAME);


    private static final DefaultLogAppenderBuilder DEFAULT_LOG_APPENDER_RESOLVER = new DefaultLogAppenderBuilder();
    private LogAppenderDto dto = null;
    private ApplicationContext applicationContext;
    private LogEventService logEventService;;

    @Before
    public void init() {
        applicationContext = mock(ApplicationContext.class);
        logEventService = mock(LogEventService.class);

        ReflectionTestUtils.setField(DEFAULT_LOG_APPENDER_RESOLVER, "applicationContext", applicationContext);
        ReflectionTestUtils.setField(MONGO_DB_LOG_APPENDER, "logEventService", logEventService);

        dto = new LogAppenderDto();
        dto.setApplicationId("1");
        dto.setTenantId("2");
        dto.setName(APPENDER_NAME);
        dto.setSchema(new SchemaDto("12", 1, 0));
        dto.setStatus(LogAppenderStatusDto.REGISTERED);
        dto.setType(LogAppenderTypeDto.MONGO);
    }

    @Test
    public void getAppenderTest() {
        when(applicationContext.getBean(MongoDBLogAppender.class)).thenReturn(MONGO_DB_LOG_APPENDER);
        LogAppender logAppender = DEFAULT_LOG_APPENDER_RESOLVER.getAppender(dto);
        Assert.assertEquals(APPENDER_NAME, logAppender.getName());
    }

    @Test
    public void resolveNoSuchAppenderTest() {
        LogAppender logAppender = DEFAULT_LOG_APPENDER_RESOLVER.getAppender(null);
        Assert.assertNull(logAppender);
    }
}
