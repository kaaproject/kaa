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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.DeviceEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.FanEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.FunControlUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfo;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoResponse;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.fan.FanStatus;
import org.kaaproject.kaa.demo.iotworld.fan.FanStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.fan.SwitchRequest;

import de.greenrobot.event.EventBus;

public class FanControlDevice implements DeviceEventClassFamily.Listener,
                                         FanEventClassFamily.Listener {

    public static List<String> FAN_CONTROL_FQNS = new ArrayList<>();
    
    static {
        FAN_CONTROL_FQNS.add(DeviceInfoRequest.class.getName());
        FAN_CONTROL_FQNS.add(DeviceStatusSubscriptionRequest.class.getName());
        FAN_CONTROL_FQNS.add(DeviceChangeNameRequest.class.getName());
        FAN_CONTROL_FQNS.add(SwitchRequest.class.getName());
    }

    private String mEndpointKey;
    private DeviceInfo mDeviceInfo;
    private FanStatus mStatus;
    private final KaaClient mClient;
    private final EventBus mEventBus;
    
    private final DeviceEventClassFamily mDeviceEventClassFamily;
    private final FanEventClassFamily mFanEventClassFamily;
    
    
    public FanControlDevice(String endpointKey,
                            KaaClient client,
                            EventBus eventBus) {
        mEndpointKey = endpointKey;
        mClient = client;
        mEventBus = eventBus;
        mDeviceEventClassFamily = mClient.getEventFamilyFactory().getDeviceEventClassFamily();
        mFanEventClassFamily = mClient.getEventFamilyFactory().getFanEventClassFamily();
        mDeviceEventClassFamily.addListener(this);
        mFanEventClassFamily.addListener(this);
        requestDeviceInfo();
    }
    
    private void requestDeviceInfo() {
        mDeviceEventClassFamily.sendEvent(new DeviceInfoRequest(), mEndpointKey);
    }

    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String endpointKey) {
        if (mEndpointKey.equals(endpointKey)) {
            mDeviceInfo = deviceInfoResponse.getDeviceInfo();
            fireFunControlUpdated();
            mDeviceEventClassFamily.sendEvent(new DeviceStatusSubscriptionRequest(), mEndpointKey);
        }
    }
    
    @Override
    public void onEvent(FanStatusUpdate fanStatusUpdate, String endpointKey) {
        if (mEndpointKey.equals(endpointKey)) {
            mStatus = fanStatusUpdate.getStatus();
            fireFunControlUpdated();
        }
    }
    
    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }
    
    public FanStatus getFanStatus() {
        return mStatus;
    }
    
    public void switchFunStatus(FanStatus newStatus) {
        mFanEventClassFamily.sendEvent(new SwitchRequest(newStatus), mEndpointKey);
    }

    @Override
    public void onEvent(DeviceStatusSubscriptionRequest request, String endpointKey) {}

    @Override
    public void onEvent(DeviceChangeNameRequest request, String endpointKey) {}
    
    @Override
    public void onEvent(DeviceInfoRequest request, String endpointKey) {}

    private void fireFunControlUpdated() {
        mEventBus.post(new FunControlUpdatedEvent(mEndpointKey));
    }
}
