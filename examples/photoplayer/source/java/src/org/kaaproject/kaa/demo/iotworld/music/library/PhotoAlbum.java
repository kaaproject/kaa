package org.kaaproject.kaa.demo.iotworld.music.library;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;

public class PhotoAlbum {

    private final PhotoAlbumInfo info;
    private final List<String> photos;
    
    public PhotoAlbum(String albumId){
        this.info = new PhotoAlbumInfo();
        this.info.setId(albumId);
        this.photos = new CopyOnWriteArrayList<String>();
    }
    
    public String getAlbumId() {
        return info.getId();
    }

    public PhotoAlbumInfo getInfo() {
        return info;
    }
    
    public void addPhoto(String photo){
        photos.add(photo);
        info.setSize(photos.size());
    }

    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }
}
