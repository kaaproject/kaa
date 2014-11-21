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
package org.kaaproject.kaa.demo.smarthousedemo.data;

import android.support.v4.app.Fragment;

public class FragmentInfo {
    
    private int iconResId;
    private int titleResId;        
    private Fragment fragment;
    
    public FragmentInfo(int iconResId, int titleResId, Fragment fragment) {
        this.fragment = fragment;
        this.titleResId = titleResId;
        this.iconResId = iconResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public Fragment getFragment() {
        return fragment;
    }
    
    
}