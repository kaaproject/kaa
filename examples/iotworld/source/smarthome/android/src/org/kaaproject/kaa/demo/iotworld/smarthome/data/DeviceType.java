/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import static org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType.CommonDeviceListenersFqns.deviceFqns;
import static org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType.CommonDeviceListenersFqns.geoFencingDeviceFqns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.demo.iotworld.device.DeviceChangeNameRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceInfoRequest;
import org.kaaproject.kaa.demo.iotworld.device.DeviceStatusSubscriptionRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.irrigation.IrrigationControlRequest;
import org.kaaproject.kaa.demo.iotworld.irrigation.StartIrrigationRequest;
import org.kaaproject.kaa.demo.iotworld.light.BulbListRequest;
import org.kaaproject.kaa.demo.iotworld.light.ChangeBulbBrightnessRequest;
import org.kaaproject.kaa.demo.iotworld.light.ChangeBulbStatusRequest;
import org.kaaproject.kaa.demo.iotworld.music.ChangeVolumeRequest;
import org.kaaproject.kaa.demo.iotworld.music.PauseRequest;
import org.kaaproject.kaa.demo.iotworld.music.PlayListRequest;
import org.kaaproject.kaa.demo.iotworld.music.PlayRequest;
import org.kaaproject.kaa.demo.iotworld.music.SeekRequest;
import org.kaaproject.kaa.demo.iotworld.music.StopRequest;
import org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest;
import org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.thermo.ChangeDegreeRequest;

public enum DeviceType {
    
    CLIMATE(R.drawable.ic_nav_climate, 
            R.string.nav_climate, 
            R.drawable.card_climate,
            R.color.device_color_climate,
            deviceFqns, geoFencingDeviceFqns, new String[]{ChangeDegreeRequest.class.getName()}),
    
    MUSIC(R.drawable.ic_nav_music, 
            R.string.nav_music, 
            R.drawable.card_music,
            R.color.device_color_music,
            deviceFqns, geoFencingDeviceFqns, new String[]{PlayListRequest.class.getName(), 
                                                         ChangeVolumeRequest.class.getName(),
                                                         PlayRequest.class.getName(),
                                                         PauseRequest.class.getName(),
                                                         StopRequest.class.getName(),
                                                         SeekRequest.class.getName()}),
                                                         
    PHOTO(R.drawable.ic_nav_photo, 
            R.string.nav_photos, 
            R.drawable.card_photo,
            R.color.device_color_photo,
            deviceFqns, geoFencingDeviceFqns, new String[]{PhotoAlbumsRequest.class.getName(),
                                                         StartSlideShowRequest.class.getName(),
                                                         PauseSlideShowRequest.class.getName(),
                                                         PhotoUploadRequest.class.getName(),
                                                         DeleteUploadedPhotosRequest.class.getName()}),
                                                         
    LIGHTNING(R.drawable.ic_nav_lightning, 
              R.string.nav_lightning, 
              R.drawable.card_lightning,         
              R.color.device_color_lightning,
              deviceFqns, geoFencingDeviceFqns, new String[]{BulbListRequest.class.getName(),
                                                             ChangeBulbBrightnessRequest.class.getName(),
                                                             ChangeBulbStatusRequest.class.getName()}),
                                                             
    IRRIGATION(R.drawable.ic_nav_irrigation, 
             R.string.nav_irrigation, 
             R.drawable.card_irrigation,
             R.color.device_color_irrigation,
             deviceFqns, new String[]{StartIrrigationRequest.class.getName(),
                                      IrrigationControlRequest.class.getName()});
    
    private final List<String> listenerFqns;
    private final int navIconResId;
    private final int navTitleResId;
    private final int cardIconResId;
    private final int baseColorResId;
    
    DeviceType(int navIconResId, 
            int navTitleResId, 
            int cardIconResId, 
            int baseColorResId, 
            String[]... listenerFqns) {
        this.navIconResId = navIconResId;
        this.navTitleResId = navTitleResId;
        this.cardIconResId = cardIconResId;
        this.baseColorResId = baseColorResId;
        this.listenerFqns = new ArrayList<>();
        for (String[] listenerFqnsArray : listenerFqns) {
            this.listenerFqns.addAll(Arrays.asList(listenerFqnsArray));
        }
    }
    
    public int getNavIconResId() {
        return navIconResId;
    }
    
    public int getNavTitleResId() {
        return navTitleResId;
    }
    
    public int getCardIconResId() {
        return cardIconResId;
    }
    
    public int getBaseColorResId() {
        return baseColorResId;
    }
    
    public List<String> getListenerFqns() {
        return listenerFqns;
    }
    
    interface CommonDeviceListenersFqns {
        
        public static final String[] deviceFqns = 
                new String[]{DeviceInfoRequest.class.getName(), 
                             DeviceStatusSubscriptionRequest.class.getName(),
                             DeviceChangeNameRequest.class.getName()};
        
        public static final String[] geoFencingDeviceFqns = 
                new String[]{GeoFencingStatusRequest.class.getName(),
                             OperationModeUpdateRequest.class.getName()};
    }

}
