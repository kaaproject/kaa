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

public abstract class SmartDeviceInfo {
    
    private Long id;
    private String endpointKey;
    private String deviceName;
    
    protected Object deviceInfo;
    
    private DeviceStatus deviceStatus = DeviceStatus.UNKNOWN;
    
    public static SmartDeviceInfo createDeviceInfo(String endpointKey, org.kaaproject.kaa.demo.smarthouse.device.DeviceType deviceType) {
        DeviceType smartDeviceType = SmartDeviceInfo.toSmartDeviceType(deviceType);
        return createDeviceInfo(endpointKey, smartDeviceType);
    }
    
    public static SmartDeviceInfo createDeviceInfo(String endpointKey, DeviceType deviceType) {
        switch (deviceType) {
        case THERMOSTAT:
            return new ThermostatInfo(endpointKey);
        case TV:
            return new TvInfo(endpointKey);
        case SOUND_SYSTEM:
            return new SoundSystemInfo(endpointKey);
        case LAMP:
            return new LampInfo(endpointKey);
        }
        return null;
    }
    
    public abstract DeviceType getDeviceType();
    
    public Object getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(Object info) {
        this.deviceInfo = info;
    }
    
    public SmartDeviceInfo(String endpointKey) {
        this.endpointKey = endpointKey;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }



    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((endpointKey == null) ? 0 : endpointKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SmartDeviceInfo other = (SmartDeviceInfo) obj;
        if (endpointKey == null) {
            if (other.endpointKey != null)
                return false;
        } else if (!endpointKey.equals(other.endpointKey))
            return false;
        return true;
    }

    public static DeviceType toSmartDeviceType(org.kaaproject.kaa.demo.smarthouse.device.DeviceType deviceType) {
        if (deviceType != null) {
            switch (deviceType) {
            case THERMOSTAT:
                return DeviceType.THERMOSTAT;
            case TV:
                return DeviceType.TV;
            case SOUND_SYSTEM:
                return DeviceType.SOUND_SYSTEM;
            case LAMP:
                return DeviceType.LAMP;
            }
        }
        return null;
    }

}
