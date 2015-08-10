package org.kaaproject.kaa.server.appenders.flume.appender.client.async;

import java.util.List;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;

import com.google.common.util.concurrent.ListenableFuture;

public interface AsyncRpcClient extends RpcClient {
	public ListenableFuture<AppendAsyncResultPojo> appendAsync(Event event) throws EventDeliveryException;

	public ListenableFuture<AppendBatchAsyncResultPojo> appendBatchAsync(List<Event> events) throws EventDeliveryException;
}
