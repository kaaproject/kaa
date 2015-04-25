package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;

import android.content.Context;
import android.widget.TextView;

public abstract class AbstractGeoFencingDeviceCard<T extends AbstractGeoFencingDevice> extends AbstractDeviceCard<T> {
    
    protected TextView mGeofencingStatusView;

    public AbstractGeoFencingDeviceCard(Context context) {
        super(context);
        mGeofencingStatusView = (TextView) findViewById(R.id.geoFencingStatus);
    }

    @Override
    public void bind(T device) {
        super.bind(device);
        OperationMode mode = device.getOperationMode();
        if (mode == null) {
            mode = OperationMode.OFF;
        } 
        mGeofencingStatusView.setText(getResources().getStringArray(R.array.geofencing_status)[mode.ordinal()]);
    }
}
