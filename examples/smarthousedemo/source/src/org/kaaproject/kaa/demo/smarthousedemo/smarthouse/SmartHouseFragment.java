/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.qrcode.Intents;
import org.kaaproject.kaa.demo.qrcode.QrCodeCaptureActivity;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.SmartHouseActivity;
import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.FragmentInfo;
import org.kaaproject.kaa.demo.smarthousedemo.data.SmartDeviceInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.zxing.BarcodeFormat;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

public class SmartHouseFragment extends Fragment {
    
    private static final int DEVICE_QR_CODE_REQUEST = 3001;
    
    private SmartHouseActivity mActivity;
    private ProgressDialog mProgress;
    
    private String mConnectedEndpointKeyHash;
    
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private TabPageIndicator mTitleIndicator;
    
    public static SmartHouseFragment newInstance() {
        SmartHouseFragment fragment = new SmartHouseFragment();
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (SmartHouseActivity)this.getActivity();
        if (!mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().register(this);
        }
    }
    
    @Override
    public void onDestroy() {
        if (mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().unregister(this);
        }
        mActivity = null;
        super.onDestroy();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_smart_house, container, false);
        
        Button connectNewDeviceButton = (Button)view.findViewById(R.id.connectDeviceButton);
        
        connectNewDeviceButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		connectDevice();
			}
		});
        
        this.intialiseViewPager(view);
        return view;
    }
    
    public void onEventMainThread(DeviceStore.DeviceAdded deviceAdded) {
        SmartDeviceInfo device = deviceAdded.device;
        if (mConnectedEndpointKeyHash != null && mConnectedEndpointKeyHash.equals(device.getEndpointKey())) {
            switchToDeviceType(device.getDeviceType());
            mConnectedEndpointKeyHash = null;
        }
    }
    
    public void switchToDeviceType(DeviceType deviceType) {
        DeviceType[] types = DeviceType.enabledValues();
        int index = 0;
        for (int i=0;i<types.length;i++) {
            if (deviceType == types[i]) {
                index = i;
            }
        }
        mTitleIndicator.setCurrentItem(index+1);
    }
    
    public boolean isHomeSelected() {
       return mViewPager.getCurrentItem() == 0;
    }
    
    public boolean switchToHome() {
        if (mViewPager.getCurrentItem() != 0) {
            mTitleIndicator.setCurrentItem(0);
            return true;
        }
        return false;
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
                mProgress = new ProgressDialog(getActivity());
                mProgress.setTitle(getString(R.string.msg_connecting_device_title));
                mProgress.setMessage(getString(R.string.msg_connecting_device));
                mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgress.setIndeterminate(true);
                mProgress.show();
                mActivity.getSmartHouseController().attachEndpoint(result, new AttachEdnpointCallback());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    class AttachEdnpointCallback implements CommandCallback<String> {

        @Override
        public void onCommandFailure(Throwable t) {
        	mProgress.dismiss();
            String message = String.format(getString(R.string.msg_connecting_device_failed),
                    t.getMessage());
            displayErrorMessage(message);
        }

        @Override
        public void onCommandSuccess(String endpointKeyHash) {
        	mProgress.dismiss();
            //Toast.makeText(getActivity(), "Device successfuly connected: " + endpointKeyHash, Toast.LENGTH_LONG).show();
            mConnectedEndpointKeyHash = endpointKeyHash;
            mActivity.getSmartHouseController().discoverDevice(endpointKeyHash);
        }

        @Override
        public void onCommandTimeout() {
        	mProgress.dismiss();
            displayErrorMessage(getString(R.string.msg_connecting_device_timeout));
        }
    }

    private void displayErrorMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.msg_connecting_device_failed_title));
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
 
    /**
     * Initialise ViewPager
     */
    private void intialiseViewPager(View view) {
 
        List<FragmentInfo> fragments = new ArrayList<FragmentInfo>();
        
        Bundle args = new Bundle();
        Fragment fragment = Fragment.instantiate(this.getActivity(), HomeFragment.class.getName(), args);
        fragments.add(new FragmentInfo(R.drawable.group_home, R.string.home, fragment));
        
        for (DeviceType deviceType : DeviceType.enabledValues()) {
            fragment = DevicesFragment.newInstance(deviceType);
                   // Fragment.instantiate(this.getActivity(), DevicesFragment.class.getName(), args);
            fragments.add(new FragmentInfo(deviceType.getGroupIconRes(), deviceType.getTitleRes(), fragment));
        }
        this.mPagerAdapter  = new SmartHousePagerAdapter(getFragmentManager(), fragments);
        //
        this.mViewPager = (ViewPager)view.findViewById(R.id.viewpager);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        
        this.mTitleIndicator = (TabPageIndicator)view.findViewById(R.id.titles);
        this.mTitleIndicator.setViewPager(this.mViewPager);
    }
    
    class SmartHousePagerAdapter extends FragmentStatePagerAdapter  implements IconPagerAdapter {

        private List<FragmentInfo> fragments;
        
        public SmartHousePagerAdapter(FragmentManager fm, List<FragmentInfo> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position).getFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;//getString(fragments.get(position).getTitleResId()).toUpperCase();
        }

        @Override
        public int getIconResId(int index) {
            return fragments.get(index).getIconResId();
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
        
    }
 

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((SmartHouseActivity) activity).onSectionAttached(0);
        if (this.mViewPager != null) {
            this.mViewPager.requestLayout();
        }

    }
 


}
