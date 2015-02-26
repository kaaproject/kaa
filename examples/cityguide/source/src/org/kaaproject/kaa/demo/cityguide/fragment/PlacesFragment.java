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

import java.util.List;

import org.kaaproject.kaa.demo.cityguide.Category;
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.PlacesAdapter;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class PlacesFragment extends CityGuideFragment {

	private String mAreaName;
    private String mCityName;
    private Category mPlaceCategory;
	
    private ListView mPlacesListView;
    private PlacesAdapter mPlacesAdapter;
    
    public PlacesFragment() {
    	super();
    }
	
	public PlacesFragment(String areaName, String cityName, Category placeCategory) {
		super();
		mAreaName = areaName;
		mCityName = cityName;
		mPlaceCategory = placeCategory;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mAreaName == null) {
			mAreaName = savedInstanceState.getString(AREA_NAME);
			mCityName = savedInstanceState.getString(CITY_NAME);
			mPlaceCategory = Category.values()[savedInstanceState.getInt(PLACE_CATEGORY)];
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAreaName != null) {
			outState.putString(AREA_NAME, mAreaName);
			outState.putString(CITY_NAME, mCityName);
			outState.putInt(PLACE_CATEGORY, mPlaceCategory.ordinal());
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_places,
				container, false);
		
		mPlacesListView = (ListView) rootView.findViewById(R.id.placesList);
		mPlacesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	onPlaceClicked(position);
            }
        });
		List<Place> places = Utils.getPlaces(mApplication.getCityGuideConfiguration(), mAreaName, mCityName, mPlaceCategory);
		if (places != null) {
			mPlacesAdapter = new PlacesAdapter(mActivity, mApplication.getImageLoader(), places);
			mPlacesListView.setAdapter(mPlacesAdapter);
		}
		return rootView;
	}
	
	private void onPlaceClicked(int position) {
		Place place = mPlacesAdapter.getItem(position);
		PlaceFragment placeFragment = new PlaceFragment(mAreaName, mCityName, mPlaceCategory, place.getTitle());
		mActivity.openFragment(placeFragment);
	}
	
	public void onEventMainThread(ConfigurationUpdated configurationUpdated) {
		List<Place> places = Utils.getPlaces(mApplication.getCityGuideConfiguration(), mAreaName, mCityName, mPlaceCategory);
		if (places != null) {
			mPlacesAdapter = new PlacesAdapter(mActivity, mApplication.getImageLoader(), places);
			mPlacesListView.setAdapter(mPlacesAdapter);
		}
    }
	
	@Override
	protected boolean updateActionBar() {
		return false;
	}
	
	@Override
	protected boolean useEventBus() {
		return true;
	}

	@Override
	protected boolean displayHomeAsUp() {
		return true;
	}

}
