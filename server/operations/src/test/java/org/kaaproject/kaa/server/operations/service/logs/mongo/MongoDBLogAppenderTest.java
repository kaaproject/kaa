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

package org.kaaproject.kaa.server.operations.service.logs.mongo;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.common.dao.LogSchemaService;
import org.kaaproject.kaa.server.common.dao.UserService;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.LogSchema;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-test-context.xml")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class MongoDBLogAppenderTest extends AbstractTest{

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String APPLICATION_ID = "application_id";
    private static final String APPLICATION_TOKEN = "application_token";
    private static final String TENANT_ID = "tenant_id";
    private static final String CUSTOMER_ID = "customer id";
    private static final String APPLICATION_NAME = "application name";
    private static final String NEW_APPENDER_NAME = "new name";
    private static final String ENDPOINT_KEY = "endpoint key";
    private static final String LOG_DATA = "null";
    private static final long DATE_CREATED = System.currentTimeMillis();

    private ApplicationDto applicationDto;
    private TenantDto customer;

    private LogAppender logAppender;

    @Autowired
    private LogAppenderService logAppenderService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogSchemaService logSchemaService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogEventService logEventService;

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @After
    public void after() throws Exception {
        clearDBData();
    }

    @Before
    public void beforeTest() {
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
        logAppender = new MongoDBLogAppender();
        ReflectionTestUtils.setField(logAppender, "logEventService", logEventService);
        ReflectionTestUtils.setField(logAppender, "applicationService", applicationService);

        LogAppenderDto appenderDto = new LogAppenderDto();
        appenderDto.setApplicationId(APPLICATION_ID);
        appenderDto.setApplicationToken(APPLICATION_TOKEN);
        appenderDto.setTenantId(TENANT_ID);
        appenderDto.setType(LogAppenderTypeDto.MONGO);
        appenderDto.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.values()));

        logAppender.init(appenderDto);
    }

    @Test
    public void changeAppenderNameTest() {
        String oldName = logAppender.getName();
        logAppender.setName(NEW_APPENDER_NAME);

        Assert.assertNotEquals(oldName, logAppender.getName());
        Assert.assertEquals(NEW_APPENDER_NAME, logAppender.getName());
    }

    @Test
    public void closeAppenderTest() {
        Assert.assertFalse((boolean) ReflectionTestUtils.getField(logAppender, "closed"));
        logAppender.close();
        Assert.assertTrue((boolean) ReflectionTestUtils.getField(logAppender, "closed"));
    }

    @Test
    public void getConverterTest() {
        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());
        String schema = schemas.get(0).getSchema();
        Assert.assertTrue(((Map) ReflectionTestUtils.getField(logAppender, "converters")).isEmpty());
        GenericAvroConverter<GenericRecord> converter1 = ReflectionTestUtils.invokeMethod(logAppender, "getConverter", schema);
        Assert.assertEquals(1, ((Map) ReflectionTestUtils.getField(logAppender, "converters")).size());
        GenericAvroConverter<GenericRecord> converter2 = ReflectionTestUtils.invokeMethod(logAppender, "getConverter", schema);
        Assert.assertEquals(1, ((Map) ReflectionTestUtils.getField(logAppender, "converters")).size());
        Assert.assertEquals(converter1, converter2);
    }

    @Test //Not throws NullPointerException
    public void doAppendClosedTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Logger testLogger = Mockito.mock(Logger.class);

        Field field = logAppender.getClass().getDeclaredField("LOG");

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, testLogger);

        logAppender.close();
        logAppender.doAppend(new LogEventPack());

        Mockito.verify(testLogger).info(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void doAppendWithCatchIOExceptionTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<LogEvent> events = new ArrayList<>();
        LogEvent event1 = new LogEvent();
        event1.setLogData(LOG_DATA.getBytes(UTF_8));
        LogEvent event2 = new LogEvent();
        event1.setLogData(LOG_DATA.getBytes(UTF_8));
        LogEvent event3 = new LogEvent();
        event1.setLogData(LOG_DATA.getBytes(UTF_8));
        events.add(event1);
        events.add(event2);
        events.add(event3);

        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());

        LogSchemaDto dto = schemas.get(0);
        LogSchema schema = new LogSchema(dto);
        int version = dto.getMajorVersion();

        LogEventPack logEventPack = new LogEventPack(ENDPOINT_KEY, DATE_CREATED, schema, events);
        logEventPack.setLogSchemaVersion(version);

        Map<String, GenericAvroConverter<GenericRecord>> converters = new HashMap<>();

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(dto.getSchema()) {

            @Override
            public GenericRecord decodeBinary(byte[] bytes) throws IOException {
                throw new IOException();
            }

            @Override
            public String endcodeToJson(GenericRecord record) throws IOException {
                throw new IOException();
            }
        };

        converters.put(dto.getSchema(), converter);
        ReflectionTestUtils.setField(logAppender, "converters", converters);
        LogEventService logEventService = Mockito.mock(LogEventService.class);

        LogEventService eventService = (LogEventService) ReflectionTestUtils.getField(logAppender, "logEventService");
        ReflectionTestUtils.setField(logAppender, "logEventService", logEventService);
        logAppender.doAppend(logEventPack);
        Mockito.verify(logEventService, Mockito.never()).save(Mockito.anyList(), Mockito.anyString());
        ReflectionTestUtils.setField(logAppender, "logEventService", eventService);
    }

    @Test
    public void doAppendTest() {
        List<LogEvent> events = new ArrayList<>();
        LogEvent event1 = new LogEvent();
        event1.setLogData(LOG_DATA.getBytes(UTF_8));
        LogEvent event2 = new LogEvent();
        event2.setLogData(LOG_DATA.getBytes(UTF_8));
        LogEvent event3 = new LogEvent();
        event3.setLogData(LOG_DATA.getBytes(UTF_8));
        events.add(event1);
        events.add(event2);
        events.add(event3);
        List<LogSchemaDto> schemas = logSchemaService.findLogSchemasByAppId(applicationDto.getId());

        LogSchemaDto dto = schemas.get(0);
        LogSchema schema = new LogSchema(dto);
        int version = dto.getMajorVersion();

        LogEventPack logEventPack = new LogEventPack(ENDPOINT_KEY, DATE_CREATED, schema, events);
        logEventPack.setLogSchemaVersion(version);

        Map<String, GenericAvroConverter<GenericRecord>> converters = new HashMap<>();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(dto.getSchema()) {

            @Override
            public GenericRecord decodeBinary(byte[] bytes) {
                return null;
            }

            @Override
            public String endcodeToJson(GenericRecord record) {
                return LOG_DATA;
            }
        };

        converters.put(dto.getSchema(), converter);
        ReflectionTestUtils.setField(logAppender, "converters", converters);


        String collectionName = (String) ReflectionTestUtils.getField(logAppender, "collectionName");
        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection(collectionName).count());
        logAppender.doAppend(logEventPack);
        collectionName = (String) ReflectionTestUtils.getField(logAppender, "collectionName");
        Assert.assertEquals(3, MongoDBTestRunner.getDB().getCollection(collectionName).count());
    }
}
