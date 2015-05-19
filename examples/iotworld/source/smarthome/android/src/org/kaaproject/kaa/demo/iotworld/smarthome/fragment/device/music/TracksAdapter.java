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

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {

    private final RecyclerView mRecyclerView;
    private final TrackSelectionListener mTrackSelectionListener;
    private final TrackItemClickListener mTrackItemClickListener = new TrackItemClickListener();
    private final MusicDevice mDevice;
    private final AlbumInfo mAlbum;
    
    public TracksAdapter(RecyclerView recyclerView, 
                         MusicDevice device,
                         AlbumInfo album, 
                         TrackSelectionListener trackSelectionListener) {
        mRecyclerView = recyclerView;
        mDevice = device;
        mAlbum = album;
        mRecyclerView.setAdapter(this);
        mTrackSelectionListener = trackSelectionListener;
    }
    
    @Override
    public int getItemCount() {
        return mAlbum.getSongs().size();
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SongInfo song = mAlbum.getSongs().get(position);
        int selectedSongPosition = -1;
        if (mDevice.getPlaybackInfo() != null && 
            mDevice.getPlaybackInfo().getSong() != null &&
            mDevice.getPlaybackInfo().getSong().getAlbumId().equals(mAlbum.getAlbumId())) {
            selectedSongPosition = mAlbum.getSongs().indexOf(mDevice.getPlaybackInfo().getSong());
        }
        holder.trackNumberView.setText(""+(position+1));
        holder.trackTitleView.setText(song.getTitle());
        holder.trackDurationView.setText(TimeUtils.milliSecondsToTimer(song.getDuration()));
        
        holder.setSelected(position == selectedSongPosition);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_list_item,parent,false);
        v.setOnClickListener(mTrackItemClickListener);
        FontUtils.setRobotoFont(v);
        ViewHolder vhItem = new ViewHolder(v);
        return vhItem;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView trackNumberView;
        private TextView trackTitleView;
        private TextView trackDurationView;
        
        private int selectionColor;
        private int regularColor;
        
        private View itemView;
        
        public ViewHolder(View itemView) {
            super(itemView);         
            this.itemView = itemView;
            trackNumberView = (TextView) itemView.findViewById(R.id.trackNumber);
            trackTitleView = (TextView) itemView.findViewById(R.id.track);
            trackDurationView = (TextView) itemView.findViewById(R.id.trackDuration);
            
            selectionColor = itemView.getResources().getColor(android.R.color.white);
            regularColor = trackNumberView.getTextColors().getDefaultColor();
        }
        
        public void setSelected(boolean selected) {
            int color = selected ? selectionColor : regularColor;
            trackNumberView.setTextColor(color);
            trackTitleView.setTextColor(color);
            trackDurationView.setTextColor(color);
            itemView.setSelected(selected);
        }
    }
    
    class TrackItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int itemPosition = mRecyclerView.getChildPosition(view);
            SongInfo song = mAlbum.getSongs().get(itemPosition);
            mTrackSelectionListener.onTrackSelected(song);
        }

    }
    
    public static interface TrackSelectionListener {
        
        public void onTrackSelected(SongInfo song);
        
    }
    
}
