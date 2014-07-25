package org.kaaproject.kaa.server.operations.service.logs.filesystem;

import java.io.IOException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.operations.service.logs.service.FileSystemLogEventService;
import org.kaaproject.kaa.server.operations.service.logs.service.FileSystemLogEventServiceImpl;
import org.mockito.Mockito;

public class FileSystemLogEventServiceImplTest {

    @Test
    public void testAppend() throws IOException{
        FileSystemLogEventService service = new FileSystemLogEventServiceImpl();
        WriterAppender appenderMock = Mockito.mock(WriterAppender.class);
        Logger loggerMock = Mockito.mock(Logger.class);

        LogEventDto logEventDto = new LogEventDto("endpointkey", System.currentTimeMillis(), "event");
        service.save(Collections.singletonList(logEventDto), loggerMock, appenderMock);

        Mockito.verify(appenderMock).doAppend(Mockito.any(LoggingEvent.class));
    }
}
