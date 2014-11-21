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

import org.kaaproject.kaa.demo.smarthousedemo.SmartHouseActivity;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SmartDeviceInfo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public abstract class BaseControlFragment<T extends SmartDeviceInfo> extends Fragment {

    private static final String DEV_POSITION = "dev_position";
    
    protected SmartHouseActivity mActivity;
    protected T mDevice;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public static BaseControlFragment<?> newInstance(final DeviceType deviceType, int position) {
        Bundle args = new Bundle();
        args.putInt(DEV_POSITION, position);
        BaseControlFragment<?> result = null;
        switch (deviceType) {
        case THERMOSTAT:
            result = new ThermostatControlFragment();
            break;
        case TV:
            result = new TvControlFragment();
            break;
        case SOUND_SYSTEM:
            result = new SoundSystemControlFragment();
            break;
        case LAMP:
            result = new LampControlFragment();
            break;
            default:
                result = new BaseControlFragment<SmartDeviceInfo>() {

                    @Override
                    public DeviceType getDeviceType() {
                        return deviceType;
                    }
                    
                };
                break;
        }
        if (result != null) {
            result.setArguments(args);
        }
        return result;
    }
    
    public abstract DeviceType getDeviceType();
    
    @Override
    public void onDestroy() {
        mActivity.getDeviceStore().getEventBus().unregister(this);
        mActivity = null;
        super.onDestroy();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (SmartHouseActivity) activity;
        mActivity.resetActionBar();
        int position = getArguments().getInt(DEV_POSITION);
        mDevice = (T) mActivity.getDeviceStore().getDevices(getDeviceType()).get(position);
        getActionBar().setTitle(mDevice.getDeviceName());
        if (!mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().register(this);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void onEventMainThread(DeviceStore.DeviceUpdated deviceUpdated) {
        if (mDevice.getEndpointKey().equals(deviceUpdated.device.getEndpointKey())) {
            mDevice = (T)deviceUpdated.device;
            onDeviceInfoUpdated();
        }
    }
    
    protected void onDeviceInfoUpdated() {}
    
    protected ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
    
}
