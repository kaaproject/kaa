package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.GeoFencingEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse;
import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;

import de.greenrobot.event.EventBus;

public abstract class AbstractGeoFencingDevice extends AbstractDevice implements GeoFencingEventClassFamily.Listener {

    private final GeoFencingEventClassFamily mGeoFencingEventClassFamily;
    
    private OperationMode mOperationMode;
    
    public AbstractGeoFencingDevice(String endpointKey,
                                    DeviceStore deviceStore, 
                                    KaaClient client, 
                                    EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mGeoFencingEventClassFamily = mClient.getEventFamilyFactory().getGeoFencingEventClassFamily();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mGeoFencingEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mGeoFencingEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mGeoFencingEventClassFamily.sendEvent(new GeoFencingStatusRequest(), mEndpointKey);
    }

    @Override
    public void onEvent(GeoFencingStatusResponse geoFencingStatusResponse, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mOperationMode = geoFencingStatusResponse.getMode();
            fireDeviceUpdated();
        }
    }
    
    public OperationMode getOperationMode() {
        return mOperationMode;
    }
    
    public void changeOperationMode(OperationMode newOperationMode) {
        mGeoFencingEventClassFamily.sendEvent(new OperationModeUpdateRequest(newOperationMode), mEndpointKey);
    }

}
