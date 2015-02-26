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

package org.kaaproject.kaa.demo.cityguide.dialog;

import java.util.List;

import org.kaaproject.kaa.demo.cityguide.AvailableArea;
import org.kaaproject.kaa.demo.cityguide.CityGuideApplication;
import org.kaaproject.kaa.demo.cityguide.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SetLocationDialog extends Dialog {
	
	private CityGuideApplication mApplication;
	private ArrayAdapter<String> mAreasAdapter;
	private ArrayAdapter<String> mCitiesAdapter;
	private Spinner mSelectAreaSpinner;
	private Spinner mSelectCitySpinner;

	public SetLocationDialog(Context context, 
						     CityGuideApplication application, 
						     final SetLocationCallback callback) {
		super(context);
		mApplication = application;
		setContentView(R.layout.dialog_set_location);
		setTitle(R.string.action_set_location);
		
		mSelectAreaSpinner = (Spinner)findViewById(R.id.selectAreaSpinner);
		mAreasAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
		mSelectAreaSpinner.setAdapter(mAreasAdapter);
		
		mSelectCitySpinner = (Spinner)findViewById(R.id.selectCitySpinner);
		mCitiesAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item);
		mSelectCitySpinner.setAdapter(mCitiesAdapter);
		
		Button okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				String area = (String)mSelectAreaSpinner.getSelectedItem();
				if (area != null && area.length() == 0) {
					area = null;
				}
				String city = (String)mSelectCitySpinner.getSelectedItem();
				if (city != null && city.length() == 0) {
					city = null;
				}
				callback.onLocationSelected(area, city);
			}
		});
		
		
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		updateAreasSpinner();
		
		int position = 0;
		String currentArea = mApplication.getCityGuideProfile().getArea();
		if (currentArea != null) {
			position = mAreasAdapter.getPosition(currentArea);
		}
		mSelectAreaSpinner.setSelection(position);

		updateCitiesSpinner();
		
		position = 0;
		String currentCity = mApplication.getCityGuideProfile().getCity();
		if (currentCity != null) {
			position = mCitiesAdapter.getPosition(currentCity);
		}
		
		mSelectCitySpinner.setSelection(position);
		
		mSelectAreaSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				updateCitiesSpinner();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				updateCitiesSpinner();
			}
		});
		
	}
	
	private void updateAreasSpinner() {
		mAreasAdapter.clear();
		mAreasAdapter.add("");
		List<AvailableArea> availableAreas = mApplication.getCityGuideConfiguration().getAvailableAreas();
		for (AvailableArea area : availableAreas) {
			mAreasAdapter.add(area.getName());
		}
	}
	
	private void updateCitiesSpinner() {
		mCitiesAdapter.clear();
		mCitiesAdapter.add("");
		String areaName = (String)mSelectAreaSpinner.getSelectedItem();
		if (areaName != null && areaName.length() > 0) {
			List<AvailableArea> availableAreas = mApplication.getCityGuideConfiguration().getAvailableAreas();
			for (AvailableArea area : availableAreas) {
				if (area.getName().equals(areaName)) {
					for (String city : area.getAvailableCities()) {
						mCitiesAdapter.add(city);
					}
					break;
				}
			}
		}
	}
	
	public static interface SetLocationCallback {
		
		void onLocationSelected (String area, String city);
		
	}
	

}
