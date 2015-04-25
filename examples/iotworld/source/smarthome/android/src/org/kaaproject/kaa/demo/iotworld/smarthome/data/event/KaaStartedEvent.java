package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

/**
 * An event class which is used to notify UI components that the Kaa client has started.
 */
public class KaaStartedEvent extends BasicEvent {

    public KaaStartedEvent() {
        super();
    }

    public KaaStartedEvent(String errorMessage) {
        super(errorMessage);
    }

}