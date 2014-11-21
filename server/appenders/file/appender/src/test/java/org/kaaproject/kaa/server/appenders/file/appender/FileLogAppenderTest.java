package org.kaaproject.kaa.server.appenders.file.appender;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.dto.logs.avro.CustomAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.appenders.file.config.FileConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FileLogAppenderTest {

    private static final String APPENDER_ID = "appender_id";
    private static final String APPLICATION_ID = "application_id";
    private static final String TENANT_ID = "tenant_id";
    private static final String APPENDER_NAME = "test";

    private FileSystemLogEventService fileSystemLogEventService;

    @Test
    public void testAppend() throws IOException {
        FileLogAppender appender = new FileLogAppender();
        appender.setName("test");
        FileSystemLogEventService service = Mockito.mock(FileSystemLogEventService.class);
        FileSystemLogger logger = Mockito.mock(FileSystemLogger.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", service);
        ReflectionTestUtils.setField(appender, "logger", logger);
        ReflectionTestUtils.setField(appender, "header", Arrays.asList(LogHeaderStructureDto.values()));
        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");

        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        LogSchema schema = new LogSchema(schemaDto);
        LogEvent logEvent = new LogEvent();

        logEvent.setLogData(converter.encode(theLog));
        LogEventPack logEventPack = new LogEventPack("endpointKey", 1234567l, schema, Collections.singletonList(logEvent));
        appender.doAppend(logEventPack);

        Mockito.verify(logger).append(Mockito.anyString());
    }

    @Test
    public void initTest() throws IOException {
        FileLogAppender appender = new FileLogAppender();
        fileSystemLogEventService = mock(FileSystemLogEventService.class);
        FileSystemLogger logger = Mockito.mock(FileSystemLogger.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", fileSystemLogEventService);
        ReflectionTestUtils.setField(appender, "logger", logger);
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        LogAppenderDto logAppenderDto = prepateConfig();
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
    
    private LogAppenderDto prepateConfig() throws IOException {
        LogAppenderDto logAppenderDto = new LogAppenderDto();
        logAppenderDto.setApplicationId(APPLICATION_ID);
        logAppenderDto.setName("test");
        logAppenderDto.setTenantId(TENANT_ID);
        
        LogAppenderParametersDto parameters = new LogAppenderParametersDto();
        CustomAppenderParametersDto customParameters = new CustomAppenderParametersDto();
        FileConfig fileConfig = FileConfig.newBuilder().build();
        
        AvroByteArrayConverter<FileConfig> converter = new AvroByteArrayConverter<>(FileConfig.class);
        byte[] rawConfiguration = converter.toByteArray(fileConfig);
        
        customParameters.setRawConfiguration(rawConfiguration);
        parameters.setParameters(customParameters);
        
        logAppenderDto.setProperties(parameters);        
        return logAppenderDto;
    }

}
