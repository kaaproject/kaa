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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter;

import java.util.List;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.PressableAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class AbstractDeviceListAdapter<T extends AbstractDevice> extends 
        PressableAdapter<AbstractDeviceListAdapter.ViewHolder<T>> {

    private final RecyclerView mRecyclerView;
    private final DeviceSelectionListener mDeviceSelectionListener;
    private final DeviceStore mDeviceStore;
    
    private final DeviceItemClickListener mDeviceItemClickListener = new DeviceItemClickListener();
    private final DeviceOnContextMenuCreateListener mDeviceOnContextMenuCreateListener = new DeviceOnContextMenuCreateListener();
    
    private AbstractDevice mContextMenuDevice;
    
    public AbstractDeviceListAdapter(RecyclerView recyclerView, 
                            DeviceStore deviceStore,
                            DeviceSelectionListener deviceSelectionListener) {
        mRecyclerView = recyclerView;
        mDeviceStore = deviceStore;
        mRecyclerView.setAdapter(this);
        mDeviceSelectionListener = deviceSelectionListener;
    }
    
    @Override
    public int getItemCount() {
        List<T> devices = mDeviceStore.getDevices(getDeviceType());
        if (devices != null) {
            return devices.size();
        } else {
            return 0;
        }
    }
 
    @Override
    public void onBindViewHolder(ViewHolder<T> holder, int position) {
        List<T> devices = mDeviceStore.getDevices(getDeviceType());
        if (devices != null) {
            T device = devices.get(position);
            if (device != null) {
                holder.bind(device);
            }
        }
    }

    @Override
    public ViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getDeviceListItemLayoutResource(),parent,false);
        FontUtils.setRobotoFont(v);
        ViewHolder<T> vhItem = constructViewHolder(v);
        v.setOnClickListener(mDeviceItemClickListener);
        v.setOnCreateContextMenuListener(mDeviceOnContextMenuCreateListener);
        return vhItem;
    }
    
    protected abstract DeviceType getDeviceType();
    
    protected abstract int getDeviceListItemLayoutResource();
    
    protected abstract ViewHolder<T> constructViewHolder(View v);
    
    static abstract class ViewHolder<T extends AbstractDevice> extends RecyclerView.ViewHolder {

        private TextView deviceTitleView;
        private View deviceListItemNoInfoLayout;
        private View deviceDetailsLayout;
        
        public ViewHolder(View itemView) {
            super(itemView);         
            deviceTitleView = (TextView) itemView.findViewById(R.id.deviceTitle);
            deviceListItemNoInfoLayout = itemView.findViewById(R.id.deviceListItemNoInfoLayout);
            deviceDetailsLayout = itemView.findViewById(R.id.deviceDetailsLayout);
        }
        
        public void bind(T device) {
            deviceTitleView.setText(device.getDeviceInfo().getName());
            if (showContent(device)) {
                deviceListItemNoInfoLayout.setVisibility(View.GONE);
                deviceDetailsLayout.setVisibility(View.VISIBLE);
                bindDeviceDetails(device);
            } else {
                deviceListItemNoInfoLayout.setVisibility(View.VISIBLE);
                deviceDetailsLayout.setVisibility(View.GONE);
            }
        }
        
        protected abstract boolean showContent(T device);
        
        protected abstract void bindDeviceDetails(T device);
    }
    
    class DeviceItemClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);
            List<T> devices = mDeviceStore.getDevices(getDeviceType());
            if (devices != null) {
                T device = devices.get(position);
                if (device != null) {
                    mDeviceSelectionListener.onDeviceSelected(getDeviceType(), device.getEndpointKey());
                }
            }
        }
    }
    
    class DeviceOnContextMenuCreateListener implements OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            int position = mRecyclerView.getChildPosition(v);
            List<T> devices = mDeviceStore.getDevices(getDeviceType());
            if (devices != null) {
                mContextMenuDevice = devices.get(position);
                if (mContextMenuDevice != null && mContextMenuDevice.getDeviceInfo() != null) {
                    menu.setHeaderTitle(v.getResources().getString(R.string.actions_for_device_title, 
                            mContextMenuDevice.getDeviceInfo().getName()));
                    menu.add(Menu.NONE, R.id.ctx_menu_refresh_device,Menu.NONE,R.string.refresh);
                    menu.add(Menu.NONE, R.id.ctx_menu_rename_device,Menu.NONE,R.string.rename);
                    if (mContextMenuDevice instanceof AbstractGeoFencingDevice) {
                        menu.add(Menu.NONE, R.id.ctx_menu_change_mode,Menu.NONE,R.string.change_mode);
                    }
                    menu.add(Menu.NONE, R.id.ctx_menu_detach_device,Menu.NONE,R.string.detach);
                }
            } else {
                mContextMenuDevice = null;
            }
        }
    }
    
    public AbstractDevice getContextMenuDevice() {
        return mContextMenuDevice;
    }
    
    public static interface DeviceSelectionListener {
        
        public void onDeviceSelected(DeviceType deviceType, String endpointKey);
        
    }
    
}
