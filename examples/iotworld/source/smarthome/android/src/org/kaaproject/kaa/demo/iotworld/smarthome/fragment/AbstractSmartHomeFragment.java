package org.kaaproject.kaa.demo.iotworld.smarthome.fragment;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.SmartHomeActivity;
import org.kaaproject.kaa.demo.iotworld.smarthome.SmartHomeApplication;
import org.kaaproject.kaa.demo.iotworld.smarthome.SmartHomeController;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.BasicEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.SmartHomeToolbar;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for most application fragments.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides functions for switching between views representing busy progress, an error message, and content.   
 */
public abstract class AbstractSmartHomeFragment extends Fragment {

    protected SmartHomeActivity mActivity;
    protected SmartHomeApplication mApplication;
    protected SmartHomeController mController;
    protected DeviceStore mDeviceStore;
    protected ActionBar mActionBar;
    protected SmartHomeToolbar mToolbar;
    
    protected View mWaitLayout;
    protected View mContentLayout;
    protected View mErrorLayout;
    protected TextView mErrorText;
    
    public AbstractSmartHomeFragment() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (SmartHomeActivity) activity;
            mActionBar = mActivity.getSupportActionBar();
            mToolbar = mActivity.getSmartHomeToolbar();
            mApplication = mActivity.getSmartHomeApplication();
            mController = mApplication.getController();
            mDeviceStore = mApplication.getDeviceStore();
        }
    }
    
    protected void setupView(View rootView) {
        mWaitLayout = rootView.findViewById(R.id.waitLayout);
        mContentLayout = rootView.findViewById(R.id.contentLayout);
        mErrorLayout = rootView.findViewById(R.id.errorLayout);
        mErrorText = (TextView) rootView.findViewById(R.id.errorText);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (showNavigationDrawer()) {
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeButtonEnabled(false);
            mActivity.showNavigationDrawer(true);
        } else {
            mActivity.showNavigationDrawer(false);
            if (mActionBar != null) {
                mActionBar.setDisplayHomeAsUpEnabled(displayHomeAsUp());
                mActionBar.setHomeButtonEnabled(displayHomeAsUp());
                if (!displayHomeAsUp()) {
                    mActionBar.setHomeAsUpIndicator(null);
                }
            }
        }
        if (mActionBar != null) {
            int options = 0;
            if (displayHomeAsUp()) {
                options |= ActionBar.DISPLAY_HOME_AS_UP;
            }
            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
            mToolbar.setTitle(getTitle());
            mToolbar.setSubtitle(getSubTitle());
            mToolbar.setCustomToolbarContentEnabled(false);
        }        
        mToolbar.setBackgroundColor(getBarsBackgroundColor());
        if (useEventBus() && !mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
        
        FontUtils.setRobotoFont(mActivity);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (useEventBus() && mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().unregister(this);
        }
    }

    protected boolean checkEvent(BasicEvent event) {
        if (event.getErrorMessage() != null) {
            showError(event.getErrorMessage());
            return false;
        } 
        return true;
    }
    
    protected void showWait() {
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.GONE);
        }
        mErrorLayout.setVisibility(View.GONE);
        mWaitLayout.setVisibility(View.VISIBLE);
    }
    
    protected void showContent() {
        mWaitLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }
    
    protected void showError(String error) {
        mWaitLayout.setVisibility(View.GONE);
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.GONE);
        }
        mErrorLayout.setVisibility(View.VISIBLE);
        mErrorText.setText(error);
    }

    protected String getTitle() {
        return "";
    }
    
    protected String getSubTitle() {
        return null;
    }
    
    protected boolean showNavigationDrawer() {
        return false;
    }

    protected boolean useEventBus() {
        return true;
    }
    
    protected int getBarsBackgroundColor() {
        return Color.BLACK;
    }

    protected abstract boolean displayHomeAsUp();
    
    public abstract String getFragmentTag();
 
}
