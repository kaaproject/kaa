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

package org.kaaproject.kaa.demo.cityguide.adapter;

import java.util.List;

import org.kaaproject.kaa.demo.cityguide.Place;
import org.kaaproject.kaa.demo.cityguide.R;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader;
import org.kaaproject.kaa.demo.cityguide.image.ImageLoader.ImageType;
import org.kaaproject.kaa.demo.cityguide.image.LoadingImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * The implementation of the {@link BaseAdapter} class. Used as an adapter class for the places list view.
 * Provides list item views containing a photo, name and description of each place.
 */
public class PlacesAdapter extends BaseAdapter {

    private Context mContext;
    private ImageLoader mImageLoader;
    private List<Place> mPlaces;

    public PlacesAdapter(Context context, ImageLoader imageLoader,
            List<Place> places) {
        mContext = context;
        mImageLoader = imageLoader;
        mPlaces = places;
    }

    @Override
    public int getCount() {
        return mPlaces.size();
    }

    @Override
    public Place getItem(int position) {
        if (position < getCount()) {
            return mPlaces.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.place_list_item, null);
        }
        Place place = mPlaces.get(position);

        LoadingImageView placePhotoView = (LoadingImageView) v
                .findViewById(R.id.placePhoto);
        mImageLoader.loadImage(place.getPhotoUrl(), placePhotoView,
                ImageType.THUMBNAIL);

        TextView placeNameView = (TextView) v.findViewById(R.id.placeName);
        placeNameView.setText(place.getTitle());

        TextView placeDescView = (TextView) v.findViewById(R.id.placeDesc);
        placeDescView.setText(place.getDescription());

        return v;
    }

}
