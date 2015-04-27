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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.lightning.BulbsListAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.lightning.BulbsListAdapter.BulbItemListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LightningDeviceFragment 
            extends AbstractGeoFencingDeviceFragment<LightningDevice> 
            implements BulbItemListener {
    
    private TextView mNoDataText;
    private RecyclerView mRecyclerView;
    private BulbsListAdapter mBulbsListAdapter;
    
    public LightningDeviceFragment() {
        super();
    }

    public LightningDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_lightning_device;
    }

    @Override
    public String getFragmentTag() {
        return LightningDeviceFragment.class.getSimpleName();
    }
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        
        
        mBulbsListAdapter = new BulbsListAdapter(mRecyclerView, mDevice, this);
        
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        mBulbsListAdapter.notifyDataSetChanged();
        if (mBulbsListAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBulbBrightnessChanged(String bulbId, int value) {
        mDevice.changeBulbBrightness(bulbId, value);
    }

    @Override
    public void onBulbStateChanged(String bulbId, boolean enabled) {
        mDevice.changeBulbState(bulbId, enabled ? BulbStatus.ON : BulbStatus.OFF);
    }
 
    @Override
    protected void updateControlsState() {}

}
