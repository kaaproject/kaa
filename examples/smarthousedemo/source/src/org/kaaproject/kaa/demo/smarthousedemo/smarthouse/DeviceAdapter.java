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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SmartDeviceInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DeviceAdapter<T extends SmartDeviceInfo> extends BaseAdapter {

    private DeviceType mDeviceType;
    private DeviceStore mStore;
    private Context mContext;
    
    public DeviceAdapter(DeviceStore store, Context context, DeviceType deviceType) {
        mStore = store;
        mContext = context;
        mDeviceType = deviceType;
    }
    
    @Override
    public int getCount() {
        return mStore.getDevices(mDeviceType).size();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T getItem(int position) {
        if (position < getCount()) {
            return (T)mStore.getDevices(mDeviceType).get(position);
        }
        return null;
    }
    
    @Override
    public long getItemId(int position) {
        return mStore.getDevices(mDeviceType).get(position).getId();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) 
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.device_list_item_common, null);
            LinearLayout mDeviceInfoLayout = (LinearLayout)v.findViewById(R.id.deviceInfoLayout);
            View deviceInfoView = inflater.inflate(getLayoutResId(), null);
            mDeviceInfoLayout.addView(deviceInfoView, 
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        T entry = (T)mStore.getDevices(mDeviceType).get(position);
        TextView deviceNameView = (TextView) v.findViewById(R.id.deviceName);
        View deviceStatusView = (View) v.findViewById(R.id.deviceStatus);
        deviceNameView.setText(entry.getDeviceName());
        deviceStatusView.setBackgroundResource(entry.getDeviceStatus().getDrawableRes());
        setAdditionalInfo(v, entry);
        return v;
    }
    
    protected int getLayoutResId() {
        return R.layout.device_list_item;
    }
    
    protected void setAdditionalInfo(View v, T entry) {
    }
}
