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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter.AbstractDeviceListAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter.MusicDeviceListAdapter;

import android.support.v7.widget.RecyclerView;

public class MusicDeviceListFragment extends AbstractDeviceListFragment<MusicDevice> {

    @Override
    protected String getTitle() {
        return getString(R.string.nav_music);
    }
    
    @Override
    public String getFragmentTag() {
        return MusicDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.MUSIC;
    }

    @Override
    protected AbstractDeviceListAdapter<MusicDevice> createListAdapter(RecyclerView recyclerView) {
        return new MusicDeviceListAdapter(recyclerView, mDeviceStore, this);
    }

}
