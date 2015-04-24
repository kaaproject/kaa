package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.IrrigationDevice;

public class IrrigationDeviceListFragment extends AbstractDeviceListFragment<IrrigationDevice> {

    @Override
    protected String getTitle() {
        return getString(R.string.nav_irrigation);
    }
    
    @Override
    public String getFragmentTag() {
        return IrrigationDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.IRRIGATION;
    }

}
