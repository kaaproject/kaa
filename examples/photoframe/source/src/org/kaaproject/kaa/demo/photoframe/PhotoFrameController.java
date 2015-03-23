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

package org.kaaproject.kaa.demo.photoframe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;
import org.kaaproject.kaa.demo.photoframe.PhotoFrameEventClassFamily;
import org.kaaproject.kaa.demo.photoframe.event.AlbumListEvent;
import org.kaaproject.kaa.demo.photoframe.event.DeviceInfoEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayAlbumEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayInfoEvent;
import org.kaaproject.kaa.demo.photoframe.event.StopPlayEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserAttachEvent;
import org.kaaproject.kaa.demo.photoframe.event.UserDetachEvent;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import de.greenrobot.event.EventBus;

/**
 * The Class PhotoFrameController.
 * Receives Kaa events and user attach/detach callbacks and dispatch them to other 
 * application components via event bus. It is also responsible for fetching albums 
 * from android {@link MediaStore}.
 */
public class PhotoFrameController implements PhotoFrameEventClassFamily.Listener, UserAttachCallback, OnDetachEndpointOperationCallback {

    private final Context mContext;
    private final EventBus mEventBus;
    private final PhotoFrameEventClassFamily mPhotoFrameEventClassFamily;
    private final KaaClient mClient;
    
    // Local device information
    private final DeviceInfo mDeviceInfo = new DeviceInfo();
    private final PlayInfo mPlayInfo = new PlayInfo();
    private final Map<String, AlbumInfo> mAlbumsMap = new HashMap<>();

    // Remote devices information 
    private final LinkedHashMap<String, DeviceInfo> mRemoteDevicesMap = new LinkedHashMap<>();    
    private final Map<String, PlayInfo> mRemotePlayInfoMap = new HashMap<>();
    private final Map<String, List<AlbumInfo>> mRemoteAlbumsMap = new HashMap<>();

    private boolean mUserAttached = false;

    public PhotoFrameController(Context context, EventBus eventBus, KaaClient client) {
        mContext = context;
        mEventBus = eventBus;
        mClient = client;
        
        /*
         * Obtain reference to Photo frame event class family class 
         * which responsive for sending/receiving declared family events
         */
        mPhotoFrameEventClassFamily = client.getEventFamilyFactory().
                                                getPhotoFrameEventClassFamily();
        
        /*
         * Register listener to receive photo frame family events
         */        
        mPhotoFrameEventClassFamily.addListener(this);
        
        /*
         * Check if endpoint already attached to verified user. 
         */        
        mUserAttached = mClient.isAttachedToUser();
        
        /*
         * Initialize all device information needed to provide response events
         * on corresponding requests
         */        
        initDeviceInfo();
    }
    
    private void initDeviceInfo() {
        mDeviceInfo.setManufacturer(android.os.Build.MANUFACTURER);
        mDeviceInfo.setModel(android.os.Build.MODEL);
        mPlayInfo.setStatus(PlayStatus.STOPPED);
        fetchAlbums();
    }
    
