package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.ClimateDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.Thermostat;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.Thermostat.OnThermostatChangeListener;
import org.kaaproject.kaa.demo.iotworld.thermo.ThermostatInfo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ClimateDeviceFragment extends AbstractGeoFencingDeviceFragment<ClimateDevice> implements OnThermostatChangeListener {
    
    private TextView mTemperatureText;
    private Thermostat mThermostat;
    
    public ClimateDeviceFragment() {
        super();
    }

    public ClimateDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_climate_device;
    }

    @Override
    public String getFragmentTag() {
        return ClimateDeviceFragment.class.getSimpleName();
    }
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        mTemperatureText = (TextView) rootView.findViewById(R.id.temperatureText);
        mThermostat = (Thermostat) rootView.findViewById(R.id.thermostat);
    }

    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        ThermostatInfo thermostatInfo = mDevice.getThermostatInfo();
        if (thermostatInfo != null) {
            mTemperatureText.setText(getResources().getString(R.string.climate_device_temperature_text,
                                thermostatInfo.getDegree(), thermostatInfo.getTargetDegree()));
            mThermostat.setTemp(thermostatInfo.getDegree());
            if (!thermostatInfo.getIgnoreDegreeUpdate() || firstLoad) {
                mThermostat.setTargetTemp(thermostatInfo.getTargetDegree(), false);
            }
        }
        if (firstLoad) {
            mThermostat.setOnThermostatChangeListener(this);
        }
    }

    @Override
    public void onProgressChanged(Thermostat thermostat, int progress, boolean fromUser) {}

    @Override
    public void onTargetProgressChanged(Thermostat thermostat, int targetProgress, boolean fromUser) {
        mDevice.changeDegree(mThermostat.getTargetTemp());
    }

    @Override
    public void onStartTrackingTouch(Thermostat thermostat) {}

    @Override
    public void onStopTrackingTouch(Thermostat thermostat) {}

}
