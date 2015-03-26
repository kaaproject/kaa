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
package org.kaaproject.kaa.demo.smarthousedemo;

import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.controller.BaseDeviceListener;
import org.kaaproject.kaa.demo.smarthousedemo.controller.SmartHouseController;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.device.DeviceFragment;
import org.kaaproject.kaa.demo.smarthousedemo.device.QrcodeFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class BaseDeviceActivity extends FragmentActivity implements BaseDeviceListener {
    
    protected DeviceType deviceType;
    
    /** A controller based on the Kaa client SDK API. Implements methods for sending and handling events. */
    private SmartHouseController mSmartHouseController;
    
    private ProgressFragment mProgessFragment;
    
    protected DeviceFragment mDeviceFragment;
    
    private boolean mQrcodeIsShowing = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_device);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = super.onKeyDown(keyCode, event);
		if (mDeviceFragment != null) {
			mDeviceFragment.onKeyDown(keyCode);
		}
		return result;
	}
    
    protected void init(DeviceType deviceType) {
        this.deviceType = deviceType;
        
        mSmartHouseController = new SmartHouseController(this, deviceType, null, null);
        
        mProgessFragment = ProgressFragment.newInstance(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        .replace(R.id.container, mProgessFragment)
        .commit();

        /** Initialization of a smart house controller instance. */
        mSmartHouseController.init(new InitCallback());
        
    }
    
    class InitCallback implements CommandCallback<Void> {

        @Override
        public void onCommandFailure(Throwable t) {
            Log.e("Kaa", "Smart House controller initialization failed", t);
            String message;
            if (t != null) {
                message = "Unexpected error: " + t.getMessage();
            }
            else {
                message = "Unknown error!";
            }
            onError(message);
        }

        @Override
        public void onCommandSuccess(Void result) {
            if (!mSmartHouseController.isAttachedToUser()) {
                showQrCode(mSmartHouseController.getEndpointAccessToken());
            }
            else {
                showDeviceFragment();
            }
        }

        @Override
        public void onCommandTimeout() {
            onTimeout();            
        }
        
    }
    
    private void onError(final String message) {
        mProgessFragment.onError(message);
    }
    
    private void onTimeout() {
        mProgessFragment.onError("Unable to complete request within a given timeout!");
    }
  
    public SmartHouseController getSmartHouseController() {
        return mSmartHouseController;
    }

    @Override
    protected void onPause() {
        if (mSmartHouseController != null) {
            mSmartHouseController.pause();
        }
        super.onPause();
    }     

    @Override
    protected void onResume() {
        super.onResume();
        if (mSmartHouseController != null) {
            mSmartHouseController.resume();
        }
    }     
    
    @Override
    protected void onDestroy() {
        if (mSmartHouseController != null) {
            mSmartHouseController.stop();
        }
        super.onDestroy();
    }
    
    protected void showDeviceFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mDeviceFragment = DeviceFragment.newInstance(deviceType);
        fragmentManager.beginTransaction()
        .replace(R.id.container, mDeviceFragment)
        .commit();
        mQrcodeIsShowing = false;
    }
    
    protected void showQrCode(String code) {
        Resources res = getResources();
        String desc = String.format(res.getString(R.string.msg_device_scan_qr_code), getString(deviceType.getNameRes()));
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
        .replace(R.id.container, QrcodeFragment.newInstance(code, desc))
        .commit();
        mQrcodeIsShowing = true;
    }

    @Override
    public void onAttached() {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDeviceFragment();
                }
            });
    }

    @Override
    public void onDetached() {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showQrCode(mSmartHouseController.getEndpointAccessToken());
                }
            });
    }

    @Override
    public void onEvent(Object event, String endpointSourceKey) {
        if (mDeviceFragment != null) {
            mDeviceFragment.handleEvent(event, endpointSourceKey);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (mQrcodeIsShowing) {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(this);
            Editor editor = sp.edit();
            editor.putInt(MainActivity.STARTED_ACTIVITY_PREF, -1);
            editor.commit();            
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
    
}
