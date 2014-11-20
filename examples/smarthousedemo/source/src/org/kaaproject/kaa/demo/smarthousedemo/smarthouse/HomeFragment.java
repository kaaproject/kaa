/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import java.util.Arrays;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.SmartHouseActivity;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.adapter.SectionAdapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private SmartHouseActivity mActivity;
    private SectionAdapter mSectionAdapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (SmartHouseActivity)this.getActivity();
        mSectionAdapter = new SectionAdapter() {
            @Override
            protected View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
                LayoutInflater inflater = mActivity.getLayoutInflater();
                TextView result = (TextView) convertView;
                if (convertView == null) {
                    result = (TextView) inflater.inflate(R.layout.home_list_header, null);
                }
                result.setText(caption);
                return result;
            }
        };
        mSectionAdapter.addSection(HomeSection.DEVICES.getDesc(),
                new DevicesSectionAdapter(mActivity));
        if (!mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().register(this);
        }
    }
    
    @Override
    public void onDestroy() {
        if (mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().unregister(this);
        }
        mActivity = null;
        super.onDestroy();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ListView homeListView =  (ListView)view.findViewById(R.id.homeList);

        homeListView.setAdapter(mSectionAdapter);
        
        homeListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getAdapter().getItem(position);
                if (item instanceof DeviceType) {
                    mActivity.switchToDeviceType((DeviceType)item);
                }
            }
        });

        return view;
    }
    
    public void onEventMainThread(DeviceStore.DeviceAdded deviceAdded) {
        mSectionAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(DeviceStore.DeviceRemoved deviceRemoved) {
        mSectionAdapter.notifyDataSetChanged();
    }
    
    public void onEventMainThread(DeviceStore.DeviceUpdated deviceUpdated) {
        mSectionAdapter.notifyDataSetChanged();
    }
    
    private class DevicesSectionAdapter extends HomeSectionAdapter<DeviceType> {

        public DevicesSectionAdapter(Context context) {
            super(context, Arrays.asList(DeviceType.enabledValues()));
        }

        @Override
        protected View updateView(int position, View row) {
            final DeviceType type = getItem(position);
            ImageView image = (ImageView) row.findViewById(R.id.homeItemImage);
            image.setImageResource(type.getGroupIconRes());

            TextView textView = (TextView) row.findViewById(R.id.homeItemTitle);
            textView.setText(getContext().getResources().getString(type.getTitleRes()));

            textView = (TextView) row.findViewById(R.id.homeItemLabel);
            textView.setText(String.valueOf(mActivity.getDeviceStore().getDevices(type).size()));

            return row;
        }
    }

}
