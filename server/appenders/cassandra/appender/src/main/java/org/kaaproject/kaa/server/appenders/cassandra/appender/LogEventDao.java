package org.kaaproject.kaa.server.appenders.cassandra.appender;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;

import java.util.List;
import java.util.concurrent.Future;

public interface LogEventDao {

    void createTable(String collectionName);

    List<LogEventDto> save(List<LogEventDto> logEventDtoList, String collectionName);

    Future<List<LogEventDto>> saveAsync(List<LogEventDto> logEventDtoList, String collectionName);

    void removeAll(String collectionName);

    void close();
}
