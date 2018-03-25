package org.kaaproject.kaa.server.appenders.hbase.appender;

import java.io.IOException;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;


public interface LogEventDao {

  String createHbTable();

  void save(List<HBaseLogEventDto> logEventDtoList, String collectionName, 
      GenericAvroConverter<GenericRecord> eventConverter,
      GenericAvroConverter<GenericRecord> headerConverter) throws IOException;

  void removeAll(String collectionName);

  void close();

}
