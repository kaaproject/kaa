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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.ClimateDevice;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

public class ClimateCard extends AbstractGeoFencingDeviceCard<ClimateDevice> {

    private TextView mCurrentDegreeView;
    private TextView mTargetDegreeView;
    private ImageView mStatusIconView;
    private TextView mStatusTextView;
    
    public ClimateCard(Context context) {
        super(context);
        mCurrentDegreeView = (TextView) findViewById(R.id.currentDegreeView);
        mTargetDegreeView = (TextView) findViewById(R.id.targetDegreeView);
        mStatusIconView = (ImageView) findViewById(R.id.statusIconView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
    }

    @Override
    protected int getCardLayout() {
        return R.layout.card_climate_device;
    }
    
    @Override
    public void bind(ClimateDevice device) {
        super.bind(device);
        ThermostatInfo thermostatInfo = device.getThermostatInfo();
        if (thermostatInfo != null) {
            setDetailsVisible(true);
            mCurrentDegreeView.setText(getResources().getString(R.string.degree_text, thermostatInfo.getDegree()));
            mTargetDegreeView.setText(getResources().getString(R.string.degree_text, thermostatInfo.getTargetDegree()));
            if (thermostatInfo.getIsOperating()) {
                if (thermostatInfo.getDegree() > thermostatInfo.getTargetDegree()) {
                    mStatusIconView.setImageResource(R.drawable.cooling_white);
                    mStatusTextView.setText(R.string.cooling);
                } else {
                    mStatusIconView.setImageResource(R.drawable.heating_white);
                    mStatusTextView.setText(R.string.heating);
                }
            } else {
                mStatusIconView.setImageResource(R.drawable.pause_white);
                mStatusTextView.setText(R.string.idle);
            }
        } else {
            setDetailsVisible(false);
        }
    }

}
