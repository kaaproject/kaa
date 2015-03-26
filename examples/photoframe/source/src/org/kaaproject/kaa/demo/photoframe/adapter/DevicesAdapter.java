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

package org.kaaproject.kaa.demo.photoframe.adapter;

import org.kaaproject.kaa.demo.photoframe.DeviceInfo;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameController;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The implementation of the {@link BaseAdapter} class. Used as an adapter class for the devices list view.
 * Provides list item views with the information about remote devices.
 */
public class DevicesAdapter extends BaseAdapter {
    
    private final Context mContext;
    private final PhotoFrameController mController;
    
    public DevicesAdapter(Context context, PhotoFrameController controller) {
        mContext = context;
        mController = controller;
    }

    @Override
    public int getCount() {
        return mController.getRemoteDevicesMap().size();
    }

    @Override
    public Object getItem(int position) {
        return mController.getRemoteDevicesMap().values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.device_list_item, null);
        }
        TextView modelNameView = (TextView) v.findViewById(R.id.modelName);
        DeviceInfo deviceInfo = (DeviceInfo)getItem(position);
        modelNameView.setText(deviceInfo.getModel());
        
        TextView manufacturerNameView = (TextView) v.findViewById(R.id.manufacturerName);        
        String byManufacturer = mContext.getString(R.string.by_pattern, deviceInfo.getManufacturer());
        manufacturerNameView.setText(byManufacturer);
        
        TextView playStatusView = (TextView) v.findViewById(R.id.playStatus);     
        String endpointKey = (String) mController.getRemoteDevicesMap().keySet().toArray()[position];
        PlayInfo playInfo = mController.getRemoteDeviceStatus(endpointKey);
        if (playInfo != null) {
            if (playInfo.getStatus() == PlayStatus.STOPPED) {
                playStatusView.setText(R.string.stopped);
            } else {
                playStatusView.setText(mContext.getString(R.string.playing, playInfo.getCurrentAlbumInfo().getTitle()));
            }
        } else {
            playStatusView.setText(R.string.unknown);
        }
        
        return v;
    }

}
