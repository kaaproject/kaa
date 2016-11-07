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

package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.avro.generic.GenericRecord;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDBCredential;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDbConfig;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDbServer;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseSchemaInfo;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoDaoUtil;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;

public class MongoDBLogAppenderTest {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBLogAppenderTest.class);

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String APPLICATION_ID = "application_id";
    private static final String APPLICATION_TOKEN = "application_token";
    private static final String TENANT_ID = "tenant_id";
    private static final String NEW_APPENDER_NAME = "new name";
    private static final String ENDPOINT_KEY = "endpoint key";
    private static final String EMPTY_SCHEMA = "{" + "\"type\": \"record\"," + "\"name\": \"Log\","
            + "\"namespace\": \"org.kaaproject.kaa.schema.base\"," + "\"fields\": []" + "}";
    private static final String LOG_DATA = "null";
    private static final long DATE_CREATED = System.currentTimeMillis();

    private static final String SERVER_PROFILE_SCHEMA_FILE = "server_profile_schema.avsc";
    private static final String SERVER_PROFILE_CONTENT_FILE = "server_profile_content.json";

    // According to the server profile schema file
    private static final String SERVER_FIELD_KEY = "country．＄";

    // According to the server profile content file
    private static final String SERVER_FIELD_VALUE = "1.0.$.";

    private static final String SERVER_PROFILE = "serverProfile";

    private LogAppender logAppender;

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
        LOG.info("Deleting data from MongoDB database");
        DB db = MongoDBTestRunner.getDB();
        if (db != null) {
            db.dropDatabase();
        }
    }

    @Before
    public void beforeTest() throws Exception {
        // Do not include client and server profiles by default
        this.initLogAppender(false, false);
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
        String schema = EMPTY_SCHEMA;
        Assert.assertTrue(((Map) ReflectionTestUtils.getField(logAppender, "converters")).isEmpty());
        GenericAvroConverter<GenericRecord> converter1 = ReflectionTestUtils.invokeMethod(logAppender, "getConverter", schema);
        Assert.assertEquals(1, ((Map) ReflectionTestUtils.getField(logAppender, "converters")).size());
        GenericAvroConverter<GenericRecord> converter2 = ReflectionTestUtils.invokeMethod(logAppender, "getConverter", schema);
        Assert.assertEquals(1, ((Map) ReflectionTestUtils.getField(logAppender, "converters")).size());
        Assert.assertEquals(converter1, converter2);
    }

    @Test
    // Not throws NullPointerException
    public void doAppendClosedTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Logger testLogger = Mockito.mock(Logger.class);

        Field field = logAppender.getClass().getDeclaredField("LOG");

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, testLogger);

        logAppender.close();

        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, 1, null);

        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setVersion(1);
        LogSchema schema = new LogSchema(schemaDto, BasicEndpointProfile.SCHEMA$.toString());
        logEventPack.setLogSchema(schema);

        logAppender.doAppend(logEventPack, callback);

        Assert.assertTrue(callback.internallError);
    }

    @Test
    public void doAppendWithCatchIOExceptionTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, IOException {
        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");
        List<LogEvent> events = new ArrayList<>();
        LogEvent event1 = new LogEvent();
        event1.setLogData(new byte[0]);
        LogEvent event2 = new LogEvent();
        event2.setLogData(converter.encode(theLog));
        LogEvent event3 = new LogEvent();
        event3.setLogData(converter.encode(theLog));
        events.add(event1);
        events.add(event2);
        events.add(event3);

        LogSchemaDto schemaDto = new LogSchemaDto();
        LogSchema schema = new LogSchema(schemaDto, BasicEndpointProfile.SCHEMA$.toString());

        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, schema.getVersion(), events);
        logEventPack.setLogSchema(schema);

        LogEventDao logEventDao = Mockito.mock(LogEventDao.class);

        LogEventDao eventDao = (LogEventDao) ReflectionTestUtils.getField(logAppender, "logEventDao");
        ReflectionTestUtils.setField(logAppender, "logEventDao", logEventDao);

        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        logAppender.doAppend(logEventPack, callback);
        Assert.assertTrue(callback.internallError);
        Mockito.verify(logEventDao, Mockito.never()).save(Mockito.anyList(), Mockito.<ProfileInfo>anyObject(), Mockito.<ProfileInfo>anyObject(), Mockito.anyString());
        ReflectionTestUtils.setField(logAppender, "logEventDao", eventDao);
    }

    @Test
    public void doAppendWithoutServerProfileTest() throws IOException {
        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");
        List<LogEvent> events = new ArrayList<>();
        LogEvent event1 = new LogEvent();
        event1.setLogData(converter.encode(theLog));
        LogEvent event2 = new LogEvent();
        event2.setLogData(converter.encode(theLog));
        LogEvent event3 = new LogEvent();
        event3.setLogData(converter.encode(theLog));
        events.add(event1);
        events.add(event2);
        events.add(event3);

        LogSchemaDto schemaDto = new LogSchemaDto();
        LogSchema schema = new LogSchema(schemaDto, BasicEndpointProfile.SCHEMA$.toString());

        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, schema.getVersion(), events);
        logEventPack.setLogSchema(schema);

        String collectionName = (String) ReflectionTestUtils.getField(logAppender, "collectionName");
        Assert.assertEquals(0, MongoDBTestRunner.getDB().getCollection(collectionName).count());
        TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
        logAppender.doAppend(logEventPack, callback);
        Assert.assertTrue(callback.success);
        collectionName = (String) ReflectionTestUtils.getField(logAppender, "collectionName");
        Assert.assertEquals(3, MongoDBTestRunner.getDB().getCollection(collectionName).count());
    }

    @Test
    public void doAppendWithServerProfileTest() throws Exception {
        // Reinitilize the log appender to include server profile data
        this.initLogAppender(false, true);

        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile log = new BasicEndpointProfile("body");
        List<LogEvent> logEvents = new ArrayList<>();

        LogEvent alpha = new LogEvent();
        alpha.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogEvent beta = new LogEvent();
        beta.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogEvent gamma = new LogEvent();
        gamma.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        LogSchema logSchema = new LogSchema(logSchemaDto, BasicEndpointProfile.SCHEMA$.toString());

        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, logSchema.getVersion(), logEvents);
        logEventPack.setLogSchema(logSchema);

        // Add server profile data
        BaseSchemaInfo schemaInfo = new BaseSchemaInfo(Integer.toString(new Random().nextInt()), this.getResourceAsString(SERVER_PROFILE_SCHEMA_FILE));
        String body = this.getResourceAsString(SERVER_PROFILE_CONTENT_FILE);
        logEventPack.setServerProfile(new BaseProfileInfo(schemaInfo, body));

        this.logAppender.doAppend(logEventPack, new TestLogDeliveryCallback());
        String collectionName = (String) ReflectionTestUtils.getField(this.logAppender, "collectionName");
        DBObject serverProfile = (DBObject) MongoDBTestRunner.getDB().getCollection(collectionName).findOne().get(SERVER_PROFILE);
        DBObject profile = (DBObject) serverProfile.get("Profile");
        DBObject profileNamespace = (DBObject) profile.get("org．kaaproject．kaa．schema．sample．profile");
        Assert.assertEquals(SERVER_FIELD_VALUE, profileNamespace.get(SERVER_FIELD_KEY));
    }

    @Test
    public void doAppendWithEmptyServerProfileTest() throws Exception {
        // Reinitilize the log appender to include server profile data
        this.initLogAppender(false, true);

        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile log = new BasicEndpointProfile("body");
        List<LogEvent> logEvents = new ArrayList<>();

        LogEvent alpha = new LogEvent();
        alpha.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogEvent beta = new LogEvent();
        beta.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogEvent gamma = new LogEvent();
        gamma.setLogData(converter.encode(log));
        logEvents.add(alpha);

        LogSchemaDto logSchemaDto = new LogSchemaDto();
        LogSchema logSchema = new LogSchema(logSchemaDto, BasicEndpointProfile.SCHEMA$.toString());

        EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", ENDPOINT_KEY, 1, "", 0, null);
        BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, DATE_CREATED, logSchema.getVersion(), logEvents);
        logEventPack.setLogSchema(logSchema);

        this.logAppender.doAppend(logEventPack, new TestLogDeliveryCallback());
        String collectionName = (String) ReflectionTestUtils.getField(this.logAppender, "collectionName");
        DBObject serverProfile = (DBObject) MongoDBTestRunner.getDB().getCollection(collectionName).findOne().get(SERVER_PROFILE);
        Assert.assertEquals(null, serverProfile);
    }

    private static class TestLogDeliveryCallback implements LogDeliveryCallback {

        private volatile boolean success;
        private volatile boolean internallError;
        private volatile boolean connectionError;
        private volatile boolean remoteError;

        @Override
        public void onSuccess() {
            success = true;
        }

        @Override
        public void onInternalError() {
            internallError = true;
        }

        @Override
        public void onConnectionError() {
            connectionError = true;
        }

        @Override
        public void onRemoteError() {
            remoteError = true;
        }

    }

    private void initLogAppender(boolean includeClientProfile, boolean includeServerProfile) throws Exception {
        logAppender = new MongoDbLogAppender();

        LogAppenderDto appenderDto = new LogAppenderDto();
        appenderDto.setApplicationId(APPLICATION_ID);
        appenderDto.setApplicationToken(APPLICATION_TOKEN);
        appenderDto.setTenantId(TENANT_ID);
        appenderDto.setHeaderStructure(Arrays.asList(LogHeaderStructureDto.values()));

        String dbName = MongoDBTestRunner.getDB().getName();
        List<ServerAddress> serverAddresses = MongoDBTestRunner.getDB().getMongo().getServerAddressList();
        List<MongoDbServer> servers = new ArrayList<>();
        for (ServerAddress serverAddress : serverAddresses) {
            servers.add(new MongoDbServer(serverAddress.getHost(), serverAddress.getPort()));
        }
        List<MongoDBCredential> credentials = new ArrayList<>();

        RawSchema rawSchema = new RawSchema(MongoDbConfig.getClassSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm = new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        AvroJsonConverter<MongoDbConfig> converter = new AvroJsonConverter<>(MongoDbConfig.getClassSchema(), MongoDbConfig.class);
        MongoDbConfig mongoDbConfig = converter.decodeJson(rawData.getRawData());

        mongoDbConfig.setMongoServers(servers);
        mongoDbConfig.setMongoCredentials(credentials);
        mongoDbConfig.setDbName(dbName);
        mongoDbConfig.setIncludeClientProfile(includeClientProfile);
        mongoDbConfig.setIncludeServerProfile(includeServerProfile);

        AvroByteArrayConverter<MongoDbConfig> byteConverter = new AvroByteArrayConverter<>(MongoDbConfig.class);
        byte[] rawConfiguration = byteConverter.toByteArray(mongoDbConfig);

        appenderDto.setRawConfiguration(rawConfiguration);

        logAppender.init(appenderDto);
    }

    protected String getResourceAsString(String path) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        File file = new File(url.getPath());
        String result;
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            result = sb.toString();
        } finally {
            br.close();
        }
        return result;
    }
}
