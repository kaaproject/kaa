package org.kaaproject.kaa.server.appenders.cassandra.appender;

import com.datastax.driver.core.ResultSet;
import com.google.common.util.concurrent.ListenableFuture;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;

import java.util.List;
import java.util.concurrent.Future;

public interface LogEventDao {

    void createTable(String collectionName);

    List<LogEventDto> save(List<LogEventDto> logEventDtoList, String collectionName);

    ListenableFuture<ResultSet> saveAsync(List<LogEventDto> logEventDtoList, String collectionName);

    void removeAll(String collectionName);

    void close();
}
