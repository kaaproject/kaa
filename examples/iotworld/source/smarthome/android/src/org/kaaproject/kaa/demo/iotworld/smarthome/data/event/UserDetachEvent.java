package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

/**
 * An event class which is used to notify UI components 
 * of the completion of the endpoint detach operation.
 */
public class UserDetachEvent extends BasicEvent {

    public UserDetachEvent() {
        super();
    }

    public UserDetachEvent(String errorMessage) {
        super(errorMessage);
    }
    
}
