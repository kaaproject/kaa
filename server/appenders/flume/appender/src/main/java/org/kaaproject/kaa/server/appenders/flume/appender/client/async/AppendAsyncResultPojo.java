package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import org.apache.flume.Event;

public class AppendAsyncResultPojo {
	public boolean isSuccessful;
	public Event event;

	public AppendAsyncResultPojo(boolean isSuccessful, Event event) {
		super();
		this.isSuccessful = isSuccessful;
		this.event = event;
	}

	public boolean isSuccessful() {
		return isSuccessful;
	}

	public Event getEvent() {
		return event;
	}

}
