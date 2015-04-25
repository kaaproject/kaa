package org.kaaproject.kaa.demo.iotworld.smarthome.data.event;

import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;

public class DeviceUpdatedEvent {

    private final String mEndpointKey;
    private final DeviceType mDeviceType;
    
    public DeviceUpdatedEvent(String endpointKey, DeviceType deviceType) {
        mEndpointKey = endpointKey;
        mDeviceType = deviceType;
    }

    public String getEndpointKey() {
        return mEndpointKey;
    }
    
    public DeviceType getDeviceType() {
        return mDeviceType;
    }
    
}
