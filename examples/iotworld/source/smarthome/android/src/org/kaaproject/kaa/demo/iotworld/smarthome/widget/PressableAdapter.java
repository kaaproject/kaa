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

package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;

public abstract class PressableAdapter<VH extends ViewHolder> extends RecyclerView.Adapter<VH> {

    private boolean mIsPressed = false;
    private boolean mNotifyDataSetChangedScheduled = false;
    protected int mItemCount = 0;

    public void tryNotifyDataSetChanged() {
        if (!mIsPressed) {
            mItemCount = getCurrentItemCount();
            notifyDataSetChanged();
        } else {
            mNotifyDataSetChangedScheduled = true;
        }
    }
    
    public void setRecyclerViewIsPressed(boolean isPressed) {
        mIsPressed = isPressed;
        if (!mIsPressed && mNotifyDataSetChangedScheduled) {
            mItemCount = getCurrentItemCount();
            notifyDataSetChanged();
            mNotifyDataSetChangedScheduled = false;
        }
    }
    
    @Override
    public int getItemCount() {
        return mItemCount;
    }
    
    protected abstract int getCurrentItemCount();
    
}
