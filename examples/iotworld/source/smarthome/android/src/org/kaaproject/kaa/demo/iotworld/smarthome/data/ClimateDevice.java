package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.ThermoEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatStatusUpdate;

import de.greenrobot.event.EventBus;

public class ClimateDevice extends AbstractGeoFencingDevice implements ThermoEventClassFamily.Listener {

    private final ThermoEventClassFamily mThermoEventClassFamily;
    
    private ThermostatInfo mThermostatInfo;

    public ClimateDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mThermoEventClassFamily = mClient.getEventFamilyFactory().getThermoEventClassFamily();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mThermoEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mThermoEventClassFamily.removeListener(this);
    }

    public ThermostatInfo getThermostatInfo() {
        return mThermostatInfo;
    }
 
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CLIMATE;
    }

    @Override
    public void onEvent(ThermostatStatusUpdate thermostatStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mThermostatInfo = thermostatStatusUpdate.getThermostatInfo();
            fireDeviceUpdated();
        }
    }
    
    public void changeDegree(int newDegree) {
        mThermoEventClassFamily.sendEvent(new ChangeDegreeRequest(newDegree), mEndpointKey);
    }

}
