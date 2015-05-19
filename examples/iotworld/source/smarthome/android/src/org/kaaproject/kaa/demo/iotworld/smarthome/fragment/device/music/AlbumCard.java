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

import java.nio.ByteBuffer;

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.RippleView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class AlbumCard extends RippleView {
    
    private View mNoTrackSelectedView;
    private View mCurrentTrackView;
    
    private ImageView mAlbumCoverView;
    private TextView mAlbumTitleView;
    private TextView mArtistTitleView;
    private TextView mTrackCountView;
    
    private TextView mTrackTitleView;
    private SeekBar mTrackProgressView;
    private TextView mTrackProgressTextView;
    
    private Bitmap mAlbumCoverBitmap;

    private CardView mCardView;
    
    public AlbumCard(Context context) {
        super(context);
        init();
    }

    private void init() {
        int cardsWidth = getResources().getDimensionPixelSize(R.dimen.card_width);
        int cardsHeight = getResources().getDimensionPixelSize(R.dimen.card_height);
        
        LayoutParams lp = new LayoutParams(cardsWidth, cardsHeight);
        setLayoutParams(lp);
        
        setRippleType(RECTANGLE);
        setCentered(false);
        setDuration(200);
        
        mCardView = new CardView(getContext());
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mCardView, lp);
        
        
        int cardContentPadding = getResources().getDimensionPixelSize(R.dimen.card_content_padding);
        mCardView.setContentPadding(cardContentPadding, cardContentPadding, cardContentPadding, cardContentPadding);
        mCardView.setUseCompatPadding(true);
        
        int cardCornerRadius = getResources().getDimensionPixelSize(R.dimen.card_corner_radius);
        mCardView.setRadius(cardCornerRadius);
        
        RelativeLayout rl = new RelativeLayout(getContext());
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mCardView.addView(rl, lp);
        
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_album, rl, true);
        
        mNoTrackSelectedView = findViewById(R.id.noTrackSelectedView);
        mCurrentTrackView = findViewById(R.id.currentTrackView);
        
        mAlbumCoverView = (ImageView) findViewById(R.id.albumCoverView);
        mAlbumTitleView = (TextView)findViewById(R.id.albumTitleView);
        mArtistTitleView = (TextView)findViewById(R.id.artistTitleView);
        mTrackCountView = (TextView)findViewById(R.id.trackCountView);
        
        mTrackTitleView = (TextView)findViewById(R.id.trackTitleView);
        mTrackProgressView = (SeekBar)findViewById(R.id.trackProgressView);
        mTrackProgressTextView = (TextView)findViewById(R.id.trackProgressTextView);
        
        mCardView.setCardBackgroundColor(getResources().getColor(R.color.album_color));
        
        mTrackProgressView.setEnabled(false);
    }
    
    public void bind(AlbumInfo album, PlaybackInfo playbackInfo) {
        
        if (playbackInfo != null && 
                playbackInfo.getSong() != null && 
                playbackInfo.getSong().getAlbumId().equals(album.getAlbumId())) {
            mNoTrackSelectedView.setVisibility(View.GONE);
            mCurrentTrackView.setVisibility(View.VISIBLE);
            SongInfo song = playbackInfo.getSong();
            mTrackTitleView.setText(song.getTitle());
            mTrackProgressView.setMax(song.getDuration());
            mTrackProgressView.setProgress(playbackInfo.getTime());
            String timeText = TimeUtils.milliSecondsToTimer(playbackInfo.getTime()) + "/" +
                    TimeUtils.milliSecondsToTimer(song.getDuration());
            mTrackProgressTextView.setText(timeText);
        } else {
            mNoTrackSelectedView.setVisibility(View.VISIBLE);
            mCurrentTrackView.setVisibility(View.GONE);
        }
        
        ByteBuffer buffer = album.getCover();
        byte[] coverData = buffer.array();
        
        Bitmap prevBitmap = mAlbumCoverBitmap;
        mAlbumCoverBitmap = BitmapFactory.decodeByteArray(coverData, 0, coverData.length);
        mAlbumCoverView.setImageBitmap(mAlbumCoverBitmap);
        if (prevBitmap != null) {
            prevBitmap.recycle();
        }
        mAlbumTitleView.setText(album.getTitle());
        mArtistTitleView.setText(album.getArtist());
        mTrackCountView.setText(getResources().getString(R.string.track_count_text, album.getSongs().size()));
        
    }
}
