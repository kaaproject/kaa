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

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.ClimateDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ClimateDeviceListAdapter extends AbstractGeoFencingDeviceListAdapter<ClimateDevice> {

    public ClimateDeviceListAdapter(
            RecyclerView recyclerView,
            DeviceStore deviceStore,
            DeviceSelectionListener deviceSelectionListener) {
        super(recyclerView, deviceStore, deviceSelectionListener);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.CLIMATE;
    }

    @Override
    protected int getDeviceListItemLayoutResource() {
        return R.layout.climate_device_list_item;
    }

    @Override
    protected AbstractDeviceListAdapter.ViewHolder<ClimateDevice> constructViewHolder(View v) {
        return new ViewHolder(v);
    }
    
    static class ViewHolder extends AbstractGeoFencingDeviceListAdapter.ViewHolder<ClimateDevice> {

        private TextView temperatureTextView;
        private ImageView climateStatusImageView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            temperatureTextView = (TextView) itemView.findViewById(R.id.temperatureText);
            climateStatusImageView = (ImageView) itemView.findViewById(R.id.climateStatusImage);
        }

        @Override
        protected boolean showContent(ClimateDevice device) {
            return device.getThermostatInfo() != null;
        }

        @Override
        protected void bindDeviceDetails(ClimateDevice device) {
            ThermostatInfo thermostatInfo = device.getThermostatInfo();            
            temperatureTextView.setText(temperatureTextView.getResources().getString(R.string.climate_device_temperature_text,
                    thermostatInfo.getDegree(), thermostatInfo.getTargetDegree()));
            if (thermostatInfo.getIsOperating()) {
                if (thermostatInfo.getDegree() > thermostatInfo.getTargetDegree()) {
                    climateStatusImageView.setImageResource(R.drawable.cooling);
                } else {
                    climateStatusImageView.setImageResource(R.drawable.heating);
                }
            } else {
                climateStatusImageView.setImageResource(R.drawable.idle);
            }
        }
        
    }
}
