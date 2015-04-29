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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.home;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.AbstractDeviceCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.AddDeviceCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.ClimateCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.IrrigationCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.LightningCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.MusicCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.PhotoCard;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView.OnContextMenuListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView.OnItemClickListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.PressableAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class HomeAdapter extends PressableAdapter<HomeAdapter.ViewHolder> 
        implements OnItemClickListener, OnContextMenuListener {

    private final AutoSpanRecyclerView mRecyclerView;
    private final DeviceStore mDeviceStore;
    private final DeviceSelectionListener mDeviceSelectionListener;
    
    private AbstractDevice mContextMenuDevice;
    
    public HomeAdapter(AutoSpanRecyclerView recyclerView, DeviceStore deviceStore, DeviceSelectionListener deviceSelectionListener) {
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(this);
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setOnContextMenuListener(this);
        mDeviceStore = deviceStore;
        mDeviceSelectionListener = deviceSelectionListener;
    }
    
    @Override
    public int getItemCount() {
        return mDeviceStore.getSize() + 1;
    }
    
    @Override
    public int getItemViewType(int position) { 
        if (position == mDeviceStore.getSize()) {
            return DeviceType.values().length;
        } else {
            return mDeviceStore.getDevice(position).getDeviceType().ordinal();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.mHolderId < DeviceType.values().length) {
            AbstractDevice device = mDeviceStore.getDevice(position);
            if (device != null) {
                holder.bind(device);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View card = null;
        if (viewType == DeviceType.values().length) {
            card = new AddDeviceCard(parent.getContext());
        } else {
            DeviceType deviceType = DeviceType.values()[viewType];
            switch (deviceType) {
            case CLIMATE:
                card = new ClimateCard(parent.getContext());
                break;
            case LIGHTNING:
                card = new LightningCard(parent.getContext());
                break;
            case MUSIC:
                card = new MusicCard(parent.getContext());
                break;
            case PHOTO:
                card = new PhotoCard(parent.getContext());
                break;
            case IRRIGATION:
                card = new IrrigationCard(parent.getContext());
                break;
            default:
                break;
            }
        }
        FontUtils.setRobotoFont(card);
        ViewHolder vhCard = new ViewHolder(card,viewType);
        return vhCard;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        int mHolderId;
        
        private View mCard;
        
        public ViewHolder(View card, int viewType) {
            super(card);         
            mCard = card;
            mHolderId = viewType;
        }
        
        @SuppressWarnings("unchecked")
        protected void bind(AbstractDevice device) {
            ((AbstractDeviceCard<AbstractDevice>)mCard).bind(device);
        }
        
    }

    @Override
    public void onItemClick(AutoSpanRecyclerView parent, View view, int itemPosition, long id) {
        if (itemPosition == mDeviceStore.getSize()) {
            mDeviceSelectionListener.onAddDeviceSelected();
        } else {
            AbstractDevice device = mDeviceStore.getDevice(itemPosition);
            mDeviceSelectionListener.onDeviceSelected(device.getDeviceType(), device.getEndpointKey());
        }
    }

    @Override
    public void onCreateContextMenu(AutoSpanRecyclerView parent, ContextMenu menu, View v, int itemPosition, long id) {
        if (itemPosition < mDeviceStore.getSize()) {
            mContextMenuDevice = mDeviceStore.getDevice(itemPosition);
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
        }
    }
    
    public AbstractDevice getContextMenuDevice() {
        return mContextMenuDevice;
    }
 
    public static interface DeviceSelectionListener {
        
        public void onDeviceSelected(DeviceType deviceType, String endpointKey);
        
        public void onAddDeviceSelected();
        
    }

}
