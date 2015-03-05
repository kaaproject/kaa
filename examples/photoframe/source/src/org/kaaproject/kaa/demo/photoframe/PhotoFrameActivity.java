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

package org.kaaproject.kaa.demo.photoframe;

import org.kaaproject.kaa.demo.photoframe.event.KaaStartedEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayAlbumEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserAttachEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserDetachEvent;
import org.kaaproject.kaa.demo.photoframe.fragment.DevicesFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.LoginFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.SlideshowFragment;
import org.kaaproject.kaa.demo.photoframe.fragment.WaitFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class PhotoFrameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_frame);
        if (savedInstanceState == null) {
            if (!getPhotoFrameApplication().isKaaStarted()) {
                showWait();
            } else if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showDevices();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if (getPhotoFrameApplication().getEventBus().isRegistered(this)) {
            getPhotoFrameApplication().getEventBus().unregister(this);
        }

        /*
         * Notify application about background state.
         */

        getPhotoFrameApplication().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (!getPhotoFrameApplication().getEventBus().isRegistered(this)) {
            getPhotoFrameApplication().getEventBus().register(this);
        }

        /*
         * Notify application about foreground state.
         */

        getPhotoFrameApplication().resume();
    }
    
    private void showWait() {
        WaitFragment waitFragment = new WaitFragment();
        replaceFragment(waitFragment, waitFragment.getFragmentTag());
    }
    
    private void showLogin() {
        LoginFragment loginFragment = new LoginFragment();
        replaceFragment(loginFragment, loginFragment.getFragmentTag());
    }
    
    private void showDevices() {
        DevicesFragment devicesFragment = new DevicesFragment();
        replaceFragment(devicesFragment, devicesFragment.getFragmentTag());
    }
    
    public void onEventMainThread(PlayAlbumEvent playAlbumEvent) {
        Fragment fragment = getTopFragment();
        if (fragment != null && fragment instanceof SlideshowFragment) {
            ((SlideshowFragment)fragment).updateBucketId(playAlbumEvent.getBucketId());
            
        } else {
            SlideshowFragment slideshowFragment = new SlideshowFragment(playAlbumEvent.getBucketId());
            addBackStackFragment(slideshowFragment, slideshowFragment.getFragmentTag());
        }
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        if (kaaStarted.getErrorMessage() == null) {
            if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showDevices();
            }
        }
    }
    
    public void onEventMainThread(UserDetachEvent userDetachEvent) {
        if (userDetachEvent.getErrorMessage() != null) {
            Toast.makeText(this, userDetachEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
        showLogin();
    }
    
    public void onEventMainThread(UserAttachEvent userAttachEvent) {
        if (userAttachEvent.getErrorMessage() != null) {
            Toast.makeText(this, userAttachEvent.getErrorMessage(), Toast.LENGTH_LONG).show();
        } else {
            showDevices();
        }
    }
    
    private Fragment getTopFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            BackStackEntry entry = getSupportFragmentManager().
                    getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount()-1);
            return getSupportFragmentManager().findFragmentByTag(entry.getName());
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_frame, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            popBackStack();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().
            replace(R.id.container, fragment, tag).commit();
    }
    
    public void addBackStackFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment, tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public void popBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public PhotoFrameApplication getPhotoFrameApplication() {
        return (PhotoFrameApplication) getApplication();
    }
    
    public PhotoFrameController getController() {
        return getPhotoFrameApplication().getController();
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setLightsOutMode(boolean enabled) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            if (enabled) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);              
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(enabled ? View.SYSTEM_UI_FLAG_FULLSCREEN : 0);
        }
    }
    
}
