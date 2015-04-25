package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.LightEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.light.BulbControlRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbListRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbListStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;

import de.greenrobot.event.EventBus;

public class LightningDevice extends AbstractGeoFencingDevice implements LightEventClassFamily.Listener {

    private final LightEventClassFamily mLightEventClassFamily;
    
    private List<BulbInfo> mBulbs;
    
    public LightningDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mLightEventClassFamily = mClient.getEventFamilyFactory().getLightEventClassFamily();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mLightEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mLightEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mLightEventClassFamily.sendEvent(new BulbListRequest(), mEndpointKey);
    }
    
    public List<BulbInfo> getBulbs() {
        return mBulbs;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.LIGHTNING;
    }

    @Override
    public void onEvent(BulbListStatusUpdate bulbListStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mBulbs = bulbListStatusUpdate.getBulbs();
            fireDeviceUpdated();
        }
    }
    
    private BulbInfo getBulbById(String bulbId) {
        if (mBulbs != null) {
            for (BulbInfo bulb : mBulbs) {
                if (bulb.getBulbId().equals(bulbId)) {
                    return bulb;
                }
            }
        }
        return null;
    }
    
    public void changeBulbBrightness(String bulbId, int brightness) {
        BulbInfo bulb = getBulbById(bulbId);
        if (bulb != null) {
            controlBulb(bulbId, bulb.getStatus(), brightness);
        }
    }
    
    public void changeBulbState(String bulbId, BulbStatus status) {
        BulbInfo bulb = getBulbById(bulbId);
        if (bulb != null) {
            controlBulb(bulbId, status, bulb.getBrightness());
        }
    }
    
    public void controlBulb(String bulbId, BulbStatus status, int brightness) {
        mLightEventClassFamily.sendEvent(new BulbControlRequest(bulbId, status, brightness), mEndpointKey);
    }

}
