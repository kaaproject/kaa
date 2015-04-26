package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.kaaproject.kaa.demo.iotworld.geo.OperationMode;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.ColorUtils;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
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
            mGeoFencingStatusSpinner.setPopupBackgroundDrawable(new ColorDrawable(ColorUtils.darkerColor(getResources().getColor(
                    mDevice.getDeviceType().getBaseColorResId()))));
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
    }



}
