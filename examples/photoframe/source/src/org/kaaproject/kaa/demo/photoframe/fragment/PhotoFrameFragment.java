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

package org.kaaproject.kaa.demo.photoframe.fragment;

import org.kaaproject.kaa.demo.photoframe.PhotoFrameActivity;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameApplication;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameController;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.event.BasicEvent;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

/**
 * The Class PhotoFrameFragment.
 * Implementation of {@link Fragment} class. Used as superclass for most application fragments.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides functions to switch between views representing busy progress, error message and content.   
 */
public abstract class PhotoFrameFragment extends Fragment {

    protected PhotoFrameActivity mActivity;
    protected PhotoFrameApplication mApplication;
    protected PhotoFrameController mController;
    protected ActionBar mActionBar;
    
    protected View mWaitLayout;
    protected View mContentLayout;
    protected View mErrorLayout;
    protected TextView mErrorText;
    
    public PhotoFrameFragment() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null) {
            mActivity = (PhotoFrameActivity) activity;
            mActionBar = mActivity.getSupportActionBar();
            mApplication = mActivity.getPhotoFrameApplication();
            mController = mApplication.getController();
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
        if (updateActionBar() && mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            if (displayHomeAsUp())
                options |= ActionBar.DISPLAY_HOME_AS_UP;
            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(getTitle());
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setHomeButtonEnabled(displayHomeAsUp());

        }
        if (useEventBus() && !mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
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

    protected boolean updateActionBar() {
        return true;
    }

    protected boolean useEventBus() {
        return true;
    }

    protected abstract boolean displayHomeAsUp();
    
    public abstract String getFragmentTag();
    
}
