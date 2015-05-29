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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.photo;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.RippleView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoAlbumCard extends RippleView {
    
    private ImageView mThumbnailView;
    private TextView mAlbumTitleView;
    private View mAlbumInfoView;
    private TextView mPhotoCountView;
    private View mCurrentPhotoView;
    private ImageView mSlideshowStatusImageView;
    private TextView mPhotoNumberView;
    
    private Bitmap mThumbnailBitmap;

    private CardView mCardView;
    
    public PhotoAlbumCard(Context context) {
        super(context);
        init();
    }

    private void init() {
        int cardsWidth = getResources().getDimensionPixelSize(R.dimen.photo_album_card_width);
        
        LayoutParams lp = new LayoutParams(cardsWidth, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        
        setRippleType(RECTANGLE);
        setCentered(false);
        setDuration(200);
        
        mCardView = new CardView(getContext());
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mCardView, lp);
        
        
        int cardContentPadding = getResources().getDimensionPixelSize(R.dimen.photo_album_card_content_padding);
        mCardView.setContentPadding(cardContentPadding, cardContentPadding, cardContentPadding, cardContentPadding);
        mCardView.setUseCompatPadding(true);
        
        int cardCornerRadius = getResources().getDimensionPixelSize(R.dimen.card_corner_radius);
        mCardView.setRadius(cardCornerRadius);
        
        RelativeLayout rl = new RelativeLayout(getContext());
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mCardView.addView(rl, lp);
        
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_photo_album, rl, true);
        
        mThumbnailView = (ImageView) findViewById(R.id.thumbnailView);
        mAlbumTitleView = (TextView)findViewById(R.id.albumTitleView);
        
        mAlbumInfoView = findViewById(R.id.albumInfoView);
        mPhotoCountView = (TextView)findViewById(R.id.photoCountView);
        mCurrentPhotoView = findViewById(R.id.currentPhotoView);
        
        mSlideshowStatusImageView = (ImageView) findViewById(R.id.slideshowStatusImage);
        mPhotoNumberView = (TextView)findViewById(R.id.photoNumberView);
        
        mCardView.setCardBackgroundColor(getResources().getColor(R.color.photo_album_color));
    }
    
    public void bind(PhotoAlbumInfo album, PhotoFrameStatusUpdate status) {
        
        byte[] thumbnailData = null;
        if (status != null && 
                status.getAlbumId() != null && 
                        status.getAlbumId().equals(album.getId())) {
            mAlbumInfoView.setVisibility(View.GONE);
            mCurrentPhotoView.setVisibility(View.VISIBLE);
            
            mSlideshowStatusImageView.setImageResource(status.getStatus() == SlideShowStatus.PLAYING ? 
                    R.drawable.ic_slideshow_status_playing : R.drawable.ic_slideshow_status_paused);
            mPhotoNumberView.setText(getResources().getString(R.string.photo_number_text, status.getPhotoNumber(), album.getSize()));
            
            thumbnailData = status.getThumbnail().array();
        } else {
            mAlbumInfoView.setVisibility(View.VISIBLE);
            mCurrentPhotoView.setVisibility(View.GONE);
            mPhotoCountView.setText(getResources().getString(R.string.photos_count_text, album.getSize()));
            thumbnailData = album.getThumbnail().array();
        }
        
        Bitmap prevBitmap = mThumbnailBitmap;
        mThumbnailBitmap = BitmapFactory.decodeByteArray(thumbnailData, 0, thumbnailData.length);
        mThumbnailView.setImageBitmap(mThumbnailBitmap);
        if (prevBitmap != null) {
            prevBitmap.recycle();
        }
        mAlbumTitleView.setText(album.getTitle());
    }
   
}
