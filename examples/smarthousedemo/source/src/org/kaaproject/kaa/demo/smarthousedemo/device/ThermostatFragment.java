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
package org.kaaproject.kaa.demo.smarthousedemo.device;

import org.kaaproject.kaa.demo.smarthouse.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermostatInfo;
import org.kaaproject.kaa.demo.smarthouse.thermo.ThermostatInfoRequest;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.widget.Thermostat;
import org.kaaproject.kaa.demo.smarthousedemo.widget.Thermostat.OnThermostatChangeListener;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ThermostatFragment extends DeviceFragment {

    private ThermostatInfo thermostatInfo = new ThermostatInfo();
    private Handler thermostatHandler = new Handler();
    private ThermoSimulator thermoSimulator = new ThermoSimulator();
    private Thermostat mThermostat;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thermostatInfo.setDegree(60);
        thermostatInfo.setTargetDegree(70);
        new Thread(thermoSimulator).start();
    }
    
    @Override
    public void handleEvent(Object event, String endpointSourceKey) {
        if (event instanceof ChangeDegreeRequest) {
            final ChangeDegreeRequest request = (ChangeDegreeRequest)event;
            thermostatHandler.post(new Runnable() {
                @Override
                public void run() {
                    thermostatInfo.setTargetDegree(request.getDegree());
                    mThermostat.setTargetTemp(request.getDegree(), false);
                }
            });
        }
        else if (event instanceof ThermostatInfoRequest) {
            mActivity.getSmartHouseController().sendThermostatInfo(thermostatInfo, endpointSourceKey);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thermostat, container,
                false);
        mThermostat = (Thermostat)rootView.findViewById(R.id.thermostat);
        mThermostat.setTemp(thermostatInfo.getDegree());
        mThermostat.setTargetTemp(thermostatInfo.getTargetDegree(), false);
        
        mThermostat.setOnThermostatChangeListener(new OnThermostatChangeListener() {
            
            @Override
            public void onTargetProgressChanged(Thermostat thermostat,
                    int targetProgress, boolean fromUser) {
                thermostatInfo.setTargetDegree(thermostat.getTargetTemp());
                thermostatInfo.setIsSetManually(true);
                mActivity.getSmartHouseController().updateThermostatInfo(thermostatInfo);
            }
            @Override
            public void onProgressChanged(Thermostat Thermostat, int progress,
                    boolean fromUser) {
            }
            
            @Override
            public void onStopTrackingTouch(Thermostat Thermostat) {}
            
            @Override
            public void onStartTrackingTouch(Thermostat Thermostat) {}
            
        });
        updateThermostatUI();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        thermoSimulator.onResume();
    }

    @Override
    public void onPause() {
        thermoSimulator.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        thermoSimulator.onDestroy();
        super.onDestroy();
    }
    
    private void updateThermostatUI() {
        mThermostat.setTemp(thermostatInfo.getDegree());
    }

    class ThermoSimulator implements Runnable {
        private Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;
        private int inc = 1;

        public ThermoSimulator() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
        }

        public void run() {
            while (!mFinished) {
                
                inc = (int) Math.signum(thermostatInfo.getTargetDegree() - thermostatInfo.getDegree()); 
                if (inc != 0) {
                    thermostatInfo.setDegree(thermostatInfo.getDegree()+inc);
                    thermostatHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateThermostatUI();
                        }
                    });
                    thermostatInfo.setIsSetManually(false);
                    mActivity.getSmartHouseController().updateThermostatInfo(thermostatInfo);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        /**
         * Call this on pause.
         */
        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        /**
         * Call this on resume.
         */
        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }
        
        public void onDestroy() {
            mFinished = true;
        }

    }
}
