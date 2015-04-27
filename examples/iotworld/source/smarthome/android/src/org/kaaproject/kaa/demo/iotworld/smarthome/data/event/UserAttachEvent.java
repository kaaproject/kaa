package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

/**
 * An event class which is used to notify UI components 
 * of the completion of the endpoint attach operation.
 */
public class UserAttachEvent extends BasicEvent {

    public UserAttachEvent() {
        super();
    }

    public UserAttachEvent(String errorMessage) {
        super(errorMessage);
    }
    
}
