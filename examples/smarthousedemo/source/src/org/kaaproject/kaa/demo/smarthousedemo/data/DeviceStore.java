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
package org.kaaproject.kaa.demo.smarthousedemo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.demo.smarthouse.device.DeviceInfo;
import org.kaaproject.kaa.demo.smarthousedemo.data.persistent.DeviceDataSource;

import android.content.Context;
import android.content.res.Resources;
import de.greenrobot.event.EventBus;

public class DeviceStore {
    
    private Context mContext;
    
    private EventBus eventBus = new EventBus();
    
    private DeviceDataSource datasource;
    
    private Map<DeviceType, List<SmartDeviceInfo>> deviceMap = new HashMap<>();
    {
        for (DeviceType deviceType : DeviceType.enabledValues()) {
            List<SmartDeviceInfo> devices = new LinkedList<>();
            deviceMap.put(deviceType, devices);
        }
    }
    
    public DeviceStore (Context context) {
        mContext = context;
        datasource = new DeviceDataSource(context);
        datasource.open();
        List<SmartDeviceInfo> devices = datasource.getAllDevices();
        for (SmartDeviceInfo device : devices) {
            List<SmartDeviceInfo> typeDevices = deviceMap.get(device.getDeviceType());
            typeDevices.add(device);
        }
        eventBus.register(this);
    }
    
    public void onResume() {
        datasource.open();
    }
    
    public void onPause() {
        datasource.close();
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public List<SmartDeviceInfo> getDevices(DeviceType deviceType) {
        return deviceMap.get(deviceType);
    }
    
    public void onEventMainThread(DeviceStore.DeviceAdded deviceAdded) {
        //SmartDeviceInfo device = deviceAdded.device;
        //Toast.makeText(mContext, "New device added: " + device.getEndpointKey(), Toast.LENGTH_SHORT).show();
    }
    
    public void onEventMainThread(DeviceStore.DeviceRemoved deviceRemoved) {
        //SmartDeviceInfo device = deviceRemoved.device;
        //Toast.makeText(mContext, "Device removed: " + device.getEndpointKey(), Toast.LENGTH_SHORT).show();
    }
    
    public void onDeviceDiscovered(String endpointKey, DeviceInfo deviceInfo) {
        SmartDeviceInfo device = SmartDeviceInfo.createDeviceInfo(endpointKey, deviceInfo.getDeviceType()); 
        List<SmartDeviceInfo> devices = deviceMap.get(device.getDeviceType());
        if (!devices.contains(device)) {
            device.setDeviceName(generateDeviceName(device.getDeviceType()));
            SmartDeviceInfo savedDevice = datasource.addDevice(device);
            savedDevice.setDeviceStatus(DeviceStatus.ONLINE);
            devices.add(savedDevice);
            DeviceAdded deviceAdded = new DeviceAdded();
            deviceAdded.device = savedDevice;
            eventBus.post(deviceAdded);
        }
        else {
            int index = devices.indexOf(device);
            SmartDeviceInfo storedDevice = devices.get(index);
            storedDevice.setDeviceStatus(DeviceStatus.ONLINE);
            DeviceUpdated deviceUpdated = new DeviceUpdated();
            deviceUpdated.device = storedDevice;
            eventBus.post(deviceUpdated);
        }
    }
    
    public void onDeviceInfoDiscovered(String endpointKey, DeviceType type, Object deviceInfo) {
        SmartDeviceInfo device = SmartDeviceInfo.createDeviceInfo(endpointKey, type);
        List<SmartDeviceInfo> devices = deviceMap.get(device.getDeviceType());
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            SmartDeviceInfo storedDevice = devices.get(index);
            storedDevice.setDeviceStatus(DeviceStatus.ONLINE);
            storedDevice.setDeviceInfo(deviceInfo);
            DeviceUpdated deviceUpdated = new DeviceUpdated();
            deviceUpdated.device = storedDevice;
            eventBus.post(deviceUpdated);
        }
    }
    
    public void onDeviceRemoved(SmartDeviceInfo device) {
        List<SmartDeviceInfo> devices = deviceMap.get(device.getDeviceType());
        boolean removed = devices.remove(device);
        if (removed) {
            datasource.deleteDevice(device);
            DeviceRemoved deviceRemoved = new DeviceRemoved();
            deviceRemoved.device = device;
            eventBus.post(deviceRemoved);
        }
    }
    
    public void onDeviceRenamed(SmartDeviceInfo device, String newName) {
        List<SmartDeviceInfo> devices = deviceMap.get(device.getDeviceType());
        if (devices.contains(device)) {
            int index = devices.indexOf(device);
            SmartDeviceInfo storedDevice = devices.get(index);
            datasource.renameDevice(storedDevice, newName);
            storedDevice.setDeviceName(newName);
            DeviceUpdated deviceUpdated = new DeviceUpdated();
            deviceUpdated.device = storedDevice;
            eventBus.post(deviceUpdated);
        }
    }
    
    public void deleteAllDevices() {
    	datasource.deleteAllDevices();
    	deviceMap.clear();
    }
    
    public List<String> getAllDeviceKeys() {
    	List<String> keys = new ArrayList<>();
    	for (DeviceType type : DeviceType.enabledValues()) {
    		List<SmartDeviceInfo> devices = deviceMap.get(type);
    		for (SmartDeviceInfo device : devices) {
    			keys.add(device.getEndpointKey());
    		}
    	}
    	return keys;
    }
    
    private String generateDeviceName(DeviceType deviceType) {
        List<SmartDeviceInfo> devices = deviceMap.get(deviceType);
        String deviceName;
        int c = 1;
        Resources res = mContext.getResources();
        while(true) {
            deviceName = res.getString(deviceType.getNameRes()) + " " + c;
            boolean found = false;
            for (SmartDeviceInfo device : devices) {
                if (device.getDeviceName().equals(deviceName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return deviceName;
            }
            c++;
        }
    }
    
    public class DeviceAdded {
        public SmartDeviceInfo device;
    }
    
    public class DeviceRemoved {
        public SmartDeviceInfo device;
    }
    
    public class DeviceUpdated {
        public SmartDeviceInfo device;
    }
}
