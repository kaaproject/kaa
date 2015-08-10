package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import java.util.List;

import org.apache.flume.Event;

public class AppendBatchAsyncResultPojo {
	public boolean isSuccessful;
	public List<Event> events;

	public AppendBatchAsyncResultPojo(boolean isSuccessful, List<Event> events) {
		super();
		this.isSuccessful = isSuccessful;
		this.events = events;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public List<Event> getEvents() {
		return events;
	}

}
