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
package org.kaaproject.kaa.demo.iotworld.climate;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.KaaStartedEvent;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.UserAttachEvent;
import org.kaaproject.kaa.demo.iotworld.climate.data.event.UserDetachEvent;
import org.kaaproject.kaa.demo.iotworld.climate.deivce.ThermostatDevice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

public class ClimateController implements UserAttachCallback, AttachEndpointToUserCallback, DetachEndpointFromUserCallback {

    private static final String DEFAULT_CLIMATE_CONTROL_ACCESS_TOKEN = "CLIMATE_CONTROL_ACCESS_CODE";
    
    private final SharedPreferences mPreferences;
    private final KaaClient mClient;
    private final EventBus mEventBus;
    
    private ThermostatDevice mThermostatDevice;

    public ClimateController(Context context, KaaClient client, EventBus eventBus) {
        mPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        mClient = client;
        
        mEventBus = eventBus;
        mThermostatDevice = new ThermostatDevice(mPreferences, client, eventBus);
        mEventBus.register(this);
        mClient.setAttachedListener(this);
        mClient.setDetachedListener(this);
        mClient.setEndpointAccessToken(DEFAULT_CLIMATE_CONTROL_ACCESS_TOKEN);
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        if (!mClient.isAttachedToUser()) {
            mClient.attachUser("kaa", "dummy", this);
        } else {
            mEventBus.post(new UserAttachEvent());
        }
    }
    
    /*
     * Receive the result of the endpoint attach operation. 
     * Notify remote devices about availability in case of success.
     */
    @Override
    public void onAttachResult(UserAttachResponse response) {
        SyncResponseResultType result = response.getResult();
        if (result == SyncResponseResultType.SUCCESS) {
            mEventBus.post(new UserAttachEvent());
        } else {
            String error = response.getErrorReason();
            mEventBus.post(new UserAttachEvent(error));
        }
    }
    
    @Override
    public void onDetachedFromUser(String arg0) {
        mEventBus.post(new UserDetachEvent());
    }

    @Override
    public void onAttachedToUser(String arg0, String arg1) {
        mEventBus.post(new UserAttachEvent());
    }
    
    public ThermostatDevice getThermostatDevice() {
        return mThermostatDevice;
    }

}
