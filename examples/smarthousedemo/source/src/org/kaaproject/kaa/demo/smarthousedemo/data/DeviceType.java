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

import org.kaaproject.kaa.demo.smarthousedemo.R;

public enum DeviceType {

    THERMOSTAT(R.string.thermostats, R.string.thermostat, R.drawable.group_thermo),
    TV(R.string.tvs, R.string.tv, R.drawable.group_tv),
    SOUND_SYSTEM(R.string.sound_systems, R.string.sound_system, R.drawable.group_sound_system),
    LAMP(R.string.lamps, R.string.lamp, R.drawable.group_lamp);
    
    int titleRes;
    int nameRes;
    int groupIconRes;
    
    DeviceType(int _titleRes, int _nameRes, int _groupIconRes) {
        titleRes = _titleRes;
        nameRes = _nameRes;
        groupIconRes = _groupIconRes;
    }
    
    public int getTitleRes() {
        return titleRes;
    }

    public int getNameRes() {
        return nameRes;
    }

    public int getGroupIconRes() {
        return groupIconRes;
    }
    
    private static final DeviceType[] enabledValues = new DeviceType[]{THERMOSTAT, SOUND_SYSTEM};
    
    public static DeviceType[] enabledValues() {
        return enabledValues;
    }
}
