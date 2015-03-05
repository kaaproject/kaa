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

package org.kaaproject.kaa.demo.photoframe.fragment;

import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.DevicesAdapter;
import org.kaaproject.kaa.demo.photoframe.event.DeviceInfoEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayInfoEvent;

import android.os.Bundle;
import android.widget.BaseAdapter;

public class DevicesFragment extends ListFragment {
    
    public DevicesFragment() {
        super();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onRefresh();
    }
    
    public void onEventMainThread(DeviceInfoEvent deviceInfoEvent) {
        notifyDataChanged();
    }
    
    public void onEventMainThread(PlayInfoEvent playInfoEvent) {
        notifyDataChanged();
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new DevicesAdapter(mActivity, mController);
    }

    @Override
    protected String getNoDataText() {
        return getString(R.string.no_devices);
    }
    
    protected String getTitle() {
        return getString(R.string.devices);
    }

    @Override
    protected void onRefresh() {
        mController.discoverRemoteDevices();
    }

    @Override
    protected void onItemClicked(int position) {
        String endpointKey = (String)mController.getRemoteDevicesMap().keySet().toArray()[position];
        AlbumsFragment albumsFragment = new AlbumsFragment(endpointKey);
        mActivity.addBackStackFragment(albumsFragment, albumsFragment.getFragmentTag());
    }

    @Override
    public String getFragmentTag() {
        return DevicesFragment.class.getSimpleName();
    }
    
    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

}
