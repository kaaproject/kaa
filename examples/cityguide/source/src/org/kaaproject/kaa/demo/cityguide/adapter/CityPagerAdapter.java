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

package org.kaaproject.kaa.demo.cityguide.adapter;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.fragment.PlacesFragment;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CityPagerAdapter extends FragmentStatePagerAdapter {

    private static final int[] pageTitles = new int[] { R.string.hotels,
            R.string.shops, R.string.museums, R.string.restaurants };

    private Context mContext;
    private String mAreaName;
    private String mCityName;

    private List<Fragment> fragments;

    public CityPagerAdapter(Context context, String areaName, String cityName,
            FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
        mAreaName = areaName;
        mCityName = cityName;
        fragments = new ArrayList<>(Category.values().length);
        for (int i = 0; i < Category.values().length; i++) {
            fragments.add(new PlacesFragment(mAreaName, mCityName, Category
                    .values()[i]));
        }
    }

    @Override
    public int getCount() {
        return Category.values().length;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(pageTitles[position]);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}
