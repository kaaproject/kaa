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
package org.kaaproject.kaa.demo.iotworld.photo.library;

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
