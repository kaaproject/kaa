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

package org.kaaproject.kaa.server.appenders.file.appender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.appenders.file.config.gen.FileConfig;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.RawData;
import org.kaaproject.kaa.server.common.core.configuration.RawDataFactory;
import org.kaaproject.kaa.server.common.core.schema.RawSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FileSystemLogAppenderTest {

    private static final String APPENDER_ID = "appender_id";
    private static final String APPLICATION_ID = "application_id";
    private static final String TENANT_ID = "tenant_id";
    private static final String APPENDER_NAME = "test";

    private FileSystemLogEventService fileSystemLogEventService;

    @Test
    public void testAppend() throws Exception {
        FileSystemLogAppender appender = new FileSystemLogAppender();
        appender.setName("test");
        FileSystemLogEventService service = Mockito.mock(FileSystemLogEventService.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", service);
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        LogAppenderDto logAppenderDto = prepareConfig();
        logAppenderDto.setApplicationId(APPLICATION_ID);
        logAppenderDto.setName("test");
        logAppenderDto.setTenantId(TENANT_ID);

        try {
            appender.init(logAppenderDto);

            ReflectionTestUtils.setField(appender, "header", Arrays.asList(LogHeaderStructureDto.values()));
            GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(
                    BasicEndpointProfile.SCHEMA$);
            BasicEndpointProfile theLog = new BasicEndpointProfile("test");

            LogSchemaDto schemaDto = new LogSchemaDto();
            LogSchema schema = new LogSchema(schemaDto, BasicEndpointProfile.SCHEMA$.toString());
            LogEvent logEvent = new LogEvent();

            logEvent.setLogData(converter.encode(theLog));
            EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", "endpointKey", 1, "", 0, null);
            BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, 1234567l, schema.getVersion(), Collections.singletonList(logEvent));
            logEventPack.setLogSchema(schema);
            TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
            appender.doAppend(logEventPack, callback);
            Assert.assertTrue(callback.success);
        } finally {
            appender.close();
        }
    }

    @Test
    public void appendToClosedAppenderTest() {
        FileSystemLogAppender appender = new FileSystemLogAppender();
        LogDeliveryCallback listener = mock(LogDeliveryCallback.class);
        ReflectionTestUtils.setField(appender, "closed", true);
        appender.doAppend(null, null, listener);
        verify(listener).onInternalError();
    }

    @Test
    public void testAppendWithInternalError() throws Exception {
        FileSystemLogAppender appender = new FileSystemLogAppender();
        appender.setName("test");
        FileSystemLogEventService service = Mockito.mock(FileSystemLogEventService.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", service);
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        LogAppenderDto logAppenderDto = prepareConfig();
        logAppenderDto.setApplicationId(APPLICATION_ID);
        logAppenderDto.setName("test");
        logAppenderDto.setTenantId(TENANT_ID);

        try {
            appender.init(logAppenderDto);

            ReflectionTestUtils.setField(appender, "header", Arrays.asList(LogHeaderStructureDto.values()));

            LogSchemaDto schemaDto = new LogSchemaDto();
            LogSchema schema = new LogSchema(schemaDto, BasicEndpointProfile.SCHEMA$.toString());
            LogEvent logEvent = new LogEvent();

            logEvent.setLogData(new byte[0]);
            EndpointProfileDataDto profileDto = new EndpointProfileDataDto("1", "endpointKey", 1, "", 0, null);
            BaseLogEventPack logEventPack = new BaseLogEventPack(profileDto, 1234567l, schema.getVersion(), Collections.singletonList(logEvent));
            logEventPack.setLogSchema(schema);
            TestLogDeliveryCallback callback = new TestLogDeliveryCallback();
            appender.doAppend(logEventPack, callback);
            Assert.assertTrue(callback.internallError);
        } finally {
            appender.close();
        }
    }

    @Test
    public void initTest() throws Exception {
        FileSystemLogAppender appender = new FileSystemLogAppender();
        fileSystemLogEventService = mock(FileSystemLogEventService.class);
        FileSystemLogger logger = Mockito.mock(FileSystemLogger.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", fileSystemLogEventService);
        ReflectionTestUtils.setField(appender, "logger", logger);
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        LogAppenderDto logAppenderDto = prepareConfig();
        logAppenderDto.setApplicationId(APPLICATION_ID);
        logAppenderDto.setName("test");
        logAppenderDto.setTenantId(TENANT_ID);

        try {
            appender.init(logAppenderDto);
            Assert.assertEquals(APPENDER_NAME, appender.getName());
            Assert.assertEquals(APPENDER_ID, appender.getAppenderId());
        } finally {
            appender.close();
        }
    }

    private LogAppenderDto prepareConfig() throws Exception {

        LogAppenderDto logAppenderDto = new LogAppenderDto();
        logAppenderDto.setApplicationId(APPLICATION_ID);
        logAppenderDto.setName("test");
        logAppenderDto.setTenantId(TENANT_ID);

        RawSchema rawSchema = new RawSchema(FileConfig.getClassSchema().toString());
        DefaultRecordGenerationAlgorithm<RawData> algotithm =
                new DefaultRecordGenerationAlgorithmImpl<>(rawSchema, new RawDataFactory());
        RawData rawData = algotithm.getRootData();
        AvroJsonConverter<FileConfig> converter =
                new AvroJsonConverter<>(FileConfig.getClassSchema(), FileConfig.class);
        FileConfig fileConfig = converter.decodeJson(rawData.getRawData());

        fileConfig.setLogsRootPath(System.getProperty("java.io.tmpdir") + File.separator + "tmp_logs_"+System.currentTimeMillis());

        AvroByteArrayConverter<FileConfig> byteConverter = new AvroByteArrayConverter<>(FileConfig.class);
        byte[] rawConfiguration = byteConverter.toByteArray(fileConfig);

        logAppenderDto.setRawConfiguration(rawConfiguration);

        return logAppenderDto;
    }

    private static class TestLogDeliveryCallback implements LogDeliveryCallback {

        private volatile boolean success;
        private volatile boolean internallError;

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
        }

        @Override
        public void onRemoteError() {
        }

    }
}
