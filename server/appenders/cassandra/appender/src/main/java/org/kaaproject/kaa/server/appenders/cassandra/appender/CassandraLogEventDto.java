package org.kaaproject.kaa.server.appenders.cassandra.appender;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;

public class CassandraLogEventDto {

	private final RecordHeader header;
	private final GenericRecord event;

	public CassandraLogEventDto(RecordHeader header, GenericRecord event) {
		super();
		this.header = header;
		this.event = event;
	}

	public RecordHeader getHeader() {
		return header;
	}

	public GenericRecord getEvent() {
		return event;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CassandraLogEventDto other = (CassandraLogEventDto) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;
		return true;
	}

}
