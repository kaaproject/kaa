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
