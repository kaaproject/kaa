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

package org.kaaproject.kaa.demo.photoframe.fragment;

import org.kaaproject.kaa.demo.photoframe.AlbumInfo;
import org.kaaproject.kaa.demo.photoframe.PlayInfo;
import org.kaaproject.kaa.demo.photoframe.PlayStatus;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.adapter.AlbumsAdapter;
import org.kaaproject.kaa.demo.photoframe.event.AlbumListEvent;
import org.kaaproject.kaa.demo.photoframe.event.PlayInfoEvent;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;

public class AlbumsFragment extends ListFragment {
    
    private static final String ENDPOINT_KEY = "endpointKey";
    
    private String mEndpointKey;   
    
    public AlbumsFragment() {
        super();
    }
    
    public AlbumsFragment(String endpointKey) {
        super();
        mEndpointKey = endpointKey;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mEndpointKey == null) {
            mEndpointKey = savedInstanceState.getString(ENDPOINT_KEY);
        }
        onRefresh();
    }
    
    @Override
    protected void notifyDataChanged() {
        super.notifyDataChanged();
        mActivity.supportInvalidateOptionsMenu();
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        PlayInfo playInfo = mController.getRemoteDeviceStatus(mEndpointKey);
        menu.findItem(R.id.stopPlay).setVisible(playInfo != null && 
              playInfo.getStatus() == PlayStatus.PLAYING);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.stopPlay) {
            mController.stopPlayRemoteDeviceAlbum(mEndpointKey);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEndpointKey != null) {
            outState.putString(ENDPOINT_KEY, mEndpointKey);
        }
    }
    
    public void onEventMainThread(AlbumListEvent albumListEvent) {
        if (albumListEvent.getEndpointKey().equals(mEndpointKey)) {
            notifyDataChanged();
        }
    }
    
    public void onEventMainThread(PlayInfoEvent playInfoEvent) {
        if (playInfoEvent.getEndpointKey().equals(mEndpointKey)) {
            notifyDataChanged();
        }
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new AlbumsAdapter(mActivity, mController, mEndpointKey);
    }

    @Override
    protected String getNoDataText() {
        return getString(R.string.no_albums);
    }
    
    protected String getTitle() {
        return mController.getRemoteDevicesMap().get(mEndpointKey).getModel();
    }

    @Override
    protected void onRefresh() {
        mController.requestRemoteDeviceAlbums(mEndpointKey);
        mController.requestRemoteDeviceStatus(mEndpointKey);
    }

    @Override
    protected void onItemClicked(int position) {
        AlbumInfo album = mController.getRemoteDeviceAlbums(mEndpointKey).get(position);
        mController.playRemoteDeviceAlbum(mEndpointKey, album.getBucketId());
    }

    @Override
    public String getFragmentTag() {
        return AlbumsFragment.class.getSimpleName() + mEndpointKey;
    }

}
