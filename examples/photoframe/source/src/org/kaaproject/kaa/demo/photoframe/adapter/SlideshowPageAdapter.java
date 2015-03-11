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

package org.kaaproject.kaa.demo.photoframe.adapter;

import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.image.ImageLoader;
import org.kaaproject.kaa.demo.photoframe.image.ImageLoader.ImageType;
import org.kaaproject.kaa.demo.photoframe.image.LoadingImageView;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * The Class SlideshowPageAdapter.
 * Implementation of {@link PagerAdapter} class. Used as adapter class for images slideshow view.
 * Provides image views with screenails fetched via cursor from {@link MediaStore} 
 * for requested album identified by bucketId.
 */
public class SlideshowPageAdapter extends PagerAdapter {

    private ImageLoader mImageLoader;
    private Cursor mCursor;
    private int mDataIndex;
    private LayoutInflater mLayoutInflater;

    public SlideshowPageAdapter(Context context, ImageLoader imageLoader, String bucketId) {
        mImageLoader = imageLoader;
        mCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media.DATA },
                MediaStore.Images.Media.BUCKET_ID + "=? ",
                new String[] { bucketId }, null);
        mDataIndex = mCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.image_item, container,
                false);
        LoadingImageView imageView = (LoadingImageView) itemView.findViewById(R.id.imageView);

        mCursor.moveToPosition(position);
        String imagePath = mCursor.getString(mDataIndex);
        
        mImageLoader.loadImage(imagePath, imageView, ImageType.SCREENAIL);
        
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}
