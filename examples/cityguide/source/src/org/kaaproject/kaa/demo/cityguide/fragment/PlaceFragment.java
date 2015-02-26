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
import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader.ImageType;
import org.kaaproject.kaa.demo.cityguide.image.LoadingImageView;
import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PlaceFragment extends CityGuideFragment {

    private String mAreaName;
    private String mCityName;
    private Category mPlaceCategory;
    private String mPlaceTitle;
    
    private LoadingImageView mPlacePhotoView;
    private Button mShowOnMapButton;
    private TextView mPlaceTitleView;
    private TextView mPlaceDescView;
    
    public PlaceFragment() {
    	super();
    }
    
	public PlaceFragment(String areaName, String cityName, Category placeCategory, String placeTitle) {
		super();
		mAreaName = areaName;
		mCityName = cityName;
		mPlaceCategory = placeCategory;
		mPlaceTitle = placeTitle;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mAreaName == null) {
			mAreaName = savedInstanceState.getString(AREA_NAME);
			mCityName = savedInstanceState.getString(CITY_NAME);
			mPlaceCategory = Category.values()[savedInstanceState.getInt(PLACE_CATEGORY)];
			mPlaceTitle = savedInstanceState.getString(PLACE_TITLE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAreaName != null) {
			outState.putString(AREA_NAME, mAreaName);
			outState.putString(CITY_NAME, mCityName);
			outState.putInt(PLACE_CATEGORY, mPlaceCategory.ordinal());
			outState.putString(PLACE_TITLE, mPlaceTitle);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_place,
				container, false);
		
		mPlacePhotoView = (LoadingImageView) rootView.findViewById(R.id.placePhoto);
		mShowOnMapButton = (Button) rootView.findViewById(R.id.showOnMap);
		mPlaceTitleView = (TextView) rootView.findViewById(R.id.placeName);
		mPlaceDescView = (TextView) rootView.findViewById(R.id.placeDesc);
		
		showPlace();
		return rootView;
	}
	
	private void showPlace() {
		final Place place = Utils.getPlace(mApplication.getCityGuideConfiguration(), mAreaName, mCityName, mPlaceCategory, mPlaceTitle);
		if (place != null) {
			mApplication.getImageLoader().loadImage(place.getPhotoUrl(), mPlacePhotoView, ImageType.SCREENAIL);
			mPlaceTitleView.setText(place.getTitle());
			mPlaceDescView.setText(place.getDescription());
			mShowOnMapButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Utils.showOnMap(mActivity, place.getLocation().getLatitude(), 
							place.getLocation().getLongitude());
				}
			});
		} else {
			mActivity.popBackStack();
		}
	}
	
	public void onEventMainThread(ConfigurationUpdated configurationUpdated) {
		showPlace();
    }
	
	@Override
	protected String getTitle() {
		return mPlaceTitle;
	}
	
	@Override
	protected boolean displayHomeAsUp() {
		return true;
	}

}
