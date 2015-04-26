package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

import de.greenrobot.event.EventBus;

/**
 * A superclass for all application events dispatched via {@link EventBus}.
 */
public class BasicEvent {

    private String mErrorMessage;
    
    public BasicEvent() {
        super();
    }
    
    public BasicEvent(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }
    
}
