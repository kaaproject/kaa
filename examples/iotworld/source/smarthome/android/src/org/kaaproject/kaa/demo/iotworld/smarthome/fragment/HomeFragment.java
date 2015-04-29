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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractGeoFencingDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceRemovedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.ClimateDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.IrrigationDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.LightningDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.PhotoDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music.MusicAlbumsDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.home.HomeAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.home.HomeAdapter.DeviceSelectionListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView;
import org.kaaproject.kaa.demo.qrcode.Intents;
import org.kaaproject.kaa.demo.qrcode.QrCodeCaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;

/**
 * Implementation of the {@link AbstractSmartHomeFragment} class. 
 */
public class HomeFragment extends AbstractSmartHomeFragment implements DeviceSelectionListener {
    
    private static final int DEVICE_QR_CODE_REQUEST = 3001;
    
    private TextView mNoDataText;
    private AutoSpanRecyclerView mRecyclerView;
    private HomeAdapter mHomeAdapter;
    
    public HomeFragment() {
        super();
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int cardsSpacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        mRecyclerView.setGridLayoutManager(GridLayoutManager.VERTICAL, 1, R.dimen.card_width, cardsSpacing);

        mHomeAdapter = new HomeAdapter(mRecyclerView, mDeviceStore, this);
        showContent();
        
        notifyDataChanged();
        discoverDevices(false);
    }

    public void onEventMainThread(DeviceUpdatedEvent deviceUpdatedEvent) {
        notifyDataChanged();
    }
    
    public void onEventMainThread(DeviceRemovedEvent deviceRemovedEvent) {
        notifyDataChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_home, container,
                false);
        setupView(rootView);
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mNoDataText.setText(R.string.no_devices);
        
        mRecyclerView = (AutoSpanRecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        return rootView;
    }
    
    protected void notifyDataChanged() {
        mHomeAdapter.tryNotifyDataSetChanged();
        if (mHomeAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }
    
    protected String getTitle() {
        return getString(R.string.my_home);
    }
    
    @Override
    public String getFragmentTag() {
        return HomeFragment.class.getSimpleName();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            discoverDevices(true);
            notifyDataChanged();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    
    private void discoverDevices(boolean refresh) {
        mDeviceStore.discoverDevices(refresh, null);
    }
    
    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }
    
    @Override
    protected boolean showNavigationDrawer() {
        return true;
    }
    
    protected int getBarsBackgroundColor() {
        return getResources().getColor(R.color.bar_home);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AbstractDevice contextMenuDevice = mHomeAdapter.getContextMenuDevice();
        if (contextMenuDevice != null) {
            switch (item.getItemId()) {
            case R.id.ctx_menu_refresh_device:
                contextMenuDevice.requestDeviceInfo();
                break;
            case R.id.ctx_menu_rename_device:
                contextMenuDevice.renameDevice(mActivity);
                break;
            case R.id.ctx_menu_change_mode:
                if (contextMenuDevice instanceof AbstractGeoFencingDevice) {
                    ((AbstractGeoFencingDevice)contextMenuDevice).changeDeviceMode(mActivity);
                }
                break;                
            case R.id.ctx_menu_detach_device:
                contextMenuDevice.detach();
                break;
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDeviceSelected(DeviceType deviceType, String endpointKey) {
        Fragment fragment = null;
        switch (deviceType) {
        case CLIMATE:
            fragment = new ClimateDeviceFragment(endpointKey);
            break;
        case LIGHTNING:
            fragment = new LightningDeviceFragment(endpointKey);
            break;
        case MUSIC:
            fragment = new MusicAlbumsDeviceFragment(endpointKey);
            break;
        case PHOTO:
            fragment = new PhotoDeviceFragment(endpointKey);
            break;
        case IRRIGATION:
            fragment = new IrrigationDeviceFragment(endpointKey);
            break;
        default:
            break;
        }
        mActivity.addBackStackFragment(fragment, fragment.getTag());        
    }

    @Override
    public void onAddDeviceSelected() {
        connectDevice();
    }

    private void connectDevice() {
        Intent intent = new Intent(mActivity, QrCodeCaptureActivity.class);
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.FORMATS, BarcodeFormat.QR_CODE.name());
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, getString(R.string.msg_device_qr_code_status));
        intent.putExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS, -1l);
        startActivityForResult(intent, DEVICE_QR_CODE_REQUEST);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVICE_QR_CODE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(Intents.Scan.RESULT);
                mDeviceStore.attachDevice(result);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
