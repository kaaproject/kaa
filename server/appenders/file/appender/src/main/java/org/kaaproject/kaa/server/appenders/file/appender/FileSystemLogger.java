package org.kaaproject.kaa.server.appenders.file.appender;

import java.io.Closeable;
import java.nio.file.Path;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.file.config.FileConfig;

public interface FileSystemLogger extends Closeable{

    void init(LogAppenderDto appenderDto, FileConfig config, Path filePath);
    void append(String event);
    
}
