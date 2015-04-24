package org.kaaproject.kaa.demo.iotworld.smarthome;

import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.KaaStartedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.UserAttachEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.UserDetachEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.HomeFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.LoginFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.WaitFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.AbstractDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.ClimateDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.LightningDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.MusicDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.PhotoDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.IrrigationDeviceListFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.nav.NavAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.nav.NavAdapter.NavigationListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.SmartHomeToolbar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class SmartHomeActivity extends ActionBarActivity implements NavigationListener {

    private SmartHomeToolbar mToolbar;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mMainLayout;
    
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager; 
    private NavAdapter mNavAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smarthome);
        
        mToolbar = (SmartHomeToolbar) findViewById(R.id.smart_home_toolbar);
        setSupportActionBar(mToolbar.getToolbar());
        
        mMainLayout = (DrawerLayout) findViewById(R.id.main_layout);
        mToggle = new ActionBarDrawerToggle(
                this, 
                mMainLayout, 
                R.string.navigation_drawer_open, 
                R.string.navigation_drawer_close) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        mNavAdapter.setMode(NavAdapter.MODE_MAIN);
                    }
        };
        
        mMainLayout.setDrawerListener(mToggle);
        
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        
        mNavAdapter = new NavAdapter(mRecyclerView, this);
        
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        
        if (savedInstanceState == null) {
            if (!getSmartHomeApplication().isKaaStarted()) {
                showWait();
            } else if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showHome();
            }
        }
    }
    
    @Override
    public void onDeviceTypeSelected(DeviceType deviceType) {
        mMainLayout.closeDrawers();
        AbstractDeviceListFragment<?> deviceListFragment = null;
        switch (deviceType) {
        case CLIMATE:
            deviceListFragment = new ClimateDeviceListFragment();
            break;
        case LIGHTNING:
            deviceListFragment = new LightningDeviceListFragment();
            break;
        case MUSIC:
            deviceListFragment = new MusicDeviceListFragment();
            break;
        case PHOTO:
            deviceListFragment = new PhotoDeviceListFragment();
            break;
        case IRRIGATION:
            deviceListFragment = new IrrigationDeviceListFragment();
            break;
        default:
            break;
        }
        replaceFragment(deviceListFragment, deviceListFragment.getFragmentTag());
    }

    @Override
    public void onHomeSelected() {
        mMainLayout.closeDrawers();
        showHome();
    }

    @Override
    public void onSignOutSelected() {
        mMainLayout.closeDrawers();
        getController().logout();
    }
    
    public void showNavigationDrawer(boolean show) {
        if (show) {
            mToggle.setDrawerIndicatorEnabled(true);
            mMainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            
        } else {
            mToggle.setDrawerIndicatorEnabled(false);
            mMainLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }
    
    public SmartHomeToolbar getSmartHomeToolbar() {
        return mToolbar;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getSmartHomeApplication().getEventBus().isRegistered(this)) {
            getSmartHomeApplication().getEventBus().unregister(this);
        }

        /*
         * Notify the application about the background state.
         */

        getSmartHomeApplication().pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (!getSmartHomeApplication().getEventBus().isRegistered(this)) {
            getSmartHomeApplication().getEventBus().register(this);
        }

        /*
         * Notify the application about the foreground state.
         */

        getSmartHomeApplication().resume();
    }
    
    
    
    private void showWait() {
        WaitFragment waitFragment = new WaitFragment();
        replaceFragment(waitFragment, waitFragment.getFragmentTag());
    }
    
    private void showLogin() {
        mNavAdapter.setUsername(getController().getUsername());
        LoginFragment loginFragment = new LoginFragment();
        replaceFragment(loginFragment, loginFragment.getFragmentTag());
    }
    
    private void showHome() {
        mNavAdapter.setUsername(getController().getUsername());
        mNavAdapter.setSelection(NavAdapter.HOME_POSITION, false);
        HomeFragment homeFragment = new HomeFragment();
        replaceFragment(homeFragment, homeFragment.getFragmentTag());
    }
    
    public void onEventMainThread(KaaStartedEvent kaaStarted) {
        if (kaaStarted.getErrorMessage() == null) {
            if (!getController().isUserAttached()) {
                showLogin();
            } else {
                showHome();
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
            showHome();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                popBackStack();
                return true;
            } 
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
        mToolbar.onConfigurationChanged(newConfig);
    }
    
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().
            replace(R.id.container, fragment, tag).commit();
    }
    
    public void addBackStackFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.container, fragment, tag);
        ft.addToBackStack(tag);
        ft.commit();
    }

    public void popBackStack() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public SmartHomeApplication getSmartHomeApplication() {
        return (SmartHomeApplication) getApplication();
    }

    public SmartHomeController getController() {
        return getSmartHomeApplication().getController();
    }

    public DeviceStore getDeviceStore() {
        return getSmartHomeApplication().getDeviceStore();
    }
}
