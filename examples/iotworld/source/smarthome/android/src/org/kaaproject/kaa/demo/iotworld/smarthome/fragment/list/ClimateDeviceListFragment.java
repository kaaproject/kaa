package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.ClimateDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;

public class ClimateDeviceListFragment extends AbstractDeviceListFragment<ClimateDevice> {
    
    @Override
    protected String getTitle() {
        return getString(R.string.nav_climate);
    }
    
    @Override
    public String getFragmentTag() {
        return ClimateDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.CLIMATE;
    }

}
