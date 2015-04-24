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
            if (thermostatInfo.getDegree() > thermostatInfo.getTargetDegree()) {
                mStatusIconView.setImageResource(R.drawable.climate_status_cooling);
                mStatusTextView.setText(R.string.cooling);
            } else if (thermostatInfo.getDegree() < thermostatInfo.getTargetDegree()) {
                mStatusIconView.setImageResource(R.drawable.climate_status_heating);
                mStatusTextView.setText(R.string.heating);
            } else {
                mStatusIconView.setImageResource(R.drawable.climate_status_idle);
                mStatusTextView.setText(R.string.idle);
            }
        } else {
            setDetailsVisible(false);
        }
    }

}
