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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music;

import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.AbstractGeoFencingDeviceFragment;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.MusicTrackWidget;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public abstract class AbstractMusicDeviceFragment extends AbstractGeoFencingDeviceFragment<MusicDevice> {
    
    protected TextView mNoDataText;
    
    private View mVolumeControlView;
    private SeekBar mVolumeControls;
    
    private MusicTrackWidget mMusicTrackWidget;
    
    public AbstractMusicDeviceFragment() {
        super();
    }

    public AbstractMusicDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.volume, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_volume) {
            mToolbar.toggleCustomToolbarContent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);

        mMusicTrackWidget = (MusicTrackWidget) rootView.findViewById(R.id.music_track_view);
        
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        
        mVolumeControlView = inflater.inflate(R.layout.volume_control, null);
        mVolumeControls = (SeekBar) mVolumeControlView.findViewById(R.id.volumeSeekView);
        mVolumeControls.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (fromUser) {
                    mDevice.changeVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
        });
        mToolbar.setCustomToolbarContent(mVolumeControlView);
    }

    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        PlaybackInfo playbackInfo = mDevice.getPlaybackInfo();
        if (playbackInfo != null) {
            mVolumeControls.setMax(playbackInfo.getMaxVolume());
            if (!playbackInfo.getIgnoreVolumeUpdate() || firstLoad) {
                mVolumeControls.setProgress(playbackInfo.getVolume());
            }
        }
        if (playbackInfo != null && playbackInfo.getSong() != null) {
            mMusicTrackWidget.bind(mDevice, firstLoad);
            mMusicTrackWidget.setVisible(true, !firstLoad);
        } else {
            mMusicTrackWidget.setVisible(false, !firstLoad);
        }
    }
    
    @Override
    protected void updateControlsState() {
        mMusicTrackWidget.setControlsEnabled(mControlsEnabled);
        mVolumeControls.setEnabled(mControlsEnabled);
    }

}
