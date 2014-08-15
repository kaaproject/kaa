package org.kaaproject.kaa.server.operations.service.logs.filesystem;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.LogSchema;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FileSystemLogAppenderTest {

    private static final String APPENDER_ID = "appender_id";
    private static final String APPLICATION_ID = "application_id";
    private static final String TENANT_ID = "tenant_id";
    private static final String APPENDER_NAME = "test";

    private FileSystemLogEventService fileSystemLogEventService;

    @Test
    public void testAppend() throws IOException {
        FileSystemLogAppender appender = new FileSystemLogAppender("test");
        FileSystemLogEventService service = Mockito.mock(FileSystemLogEventService.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", service);
        GenericAvroConverter<BasicEndpointProfile> converter = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
        BasicEndpointProfile theLog = new BasicEndpointProfile("test");

        LogSchemaDto schemaDto = new LogSchemaDto();
        schemaDto.setSchema(BasicEndpointProfile.SCHEMA$.toString());
        LogSchema schema = new LogSchema(schemaDto);
        LogEvent logEvent = new LogEvent();

        logEvent.setLogData(converter.encode(theLog));
        LogEventPack logEventPack = new LogEventPack("endpointKey", System.currentTimeMillis(), schema, Collections.singletonList(logEvent));
        appender.doAppend(logEventPack);

        Mockito.verify(service).save(Mockito.anyListOf(LogEventDto.class), Mockito.any(Logger.class), Mockito.any(WriterAppender.class));
    }

    @Test
    public void initTest() {
        FileSystemLogAppender appender = new FileSystemLogAppender();
        fileSystemLogEventService = mock(FileSystemLogEventService.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", fileSystemLogEventService);
        appender.setName(APPENDER_NAME);
        appender.setAppenderId(APPENDER_ID);

        LogAppenderDto logAppenderDto = new LogAppenderDto();
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

}
