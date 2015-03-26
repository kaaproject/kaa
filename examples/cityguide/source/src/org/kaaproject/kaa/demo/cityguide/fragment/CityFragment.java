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

import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.City;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.CityPagerAdapter;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.event.KaaStarted;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.TabPageIndicator;

/**
 * The implementation of the {@link CityGuideFragment} class. 
 * Represents tabs with list views of city places separated by the place {@link Category}.
 */
public class CityFragment extends CityGuideFragment {

    private View mWaitView;
    private View mCityPages;
    private TabPageIndicator mCityPageIndicator;
    private ViewPager mCityPager;
    private String mAreaName;
    private String mCityName;
    private CityPagerAdapter mCityPagerAdapter;

    public CityFragment() {
        super();
    }

    public CityFragment(String areaName, String cityName) {
        super();
        mAreaName = areaName;
        mCityName = cityName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mAreaName == null) {
            mAreaName = savedInstanceState.getString(AREA_NAME);
            mCityName = savedInstanceState.getString(CITY_NAME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAreaName != null) {
            outState.putString(AREA_NAME, mAreaName);
            outState.putString(CITY_NAME, mCityName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_city, container,
                false);
        mWaitView = rootView.findViewById(R.id.waitProgress);
        mCityPages = rootView.findViewById(R.id.cityPages);
        mCityPageIndicator = (TabPageIndicator) rootView
                .findViewById(R.id.cityPageIndicator);
        mCityPager = (ViewPager) rootView.findViewById(R.id.cityPager);
        if (mApplication.isKaaStarted()) {
            showCity();
        }
        return rootView;
    }

    private void showCity() {
        mWaitView.setVisibility(View.GONE);
        City city = Utils.getCity(mApplication.getCityGuideConfiguration(),
                mAreaName, mCityName);
        if (city != null) {
            mCityPagerAdapter = new CityPagerAdapter(mActivity, mAreaName,
                    mCityName, mActivity.getSupportFragmentManager());
            mCityPages.setVisibility(View.VISIBLE);
            mCityPager.setAdapter(mCityPagerAdapter);
            mCityPageIndicator.setViewPager(mCityPager);
        } else {
            mActivity.popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onEventMainThread(KaaStarted kaaStarted) {
        showCity();
    }

    public void onEventMainThread(ConfigurationUpdated configurationUpdated) {
        City city = Utils.getCity(mApplication.getCityGuideConfiguration(),
                mAreaName, mCityName);
        if (city == null) {
            mActivity.popBackStack();
        }
    }

    @Override
    protected String getTitle() {
        return mCityName;
    }

    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

}
