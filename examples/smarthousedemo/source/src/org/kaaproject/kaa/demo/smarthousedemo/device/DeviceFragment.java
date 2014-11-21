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

import org.kaaproject.kaa.demo.smarthousedemo.BaseDeviceActivity;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceFragment extends Fragment {

    public static final String DEVICE_TYPE = "device_type";
    
    protected BaseDeviceActivity mActivity;
 
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static DeviceFragment newInstance(DeviceType deviceType) {
        DeviceFragment fragment;
        switch (deviceType) {
        case THERMOSTAT:
            fragment = new ThermostatFragment();
            break;
        case SOUND_SYSTEM:
            fragment = new SoundSystemFragment();
            break;
        default:
            fragment = new DeviceFragment();
            break;
        }
        Bundle args = new Bundle();
        args.putSerializable(DEVICE_TYPE, deviceType);
        fragment.setArguments(args);
        return fragment;
    }

    public DeviceFragment() {
    }
    
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseDeviceActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device, container,
                false);
        TextView deviceText = (TextView)rootView.findViewById(R.id.deviceText);
        
        Bundle args = this.getArguments();  
        DeviceType deviceType = (DeviceType)args.getSerializable(DEVICE_TYPE);
        deviceText.setText(deviceType.getNameRes());
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    public void handleEvent(Object event, String endpointSourceKey) {
        
    }
    
    public void onKeyDown(int keyCode) {
    	
    }
}
