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

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackStatus;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicTrackWidget extends RelativeLayout {
    
    private Button mTrackActionButton;
    private TextView mTrackTitleView;
    private TextView mArtistTitleView;

    private SeekBar mTrackProgressView;
    private TextView mTrackProgressTextView;
    
    private MusicDevice mDevice;
    
    private SongInfo mCurrentSong;
    private PlaybackStatus mCurrentPlaybackStatus;
    
    private int mMusicTrackPanelHeight;
    
    private View mMusicTrackPanelView;
    
    private static final int ANIMATION_DURATION = 250;
    
    private boolean mIsVisible = false;
    
    private ExpandAnimation mExpandAnimation = new ExpandAnimation();
    private CollapseAnimation mCollapseAnimation = new CollapseAnimation();

    public MusicTrackWidget(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MusicTrackWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public MusicTrackWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MusicTrackWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mMusicTrackPanelView = inflater.inflate(R.layout.music_track, this);    
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        mMusicTrackPanelView.setLayoutParams(lp);
        mMusicTrackPanelView.setVisibility(View.GONE);
        
        mMusicTrackPanelHeight = getResources().getDimensionPixelSize(R.dimen.music_track_panel_height);
        
        mTrackActionButton = (Button) findViewById(R.id.trackActionButton);
        mTrackTitleView = (TextView) findViewById(R.id.trackTitleView);
        mArtistTitleView = (TextView) findViewById(R.id.artistTitleView);
        mTrackProgressView = (SeekBar)  findViewById(R.id.trackProgressView);
        mTrackProgressTextView = (TextView) findViewById(R.id.trackProgressTextView);
        
        setBackgroundColor(getResources().getColor(R.color.device_color_music));
        
        mTrackActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayback();
            }
        });
        
        mTrackProgressView.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {                
                if (mCurrentSong != null) {                    
                    String timeText = TimeUtils.milliSecondsToTimer(progress) + "/" +
                            TimeUtils.milliSecondsToTimer(mCurrentSong.getDuration());
                    mTrackProgressTextView.setText(timeText);
                    if (fromUser) {
                        mDevice.seek(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            
        });

    }
    
    public void bind(MusicDevice device, boolean firstLoad) {
        mDevice = device;
        PlaybackInfo playbackInfo = device.getPlaybackInfo();
        if (playbackInfo != null && playbackInfo.getSong() != null) {
            mCurrentSong = playbackInfo.getSong();
            AlbumInfo album = device.getAlbum(mCurrentSong.getAlbumId());
            
            mTrackTitleView.setText(mCurrentSong.getTitle());
            mArtistTitleView.setText(album.getArtist());
            
            mTrackProgressView.setMax(mCurrentSong.getDuration());
            if (!playbackInfo.getIgnoreTimeUpdate() || firstLoad) {
                mTrackProgressView.setProgress(playbackInfo.getTime());
            }
            updateStatus(playbackInfo.getStatus());
        }
    }
    
    public void setControlsEnabled(boolean enabled) {
        mTrackProgressView.setEnabled(enabled);
        mTrackActionButton.setEnabled(enabled);
    }
    
    private void togglePlayback() {
        if (mCurrentPlaybackStatus == PlaybackStatus.PAUSED) {
            mDevice.play(mCurrentSong.getUrl());
        }
        else {
            mDevice.pause();
        }
    }
    
    private void updateStatus(PlaybackStatus status) {
        mCurrentPlaybackStatus = status;
        if (status == PlaybackStatus.PAUSED) {
            mTrackActionButton.setBackgroundResource(R.drawable.track_action_play);
        }
        else {
            mTrackActionButton.setBackgroundResource(R.drawable.track_action_pause);
        }
    }
    
    public void setVisible(boolean visible, boolean animate) {
        if (mIsVisible != visible) {
            mIsVisible = visible;
            if (mIsVisible) {
                mExpandAnimation.startAnimation(animate ? ANIMATION_DURATION : 0);
            } else {
                mCollapseAnimation.startAnimation(animate ? ANIMATION_DURATION : 0);
            }
        }
    }
    
    class ExpandAnimation extends Animation implements AnimationListener {
        
        private boolean mIsRunning = false;
        
        public ExpandAnimation() {
            setAnimationListener(this);
        }
        
        private void finish() {
            mMusicTrackPanelView.getLayoutParams().height = mMusicTrackPanelHeight;
            mMusicTrackPanelView.setVisibility(View.VISIBLE);
            mMusicTrackPanelView.requestLayout();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime == 1) {
                finish();
            } else {
                mMusicTrackPanelView.getLayoutParams().height = (int) (mMusicTrackPanelHeight * interpolatedTime);
                mMusicTrackPanelView.requestLayout();
            }
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
        
        public void startAnimation(int duration) {
            mCollapseAnimation.cancel();
            if (duration == 0) {
                cancel();
                finish();
            } else if (!mIsRunning) {
                setDuration(duration);
                mMusicTrackPanelView.startAnimation(this);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            mIsRunning = true;
            mMusicTrackPanelView.getLayoutParams().height = 0;
            mMusicTrackPanelView.setVisibility(View.VISIBLE);
            mMusicTrackPanelView.requestLayout();
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mIsRunning = false;
            
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

    }
    
    class CollapseAnimation extends Animation implements AnimationListener {

        private boolean mIsRunning = false;

        public CollapseAnimation() {
            setAnimationListener(this);
        }
        
        private void finish() {
            mMusicTrackPanelView.getLayoutParams().height = 0;
            mMusicTrackPanelView.setVisibility(View.GONE);
            mMusicTrackPanelView.requestLayout();            
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime == 1) {
                finish();
            } else {
                mMusicTrackPanelView.getLayoutParams().height = mMusicTrackPanelHeight - (int) (mMusicTrackPanelHeight * interpolatedTime);
                mMusicTrackPanelView.requestLayout();
            }
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        public void startAnimation(int duration) {
            mExpandAnimation.cancel();
            if (duration == 0) {
                cancel();
                finish();
            } else if (!mIsRunning) {
                setDuration(duration);
                mMusicTrackPanelView.startAnimation(this);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            mIsRunning = true;

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mIsRunning = false;

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

}
