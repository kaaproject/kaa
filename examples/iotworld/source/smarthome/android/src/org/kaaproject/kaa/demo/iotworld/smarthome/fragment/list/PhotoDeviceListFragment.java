package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.PhotoDevice;

public class PhotoDeviceListFragment extends AbstractDeviceListFragment<PhotoDevice> {

    @Override
    protected String getTitle() {
        return getString(R.string.nav_photos);
    }
    
    @Override
    public String getFragmentTag() {
        return PhotoDeviceListFragment.class.getSimpleName();
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.PHOTO;
    }

}
