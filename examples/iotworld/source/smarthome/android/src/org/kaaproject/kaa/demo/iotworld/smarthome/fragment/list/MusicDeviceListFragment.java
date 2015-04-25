package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;

public class MusicDeviceListFragment extends AbstractDeviceListFragment<MusicDevice> {

    @Override
    protected String getTitle() {
        return getString(R.string.nav_music);
    }
    
    @Override
    public String getFragmentTag() {
        return MusicDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.MUSIC;
    }

}
