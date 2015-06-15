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
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    private final RecyclerView mRecyclerView;
    private final AlbumSelectionListener mAlbumSelectionListener;
    private final AlbumItemClickListener mAlbumItemClickListener = new AlbumItemClickListener();
    private final MusicDevice mMusicDevice;
    
    public AlbumsAdapter(RecyclerView recyclerView, 
                         MusicDevice musicDevice, 
                         AlbumSelectionListener albumSelectionListener) {
        mRecyclerView = recyclerView;
        mMusicDevice = musicDevice;
        mRecyclerView.setAdapter(this);
        mAlbumSelectionListener = albumSelectionListener;
    }
    
    @Override
    public int getItemCount() {
        if (mMusicDevice.getSortedAlbums() != null) {
            return mMusicDevice.getSortedAlbums().size();
        } else {
            return 0;
        }
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlbumInfo album = mMusicDevice.getSortedAlbums().get(position);
        holder.bind(album, mMusicDevice.getPlaybackInfo());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AlbumCard card = new AlbumCard(parent.getContext());
        card.setOnClickListener(mAlbumItemClickListener);
        FontUtils.setRobotoFont(card);
        ViewHolder vhCard = new ViewHolder(card);
        return vhCard;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        private AlbumCard mCard;
        
        public ViewHolder(AlbumCard card) {
            super(card);         
            mCard = card;
        }
        
        protected void bind(AlbumInfo album, PlaybackInfo playbackInfo) {
            mCard.bind(album, playbackInfo);
        }
        
    }
    
    class AlbumItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int itemPosition = mRecyclerView.getChildPosition(view);
            AlbumInfo album = mMusicDevice.getSortedAlbums().get(itemPosition);
            mAlbumSelectionListener.onAlbumSelected(album);
        }

    }
    
    public static interface AlbumSelectionListener {
        
        public void onAlbumSelected(AlbumInfo album);
        
    }
    
}
