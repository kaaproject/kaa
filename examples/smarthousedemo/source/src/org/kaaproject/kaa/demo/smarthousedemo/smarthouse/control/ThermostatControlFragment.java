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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.ThermostatInfo;
import org.kaaproject.kaa.demo.smarthousedemo.widget.Thermostat;
import org.kaaproject.kaa.demo.smarthousedemo.widget.Thermostat.OnThermostatChangeListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ThermostatControlFragment extends BaseControlFragment<ThermostatInfo> implements OnThermostatChangeListener {

    private TextView mThermostatInfoText;
    private Thermostat mThermostat;
    
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.THERMOSTAT;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thermostat_control, container,
                false);
        mThermostatInfoText = (TextView) rootView.findViewById(R.id.thermostatInfoText);
        
        mThermostat = (Thermostat) rootView.findViewById(R.id.thermostat);
        
        updateDeviceInfo(true);
        
        mThermostat.setOnThermostatChangeListener(this);
        
        return rootView;
    }
    
    private void updateDeviceInfo(boolean updateTarget) {
        String temperatureText;
        if (mDevice.getThermostatInfo() != null) {
            temperatureText = String.format(getString(R.string.msg_temperature),mDevice.getThermostatInfo().getDegree(),
                    mDevice.getThermostatInfo().getTargetDegree());
            mThermostat.setTemp(mDevice.getThermostatInfo().getDegree());
            if (mDevice.getThermostatInfo().getIsSetManually() || updateTarget) {
                mThermostat.setTargetTemp(mDevice.getThermostatInfo().getTargetDegree(), false);
            }
        }
        else {
            temperatureText = getString(R.string.msg_temperature_na);
            mThermostat.setTemp(70);
            mThermostat.setTargetTemp(70, false);
        }
        mThermostatInfoText.setText(temperatureText);
        
    }
    
    @Override
    protected void onDeviceInfoUpdated() {
        updateDeviceInfo(false);
    }

    @Override
    public void onProgressChanged(Thermostat Thermostat, int progress,
            boolean fromUser) {
    }

    @Override
    public void onTargetProgressChanged(Thermostat Thermostat,
            int targetProgress, boolean fromUser) {
        mActivity.getSmartHouseController().changeDegree(mDevice.getEndpointKey(), mThermostat.getTargetTemp());
    }

    @Override
    public void onStartTrackingTouch(Thermostat Thermostat) {
    }

    @Override
    public void onStopTrackingTouch(Thermostat Thermostat) {
    }
    
}
