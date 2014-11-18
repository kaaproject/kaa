/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.ThermostatInfo;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control.ThermostatControlFragment;

import android.view.View;
import android.widget.TextView;

public class ThermostatsFragment extends DevicesFragment<ThermostatInfo>{

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.THERMOSTAT;
    }

    @Override
    protected DeviceAdapter<ThermostatInfo> createDeviceAdapter() {
        return new DeviceAdapter<ThermostatInfo>(mActivity.getDeviceStore(), mActivity, getDeviceType()) {
            
            @Override
            protected int getLayoutResId() {
                return R.layout.thermostat_list_item;
            }
            
            protected void setAdditionalInfo(View v, ThermostatInfo entry) {
                TextView temeratureTextView = (TextView)v.findViewById(R.id.temperatureText);
                String temperatureText;
                if (entry.getThermostatInfo() != null) {
                    temperatureText = String.format(getString(R.string.msg_temperature),entry.getThermostatInfo().getDegree(),
                            entry.getThermostatInfo().getTargetDegree());
                }
                else {
                    temperatureText = getString(R.string.msg_temperature_na);
                }
                temeratureTextView.setText(temperatureText);
            }
            
        };
    }
    

}
