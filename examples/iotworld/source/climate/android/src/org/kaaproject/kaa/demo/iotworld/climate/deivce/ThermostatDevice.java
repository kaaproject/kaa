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
package org.kaaproject.kaa.demo.iotworld.climate.deivce;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.demo.iotworld.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.GeoFencingEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.ThermoEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.FunControlUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.ThermostatUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.UserAttachEvent;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.UserDetachEvent;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfo;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.fan.FanStatus;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPosition;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingPositionUpdate;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse;
import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatStatusUpdate;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import de.greenrobot.event.EventBus;


public class ThermostatDevice implements DeviceEventClassFamily.Listener,
                                         GeoFencingEventClassFamily.Listener,
                                         ThermoEventClassFamily.Listener {
    
    private static final String TAG = ThermostatDevice.class.getSimpleName();
    
    private static final String DEVICE_NAME_PROP = "deviceName";
    private static final String OPERATION_MODE_PROP = "operationMode";
    private static final String DEGREE_PROP = "degree";
    private static final String TARGET_DEGREE_PROP = "targetDegree";
    
    private static final String DEFAULT_DEVICE_NAME = "Climate control";
    private static final OperationMode DEFAULT_OPERATION_MODE = OperationMode.GEOFENCING;
    
    private static final long DEGREE_CHANGE_SPEED_MS = 5000;
    private static final int MAX_DEGREE_DEVIATION = 2;
    private static final int DEFAULT_DEGREE = 80;
    private static final int DEFAULT_TARGET_DEGREE = 70;

    private final SharedPreferences mPreferences;
    private final KaaClient mClient;
    private final EventBus mEventBus;
    private final DeviceEventClassFamily mDeviceEventClassFamily;
    private final GeoFencingEventClassFamily mGeoFencingEventClassFamily;
    private final ThermoEventClassFamily mThermoEventClassFamily;

    private final Set<String> mSubscribedEndpoints = new HashSet<>();
    
    private DeviceInfo mDeviceInfo;
    private OperationMode mOperationMode;
    private GeoFencingPosition mPosition = GeoFencingPosition.AWAY;
    private ThermostatInfo mThermostatInfo;
    
    private ThermostatUpdatedEvent mThermostatUpdatedEvent = new ThermostatUpdatedEvent();
    
    private ThermoSimulator mThermoSimulator = new ThermoSimulator();
    
    private final Map<String, FanControlDevice> discoveredFanControlsMap = new LinkedHashMap<>();
    private final Map<String, FanControlDevice> fanControlsMap = new LinkedHashMap<>();
    
    private Handler mHandler = new Handler(Looper.getMainLooper());
    
    private FanControlsDiscoveryTask mFanControlsDiscoveryTask = new FanControlsDiscoveryTask();
    
    public ThermostatDevice(SharedPreferences preferences,
                            KaaClient client,
                            EventBus eventBus) {
        mPreferences = preferences;
        mClient = client;
        mEventBus = eventBus;
        mDeviceEventClassFamily = mClient.getEventFamilyFactory().getDeviceEventClassFamily();
        mGeoFencingEventClassFamily = mClient.getEventFamilyFactory().getGeoFencingEventClassFamily();
        mThermoEventClassFamily =  mClient.getEventFamilyFactory().getThermoEventClassFamily();
        loadDeviceState();
        mEventBus.register(this);
        mDeviceEventClassFamily.addListener(this);
        mGeoFencingEventClassFamily.addListener(this);
        mThermoEventClassFamily.addListener(this);
        new Thread(mThermoSimulator).start();
    }
    
    private void loadDeviceState() {
        mDeviceInfo = new DeviceInfo();
        mDeviceInfo.setModel(android.os.Build.MODEL);
        mDeviceInfo.setName(mPreferences.getString(DEVICE_NAME_PROP, DEFAULT_DEVICE_NAME));
        mOperationMode = OperationMode.values()[mPreferences.getInt(OPERATION_MODE_PROP, DEFAULT_OPERATION_MODE.ordinal())];
        mThermostatInfo = new ThermostatInfo();
        mThermostatInfo.setDegree(mPreferences.getInt(DEGREE_PROP, DEFAULT_DEGREE));
        mThermostatInfo.setTargetDegree(mPreferences.getInt(TARGET_DEGREE_PROP, DEFAULT_TARGET_DEGREE));
        mThermostatInfo.setIsOperating(false);
    }
    
    public ThermostatInfo getThermostatInfo() {
        return mThermostatInfo;
    }
    
    public void onEvent(UserAttachEvent userAttachEvent) {
        startFanConstrolsDiscoveryTask();
        manageThermostatState();
    }
    
    public void onEvent(UserDetachEvent userDetachEvent) {
        manageThermostatState();
    }
    
    public void onEvent(FunControlUpdatedEvent fanControlUpdatedEvent) {
        FanControlDevice device = discoveredFanControlsMap.remove(fanControlUpdatedEvent.getEndpointKey());
        if (device != null) {
            stopFanConstrolsDiscoveryTask();
            device.switchFunStatus(mThermostatInfo.getIsOperating() ? FanStatus.ON : FanStatus.OFF);
            fanControlsMap.put(fanControlUpdatedEvent.getEndpointKey(), device);
        }
    }
     
    private void manageThermostatState () {
        boolean operate = mClient.isAttachedToUser() && 
                (mOperationMode == OperationMode.ON || 
                (mOperationMode == OperationMode.GEOFENCING && 
                 mPosition == GeoFencingPosition.HOME || mPosition == GeoFencingPosition.NEAR));
        if (operate) {
            mThermoSimulator.start();
        } else {
            mThermoSimulator.stop();
        }
    }
    
    private void startFanControls() {
        for (FanControlDevice device : fanControlsMap.values()) {
            device.switchFunStatus(FanStatus.ON);
        }
    }
    
    private void stopFanControls() {
        for (FanControlDevice device : fanControlsMap.values()) {
            device.switchFunStatus(FanStatus.OFF);
        }
    }
    
    // Called from thermostat UI 
    public void changeTargetDegree(int newTargetDegree, String sourceEndpoint) {
        mThermostatInfo.setTargetDegree(newTargetDegree);
        commitThermostatInfo();
        manageThermostatState();
        if (sourceEndpoint != null) {
            mEventBus.post(mThermostatUpdatedEvent);
        }
        sendThermostatInfo(sourceEndpoint);
    }
    
    private void changeDegree(int newDegree) {
        mThermostatInfo.setDegree(newDegree);
        commitThermostatInfo();
        mEventBus.post(mThermostatUpdatedEvent);
        sendThermostatInfo(null);
    }
    
    private void changeName(String newName) {
        mDeviceInfo.setName(newName);
        commitDeviceInfo();
        sendDeviceInfo();
    }
    
    private void changeOperationMode(OperationMode newOperationMode) {
        mOperationMode = newOperationMode;
        commitOperationMode();
        manageThermostatState();
        sendGeofencingStatus();
    }
    
    private void updateGeofencingPosition(GeoFencingPosition newPosition) {
        mPosition = newPosition;
        manageThermostatState();
    }
    
    private void sendDeviceInfo() {
        for (String endpointKey : mSubscribedEndpoints) {
            mDeviceEventClassFamily.sendEvent(new DeviceInfoResponse(mDeviceInfo), endpointKey);
        }
    }
    
    private void sendGeofencingStatus() {
        for (String endpointKey : mSubscribedEndpoints) {
            mGeoFencingEventClassFamily.sendEvent(new GeoFencingStatusResponse(mOperationMode, mPosition), endpointKey);
        }
    }
    
    private void sendThermostatInfo(String ignoreEndpointKey) {
        for (String endpointKey : mSubscribedEndpoints) {
            mThermostatInfo.setIgnoreDegreeUpdate(ignoreEndpointKey != null && ignoreEndpointKey.equals(endpointKey));
            mThermoEventClassFamily.sendEvent(new ThermostatStatusUpdate(mThermostatInfo), endpointKey);
        } 
    }

    private void commitDeviceInfo() {
        Editor editor = mPreferences.edit();
        editor.putString(DEVICE_NAME_PROP, mDeviceInfo.getName());
        editor.commit();
    }

    
    private void commitOperationMode() {
        Editor editor = mPreferences.edit();
        editor.putInt(OPERATION_MODE_PROP, mOperationMode.ordinal());
        editor.commit();
    }

    private void commitThermostatInfo() {
        Editor editor = mPreferences.edit();
        editor.putInt(DEGREE_PROP, mThermostatInfo.getDegree());
        editor.putInt(TARGET_DEGREE_PROP, mThermostatInfo.getTargetDegree());
        editor.commit();
    }
    

    @Override
    public void onEvent(DeviceInfoRequest deviceInfoRequest, String sourceEndpoint) {
        mDeviceEventClassFamily.sendEvent(new DeviceInfoResponse(mDeviceInfo), sourceEndpoint);
    }
    
    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String endpointKey) {}

    @Override
    public void onEvent(DeviceStatusSubscriptionRequest deviceStatusSubscriptionRequest, String sourceEndpoint) {
        mSubscribedEndpoints.add(sourceEndpoint);
        mThermoEventClassFamily.sendEvent(new ThermostatStatusUpdate(mThermostatInfo), sourceEndpoint);
    }

    @Override
    public void onEvent(DeviceChangeNameRequest deviceChangeNameRequest, String sourceEndpoint) {
        changeName(deviceChangeNameRequest.getName());
    }

    @Override
    public void onEvent(GeoFencingStatusRequest geoFencingStatusRequest, String sourceEndpoint) {
        mGeoFencingEventClassFamily.sendEvent(new GeoFencingStatusResponse(mOperationMode, mPosition), sourceEndpoint);
    }

    @Override
    public void onEvent(OperationModeUpdateRequest operationModeUpdateRequest, String sourceEndpoint) {
        changeOperationMode(operationModeUpdateRequest.getMode());
    }

    @Override
    public void onEvent(GeoFencingPositionUpdate geoFencingPositionUpdate, String sourceEndpoint) {
        updateGeofencingPosition(geoFencingPositionUpdate.getPosition());
    }

    @Override
    public void onEvent(ChangeDegreeRequest changeDegreeRequest, String sourceEndpoint) {
        changeTargetDegree(changeDegreeRequest.getDegree(), sourceEndpoint);
    }
    
    private void switchOperatopnMode(boolean isOperating) {
        if (mThermostatInfo.getIsOperating() != isOperating) {
            mThermostatInfo.setIsOperating(isOperating);
            if (isOperating) {
                startFanControls();
            } else {
                stopFanControls();
            }
            mEventBus.post(mThermostatUpdatedEvent);
            sendThermostatInfo(null);
        }
    }
    
    private void startFanConstrolsDiscoveryTask() {
        mHandler.post(mFanControlsDiscoveryTask);
    }
    
    private void stopFanConstrolsDiscoveryTask() {
        mHandler.removeCallbacks(mFanControlsDiscoveryTask);
    }
    
    private void discoverFanControls() {
        mClient.findEventListeners(FanControlDevice.FAN_CONTROL_FQNS, new FindEventListenersCallback() {
            @Override
            public void onEventListenersReceived(List<String> endpointKeys) {
                for (String endpointKey : endpointKeys) {
                    FanControlDevice device = new FanControlDevice(endpointKey, mClient, mEventBus);
                    discoveredFanControlsMap.put(endpointKey, device);
                }
            }
            @Override
            public void onRequestFailed() {
                Log.e(TAG, "Unable to discover Fan Control devices!");
            }
        });
    }
    
    private class FanControlsDiscoveryTask implements Runnable {
        @Override
        public void run() {
            discoverFanControls();
            mHandler.postDelayed(mFanControlsDiscoveryTask, 5000);
        }
    }
    
    class ThermoSimulator implements Runnable {
        private boolean mEnabled;
        private boolean mWorking;
        private boolean mFinished;
        private int inc = 1;

        public ThermoSimulator() {
            mEnabled = false;
            mWorking = false;
            mFinished = false;
        }

        public void run() {
            while (!mFinished) {
                
                int target;
                int defaultDegreeDelta = DEFAULT_DEGREE - mThermostatInfo.getTargetDegree();
                int defaultDegreeDeltaAbs = Math.abs(defaultDegreeDelta);
                int deviation = Math.min(MAX_DEGREE_DEVIATION, defaultDegreeDeltaAbs);
                
                if (mEnabled) {
                    if (mWorking) {                        
                        target = mThermostatInfo.getTargetDegree();
                    } else {
                        int sign = (int) Math.signum(defaultDegreeDelta);
                        target = mThermostatInfo.getTargetDegree() + sign*deviation;
                    }
                    
                } else {                    
                    target = DEFAULT_DEGREE;
                }
                
                inc = (int) Math.signum(target - mThermostatInfo.getDegree()); 
                if (inc != 0) {
                    changeDegree(mThermostatInfo.getDegree()+inc);
                }
                if (mEnabled) {
                    int delta = Math.abs(mThermostatInfo.getTargetDegree() - mThermostatInfo.getDegree());
                    if (mWorking && delta == 0) {
                        mWorking = false;
                    } else if (!mWorking && 
                            delta == deviation 
                            && deviation != 0) {
                        mWorking = true;
                    }
                }
                
                switchOperatopnMode(mWorking);
                
                try {
                    Thread.sleep(DEGREE_CHANGE_SPEED_MS);
                } catch (InterruptedException e) {}
            }
        }

        public void stop() {
            mEnabled = false;
            mWorking = false;
        }

        public void start() {
            mEnabled = true;
            mWorking = true;
        }
        
        public void finish() {
            mFinished = true;
        }

    }

}
