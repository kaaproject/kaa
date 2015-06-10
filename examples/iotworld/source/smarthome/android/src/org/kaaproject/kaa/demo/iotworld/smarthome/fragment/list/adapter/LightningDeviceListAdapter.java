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

import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class LightningDeviceListAdapter extends AbstractGeoFencingDeviceListAdapter<LightningDevice> {

    public LightningDeviceListAdapter(
            RecyclerView recyclerView,
            DeviceStore deviceStore,
            DeviceSelectionListener deviceSelectionListener) {
        super(recyclerView, deviceStore, deviceSelectionListener);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.LIGHTNING;
    }

    @Override
    protected int getDeviceListItemLayoutResource() {
        return R.layout.lightning_device_list_item;
    }

    @Override
    protected AbstractDeviceListAdapter.ViewHolder<LightningDevice> constructViewHolder(View v) {
        return new ViewHolder(v);
    }
    
    static class ViewHolder extends AbstractGeoFencingDeviceListAdapter.ViewHolder<LightningDevice> {

        private TextView bulbsOnTextView;
        private TextView bulbsOffTextView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            bulbsOnTextView = (TextView) itemView.findViewById(R.id.bulbsOnText);
            bulbsOffTextView = (TextView) itemView.findViewById(R.id.bulbsOffText);
        }

        @Override
        protected boolean showContent(LightningDevice device) {
            return device.getBulbs() != null;
        }

        @Override
        protected void bindDeviceDetails(LightningDevice device) {
            List<BulbInfo> bulbs = device.getBulbs();
            int bulbsOn = 0;
            for (BulbInfo bulb : bulbs) {
                if (bulb.getStatus() == BulbStatus.ON) {
                    bulbsOn++;
                }
            }
            int bulbsOff = bulbs.size() - bulbsOn;
            bulbsOnTextView.setText(bulbsOnTextView.getResources().getString(R.string.bulbs_on_text, bulbsOn));
            bulbsOffTextView.setText(bulbsOffTextView.getResources().getString(R.string.bulbs_off_text, bulbsOff));
        }
        
    }
}
