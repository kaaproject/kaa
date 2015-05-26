package org.kaaproject.kaa.demo.iotworld.irrigation.gpio;

public interface GpioManager {
    
    void togglePinToHight(long durationTime);

    void togglePinToLow();

    void stop();

}
