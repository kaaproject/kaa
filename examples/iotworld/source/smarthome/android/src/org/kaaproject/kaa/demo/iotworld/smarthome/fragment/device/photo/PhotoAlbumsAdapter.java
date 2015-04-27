package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.photo;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.PhotoDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class PhotoAlbumsAdapter extends RecyclerView.Adapter<PhotoAlbumsAdapter.ViewHolder> {

    private final RecyclerView mRecyclerView;
    private final PhotoAlbumSelectionListener mPhotoAlbumSelectionListener;
    private final PhotoAlbumItemClickListener mPhotoAlbumItemClickListener = new PhotoAlbumItemClickListener();
    private final PhotoDevice mPhotoDevice;
    
    public PhotoAlbumsAdapter(RecyclerView recyclerView, 
                         PhotoDevice photoDevice, 
                         PhotoAlbumSelectionListener photoAlbumSelectionListener) {
        mRecyclerView = recyclerView;
        mPhotoDevice = photoDevice;
        mRecyclerView.setAdapter(this);
        mPhotoAlbumSelectionListener = photoAlbumSelectionListener;
    }
    
    @Override
    public int getItemCount() {
        return mPhotoDevice.getSortedAlbums().size();
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PhotoAlbumInfo album = mPhotoDevice.getSortedAlbums().get(position);
        holder.bind(album, mPhotoDevice.getPhotoFrameStatus());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PhotoAlbumCard card = new PhotoAlbumCard(parent.getContext());
        card.setOnClickListener(mPhotoAlbumItemClickListener);
        FontUtils.setRobotoFont(card);
        ViewHolder vhCard = new ViewHolder(card);
        return vhCard;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        private PhotoAlbumCard mCard;
        
        public ViewHolder(PhotoAlbumCard card) {
            super(card);         
            mCard = card;
        }
        
        protected void bind(PhotoAlbumInfo album, PhotoFrameStatusUpdate status) {
            mCard.bind(album, status);
        }
        
    }
    
    class PhotoAlbumItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int itemPosition = mRecyclerView.getChildPosition(view);
            PhotoAlbumInfo album = mPhotoDevice.getSortedAlbums().get(itemPosition);
            mPhotoAlbumSelectionListener.onPhotoAlbumSelected(album);
        }

    }
    
    public static interface PhotoAlbumSelectionListener {
        
        public void onPhotoAlbumSelected(PhotoAlbumInfo album);
        
    }
    
}
