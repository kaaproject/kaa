package org.kaaproject.tutorials;

import org.kaaproject.ipc.tstp.gen.v1.TimeSeriesEvent;

import java.util.stream.Collectors;

public class NotificationService {

  private static String RECIPIENT_EMAIL = "pasika2012@ukr.net"; // Specify recipient of email notifications.

  /**
   * Application entrypoint.
   */
  public static void main(String[] args) throws Exception {
    System.out.println("Starting notification application...");
    EmailService emailService = new EmailService();
    TimeSeriesEventService timeSeriesEventService = new TimeSeriesEventService();
    timeSeriesEventService.onTimeSeriesEvent(timeSeriesEvent -> handleTimeSeriesEvent(timeSeriesEvent, emailService));
  }

  private static void handleTimeSeriesEvent(TimeSeriesEvent event, EmailService emailService) {
    String eventType = event.getDataPoints().stream()
        .map(dataPoint -> dataPoint.getValues().get("event_type").toString())
        .collect(Collectors.joining(", "));
    String emailMessageText = String.format("Next events has occurred: %s", eventType);
    emailService.sendNotification(RECIPIENT_EMAIL, "Smart house notifications", emailMessageText);
  }
}
