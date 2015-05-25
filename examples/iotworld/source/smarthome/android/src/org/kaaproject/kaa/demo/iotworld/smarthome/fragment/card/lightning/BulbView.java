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
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BulbView extends ImageView {

    public BulbView(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BulbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BulbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BulbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setImageResource(R.drawable.bulb);
        setScaleType(ScaleType.FIT_CENTER);
        setAdjustViewBounds(true);
    }
    
    public void bind(BulbInfo bulb) {
        setEnabled(bulb.getStatus()==BulbStatus.ON);
    }
    

}
