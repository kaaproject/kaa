package org.kaaproject.kaa.server.operations.service.logs.filesystem.loggers;

import java.io.Closeable;
import java.nio.file.Path;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;

public interface FileSystemLogger extends Closeable{

    void init(LogAppenderDto appenderDto, Path filePath);
    void append(String event);
    
}
