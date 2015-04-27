package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.music.PlaybackInfo;
import org.kaaproject.kaa.demo.iotworld.music.SongInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.MusicDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicCard extends AbstractGeoFencingDeviceCard<MusicDevice> {

    private View mNoTrackSelectedView;
    private View mMusicDetailsView;
    
    private ImageView mAlbumCoverView;
    private TextView mAlbumTitleView;
    private TextView mArtistTitleView;
    private TextView mTrackTitleView;
    private Bitmap mAlbumCoverBitmap;
    private SeekBar mTrackProgressView;
    private TextView mTrackProgressTextView;
    
    public MusicCard(Context context) {
        super(context);
        mNoTrackSelectedView = findViewById(R.id.noTrackSelectedView);
        mMusicDetailsView = findViewById(R.id.musicDetailsView);
        
        mAlbumCoverView = (ImageView) findViewById(R.id.albumCoverView);
        mAlbumTitleView = (TextView) findViewById(R.id.albumTitleView);
        mArtistTitleView = (TextView) findViewById(R.id.artistTitleView);
        mTrackTitleView = (TextView) findViewById(R.id.trackTitleView);
        mTrackProgressView = (SeekBar) findViewById(R.id.trackProgressView); 
        mTrackProgressTextView = (TextView) findViewById(R.id.trackProgressTextView); 
        
        mTrackProgressView.setEnabled(false);

    }

    @Override
    protected int getCardLayout() {
        return R.layout.card_music_device;
    }
    
    @Override
    public void bind(MusicDevice device) {
        super.bind(device);
        PlaybackInfo playbackInfo = device.getPlaybackInfo();
        if (playbackInfo != null) {
            setDetailsVisible(true);
            SongInfo song = playbackInfo.getSong();
            AlbumInfo album = null;
            if (song != null) {
                album = device.getAlbum(song.getAlbumId());
            }
            if (song != null && album != null) {
                mNoTrackSelectedView.setVisibility(View.GONE);
                mMusicDetailsView.setVisibility(View.VISIBLE);
                
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
                mTrackTitleView.setText(song.getTitle());
                
                mTrackProgressView.setMax(song.getDuration());
                mTrackProgressView.setProgress(playbackInfo.getTime());
                
                String timeText = TimeUtils.milliSecondsToTimer(playbackInfo.getTime()) + "/" +
                        TimeUtils.milliSecondsToTimer(song.getDuration());
                
                mTrackProgressTextView.setText(timeText);

            } else {
                mNoTrackSelectedView.setVisibility(View.VISIBLE);
                mMusicDetailsView.setVisibility(View.GONE);
            }
        } else {
            setDetailsVisible(false);
        }
    }

}
