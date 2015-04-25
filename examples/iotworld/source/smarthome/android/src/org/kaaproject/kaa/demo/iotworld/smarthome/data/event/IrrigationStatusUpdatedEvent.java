package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

public class IrrigationStatusUpdatedEvent {

    private final String mEndpointKey;
    
    public IrrigationStatusUpdatedEvent(String endpointKey) {
        mEndpointKey = endpointKey;
    }

    public String getEndpointKey() {
        return mEndpointKey;
    }
    
}
