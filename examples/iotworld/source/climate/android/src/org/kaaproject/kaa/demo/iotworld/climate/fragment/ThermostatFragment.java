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
package org.kaaproject.kaa.demo.iotworld.climate.fragment;

import org.kaaproject.kaa.demo.iotworld.climate.ClimateControlActivity;
import org.kaaproject.kaa.demo.iotworld.climate.ClimateControlApplication;
import org.kaaproject.kaa.demo.iotworld.climate.ClimateController;
import org.kaaproject.kaa.demo.iotworld.climate.R;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.ThermostatUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.climate.deivce.ThermostatDevice;
import org.kaaproject.kaa.demo.iotworld.climate.widget.Thermostat;
import org.kaaproject.kaa.demo.iotworld.climate.widget.Thermostat.OnThermostatChangeListener;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ThermostatFragment extends Fragment implements OnThermostatChangeListener {

    private ClimateControlActivity mActivity;
    private ClimateControlApplication mApplication;
    private ClimateController mController;
    private ThermostatDevice mThermostatDevice;

    private Thermostat mThermostat;
    
    public ThermostatFragment() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (ClimateControlActivity) activity;
            mApplication = mActivity.getClimateControlApplication();
            mController = mApplication.getController();
            mThermostatDevice = mController.getThermostatDevice();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thermostat, container,
                false);
        mThermostat = (Thermostat)rootView.findViewById(R.id.thermostat);
        
        ThermostatInfo thermostatInfo = mThermostatDevice.getThermostatInfo();
        
        mThermostat.setTemp(thermostatInfo.getDegree());
        mThermostat.setTargetTemp(thermostatInfo.getTargetDegree(), false);
        
        mThermostat.setOnThermostatChangeListener(this);
        
        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().unregister(this);
        }
    }
    
    public String getFragmentTag() {
        return ThermostatFragment.class.getSimpleName();
    }
    
    public void onEventMainThread(ThermostatUpdatedEvent thermostatUpdatedEvent) {
        ThermostatInfo thermostatInfo = mThermostatDevice.getThermostatInfo();
        mThermostat.setTemp(thermostatInfo.getDegree());
        mThermostat.setTargetTemp(thermostatInfo.getTargetDegree(), false);
        mThermostat.setOperating(thermostatInfo.getIsOperating());
    }

    @Override
    public void onTargetProgressChanged(Thermostat thermostat, int targetProgress, boolean fromUser) {
        if (fromUser) {
            mThermostatDevice.changeTargetDegree(thermostat.getTargetTemp(), null);
        }
    }

    @Override
    public void onProgressChanged(Thermostat Thermostat, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(Thermostat Thermostat) {
    }

    @Override
    public void onStopTrackingTouch(Thermostat Thermostat) {
    }

}
