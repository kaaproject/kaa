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
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceRemovedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.AbstractSmartHomeFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.ClimateDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.IrrigationDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.LightningDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.PhotoDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music.MusicAlbumsDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter.AbstractDeviceListAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter.AbstractDeviceListAdapter.DeviceSelectionListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class AbstractDeviceListFragment<D extends AbstractDevice> 
        extends AbstractSmartHomeFragment implements DeviceSelectionListener {

    private TextView mNoDataText;
    private RecyclerView mRecyclerView;
    private AbstractDeviceListAdapter<D> mDeviceListAdapter;
    
    public AbstractDeviceListFragment() {
        super();
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_device_list, container,
                false);
        setupView(rootView);
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mNoDataText.setText(R.string.no_devices);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        mDeviceListAdapter = createListAdapter(mRecyclerView);
        
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        
        showContent();
        
        notifyDataChanged();
        
        return rootView;
    }
    
    public void onEventMainThread(DeviceUpdatedEvent deviceUpdatedEvent) {
        if (deviceUpdatedEvent.getDeviceType() == getDeviceType()) {
            notifyDataChanged();
        }
    }
    
    public void onEventMainThread(DeviceRemovedEvent deviceRemovedEvent) {
        if (deviceRemovedEvent.getDeviceType() == getDeviceType()) {
            notifyDataChanged();
        }
    }
    
    protected void notifyDataChanged() {
        mDeviceListAdapter.notifyDataSetChanged();
        if (mDeviceListAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            discoverDevices();
            notifyDataChanged();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    
    private void discoverDevices() {
        mDeviceStore.discoverDevices(true, getDeviceType());
    }
    
    @Override
    protected int getBarsBackgroundColor() {
        return getResources().getColor(getDeviceType().getBaseColorResId());
    }
    
    protected abstract DeviceType getDeviceType();
    
    protected abstract AbstractDeviceListAdapter<D> createListAdapter(RecyclerView recyclerView);
    
    @Override
    protected boolean showNavigationDrawer() {
        return true;
    }
    
    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AbstractDevice contextMenuDevice = mDeviceListAdapter.getContextMenuDevice();
        if (contextMenuDevice != null) {
            switch (item.getItemId()) {
            case R.id.ctx_menu_refresh_device:
                contextMenuDevice.requestDeviceInfo();
                break;
            case R.id.ctx_menu_rename_device:
                contextMenuDevice.renameDevice(mActivity);
                break;
            case R.id.ctx_menu_change_mode:
                if (contextMenuDevice instanceof AbstractGeoFencingDevice) {
                    ((AbstractGeoFencingDevice)contextMenuDevice).changeDeviceMode(mActivity);
                }
                break;                
            case R.id.ctx_menu_detach_device:
                contextMenuDevice.detach();
                break;
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDeviceSelected(DeviceType deviceType, String endpointKey) {
        Fragment fragment = null;
        switch (deviceType) {
        case CLIMATE:
            fragment = new ClimateDeviceFragment(endpointKey);
            break;
        case LIGHTNING:
            fragment = new LightningDeviceFragment(endpointKey);
            break;
        case MUSIC:
            fragment = new MusicAlbumsDeviceFragment(endpointKey);
            break;
        case PHOTO:
            fragment = new PhotoDeviceFragment(endpointKey);
            break;
        case IRRIGATION:
            fragment = new IrrigationDeviceFragment(endpointKey);
            break;
        default:
            break;
        }
        mActivity.addBackStackFragment(fragment, fragment.getTag());        
    }

}
