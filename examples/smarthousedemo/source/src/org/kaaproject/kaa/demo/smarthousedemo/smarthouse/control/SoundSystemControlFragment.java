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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control;

import org.kaaproject.kaa.demo.smarthouse.music.PlayListResponse;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackStatus;
import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SoundSystemInfo;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control.music.SongInfoAdapter;
import org.kaaproject.kaa.demo.smarthousedemo.util.Utils;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SoundSystemControlFragment extends BaseControlFragment<SoundSystemInfo> {

    private ListView mSongsListView;
    private TextView mErrorTextView;
    private ProgressBar mSongsProgressBar;
    private SongInfoAdapter mSongsAdapter;

    private TextView mCurrentSongNameView;
    private TextView mCurrentSongDetailsView;
    private Button mCurrentSongActionButton;
    private SeekBar mCurrentSongSeekBar;
    private TextView mCurrentSongTimeTextView;
    
    private SeekBar mVolumeControls;
    private boolean mShowingVolumeControls = false;
    
    private PlaybackStatus mCurrentPlaybackStatus;
    private SongInfo mCurrentSong;
    private int requestedSongPosition = -1;
    
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SOUND_SYSTEM;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_soundsystem_control, container,
                false);
        
        mSongsListView = (ListView) rootView.findViewById(R.id.songsList);
        mErrorTextView = (TextView) rootView.findViewById(R.id.errorTextView);
        mSongsProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        mSongsListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        selectItem(position, true);
                    }
                });
        
        mCurrentSongNameView = (TextView) rootView.findViewById(R.id.currentSongName);
        mCurrentSongDetailsView = (TextView) rootView.findViewById(R.id.currentSongDetails);
        mCurrentSongActionButton = (Button) rootView.findViewById(R.id.currentSongAction);
        mCurrentSongSeekBar = (SeekBar) rootView.findViewById(R.id.songSeek);
        mCurrentSongTimeTextView = (TextView) rootView.findViewById(R.id.songSeekTime);
        
        mCurrentSongNameView.setSelected(true);
        mCurrentSongDetailsView.setSelected(true);

        mSongsListView.setFastScrollEnabled(true);
        
        mCurrentSongActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
        
        mCurrentSongSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (mCurrentSong != null) {
                    String timeText = Utils.milliSecondsToTimer(progress) + "/" +
                            Utils.milliSecondsToTimer(mCurrentSong.getDuration());
                    mCurrentSongTimeTextView.setText(timeText);
                    if (fromUser) {
                        mActivity.getSmartHouseController().seekTo(mDevice.getEndpointKey(), progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
        });
        
        final View volumeControlView = inflater.inflate(R.layout.volume_control, null);
        mVolumeControls = (SeekBar) volumeControlView.findViewById(R.id.volumeSeek);
        getActionBar().setCustomView(volumeControlView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
        
        mVolumeControls.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (fromUser) {
                    mActivity.getSmartHouseController().changeVolume(mDevice.getEndpointKey(), progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
        });
        
        updateDeviceInfo(true);
        
        refresh();
        
        return rootView;
    }
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.volume, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == R.id.action_volume) {
    		mShowingVolumeControls = !mShowingVolumeControls;
    		getActionBar().setDisplayShowCustomEnabled(mShowingVolumeControls);
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void updateDeviceInfo(boolean init) {
        if (mDevice.getPlaybackInfo() != null) {
            if (mDevice.getPlaybackInfo().getSong() != null && mSongsAdapter != null) {
                if (mCurrentSong == null || !mDevice.getPlaybackInfo().getSong().getUrl().equals(mCurrentSong.getUrl())) {
                    int position = mSongsAdapter.getPositionForUrl(mDevice.getPlaybackInfo().getSong().getUrl());
                    mSongsListView.setItemChecked(position, true);
                    mSongsListView.setSelection(position);
                    selectItem(position, false);
                    if (requestedSongPosition != position) {
                        if (init) {
                            View v = mSongsListView.getChildAt(0);
                            int top = (v == null) ? 0 : v.getTop();
                            mSongsListView.setSelectionFromTop(position, top);
                        }
                        else {
                            mSongsListView.smoothScrollToPosition(position);
                        }
                    }
                    mCurrentSong = mDevice.getPlaybackInfo().getSong();
                    mCurrentSongActionButton.setVisibility(View.VISIBLE);
                    mCurrentSongSeekBar.setVisibility(View.VISIBLE);
                    mCurrentSongTimeTextView.setVisibility(View.VISIBLE);
                    mCurrentSongNameView.setText(mCurrentSong.getTitle());
                    String artist = mCurrentSong.getArtist().contains("unknown") ? 
                            "Unknown artist" : 
                                mCurrentSong.getArtist();
                    mCurrentSongDetailsView.setText(artist);
                    mCurrentSongSeekBar.setMax(mCurrentSong.getDuration());
                }
                if (mDevice.getPlaybackInfo().getTimeSetOnDevice()) {
                    mCurrentSongSeekBar.setProgress(mDevice.getPlaybackInfo().getTime());
                }
            }
            else if (mDevice.getPlaybackInfo().getSong() == null) {
                mCurrentSong = null;
                mCurrentSongActionButton.setVisibility(View.INVISIBLE);
                mCurrentSongSeekBar.setVisibility(View.INVISIBLE);
                mCurrentSongTimeTextView.setVisibility(View.INVISIBLE);
                mCurrentSongNameView.setText("No item selected");
                mCurrentSongDetailsView.setText("");
            }
            mVolumeControls.setMax(mDevice.getPlaybackInfo().getMaxVolume());
            if (mDevice.getPlaybackInfo().getVolumeSetOnDevice() || init) {
            	mVolumeControls.setProgress(mDevice.getPlaybackInfo().getVolume());
            }
            updateStatus(mDevice.getPlaybackInfo().getStatus());
        }
    }
    
    private void refresh() {
        mSongsProgressBar.setVisibility(View.VISIBLE);
        mErrorTextView.setVisibility(View.GONE);
        mSongsListView.setVisibility(View.GONE);
        mActivity.getSmartHouseController().getPlayList(mDevice.getEndpointKey(), new PlayListCallback());
    }
    
    class PlayListCallback implements CommandCallback<PlayListResponse> {

        @Override
        public void onCommandFailure(Throwable t) {
            Log.e("Kaa", "Retrieving list of device songs failed", t);
            String message;
            if (t != null) {
                message = "Unexpected error: " + t.getMessage();
            }
            else {
                message = "Unknown error!";
            }
            onError(message);
        }

        @Override
        public void onCommandSuccess(PlayListResponse result) {
             if (result != null && result.getPlayList() != null) {
                 mSongsAdapter = new SongInfoAdapter(result.getPlayList(),
                         mActivity, mSongsListView);
                 mSongsProgressBar.setVisibility(View.GONE);
                 mSongsListView.setAdapter(mSongsAdapter);
                 mSongsListView.setVisibility(View.VISIBLE);
                 mSongsAdapter.notifyDataSetChanged();
                 updateDeviceInfo(true);
             }
        }

        @Override
        public void onCommandTimeout() {
            onTimeout();
        }
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mCurrentSongNameView.setSelected(true);
        mCurrentSongDetailsView.setSelected(true);
    }

    public void onTimeout() {
        mSongsProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText("Unable to complete request within a given timeout!");
    }
    
    public void onError(final String message) {
        mSongsProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(message);
    }
    
    @Override
    protected void onDeviceInfoUpdated() {
        updateDeviceInfo(false);
    }

    private void selectItem(int position, boolean startPlayback) {
        if (mSongsListView != null) {
            mSongsAdapter.notifyDataSetChanged();
            if (startPlayback) {
                play(position);
            }
        }
    }
    
    private void togglePlayback() {
        if (mCurrentPlaybackStatus == PlaybackStatus.PAUSED) {
            play(mSongsListView.getCheckedItemPosition());
        }
        else {
            pause();
        }
    }
    
    private void pause() {
        mActivity.getSmartHouseController().pause(mDevice.getEndpointKey());
    }
    
    private void play(int position) {
        SongInfo item = mSongsAdapter.getItem(position);
        if (item != null) {
            requestedSongPosition = position;
            mActivity.getSmartHouseController().playUrl(mDevice.getEndpointKey(), item.getUrl());
        }
    }
    
    private void updateStatus(PlaybackStatus status) {
        mCurrentPlaybackStatus = status;
        if (status == PlaybackStatus.PAUSED) {
            mCurrentSongActionButton.setBackgroundResource(R.drawable.nowplaying_play);
        }
        else {
            mCurrentSongActionButton.setBackgroundResource(R.drawable.nowplaying_pause);
        }
    }
    

    
}
