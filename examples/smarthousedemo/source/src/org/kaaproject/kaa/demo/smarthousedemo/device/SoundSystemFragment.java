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
package org.kaaproject.kaa.demo.smarthousedemo.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.demo.smarthouse.music.ChangeVolumeRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PauseRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayListRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlayRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfo;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackInfoRequest;
import org.kaaproject.kaa.demo.smarthouse.music.PlaybackStatus;
import org.kaaproject.kaa.demo.smarthouse.music.SeekRequest;
import org.kaaproject.kaa.demo.smarthouse.music.SongInfo;
import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.data.SongComparator;
import org.kaaproject.kaa.demo.smarthousedemo.util.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SoundSystemFragment extends DeviceFragment implements OnCompletionListener {

    private static final int PROGRESS_UPDATE_DELAY = 1000;
    
    /** Instance of android media player used for music playback */
    private MediaPlayer player;
    private AudioManager mAudioManager;
    private SettingsContentObserver mSettingsContentObserver;
    private PlaybackInfo playbackInfo = new PlaybackInfo();
    private SongInfo mCurrentSong;
    
    private Handler soundSystemHandler = new Handler();
    
    private Map<String,SongInfo> mSongMap = new HashMap<>();
    private List<String> mSongUrls = new ArrayList<>();
    
    private ProgressChangeTracker progressChangeTracker = new ProgressChangeTracker();
    
    private TextView mSongAuthor;
    private TextView mSongName;
    private TextView mSongAlbum;
    
    private SeekBar mSongProgress;
    private TextView mSongProgressElapsed;
    private TextView mSongProgressLeft;
    
    private Button mPreviousButton;
    private Button mCurrentActionButton;
    private Button mNextButton;
    private SongComparator mSongComparator;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mSettingsContentObserver = new SettingsContentObserver(soundSystemHandler);
        mSongComparator = new SongComparator();
        
        initPlayList();
        playbackInfo.setTimeSetOnDevice(true);
        playbackInfo.setVolumeSetOnDevice(false);
        playbackInfo.setMaxVolume(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        playbackInfo.setVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_soundsystem, container,
                false);
        
        mSongAuthor = (TextView) rootView.findViewById(R.id.songAuthor);
        mSongName = (TextView) rootView.findViewById(R.id.songName);
        mSongAlbum = (TextView) rootView.findViewById(R.id.songAlbum);
        
        mSongProgress = (SeekBar) rootView.findViewById(R.id.songProgress);
        mSongProgressElapsed = (TextView) rootView.findViewById(R.id.songProgressElapsed);
        mSongProgressLeft = (TextView) rootView.findViewById(R.id.songProgressLeft);
        
        mPreviousButton = (Button) rootView.findViewById(R.id.previousSongAction);
        mCurrentActionButton = (Button) rootView.findViewById(R.id.currentSongAction);
        mNextButton = (Button) rootView.findViewById(R.id.nextSongAction);
        
        mPreviousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        
        mCurrentActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
        
        mSongProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                if (mCurrentSong != null) {
                    if (fromUser) {
                        seekTo(progress);
                    }
                    else {
                        int elapsed = progress;
                        int left = mCurrentSong.getDuration() - elapsed;
                        String elapsedText = Utils.milliSecondsToTimer(elapsed);
                        String leftText = "-"+Utils.milliSecondsToTimer(left);
                        mSongProgressElapsed.setText(elapsedText);
                        mSongProgressLeft.setText(leftText);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
        });
        
        updateSoundSystemUI();
        
        return rootView;
    }
    
    class SettingsContentObserver extends ContentObserver {

    	 public SettingsContentObserver(Handler handler) {
    	     super(handler);
    	 } 

    	 @Override
    	 public boolean deliverSelfNotifications() {
    	      return super.deliverSelfNotifications(); 
    	 }

    	 @Override
    	 public void onChange(boolean selfChange) {
    	     super.onChange(selfChange);
    	     int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    	     if (volume != playbackInfo.getVolume()) {
    	    	 playbackInfo.setVolume(volume);
    	    	 playbackInfo.setVolumeSetOnDevice(true);
    	    	 updateSoundSystemInfo();
    	    	 playbackInfo.setVolumeSetOnDevice(false);
    	     }
    	 }
    }
    
    private void initPlayList() {
        List<SongInfo> songs = new ArrayList<>();
        mSongMap.clear();
        mSongUrls.clear();
        ContentResolver musicResolver = mActivity.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int displayNameColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            do {
                String album = musicCursor.getString(albumColumn);
                String artist = musicCursor.getString(artistColumn);
                String displayName = musicCursor.getString(displayNameColumn);
                long duration = musicCursor.getLong(durationColumn);
                String title = musicCursor.getString(titleColumn);
                long id = musicCursor.getLong(idColumn);
                
                SongInfo songInfo = new SongInfo();
                songInfo.setAlbum(album);
                songInfo.setArtist(artist);
                songInfo.setDisplayName(displayName);
                songInfo.setDuration((int)duration);
                songInfo.setTitle(title);
                songInfo.setUrl(id+"");
                songs.add(songInfo);
              }
              while (musicCursor.moveToNext());
        }
        Collections.sort(songs, mSongComparator);
        for (SongInfo song : songs) {
            mSongMap.put(song.getUrl(), song);
            mSongUrls.add(song.getUrl());
        }
    }
    
    @Override
    public void handleEvent(Object event, String endpointSourceKey) {
        if (event instanceof PlayRequest) {
            final PlayRequest request = (PlayRequest)event;
            soundSystemHandler.post(new Runnable() {
                @Override
                public void run() {
                    playUrl(request.getUrl());
                }
            });
        }
        else if (event instanceof PauseRequest) {
            soundSystemHandler.post(new Runnable() {
                @Override
                public void run() {
                    pause();
                }
            });
        }
        else if (event instanceof PlayListRequest) {
            List<SongInfo> playList = new ArrayList<>(mSongMap.values());
            Collections.sort(playList, mSongComparator);
            mActivity.getSmartHouseController().sendPlayList(playList, endpointSourceKey);
        }
        else if (event instanceof PlaybackInfoRequest) {
            mActivity.getSmartHouseController().sendPlaybackInfo(playbackInfo, endpointSourceKey);
        }
        else if (event instanceof SeekRequest) {
            final SeekRequest seekRequest = (SeekRequest)event;
            soundSystemHandler.post(new Runnable() {
                @Override
                public void run() {
                    seekTo(seekRequest.getTime());
                }
            });
        }
        else if (event instanceof ChangeVolumeRequest) {
        	ChangeVolumeRequest changeVolumeRequest = (ChangeVolumeRequest)event; 
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, changeVolumeRequest.getVolume(), 0);
            playbackInfo.setVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            updateSoundSystemInfo();
        }
    }
    
    class ProgressChangeTracker implements Runnable {
        @Override
        public void run() {
            if(player != null){
                updateSoundSystemInfo();
                if (player.isPlaying()) {
                    soundSystemHandler.postDelayed(this, PROGRESS_UPDATE_DELAY);
                }
            }
        }
    }
    
    private void seekTo(int time) {
        if (player.isPlaying()) {
            soundSystemHandler.removeCallbacks(progressChangeTracker);
        }
        playbackInfo.setTime(time);
        player.seekTo(playbackInfo.getTime());
        updateSoundSystemInfo();
        if (player.isPlaying()) {
            soundSystemHandler.postDelayed(progressChangeTracker, PROGRESS_UPDATE_DELAY);
        }
    }
    
    private void playPrevious() {
        int index = -1;
        if (mCurrentSong != null) {
            index = mSongUrls.indexOf(mCurrentSong.getUrl());
        }
        index--;
        if (index <= -1) {
            index = mSongUrls.size()-1;
        }
        String url = mSongUrls.get(index);
        playUrl(url);
    }
    
    private void playNext() {
        int index = -1;
        if (mCurrentSong != null) {
            index = mSongUrls.indexOf(mCurrentSong.getUrl());
        }
        index++;
        if (index >= mSongUrls.size()) {
            index = 0;
        }
        String url = mSongUrls.get(index);
        playUrl(url);
    }
    
    private void togglePlayback() {
        if (playbackInfo.getStatus() == PlaybackStatus.PAUSED) {
            String url = null;
            if (mCurrentSong != null) {
                url = mCurrentSong.getUrl();
            }
            else {
                url = mSongUrls.get(0);
            }
            playUrl(url);
        }
        else {
            pause();
        }
    }
    
    /** Start url playback by android media player */
    private void playUrl(String url) {
        if (playbackInfo.getSong() == null || !playbackInfo.getSong().getUrl().equals(url)) {
            SongInfo song = mSongMap.get(url);
            playbackInfo.setSong(song);
            prepareFromUrl(url);
            player.start();
            updateSoundSystemInfo();
            soundSystemHandler.postDelayed(progressChangeTracker, PROGRESS_UPDATE_DELAY);
        }
        else if (!player.isPlaying()) {
            player.start();
            updateSoundSystemInfo();
            soundSystemHandler.postDelayed(progressChangeTracker, PROGRESS_UPDATE_DELAY);
        }
    }
    
    private void prepareFromUrl(String url) {
        long songId = Long.valueOf(url);
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songId);
        try {
            player.reset();
            player.setDataSource(mActivity, trackUri);
            player.prepare();
        }
        catch (Exception e) {
          Log.e("Kaa", "Unable to start playback for uri "+trackUri, e);
        }
    }
    
    private void restorePlayer() {
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        if (playbackInfo.getSong() != null) {
            prepareFromUrl(playbackInfo.getSong().getUrl());
        }
        if (playbackInfo.getTime() != null && playbackInfo.getTime() > 0) {
            player.seekTo(playbackInfo.getTime());
        }
    }
    
    private void pause() {
        if (player.isPlaying()) {
            player.pause();
            updateSoundSystemInfo();
        }
    }
    
    private void updateSoundSystemInfo() {
        playbackInfo.setStatus(player != null && player.isPlaying() ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED);
        playbackInfo.setTime(player != null ? player.getCurrentPosition() : 0);
        updateSoundSystemUI();
        mActivity.getSmartHouseController().updatePlaybackInfo(copyPlaybackInfo());
    }
    
    private void updateSoundSystemUI() {
        if (playbackInfo.getSong() != null) {
            if (mCurrentSong == null || !playbackInfo.getSong().getUrl().equals(mCurrentSong.getUrl())) {
                mCurrentSong = playbackInfo.getSong();
                String artist = mCurrentSong.getArtist().contains("unknown") ? 
                        "Unknown artist" : 
                            mCurrentSong.getArtist();
                String album = mCurrentSong.getAlbum().contains("unknown") ? 
                        "Unknown album" : 
                            mCurrentSong.getAlbum();          
                mSongAuthor.setVisibility(View.VISIBLE);
                mSongAlbum.setVisibility(View.VISIBLE);
                mSongProgress.setVisibility(View.VISIBLE);
                mSongProgressElapsed.setVisibility(View.VISIBLE);
                mSongProgressLeft.setVisibility(View.VISIBLE);
                mSongAuthor.setText(artist);
                mSongName.setText(mCurrentSong.getTitle());
                mSongAlbum.setText(album);
                mSongProgress.setMax(mCurrentSong.getDuration());
            }
            mSongProgress.setProgress(playbackInfo.getTime());
        }
        else {
            mCurrentSong = null;
            mSongAuthor.setVisibility(View.INVISIBLE);
            mSongAlbum.setVisibility(View.INVISIBLE);
            mSongProgress.setVisibility(View.INVISIBLE);
            mSongProgressElapsed.setVisibility(View.INVISIBLE);
            mSongProgressLeft.setVisibility(View.INVISIBLE);
            mSongName.setText("No song selected");
        }
        mCurrentActionButton.setBackgroundResource(playbackInfo.getStatus() == PlaybackStatus.PAUSED ? 
                R.drawable.music_play : R.drawable.music_pause);
    }
    
    private PlaybackInfo copyPlaybackInfo() {
    	PlaybackInfo copy = new PlaybackInfo();
    	copy.setMaxVolume(playbackInfo.getMaxVolume());
    	copy.setVolume(playbackInfo.getVolume());
    	copy.setSong(playbackInfo.getSong());
    	copy.setStatus(playbackInfo.getStatus());
    	copy.setTime(playbackInfo.getTime());
    	copy.setTimeSetOnDevice(playbackInfo.getTimeSetOnDevice());
    	copy.setVolumeSetOnDevice(playbackInfo.getVolumeSetOnDevice());
    	return copy;
    }
    
    @Override
    public void onCompletion(MediaPlayer mp) {
    	soundSystemHandler.removeCallbacks(progressChangeTracker);
    	playbackInfo.setStatus(PlaybackStatus.PAUSED);
    	playbackInfo.setTime(0);
    	updateSoundSystemUI();
    	mActivity.getSmartHouseController().updatePlaybackInfo(copyPlaybackInfo());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mActivity.getApplicationContext().getContentResolver().registerContentObserver( 
        	    android.provider.Settings.System.CONTENT_URI, true, 
        	    mSettingsContentObserver );
        restorePlayer();
        updateSoundSystemInfo();
    }
    
    @Override
    public void onPause() {
        playbackInfo.setStatus(PlaybackStatus.PAUSED);
        playbackInfo.setTime(player.getCurrentPosition());
        player.release();
        player = null;
        mActivity.getSmartHouseController().updatePlaybackInfo(playbackInfo);
    	mActivity.getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
