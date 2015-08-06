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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PressableRecyclerView extends RecyclerView {

    public PressableRecyclerView(Context context) {
        super(context);
    }

    public PressableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PressableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Adapter<?> adapter = getAdapter();
            if (adapter instanceof PressableAdapter) {
                ((PressableAdapter<?>)adapter).setRecyclerViewIsPressed(true);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Adapter<?> adapter = getAdapter();
            if (adapter instanceof PressableAdapter) {
                ((PressableAdapter<?>)adapter).setRecyclerViewIsPressed(false);
            }
        }
        return super.dispatchTouchEvent(event);
    }
    

}
