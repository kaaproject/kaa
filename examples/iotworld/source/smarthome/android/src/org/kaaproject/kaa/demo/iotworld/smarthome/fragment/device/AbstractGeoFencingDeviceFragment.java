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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.ColorUtils;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public abstract class AbstractGeoFencingDeviceFragment<D extends AbstractGeoFencingDevice> extends
        AbstractDeviceFragment<D> implements OnItemSelectedListener {

    private Spinner mGeoFencingStatusSpinner;
    protected boolean mControlsEnabled = true;

    public AbstractGeoFencingDeviceFragment() {
        super();
    }

    public AbstractGeoFencingDeviceFragment(String endpointKey) {
        super(endpointKey);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Object tag = mGeoFencingStatusSpinner.getTag();
        if (tag == null || ((Integer)tag).intValue() != position) {
            OperationMode newOperationMode = OperationMode.values()[position];
            if (mDevice.getOperationMode() == null || mDevice.getOperationMode() != newOperationMode) {
                mDevice.changeOperationMode(newOperationMode);
            }
        }
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.geofencing, menu);
        MenuItem geofencingItem = menu.findItem(R.id.action_geofencing);
        MenuItemCompat.setActionView(geofencingItem, mGeoFencingStatusSpinner);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        mGeoFencingStatusSpinner = new Spinner(mActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int color = ColorUtils.darkerColor(getResources().getColor(
                          mDevice.getDeviceType().getBaseColorResId()));            
            mGeoFencingStatusSpinner.getPopupBackground().setColorFilter(new PorterDuffColorFilter(color, Mode.MULTIPLY));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mActivity, R.layout.toolbar_spinner_item,
                getResources().getStringArray(R.array.geofencing_status));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { 
            spinnerArrayAdapter.setDropDownViewResource(R.layout.toolbar_spinner_dropdown_item);
        } else {
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        mGeoFencingStatusSpinner.setAdapter(spinnerArrayAdapter);
        
        updateGeoFencingSpinnerWidth();
        
        mGeoFencingStatusSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGeoFencingSpinnerWidth();
    }
    
    private void updateGeoFencingSpinnerWidth() {
        int geofencingSpinnerWidth = getResources().getDimensionPixelSize(R.dimen.geofencing_spinner_width);
        LayoutParams lp = mGeoFencingStatusSpinner.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(geofencingSpinnerWidth, LayoutParams.MATCH_PARENT);
        } else {
            lp.width = geofencingSpinnerWidth;
        }
        mGeoFencingStatusSpinner.setLayoutParams(lp);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);

        OperationMode mode = mDevice.getOperationMode();
        if (mode == null) {
            mode = OperationMode.OFF;
        }

        mGeoFencingStatusSpinner.setTag(mode.ordinal());
        mGeoFencingStatusSpinner.setSelection(mode.ordinal());
        
        setControlsEnabled(mode != OperationMode.OFF);
    }
    
    private void setControlsEnabled(boolean enabled) {
        if (mControlsEnabled != enabled) {
            mControlsEnabled = enabled;
            updateControlsState();
        }
    }
    
    protected abstract void updateControlsState();

}
