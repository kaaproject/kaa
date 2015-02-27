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

import org.kaaproject.kaa.demo.cityguide.Area;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.adapter.AreasAdapter;
import org.kaaproject.kaa.demo.cityguide.event.ConfigurationUpdated;
import org.kaaproject.kaa.demo.cityguide.event.KaaStarted;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class AreasFragment extends CityGuideFragment {

    private View mWaitView;
    private ListView mAreasListView;
    private AreasAdapter mAreasAdapter;

    public AreasFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_areas, container,
                false);

        mWaitView = rootView.findViewById(R.id.waitProgress);
        mAreasListView = (ListView) rootView.findViewById(R.id.areasList);
        mAreasListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        onAreaClicked(position);
                    }
                });
        if (mApplication.isKaaStarted()) {
            showAreas();
        }
        return rootView;
    }

    private void showAreas() {
        mWaitView.setVisibility(View.GONE);
        List<Area> areas = mApplication.getCityGuideConfiguration().getAreas();
        mAreasAdapter = new AreasAdapter(mActivity, areas);
        mAreasListView.setVisibility(View.VISIBLE);
        mAreasListView.setAdapter(mAreasAdapter);
    }

    public void onEventMainThread(KaaStarted kaaStarted) {
        showAreas();
    }

    public void onEventMainThread(ConfigurationUpdated configurationUpdated) {
        showAreas();
    }

    private void onAreaClicked(int position) {
        Area area = mAreasAdapter.getItem(position);
        CitiesFragment citiesFragment = new CitiesFragment(area.getName());
        mActivity.openFragment(citiesFragment);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.areas_title);
    }

    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

}
