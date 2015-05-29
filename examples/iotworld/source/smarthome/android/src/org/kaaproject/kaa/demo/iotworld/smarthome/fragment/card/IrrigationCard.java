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

import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.IrrigationDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.content.Context;
import android.widget.TextView;

public class IrrigationCard extends AbstractDeviceCard<IrrigationDevice> {

    private TextView mIrrigationStatusTextView;
    private TextView mIrrigationScheduleTextView;
    private TextView mSpentThisMonthView;
    
    public IrrigationCard(Context context) {
        super(context);
        mIrrigationStatusTextView = (TextView) findViewById(R.id.irrigationStatusTextView);
        mIrrigationScheduleTextView = (TextView) findViewById(R.id.irrigationScheduleTextView);
        mSpentThisMonthView = (TextView) findViewById(R.id.spentThisMonthView);
    }

    @Override
    protected int getCardLayout() {
        return R.layout.card_irrigation_device;
    }
    
    @Override
    public void bind(IrrigationDevice device) {
        super.bind(device);
        IrrigationStatus irrigationStatus = device.getIrrigationStatus();
        if (irrigationStatus != null) {
            setDetailsVisible(true);
            mIrrigationStatusTextView.setText(
                    getResources().getStringArray(R.array.irrigation_status)[irrigationStatus.getIsIrrigation() ? 1 : 0]);
            mIrrigationScheduleTextView.setText(
                    getResources().getString(R.string.irrigation_scheduled_text, TimeUtils.secondsToTimer(irrigationStatus.getIrrigationIntervalSec(), true)));
            float mounthlySpent = (float)irrigationStatus.getMonthlySpentWater() / 1000f;
            mSpentThisMonthView.setText(getResources().getString(R.string.water_spent_this_month_text, mounthlySpent));
        } else {
            setDetailsVisible(false);
        }
    }

}
