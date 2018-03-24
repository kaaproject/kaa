package org.kaaproject.kaa.server.appenders.hbase.appender;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;

import java.io.IOException;
import java.util.List;

public interface LogEventDao {
	
	String createHBTable();
	
	void save(List<HBaseLogEventDto> logEventDtoList, String collectionName, 
			GenericAvroConverter<GenericRecord> eventConverter,
			GenericAvroConverter<GenericRecord> headerConverter) throws IOException;

	void removeAll(String collectionName);
	
	void close();

}
