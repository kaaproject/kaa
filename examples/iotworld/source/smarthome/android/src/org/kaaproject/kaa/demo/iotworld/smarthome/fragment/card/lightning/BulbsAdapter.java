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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.lightning;

import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class BulbsAdapter extends RecyclerView.Adapter<BulbsAdapter.ViewHolder> {

    private final RecyclerView mRecyclerView;
    private LightningDevice mLightningDevice;
    
    public BulbsAdapter(RecyclerView recyclerView, 
                        LightningDevice lightningDevice) {
        mRecyclerView = recyclerView;
        mLightningDevice = lightningDevice;
        mRecyclerView.setAdapter(this);
    }
    
    public void setDevice(LightningDevice lightningDevice) {
        mLightningDevice = lightningDevice;
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemCount() {
        if (mLightningDevice != null) {
            return mLightningDevice.getBulbs().size();
        } else {
            return 0;
        }
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mLightningDevice != null) {
            BulbInfo bulb = mLightningDevice.getBulbs().get(position);
            holder.bind(bulb);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BulbView bulbView = new BulbView(parent.getContext());
        ViewHolder vhCard = new ViewHolder(bulbView);
        return vhCard;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        private BulbView mBulbView;
        
        public ViewHolder(BulbView bulbView) {
            super(bulbView);         
            mBulbView = bulbView;
        }
        
        protected void bind(BulbInfo bulb) {
            mBulbView.bind(bulb);
        }
        
    }
    

}
