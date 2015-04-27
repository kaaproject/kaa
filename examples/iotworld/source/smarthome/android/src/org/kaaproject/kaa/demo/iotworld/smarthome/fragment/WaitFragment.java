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

package org.kaaproject.kaa.demo.iotworld.smarthome.fragment;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.KaaStartedEvent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * The implementation of the {@link AbstractSmartHomeFragment} class. 
 * Used to display the busy progress view.
 */
public class WaitFragment extends AbstractSmartHomeFragment {
    
    public WaitFragment() {
        super();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_wait, container,
                false);
        
        setupView(rootView);
        
        return rootView;
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        checkEvent(kaaStarted);
    }
    
    protected String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

    @Override
    public String getFragmentTag() {
        return WaitFragment.class.getSimpleName();
    }

}
