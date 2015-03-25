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

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.controller.SmartHouseController;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.SmartHouseDrawerFragment;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.SmartHouseFragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SmartHouseActivity extends ActionBarActivity  {

	/**
	 * A fragment that manages behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private SmartHouseDrawerFragment mNavigationDrawerFragment;

	//private List<FragmentInfo> fragments = new ArrayList<>();
	
	private String mUsername;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
    /** A controller based on the Kaa client SDK API. Implements methods for sending and handling events. */
    private SmartHouseController mSmartHouseController;
    
    private DeviceStore mDeviceStore;
    
    private ProgressFragment mProgessFragment;
    private SmartHouseFragment mSmartHouseFragment;

	public SmartHouseActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smarthouse);

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(this);
        mUsername = sp.getString(LoginActivity.USERNAME_PREF, null);
        if (mUsername == null) {
        	login();
        }
        else {
        	init();
        }
	}
	
	private void login() {
    	Intent intent = new Intent(this, LoginActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    	finish();
	}
	
	private void logout() {
		final List<String> deviceKeys = new ArrayList<>();
		deviceKeys.add(mSmartHouseController.getCurrentEndpointKey());
		final List<String> attachedDevicesKeys = mDeviceStore.getAllDeviceKeys();
		if (!attachedDevicesKeys.isEmpty()) {
	        new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.msg_disconnect_title)
	        .setMessage(R.string.msg_disconnect)
	        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	            	deviceKeys.addAll(attachedDevicesKeys);
	            	performLogout(deviceKeys);
	            }
	        })
	        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	dialog.dismiss();
	            	performLogout(deviceKeys);
	            }
	        })
	        .show();
		}
		else {
			performLogout(deviceKeys);
		}
	}
	
	private void performLogout(List<String> deviceKeys) {
		SignoutProcessor signoutProcessor = new SignoutProcessor(deviceKeys);
		signoutProcessor.processSignout();
	}
	
	class SignoutProcessor implements CommandCallback<Boolean> {

		private final ProgressDialog progress;
		private final List<String> detachDeviceKeys;
		
		SignoutProcessor(List<String> detachDeviceKeys) {
			this.detachDeviceKeys = detachDeviceKeys;
			this.progress = new ProgressDialog(SmartHouseActivity.this);
			this.progress.setTitle(getString(R.string.msg_logout_title));
			this.progress.setMessage(getString(R.string.msg_logout));
			this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.progress.setIndeterminate(true);
		}
		
		void processSignout() {
			this.progress.show();
			mSmartHouseController.deattachEndpoint(detachDeviceKeys.get(detachDeviceKeys.size()-1), this);
		}
		
		@Override
		public void onCommandFailure(Throwable t) {
			this.progress.dismiss();
			displayErrorMessage("Signout error!", "Unexpected error: " + t.getMessage());
			//Toast.makeText(SmartHouseActivity.this, "Unexpected error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCommandSuccess(Boolean result) {
			detachDeviceKeys.remove(detachDeviceKeys.size()-1);
			if (!detachDeviceKeys.isEmpty()) {
				processSignout();
			}
			else {
				this.progress.dismiss();
				SmartHouseActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDeviceStore.deleteAllDevices();
						mUsername = null;
				        SharedPreferences sp = PreferenceManager
				                .getDefaultSharedPreferences(SmartHouseActivity.this);
				        Editor editor = sp.edit();
				        editor.putString(LoginActivity.USERNAME_PREF, mUsername);
				        editor.commit();
				        login();
					}
				});
			}
		}

		@Override
		public void onCommandTimeout() {
			this.progress.dismiss();
			displayErrorMessage("Signout error!", "Timeout ocurred!");
			//Toast.makeText(SmartHouseActivity.this, "Timeout ocurred!", Toast.LENGTH_SHORT).show();
		}
	}
	
    private void displayErrorMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
	  
	  private void init() {
	      
	      mDeviceStore = new DeviceStore(this);
	      mSmartHouseController = new SmartHouseController(this, null, mUsername, mDeviceStore);
	      
    		mNavigationDrawerFragment = (SmartHouseDrawerFragment) getSupportFragmentManager()
    				.findFragmentById(R.id.navigation_drawer);
    		mTitle = getTitle();
    
    		// Set up the drawer.
    		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
    				(DrawerLayout) findViewById(R.id.drawer_layout), mUsername);
			
            
    		mProgessFragment = ProgressFragment.newInstance(true);
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
	            mNavigationDrawerFragment.onInitSuccess();
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
	    
	    public DeviceStore getDeviceStore() {
	        return mDeviceStore;
	    }
	    
	    public void switchToDeviceType(DeviceType deviceType) {
	        if (mSmartHouseFragment != null) {
	            mSmartHouseFragment.switchToDeviceType(deviceType);
	        }
	    }
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        if (mSmartHouseController != null) {
	            mSmartHouseController.resume();
	        }
	        if (mDeviceStore != null) {
	            mDeviceStore.onResume();
	        }
	    }

	    @Override
	    protected void onPause() {
            if (mSmartHouseController != null) {
                mSmartHouseController.pause();
            }
	        if (mDeviceStore != null) {
	            mDeviceStore.onPause();
	        }
	        super.onPause();
	    }
	    
       @Override
       protected void onDestroy() {
           if (mSmartHouseController != null) {
               mSmartHouseController.stop();
           }
           super.onDestroy();
       }

	public void onNavigationDrawerItemSelected(int position) {
		switch (position) {
		case 0:
			FragmentManager fragmentManager = getSupportFragmentManager();
			mSmartHouseFragment = SmartHouseFragment.newInstance();
			fragmentManager.beginTransaction()
					.replace(R.id.container, mSmartHouseFragment)
					.commit();
			break;
		case 1:
			logout();
			break;
		}
	}
	
	public void onSectionAttached(int number) {
		if (number == 0) {
			mTitle = getString(R.string.smart_house);
		}
	}

	private void restoreActionBar() {

        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.getDrawerToggle().setDrawerIndicatorEnabled(true);
        }
        restoreActionBarInternal();
		supportInvalidateOptionsMenu();
	}
	
	private void restoreActionBarInternal() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setTitle(mTitle);
	}
	
	private boolean isNavigationDrawerOpen() {
	    return mNavigationDrawerFragment != null && mNavigationDrawerFragment.isDrawerOpen();
	}
	
	private boolean isNavigationDrawerClosed() {
	    return mNavigationDrawerFragment != null && !mNavigationDrawerFragment.isDrawerOpen();
	}
	
	public void resetActionBar() {
	    if (mNavigationDrawerFragment != null) {
	        mNavigationDrawerFragment.getDrawerToggle().setDrawerIndicatorEnabled(false);
	    }
	    supportInvalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		if (isNavigationDrawerClosed() &&
//		        mNavigationDrawerFragment.getDrawerToggle().isDrawerIndicatorEnabled()) {
//			// Only show items in the action bar relevant to this screen
//			// if the drawer is not showing. Otherwise, let the drawer
//			// decide what to show in the action bar.
//			getMenuInflater().inflate(R.menu.main, menu);
//			restoreActionBarInternal();
//			return true;
//		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks. The action bar 
//		// automatically handles clicks on the Home/Up button, provided that
//		// you have specified a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		if (id == android.R.id.home) {
		    if (mNavigationDrawerFragment == null ||
		            !mNavigationDrawerFragment.getDrawerToggle().isDrawerIndicatorEnabled()) {
		        popBackStack();
		        return true;
		    }
		}
		return false;
	}

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            popBackStack();
        }
        else if (!isNavigationDrawerOpen() && 
                mSmartHouseFragment != null && !mSmartHouseFragment.isHomeSelected()) {
            mSmartHouseFragment.switchToHome();
        }
        else {
            super.onBackPressed();
        }
    }
    
    private void popBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            restoreActionBar();
        }
    }
	
}
