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

import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfo;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackStatus;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SoundSystemInfo;
import org.kaaproject.kaa.demo.smarthousedemo.util.Utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SoundSystemsFragment extends DevicesFragment<SoundSystemInfo>{

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SOUND_SYSTEM;
    }

    @Override
    protected DeviceAdapter<SoundSystemInfo> createDeviceAdapter() {
        return new DeviceAdapter<SoundSystemInfo>(mActivity.getDeviceStore(), mActivity, getDeviceType()) {
            
            @Override
            protected int getLayoutResId() {
                return R.layout.soundsystem_list_item;
            }
            
            protected void setAdditionalInfo(View v, SoundSystemInfo entry) {
                ImageView playbackStatusView = (ImageView)v.findViewById(R.id.playbackStatus);
                TextView currentSongNameTextView = (TextView)v.findViewById(R.id.currentSongName);
                TextView currentSongTimeView = (TextView)v.findViewById(R.id.currentSongTime);
                
                int statusResId;
                String songName = "No item";
                String songTime = "0:00 / 0:00";
                
                if (entry.getPlaybackInfo() != null) {
                    PlaybackInfo info = entry.getPlaybackInfo();
                    statusResId = info.getStatus() == PlaybackStatus.PLAYING ? 
                            R.drawable.ic_now_playing_play_normal :
                                R.drawable.ic_now_playing_pause_normal;
                    if (info.getSong() != null) {
                        songName = info.getSong().getTitle();
                        songTime = Utils.milliSecondsToTimer(info.getTime()) + " / " +
                                Utils.milliSecondsToTimer(info.getSong().getDuration());
                    }
                }
                else {
                    statusResId = R.drawable.ic_now_playing_pause_normal;
                }
                
                playbackStatusView.setBackgroundResource(statusResId);
                currentSongNameTextView.setText(songName);
                currentSongTimeView.setText(songTime);
            }
            
        };
    }
 
}
