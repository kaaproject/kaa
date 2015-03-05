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
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.SlideshowPageAdapter;
import org.kaaproject.kaa.demo.photoframe.event.StopPlayEvent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SlideshowFragment extends Fragment {

    private static final String BUCKET_ID = "bucketId";
    
    private static final int SLIDESHOW_INTERVAL_MS = 5000;
    
    protected PhotoFrameActivity mActivity;
    protected PhotoFrameApplication mApplication;
    protected PhotoFrameController mController;
    protected ActionBar mActionBar;
    
    private String mBucketId;
    
    private ViewPager mViewPager;
    private SlideshowPageAdapter mSlideShowPagerAdapter;
    
    private Handler mSlideshowHandler = new Handler();
    
    private Runnable mSlideshowAction = new Runnable() {
        @Override
        public void run() {
            int count = mSlideShowPagerAdapter.getCount();
            int position = mViewPager.getCurrentItem();
            if (position == count-1) {
                position = 0;
            } else {
                position++;
            }
            mViewPager.setCurrentItem(position, true);
            mSlideshowHandler.postDelayed(this, SLIDESHOW_INTERVAL_MS);
        }
        
    };
    
    public SlideshowFragment() {
        super();
    }
    
    public SlideshowFragment(String bucketId) {
        super();
        mBucketId = bucketId;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBucketId == null) {
            mBucketId = savedInstanceState.getString(BUCKET_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBucketId != null) {
            outState.putString(BUCKET_ID, mBucketId);
        }
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
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_slideshow, container,
                false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        mSlideShowPagerAdapter = new SlideshowPageAdapter(mActivity, mApplication.getImageLoader(), mBucketId);
        mViewPager.setAdapter(mSlideShowPagerAdapter);
        return rootView;        
    }
    
    public void onEventMainThread(StopPlayEvent stopPlayEvent) {
        mActivity.popBackStack();
    }
    
    public void updateBucketId(String bucketId) {
        if (!mBucketId.equals(bucketId)) {
            mSlideshowHandler.removeCallbacks(mSlideshowAction);
            mBucketId = bucketId;
            mSlideShowPagerAdapter = new SlideshowPageAdapter(mActivity, mApplication.getImageLoader(), mBucketId);
            mViewPager.setAdapter(mSlideShowPagerAdapter);
            mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
            mController.updateStatus(PlayStatus.PLAYING, mBucketId);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mActionBar != null) {
            mActionBar.hide();
        }
        mActivity.setLightsOutMode(true);
        if (!mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().register(this);
        }
        mSlideshowHandler.postDelayed(mSlideshowAction, SLIDESHOW_INTERVAL_MS);
        mController.updateStatus(PlayStatus.PLAYING, mBucketId);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mActionBar != null) {
            mActionBar.show();
        }
        mActivity.setLightsOutMode(false);
        if (mApplication.getEventBus().isRegistered(this)) {
            mApplication.getEventBus().unregister(this);
        }
        mSlideshowHandler.removeCallbacks(mSlideshowAction);
        mController.updateStatus(PlayStatus.STOPPED, null);
    }
    
    public String getFragmentTag() {
        return SlideshowFragment.class.getSimpleName() + mBucketId;
    }

}