    private void fetchAlbums() {
        mAlbumsMap.clear();
        
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
        
        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        
        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                int titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                while (cursor.moveToNext()) {
                    String id = cursor.getString(idIndex);
                    if (!mAlbumsMap.containsKey(id)) {
                        AlbumInfo album = new AlbumInfo();
                        album.setBucketId(id);
                        album.setTitle(cursor.getString(titleIndex));
                        album.setImageCount(1);
                        mAlbumsMap.put(id, album);
                    } else {
                        AlbumInfo album = mAlbumsMap.get(id);
                        int imageCount = album.getImageCount();
                        imageCount++;
                        album.setImageCount(imageCount);
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }
    
    public boolean isUserAttached() {
        return mUserAttached;
    }
    
    public LinkedHashMap<String, DeviceInfo> getRemoteDevicesMap() {
        return mRemoteDevicesMap;
    }
    
    public List<AlbumInfo> getRemoteDeviceAlbums(String endpointKey) {
        return mRemoteAlbumsMap.get(endpointKey);
    }
     
    public PlayInfo getRemoteDeviceStatus(String endpointKey) {
        return mRemotePlayInfoMap.get(endpointKey);
    }
    
    /*
     * Attach endpoint to provided user using default configured user verifier
     */
    public void login(String userExternalId, String userAccessToken) {
        mClient.attachUser(userExternalId, userAccessToken, this);
    }
    
    /*
     * Detach endpoint from user
     */
    public void logout() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(mClient.getEndpointKeyHash());
        mClient.detachEndpoint(endpointKey, this);
    }
    
    /*
     * Update current device status reflected in PlayInfoResponse event, 
     * send event to all user endpoints
     */
    public void updateStatus(PlayStatus status, String bucketId) {
        AlbumInfo currentAlbumInfo = null;
        if (bucketId != null) {
            currentAlbumInfo = mAlbumsMap.get(bucketId);
        }
        mPlayInfo.setCurrentAlbumInfo(currentAlbumInfo);
        mPlayInfo.setStatus(status);
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(mPlayInfo);
        mPhotoFrameEventClassFamily.sendEventToAll(playInfoResponse);
    }
    
    /*
     * Notify all user endpoints about device availability and play status
     * by sending them DeviceInfoResponse and PlayInfoResponse events.
     */
    public void notifyRemoteDevices() {
        DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse();
        deviceInfoResponse.setDeviceInfo(mDeviceInfo);
        mPhotoFrameEventClassFamily.sendEventToAll(deviceInfoResponse);
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(mPlayInfo);
        mPhotoFrameEventClassFamily.sendEventToAll(playInfoResponse);
    }
    
    /*
     * Discover all available remote user devices (endpoints) 
     * by sending them DeviceInfoRequest and PlayInfoRequest events.
     * Each operational device (endpoint) will send reply with DeviceInfoResponse
     * and PlayInfoResponse events to current endpoint.
     */
    public void discoverRemoteDevices() {
        mRemoteDevicesMap.clear();
        mPhotoFrameEventClassFamily.sendEventToAll(new DeviceInfoRequest());
        mPhotoFrameEventClassFamily.sendEventToAll(new PlayInfoRequest());
    }
    
    /*
     * Get information about remote device image albums by
     * sending AlbumListRequest event to target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceAlbums(String endpointKey) {
        AlbumListRequest albumListRequest = new AlbumListRequest();
        mPhotoFrameEventClassFamily.sendEvent(albumListRequest, endpointKey);
    }
    
    /*
     * Get information about remote device play status by
     * sending PlayInfoRequest event to target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceStatus(String endpointKey) {
        mPhotoFrameEventClassFamily.sendEvent(new PlayInfoRequest(), endpointKey);
    }
    
    /*
     * Send command to remote device to play image album with specified bucketId by
     * sending PlayAlbumRequest event to target endpoint using its endpointKey.
     */
    public void playRemoteDeviceAlbum(String endpointKey, String bucketId) {
        PlayAlbumRequest playAlbumRequest = new PlayAlbumRequest();
        playAlbumRequest.setBucketId(bucketId);
        mPhotoFrameEventClassFamily.sendEvent(playAlbumRequest, endpointKey);
    }
    
    /*
     * Send command to remote device to stop image album playback by
     * sending PlayAlbumRequest event to target endpoint using its endpointKey.
     */    
    public void stopPlayRemoteDeviceAlbum(String endpointKey) {
        StopRequest stopRequest = new StopRequest();
        mPhotoFrameEventClassFamily.sendEvent(stopRequest, endpointKey);
    }
    
    /*
     * Receive result of endpoint attach operation. 
     * Notify remote devices about availability in case of success.
     */
    @Override
    public void onAttachResult(UserAttachResponse response) {
        SyncResponseResultType result = response.getResult();
        if (result == SyncResponseResultType.SUCCESS) {
            mUserAttached = true;
            notifyRemoteDevices();
            mEventBus.post(new UserAttachEvent());
        } else {
            mUserAttached = false;
            String error = response.getErrorReason();
            mEventBus.post(new UserAttachEvent(error));
        }
    }
    
    /*
     * Receive result of endpoint detach operation. 
     */    
    @Override
    public void onDetach(SyncResponseResultType result) {
        mUserAttached = false;
        if (result == SyncResponseResultType.SUCCESS) {
            mEventBus.post(new UserDetachEvent());
        } else {
            mEventBus.post(new UserDetachEvent("Failed to detach endpoint from user!"));
        }
    }
    
    /*
     * Handle DeviceInfoRequest event from remote endpoint identified by endpoint key (sourceEndpoint parameter). 
     * Reply with current device info by sending DeviceInfoResponse event.
     */    
    @Override
    public void onEvent(DeviceInfoRequest deviceInfoRequest, String sourceEndpoint) {
       DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse();
       deviceInfoResponse.setDeviceInfo(mDeviceInfo);
       mPhotoFrameEventClassFamily.sendEvent(deviceInfoResponse, sourceEndpoint);
    }

    /*
     * Handle DeviceInfoResponse event from remote endpoint. 
     * Store remote device info in local devices map.
     */  
    @Override
    public void onEvent(DeviceInfoResponse deviceInfoResponse, String sourceEndpoint) {
        mRemoteDevicesMap.put(sourceEndpoint, deviceInfoResponse.getDeviceInfo());
        if (!mRemoteAlbumsMap.containsKey(sourceEndpoint)) {
            mRemoteAlbumsMap.put(sourceEndpoint, new ArrayList<AlbumInfo>());
        }
        mEventBus.post(new DeviceInfoEvent(sourceEndpoint));
    }

    /*
     * Handle AlbumListRequest event from remote endpoint. 
     * Reply with list of image albums located on device by sending AlbumListResponse event.
     */ 
    @Override
    public void onEvent(AlbumListRequest albumListRequest, String sourceEndpoint) {
        List<AlbumInfo> albums = new ArrayList<>(mAlbumsMap.values());
        AlbumListResponse albumListResponse = new AlbumListResponse();
        albumListResponse.setAlbumList(albums);
        mPhotoFrameEventClassFamily.sendEvent(albumListResponse, sourceEndpoint);
    }

    /*
     * Handle AlbumListResponse event from remote endpoint. 
     * Store remote device albums list in local album lists map.
     */
    @Override
    public void onEvent(AlbumListResponse albumListResponse, String sourceEndpoint) {
        mRemoteAlbumsMap.put(sourceEndpoint, albumListResponse.getAlbumList());
        mEventBus.post(new AlbumListEvent(sourceEndpoint));
    }

    /*
     * Handle PlayAlbumRequest event from remote endpoint. 
     * Notify application to start playback of image album identified by bucketId.
     */
    @Override
    public void onEvent(PlayAlbumRequest playAlbumRequest, String sourceEndpoint) {
        mEventBus.post(new PlayAlbumEvent(playAlbumRequest.getBucketId()));
    }

    /*
     * Handle StopRequest event from remote endpoint. 
     * Notify application to stop current image album playback.
     */
    @Override
    public void onEvent(StopRequest stopRequest, String sourceEndpoint) {
        mEventBus.post(new StopPlayEvent());
    }

    /*
     * Handle PlayInfoRequest event from remote endpoint. 
     * Reply with current device play status by sending PlayInfoResponse event.
     */
    @Override
    public void onEvent(PlayInfoRequest playInfoRequest, String sourceEndpoint) {
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(mPlayInfo);
        mPhotoFrameEventClassFamily.sendEvent(playInfoResponse, sourceEndpoint);
    }

    /*
     * Handle PlayInfoResponse event from remote endpoint. 
     * Store remote device play status info in local play info map.
     */
    @Override
    public void onEvent(PlayInfoResponse playInfoResponse, String sourceEndpoint) {
        mRemotePlayInfoMap.put(sourceEndpoint, playInfoResponse.getPlayInfo());
        mEventBus.post(new PlayInfoEvent(sourceEndpoint));
    }


}
