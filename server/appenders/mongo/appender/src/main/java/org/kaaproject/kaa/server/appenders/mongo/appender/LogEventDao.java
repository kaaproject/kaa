package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;

public interface LogEventDao {

    void createCollection(String collectionName);
    
    List<LogEvent> save(List<LogEventDto> logEventDtos, String collectionName);
    
    void removeAll(String collectionName);
    
    void close();
    
}
