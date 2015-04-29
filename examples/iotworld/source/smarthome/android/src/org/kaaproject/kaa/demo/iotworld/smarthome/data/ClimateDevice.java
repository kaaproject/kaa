/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
