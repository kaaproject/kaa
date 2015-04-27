package org.kaaproject.kaa.demo.iotworld.smarthome.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.demo.iotworld.PhotoEventClassFamily;
import org.kaaproject.kaa.demo.iotworld.photo.DeleteUploadedPhotosRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PauseSlideShowRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsRequest;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumsResponse;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoFrameStatusUpdate;
import org.kaaproject.kaa.demo.iotworld.photo.PhotoUploadRequest;
import org.kaaproject.kaa.demo.iotworld.photo.SlideShowStatus;
import org.kaaproject.kaa.demo.iotworld.photo.StartSlideShowRequest;

import de.greenrobot.event.EventBus;

public class PhotoDevice extends AbstractGeoFencingDevice implements PhotoEventClassFamily.Listener {

    private final PhotoEventClassFamily mPhotoEventClassFamily;
    
    private List<PhotoAlbumInfo> mAlbums;
    private List<PhotoAlbumInfo> mSortedAlbums;
    private PhotoFrameStatusUpdate mPhotoFrameStatus;
    
    private final AlbumsComparator mAlbumsComparator = new AlbumsComparator();
    
    public PhotoDevice(String endpointKey, DeviceStore deviceStore, KaaClient client, EventBus eventBus) {
        super(endpointKey, deviceStore, client, eventBus);
        mPhotoEventClassFamily = mClient.getEventFamilyFactory().getPhotoEventClassFamily();
    }
    
    @Override
    protected void initListeners() {
        super.initListeners();
        mPhotoEventClassFamily.addListener(this);
    }
    
    @Override
    protected void releaseListeners() {
        super.releaseListeners();
        mPhotoEventClassFamily.removeListener(this);
    }
    
    @Override
    public void requestDeviceInfo() {
        super.requestDeviceInfo();
        mPhotoEventClassFamily.sendEvent(new PhotoAlbumsRequest(), mEndpointKey);
    }
    
    public List<PhotoAlbumInfo> getAlbums() {
        return mAlbums;
    }
    
    public List<PhotoAlbumInfo> getSortedAlbums() {
        return mSortedAlbums;
    }
    
    public PhotoAlbumInfo getAlbum(String albumId) {
        if (mAlbums != null) {
            for (PhotoAlbumInfo album : mAlbums) {
                if (album.getId().equals(albumId)) {
                    return album;
                }
            }
        }
        return null;
    }
    
    public PhotoFrameStatusUpdate getPhotoFrameStatus() {
        return mPhotoFrameStatus;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.PHOTO;
    }
    
    private class AlbumsComparator implements Comparator<PhotoAlbumInfo> {

        @Override
        public int compare(PhotoAlbumInfo lhs, PhotoAlbumInfo rhs) {
            if (mPhotoFrameStatus != null && mPhotoFrameStatus.getAlbumId() != null) {
                String albumId = mPhotoFrameStatus.getAlbumId();
                if (lhs.getId().equals(albumId)) {
                    return -1;
                } else if (rhs.getId().equals(albumId)) {
                    return 1;
                }
            }
            return 0;
        }
        
    }

    @Override
    public void onEvent(PhotoAlbumsResponse photoAlbumsResponse, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mAlbums = photoAlbumsResponse.getAlbums();
            mSortedAlbums = new ArrayList<PhotoAlbumInfo>(mAlbums);
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }

    @Override
    public void onEvent(PhotoFrameStatusUpdate photoFrameStatusUpdate, String sourceEndpoint) {
        if (mEndpointKey.equals(sourceEndpoint)) {
            mPhotoFrameStatus = photoFrameStatusUpdate;
            Collections.sort(mSortedAlbums, mAlbumsComparator);
            fireDeviceUpdated();
        }
    }
    
    public void startStopSlideshow(String albumId) {
        if (mPhotoFrameStatus != null && mPhotoFrameStatus.getAlbumId() != null && 
                mPhotoFrameStatus.getAlbumId().equals(albumId) &&
                mPhotoFrameStatus.getStatus() == SlideShowStatus.PLAYING) {
            mPhotoEventClassFamily.sendEvent(new PauseSlideShowRequest(), mEndpointKey);
        } else {
            mPhotoEventClassFamily.sendEvent(new StartSlideShowRequest(albumId), mEndpointKey);
        }
    }
    
    public void pauseSlideshow() {
        mPhotoEventClassFamily.sendEvent(new PauseSlideShowRequest(), mEndpointKey);
    }
    
    public void uploadPhoto(String name, byte[] data) {
        mPhotoEventClassFamily.sendEvent(new PhotoUploadRequest(name, ByteBuffer.wrap(data)), mEndpointKey);
    }
    
    public void deleteUploadedPhotos() {
        mPhotoEventClassFamily.sendEvent(new DeleteUploadedPhotosRequest(), mEndpointKey);
    }

}
