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

import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.IrrigationDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class IrrigationDeviceListAdapter extends AbstractDeviceListAdapter<IrrigationDevice> {

    public IrrigationDeviceListAdapter(
            RecyclerView recyclerView,
            DeviceStore deviceStore,
            DeviceSelectionListener deviceSelectionListener) {
        super(recyclerView, deviceStore, deviceSelectionListener);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.IRRIGATION;
    }

    @Override
    protected int getDeviceListItemLayoutResource() {
        return R.layout.irrigation_device_list_item;
    }

    @Override
    protected AbstractDeviceListAdapter.ViewHolder<IrrigationDevice> constructViewHolder(View v) {
        return new ViewHolder(v);
    }
    
    static class ViewHolder extends AbstractDeviceListAdapter.ViewHolder<IrrigationDevice> {

        private TextView irrigationStatusTextView;
        private TextView scheduleTimeTextView;
        private TextView spentThisMonthTextView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            irrigationStatusTextView = (TextView) itemView.findViewById(R.id.irrigationStatusText);
            scheduleTimeTextView = (TextView) itemView.findViewById(R.id.scheduleTimeText);
            spentThisMonthTextView = (TextView) itemView.findViewById(R.id.spentThisMonthText);
        }

        @Override
        protected boolean showContent(IrrigationDevice device) {
            return device.getIrrigationStatus() != null;
        }

        @Override
        protected void bindDeviceDetails(IrrigationDevice device) {
            IrrigationStatus irrigationStatus = device.getIrrigationStatus();
            irrigationStatusTextView.setText(
                    irrigationStatusTextView.getResources().getStringArray(
                            R.array.irrigation_status)[irrigationStatus.getIsIrrigation() ? 1 : 0]);
            scheduleTimeTextView.setText(
                    scheduleTimeTextView.getResources().getString(R.string.irrigation_scheduled_text, TimeUtils.secondsToTimer(irrigationStatus.getIrrigationIntervalSec(), true)));
            float mounthlySpent = (float)irrigationStatus.getMonthlySpentWater() / 1000f;
            spentThisMonthTextView.setText(spentThisMonthTextView.getResources().getString(R.string.water_spent_this_month_text, mounthlySpent));
        }
        
    }
}
