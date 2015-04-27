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

import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.LightEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.light.BulbControlRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbListRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbListStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;

import de.greenrobot.event.EventBus;

public class LightningDevice extends AbstractGeoFencingDevice implements LightEventClassFamily.Listener {

    private final LightEventClassFamily mLightEventClassFamily;
    
    private List<BulbInfo> mBulbs;
    
    public LightningDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mLightEventClassFamily = mClient.getEventFamilyFactory().getLightEventClassFamily();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mLightEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mLightEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mLightEventClassFamily.sendEvent(new BulbListRequest(), mEndpointKey);
    }
    
    public List<BulbInfo> getBulbs() {
        return mBulbs;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.LIGHTNING;
    }

    @Override
    public void onEvent(BulbListStatusUpdate bulbListStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mBulbs = bulbListStatusUpdate.getBulbs();
            fireDeviceUpdated();
        }
    }
    
    private BulbInfo getBulbById(String bulbId) {
        if (mBulbs != null) {
            for (BulbInfo bulb : mBulbs) {
                if (bulb.getBulbId().equals(bulbId)) {
                    return bulb;
                }
            }
        }
        return null;
    }
    
    public void changeBulbBrightness(String bulbId, int brightness) {
        BulbInfo bulb = getBulbById(bulbId);
        if (bulb != null) {
            controlBulb(bulbId, bulb.getStatus(), brightness);
        }
    }
    
    public void changeBulbState(String bulbId, BulbStatus status) {
        BulbInfo bulb = getBulbById(bulbId);
        if (bulb != null) {
            controlBulb(bulbId, status, bulb.getBrightness());
        }
    }
    
    public void controlBulb(String bulbId, BulbStatus status, int brightness) {
        mLightEventClassFamily.sendEvent(new BulbControlRequest(bulbId, status, brightness), mEndpointKey);
    }

}
