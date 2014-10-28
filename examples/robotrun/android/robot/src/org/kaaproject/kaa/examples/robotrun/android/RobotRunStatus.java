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

package org.kaaproject.kaa.examples.robotrun.android;

public enum RobotRunStatus {
    
    SETUP(R.string.status_setup, R.drawable.status_setup),
    SETTING_UP(R.string.status_setting_up, R.drawable.status_setting_up),
    READY(R.string.status_ready, R.drawable.status_ready),
    STARTED(R.string.status_started, R.drawable.status_started);
    
    int backgroundResId;
    int textResId;        
    
    RobotRunStatus(int _textResId, int _backgroundResId) {
        textResId = _textResId;
        backgroundResId = _backgroundResId;
    }
    
}
