package org.kaaproject.kaa.server.operations.service.logs.filesystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.WriterAppender;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;
import org.kaaproject.kaa.common.endpoint.gen.BasicEndpointProfile;
import org.kaaproject.kaa.server.operations.service.logs.LogEvent;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.LogSchema;
import org.kaaproject.kaa.server.operations.service.logs.service.FileSystemLogEventService;
import org.mockito.Mockito;
import org.apache.log4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

public class FileSystemLogAppenderTest {

    @Test
    public void testAppend() throws IOException{
        FileSystemLogAppender appender = new FileSystemLogAppender("test");
        FileSystemLogEventService service = Mockito.mock(FileSystemLogEventService.class);
        ReflectionTestUtils.setField(appender, "fileSystemLogEventService", service);
        GenericAvroConverter<BasicEndpointProfile> converter  = new GenericAvroConverter<BasicEndpointProfile>(BasicEndpointProfile.SCHEMA$);
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

}
