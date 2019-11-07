package org.kaaproject.tutorials;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.Nats;

import org.kaaproject.ipc.tstp.gen.v1.TimeSeriesEvent;

import java.util.function.Consumer;

public class TimeSeriesEventService {

  // Inbound message <a href="https://en.wikipedia.org/wiki/Apache_Avro">Avro</a> converter.
  private static final AvroConverter<TimeSeriesEvent> AVRO_CONVERTER = new AvroConverter<>(TimeSeriesEvent.class);

  private static final String NATS_URL = "nats://localhost:4222";

  // Time series and <a href="https://docs.kaaiot.io/EPTS/docs/current/Overview/">EPTS</a> information.
  private static final String TIME_SERIES_NAME = "notification";
  private static final String EPTS_SERVICE_INSTANCE_NAME = "epts";
  private static final String TSTP_SUBSCRIBE_SUBJECT = String
      .format("kaa.v1.events.%s.endpoint.data-collection.data-points-received.%s", EPTS_SERVICE_INSTANCE_NAME, TIME_SERIES_NAME);

  private final Connection natsConnection;

  public TimeSeriesEventService() throws Exception {
    this.natsConnection = Nats.connect(NATS_URL);
  }

  public void onTimeSeriesEvent(Consumer<TimeSeriesEvent> onTimeSeriesEventCallback) {
    Dispatcher dispatcher = natsConnection.createDispatcher((msg) -> handleNatsMessage(msg, onTimeSeriesEventCallback));
    dispatcher.subscribe(TSTP_SUBSCRIBE_SUBJECT);
    System.out.println(String.format("Subscribed to the time series [%s] events", TIME_SERIES_NAME));
    while (true) {
      // loop forever
    }
  }

  private void handleNatsMessage(Message msg, Consumer<TimeSeriesEvent> onTimeSeriesEventCallback) {
    try {
      TimeSeriesEvent timeSeriesEvent = AVRO_CONVERTER.decode(msg.getData());
      System.out.println(String.format("Handling incoming time series event: %s", timeSeriesEvent));
      onTimeSeriesEventCallback.accept(timeSeriesEvent);
    } catch (Exception ex) {
      System.err.println(String.format("Exception occurred during handling time series event: %s", ex));
    }
  }
}
