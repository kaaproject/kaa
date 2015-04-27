package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;

public class LightningDeviceListFragment extends AbstractDeviceListFragment<LightningDevice> {

    @Override
    protected String getTitle() {
        return getString(R.string.nav_lightning);
    }
    
    @Override
    public String getFragmentTag() {
        return LightningDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.LIGHTNING;
    }

}
