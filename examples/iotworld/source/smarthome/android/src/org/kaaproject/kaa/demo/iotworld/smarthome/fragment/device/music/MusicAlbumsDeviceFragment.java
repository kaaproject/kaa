package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music;

import org.kaaproject.kaa.demo.iotworld.music.AlbumInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.music.AlbumsAdapter.AlbumSelectionListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView;

import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;

public class MusicAlbumsDeviceFragment extends AbstractMusicDeviceFragment implements AlbumSelectionListener {

    private AutoSpanRecyclerView mRecyclerView;
    private AlbumsAdapter mAlbumsAdapter;

    
    public MusicAlbumsDeviceFragment() {
        super();
    }

    public MusicAlbumsDeviceFragment(String endpointKey) {
        super(endpointKey);
    }
    
    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_music_albums_device;
    }
    
    @Override
    public String getFragmentTag() {
        return MusicAlbumsDeviceFragment.class.getSimpleName();
    }      
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        
        mNoDataText.setText(R.string.no_albums);

        mRecyclerView = (AutoSpanRecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        
        int cardsWidth = getResources().getDimensionPixelSize(R.dimen.card_width);
        int cardsSpacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        mRecyclerView.setGridLayoutManager(GridLayoutManager.VERTICAL, 1, cardsWidth, cardsSpacing);
        mAlbumsAdapter = new AlbumsAdapter(mRecyclerView, mDevice, this);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        
        mAlbumsAdapter.notifyDataSetChanged();
        if (mAlbumsAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected String getSubTitle() {
        return getResources().getString(R.string.albums);
    }
    
    @Override
    public void onAlbumSelected(AlbumInfo album) {
        Fragment fragment = new MusicTracksDeviceFragment(mDevice.getEndpointKey(), album.getAlbumId());
        mActivity.addBackStackFragment(fragment, fragment.getTag());        
    }


    
}
