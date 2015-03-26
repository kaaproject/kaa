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
 * Receives Kaa events and user attach/detach callbacks, then dispatches them to other 
 * application components via event bus. It is also responsible for fetching albums 
 * from Android {@link MediaStore}.
 */
public class PhotoFrameController implements PhotoFrameEventClassFamily.Listener, UserAttachCallback, OnDetachEndpointOperationCallback {

    private final Context mContext;
    private final EventBus mEventBus;
    private final PhotoFrameEventClassFamily mPhotoFrameEventClassFamily;
    private final KaaClient mClient;
    
    // A local device information.
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
         * Obtain a reference to the Photo frame event class family class 
         * which is responsible for sending/receiving the declared family events.
         */
        mPhotoFrameEventClassFamily = client.getEventFamilyFactory().
                                                getPhotoFrameEventClassFamily();
        
        /*
         * Register a listener to receive the photo frame family events.
         */        
        mPhotoFrameEventClassFamily.addListener(this);
        
        /*
         * Check if the endpoint is already attached to the verified user. 
         */        
        mUserAttached = mClient.isAttachedToUser();
        
        /*
         * Initialize all device information needed to provide response events
         * on corresponding requests.
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
     * Attach the endpoint to the provided user using the default user verifier.
     */
    public void login(String userExternalId, String userAccessToken) {
        mClient.attachUser(userExternalId, userAccessToken, this);
    }
    
    /*
     * Detach the endpoint from the user.
     */
    public void logout() {
        EndpointKeyHash endpointKey = new EndpointKeyHash(mClient.getEndpointKeyHash());
        mClient.detachEndpoint(endpointKey, this);
    }
    
    /*
     * Update the current device status reflected in the PlayInfoResponse event, 
     * send the event to all the user endpoints.
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
     * Notify all the user endpoints about the device availability and play status
     * by sending them the DeviceInfoResponse and PlayInfoResponse events.
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
     * Discover all the available remote devices (endpoints) of the user
     * by sending them the DeviceInfoRequest and PlayInfoRequest events.
     * Each operational device (endpoint) will send a reply with the DeviceInfoResponse
     * and PlayInfoResponse events to the current endpoint.
     */
    public void discoverRemoteDevices() {
        mRemoteDevicesMap.clear();
        mPhotoFrameEventClassFamily.sendEventToAll(new DeviceInfoRequest());
        mPhotoFrameEventClassFamily.sendEventToAll(new PlayInfoRequest());
    }
    
    /*
     * Get the information about a remote device image albums by
     * sending the AlbumListRequest event to the target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceAlbums(String endpointKey) {
        AlbumListRequest albumListRequest = new AlbumListRequest();
        mPhotoFrameEventClassFamily.sendEvent(albumListRequest, endpointKey);
    }
    
    /*
     * Get the information about a remote device play status by
     * sending the PlayInfoRequest event to the target endpoint using its endpointKey.
     */
    public void requestRemoteDeviceStatus(String endpointKey) {
        mPhotoFrameEventClassFamily.sendEvent(new PlayInfoRequest(), endpointKey);
    }
    
    /*
     * Send a command to a remote device to play the image album with the specified bucketId by
     * sending the PlayAlbumRequest event to the target endpoint using its endpointKey.
     */
    public void playRemoteDeviceAlbum(String endpointKey, String bucketId) {
        PlayAlbumRequest playAlbumRequest = new PlayAlbumRequest();
        playAlbumRequest.setBucketId(bucketId);
        mPhotoFrameEventClassFamily.sendEvent(playAlbumRequest, endpointKey);
    }
    
    /*
     * Send a command to a remote device to stop the image album playback by
     * sending the PlayAlbumRequest event to target endpoint using its endpointKey.
     */    
    public void stopPlayRemoteDeviceAlbum(String endpointKey) {
        StopRequest stopRequest = new StopRequest();
        mPhotoFrameEventClassFamily.sendEvent(stopRequest, endpointKey);
    }
    
    /*
     * Receive the result of the endpoint attach operation. 
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
     * Receive the result of the endpoint detach operation. 
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
     * Handle the DeviceInfoRequest event from the remote endpoint 
     * identified by the endpoint key (sourceEndpoint parameter). 
     * Reply with the current device info by sending the DeviceInfoResponse event.
     */    
    @Override
    public void onEvent(DeviceInfoRequest deviceInfoRequest, String sourceEndpoint) {
       DeviceInfoResponse deviceInfoResponse = new DeviceInfoResponse();
       deviceInfoResponse.setDeviceInfo(mDeviceInfo);
       mPhotoFrameEventClassFamily.sendEvent(deviceInfoResponse, sourceEndpoint);
    }

    /*
     * Handle the DeviceInfoResponse event from the remote endpoint. 
     * Store the remote device info in the local devices map.
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
     * Handle the AlbumListRequest event from a remote endpoint. 
     * Reply with a list of the image albums located on the device by sending the AlbumListResponse event.
     */ 
    @Override
    public void onEvent(AlbumListRequest albumListRequest, String sourceEndpoint) {
        List<AlbumInfo> albums = new ArrayList<>(mAlbumsMap.values());
        AlbumListResponse albumListResponse = new AlbumListResponse();
        albumListResponse.setAlbumList(albums);
        mPhotoFrameEventClassFamily.sendEvent(albumListResponse, sourceEndpoint);
    }

    /*
     * Handle the AlbumListResponse event from a remote endpoint. 
     * Store a remote device albums list in the local album lists map.
     */
    @Override
    public void onEvent(AlbumListResponse albumListResponse, String sourceEndpoint) {
        mRemoteAlbumsMap.put(sourceEndpoint, albumListResponse.getAlbumList());
        mEventBus.post(new AlbumListEvent(sourceEndpoint));
    }

    /*
     * Handle the PlayAlbumRequest event from a remote endpoint. 
     * Notify the application to start playback of the image album identified by bucketId.
     */
    @Override
    public void onEvent(PlayAlbumRequest playAlbumRequest, String sourceEndpoint) {
        mEventBus.post(new PlayAlbumEvent(playAlbumRequest.getBucketId()));
    }

    /*
     * Handle the StopRequest event from a remote endpoint. 
     * Notify the application to stop the current image album playback.
     */
    @Override
    public void onEvent(StopRequest stopRequest, String sourceEndpoint) {
        mEventBus.post(new StopPlayEvent());
    }

    /*
     * Handle the PlayInfoRequest event from a remote endpoint. 
     * Reply with the current device play status by sending the PlayInfoResponse event.
     */
    @Override
    public void onEvent(PlayInfoRequest playInfoRequest, String sourceEndpoint) {
        PlayInfoResponse playInfoResponse = new PlayInfoResponse();
        playInfoResponse.setPlayInfo(mPlayInfo);
        mPhotoFrameEventClassFamily.sendEvent(playInfoResponse, sourceEndpoint);
    }

    /*
     * Handle the PlayInfoResponse event from a remote endpoint. 
     * Store a remote device play status info in the local play info map.
     */
    @Override
    public void onEvent(PlayInfoResponse playInfoResponse, String sourceEndpoint) {
        mRemotePlayInfoMap.put(sourceEndpoint, playInfoResponse.getPlayInfo());
        mEventBus.post(new PlayInfoEvent(sourceEndpoint));
    }


}
