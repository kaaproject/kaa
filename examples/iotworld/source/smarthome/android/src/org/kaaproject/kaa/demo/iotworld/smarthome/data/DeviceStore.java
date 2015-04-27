package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceDiscoveryStarted;

import android.content.Context;
import android.util.Log;
import de.greenrobot.event.EventBus;

public class DeviceStore {
    
    private static final String TAG = DeviceStore.class.getSimpleName();

    private final Map<String, AbstractDevice> discoveredDevicesMap = new LinkedHashMap<>();
    private final Map<String, AbstractDevice> devicesMap = new LinkedHashMap<>();
    
    private final Context mContext;
    private final KaaClient mClient;
    private final EventBus mEventBus;
    
    private boolean isDeviceDiscoveryPerformed = false;
    
    public DeviceStore(Context context, KaaClient client, EventBus eventBus) {
        mContext = context;
        mClient = client;
        mEventBus = eventBus;
    }
    
    public void discoverDevices(boolean refresh, DeviceType discoveryDeviceType) {
        if (!isDeviceDiscoveryPerformed || refresh) {
            isDeviceDiscoveryPerformed = true;
            releaseDevices(discoveryDeviceType);
            mEventBus.post(new DeviceDiscoveryStarted());
            
            if (discoveryDeviceType == null) {
                for (final DeviceType deviceType : DeviceType.values()) {
                    discoverDeviceType(deviceType);
                }
            } else {
                discoverDeviceType(discoveryDeviceType);
            }
        }
    }
    
    public void attachDevice(String endpointAccessToken) {
        EndpointAccessToken accessToken = new EndpointAccessToken(endpointAccessToken);
        mClient.attachEndpoint(accessToken, new OnAttachEndpointOperationCallback() {
            @Override
            public void onAttach(SyncResponseResultType syncResponseResultType, EndpointKeyHash endpointKeyHash) {
                if (syncResponseResultType == SyncResponseResultType.SUCCESS) {
                    Log.d("Kaa", "Endpoint attached to user account! EndpointKeyHash = " + endpointKeyHash.getKeyHash());
                    discoverDevices(true, null);
                } else {
                    Log.e("Kaa", "Unable to attach endpoint to user account!");
                }
            }
        });
    }
    
    private void discoverDeviceType(final DeviceType discoveryDeviceType) {
        mClient.findEventListeners(discoveryDeviceType.getListenerFqns(), new FindEventListenersCallback() {
            @Override
            public void onEventListenersReceived(List<String> endpointKeys) {
                for (String endpointKey : endpointKeys) {
                    deviceDiscovered(endpointKey, discoveryDeviceType);
                }
            }
            @Override
            public void onRequestFailed() {
                Log.e(TAG, "Unable to discover " + discoveryDeviceType + " devices!");
            }
        });
    }
    
    private void releaseDevices(DeviceType type) {
        List<AbstractDevice> toReleaseList = new ArrayList<>(discoveredDevicesMap.values());
        for (AbstractDevice device : toReleaseList) {
            if (type == null || type == device.getDeviceType()) {
                device.releaseDevice();
            }
        }
        toReleaseList.clear();
        toReleaseList = new ArrayList<>(devicesMap.values());
        for (AbstractDevice device : toReleaseList) {
            if (type == null || type == device.getDeviceType()) {
                device.releaseDevice();
            }
        }
        toReleaseList.clear();
    }
    
    public void deviceDiscovered(String endpointKey, DeviceType deviceType) {
        AbstractDevice device = createDevice(endpointKey, deviceType);
        discoveredDevicesMap.put(endpointKey, device);
        device.initDevice();
    }
    
    protected void deviceInfoReceived(String endpointKey) {
        AbstractDevice device = discoveredDevicesMap.remove(endpointKey);
        if (device != null) {
            devicesMap.put(endpointKey, device);
        }
    }
    
    public void deviceRemoved(String endpointKey) {
        discoveredDevicesMap.remove(endpointKey);
        devicesMap.remove(endpointKey);
    }
    
    public Map<String, AbstractDevice> getDevicesMap() {
        return devicesMap;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AbstractDevice> T getDevice(int index) {
        T device = null;
        if (index >= 0 && index < devicesMap.values().size()) {
            device = (T) (new ArrayList<AbstractDevice>(devicesMap.values())).get(index);
        }
        return device;
    }
    
    public int getSize() {
        return devicesMap.size();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AbstractDevice> List<T> getDevices(DeviceType deviceType) {
        List<T> devices = new ArrayList<>();
        for (AbstractDevice device : devicesMap.values()) {
            if (device.getDeviceType() == deviceType) {
                devices.add((T) device);
            }
        }
        return devices;
    }
    
    private AbstractDevice createDevice(String endpointKey, DeviceType deviceType) {
        switch (deviceType) {
        case CLIMATE:
            return new ClimateDevice(endpointKey, this, mClient, mEventBus);
        case LIGHTNING:
            return new LightningDevice(endpointKey, this, mClient, mEventBus);
        case MUSIC:
            return new MusicDevice(endpointKey, this, mClient, mEventBus);
        case PHOTO:
            return new PhotoDevice(endpointKey, this, mClient, mEventBus);
        case IRRIGATION:
            return new IrrigationDevice(endpointKey, this, mClient, mEventBus);
        default:
            throw new UnsupportedOperationException("Unsupported device type: " + deviceType);
        }
    }
    
}
