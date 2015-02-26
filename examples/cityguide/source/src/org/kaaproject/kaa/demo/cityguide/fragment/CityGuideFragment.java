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

package org.kaaproject.kaa.demo.cityguide.fragment;

import org.kaaproject.kaa.demo.cityguide.CityGuideActivity;
import org.kaaproject.kaa.demo.cityguide.CityGuideApplication;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

public abstract class CityGuideFragment extends Fragment {
	
	public static final String AREA_NAME = "areaName";
	public static final String CITY_NAME = "cityName";
	public static final String PLACE_CATEGORY = "placeCategory";
	public static final String PLACE_TITLE = "placeTitle";

	protected CityGuideActivity mActivity;
	protected CityGuideApplication mApplication;
	protected ActionBar mActionBar;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mActivity == null) {
			mActivity = (CityGuideActivity)activity;
			mActionBar = mActivity.getSupportActionBar();
			mApplication = mActivity.getCityGuideApplication();
		}
	}

    @Override
	public void onResume() {
		super.onResume();
		if (updateActionBar() && mActionBar != null) {
			int options = ActionBar.DISPLAY_SHOW_TITLE;
			if (displayHomeAsUp()) options |= ActionBar.DISPLAY_HOME_AS_UP;
			mActionBar.setDisplayOptions(options, 
					ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
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
}
