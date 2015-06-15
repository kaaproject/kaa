package org.kaaproject.kaa.demo.iotworld.irrigation.gpio;

import org.kaaproject.kaa.demo.iotworld.irrigation.IMSController;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.PinEventType;

public class GpioManagerImpl implements GpioManager, GpioPinListenerDigital {

    private static final Logger LOG = LoggerFactory.getLogger(IMSController.class);
    private static final Pin RELAY_IN_PIN_OUT = RaspiPin.GPIO_28; // GPIO 38 on raspberry PI 2 board
    private static final Pin WATER_FLOW_SENSOR_PIN_IN = RaspiPin.GPIO_29; // GPIO 40 on raspberry PI 2 board

    private final GpioController gpioController;
    private final GpioPinDigitalOutput relayPin;
    private final GpioPinDigitalInput flowSensorCountPin;
    private final IrrigationStateHolder stateHolder;

    public GpioManagerImpl(IrrigationStateHolder stateHolder) {
        LOG.info("Init gpio manager with relay pin {} and water flow sensor pin {}", RELAY_IN_PIN_OUT, WATER_FLOW_SENSOR_PIN_IN);
        this.stateHolder = stateHolder;
        gpioController = GpioFactory.getInstance();
        relayPin = gpioController.provisionDigitalOutputPin(RELAY_IN_PIN_OUT, "Relay control pin", PinState.LOW);
        flowSensorCountPin = gpioController.provisionDigitalInputPin(WATER_FLOW_SENSOR_PIN_IN, "Water flow sensor pin", PinPullResistance.PULL_DOWN);
        flowSensorCountPin.addListener(this);
    }

    public void togglePinToHight(long durationTime) {
        LOG.info("Toggle relay pin to hight level for the {} mill", durationTime);
        if (durationTime > 0) {
            relayPin.pulse(durationTime, true);
        }
    }

    public void togglePinToLow() {
        LOG.info("Toggle relay pin to low level");
        relayPin.low();
    }

    public void stop() {
        LOG.info("Stop gpio manager");
        if (gpioController != null) {
            gpioController.shutdown();
        }
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent paramGpioPinDigitalStateChangeEvent) {
        if(PinEventType.DIGITAL_STATE_CHANGE.equals(paramGpioPinDigitalStateChangeEvent.getEventType())) {
            stateHolder.onGpioPinDigitalStateChangeEvent();
        }
    }
}
