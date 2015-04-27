package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music;

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music.TracksAdapter.TrackSelectionListener;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

public class MusicTracksDeviceFragment extends AbstractMusicDeviceFragment implements TrackSelectionListener {

    public static final String ALBUM_ID = "albumId";
    
    private String mAlbumId;
    
    private AlbumInfo mAlbum;
    
    private View mTrackListLayout;
    
    private RecyclerView mRecyclerView;
    private TracksAdapter mTracksAdapter;
    
    public MusicTracksDeviceFragment() {
        super();
    }

    public MusicTracksDeviceFragment(String endpointKey, String albumId) {
        super(endpointKey);
        mAlbumId = albumId;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mAlbumId == null) {
            mAlbumId = savedInstanceState.getString(ALBUM_ID);
        }
        mAlbum = mDevice.getAlbum(mAlbumId);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlbumId != null) {
            outState.putString(ALBUM_ID, mAlbumId);
        }
    }
    
    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_music_tracks_device;
    }
    
    @Override
    public String getFragmentTag() {
        return MusicTracksDeviceFragment.class.getSimpleName();
    }      
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        
        mNoDataText.setText(R.string.no_tracks);
        
        mTrackListLayout = rootView.findViewById(R.id.track_list_layout);
        
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        
        
        mTracksAdapter = new TracksAdapter(mRecyclerView, mDevice, mAlbum, this);
        
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        mTracksAdapter.notifyDataSetChanged();
        if (mTracksAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mTrackListLayout.setVisibility(View.VISIBLE);
        } else {
            mTrackListLayout.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
        if (firstLoad) {
            if (mDevice.getPlaybackInfo() != null && 
                mDevice.getPlaybackInfo().getSong() != null &&
                mDevice.getPlaybackInfo().getSong().getAlbumId().equals(mAlbum.getAlbumId())) {
                    int position = mAlbum.getSongs().indexOf(mDevice.getPlaybackInfo().getSong());
                    mRecyclerView.scrollToPosition(position);
            }
        }
    }
    
    @Override
    protected String getSubTitle() {
        return mAlbum.getTitle();
    }

    @Override
    public void onTrackSelected(SongInfo song) {
        mDevice.play(song.getUrl());
    }
    
}
