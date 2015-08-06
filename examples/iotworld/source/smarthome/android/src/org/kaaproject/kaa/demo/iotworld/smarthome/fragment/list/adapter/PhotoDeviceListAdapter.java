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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list.adapter;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.PhotoDevice;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoDeviceListAdapter extends AbstractGeoFencingDeviceListAdapter<PhotoDevice> {

    public PhotoDeviceListAdapter(
            RecyclerView recyclerView,
            DeviceStore deviceStore,
            DeviceSelectionListener deviceSelectionListener) {
        super(recyclerView, deviceStore, deviceSelectionListener);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.PHOTO;
    }

    @Override
    protected int getDeviceListItemLayoutResource() {
        return R.layout.photo_device_list_item;
    }

    @Override
    protected AbstractDeviceListAdapter.ViewHolder<PhotoDevice> constructViewHolder(View v) {
        return new ViewHolder(v);
    }
    
    static class ViewHolder extends AbstractGeoFencingDeviceListAdapter.ViewHolder<PhotoDevice> {

        private TextView noAlbumSelectedView;
        private View photoDetailsView;
        private ImageView slideshowStatusImage;
        private TextView albumTitleTextView;
        private TextView photoNumberTextView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            noAlbumSelectedView = (TextView) itemView.findViewById(R.id.noAlbumSelectedView);
            photoDetailsView = itemView.findViewById(R.id.photoDetailsView);
            slideshowStatusImage = (ImageView) itemView.findViewById(R.id.slideshowStatusImage);
            albumTitleTextView = (TextView) itemView.findViewById(R.id.albumTitleText);
            photoNumberTextView = (TextView) itemView.findViewById(R.id.photoNumberView);
        }

        @Override
        protected boolean showContent(PhotoDevice device) {
            return device.getPhotoFrameStatus() != null && device.getAlbums() != null;
        }

        @Override
        protected void bindDeviceDetails(PhotoDevice device) {
            PhotoFrameStatusUpdate photoFrameStatus = device.getPhotoFrameStatus();
            PhotoAlbumInfo album = null;
            if (photoFrameStatus.getAlbumId() != null) {
                album = device.getAlbum(photoFrameStatus.getAlbumId());
            }
            if (album != null) {
                noAlbumSelectedView.setVisibility(View.GONE);
                photoDetailsView.setVisibility(View.VISIBLE);
                if (photoFrameStatus.getStatus() == SlideShowStatus.PLAYING) {
                    slideshowStatusImage.setImageResource(R.drawable.playing);
                } else {
                    slideshowStatusImage.setImageResource(R.drawable.idle);
                }
                albumTitleTextView.setText(album.getTitle());
                
                photoNumberTextView.setText(
                        photoNumberTextView.getResources().getString(R.string.photo_number_text, photoFrameStatus.getPhotoNumber(), album.getSize()));
            } else {
                noAlbumSelectedView.setVisibility(View.VISIBLE);
                photoDetailsView.setVisibility(View.GONE);
            }
        }
        
    }
}
