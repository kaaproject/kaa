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

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.GeoFencingEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusRequest;
import org.kaaproject.kaa.demo.iotworld.geo.GeoFencingStatusResponse;
import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.geo.OperationModeUpdateRequest;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

    public void changeDeviceMode(Context context) {
        if (mOperationMode != null) {
            final OperationMode prevOperationMode = mOperationMode;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.change_device_mode);
            final Spinner spinner = new Spinner(context);
            String[] values = context.getResources().getStringArray(R.array.geofencing_status);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(mOperationMode.ordinal());
            builder.setView(spinner);
            builder.setPositiveButton(R.string.change_mode, new DialogInterface.OnClickListener() { 
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    OperationMode newOperationMode = OperationMode.values()[spinner.getSelectedItemPosition()];
                    if (newOperationMode != prevOperationMode) {
                        changeOperationMode(newOperationMode);
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }
}
