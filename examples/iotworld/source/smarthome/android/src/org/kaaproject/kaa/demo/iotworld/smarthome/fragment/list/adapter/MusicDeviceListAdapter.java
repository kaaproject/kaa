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

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackStatus;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceStore;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicDeviceListAdapter extends AbstractGeoFencingDeviceListAdapter<MusicDevice> {

    public MusicDeviceListAdapter(
            RecyclerView recyclerView,
            DeviceStore deviceStore,
            DeviceSelectionListener deviceSelectionListener) {
        super(recyclerView, deviceStore, deviceSelectionListener);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.MUSIC;
    }

    @Override
    protected int getDeviceListItemLayoutResource() {
        return R.layout.music_device_list_item;
    }

    @Override
    protected AbstractDeviceListAdapter.ViewHolder<MusicDevice> constructViewHolder(View v) {
        return new ViewHolder(v);
    }
    
    static class ViewHolder extends AbstractGeoFencingDeviceListAdapter.ViewHolder<MusicDevice> {

        private TextView noTrackSelectedView;
        private View musicDetailsView;
        private ImageView playbackStatusImage;
        private TextView songNameTextView;
        private TextView trackProgressTextView;
        
        public ViewHolder(View itemView) {
            super(itemView);
            noTrackSelectedView = (TextView) itemView.findViewById(R.id.noTrackSelectedView);
            musicDetailsView = itemView.findViewById(R.id.musicDetailsView);
            playbackStatusImage = (ImageView) itemView.findViewById(R.id.playbackStatusImage);
            songNameTextView = (TextView) itemView.findViewById(R.id.songNameText);
            trackProgressTextView = (TextView) itemView.findViewById(R.id.trackProgressTextView);
        }

        @Override
        protected boolean showContent(MusicDevice device) {
            return device.getPlaybackInfo() != null && device.getAlbums() != null;
        }

        @Override
        protected void bindDeviceDetails(MusicDevice device) {
            PlaybackInfo playbackInfo = device.getPlaybackInfo();
            SongInfo song = playbackInfo.getSong();
            AlbumInfo album = null;
            if (song != null) {
                album = device.getAlbum(song.getAlbumId());
            }
            if (song != null && album != null) {
                noTrackSelectedView.setVisibility(View.GONE);
                musicDetailsView.setVisibility(View.VISIBLE);
                if (playbackInfo.getStatus() == PlaybackStatus.PLAYING) {
                    playbackStatusImage.setImageResource(R.drawable.playing);
                } else {
                    playbackStatusImage.setImageResource(R.drawable.idle);
                }
                songNameTextView.setText(song.getTitle());
                String timeText = TimeUtils.milliSecondsToTimer(playbackInfo.getTime()) + "/" +
                        TimeUtils.milliSecondsToTimer(song.getDuration());
                
                trackProgressTextView.setText(timeText);
            } else {
                noTrackSelectedView.setVisibility(View.VISIBLE);
                musicDetailsView.setVisibility(View.GONE);
            }
        }
        
    }
}
