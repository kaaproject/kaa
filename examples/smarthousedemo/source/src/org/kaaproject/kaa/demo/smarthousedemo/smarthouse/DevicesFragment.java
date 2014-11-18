/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.SmartHouseActivity;
import org.kaaproject.kaa.demo.smarthousedemo.command.CommandCallback;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStatus;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceStore;
import org.kaaproject.kaa.demo.smarthousedemo.data.DeviceType;
import org.kaaproject.kaa.demo.smarthousedemo.data.SmartDeviceInfo;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.control.BaseControlFragment;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.dialog.RenameDeviceDialog;
import org.kaaproject.kaa.demo.smarthousedemo.smarthouse.dialog.RenameDeviceDialog.RenameDeviceDialogListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public abstract class DevicesFragment<T extends SmartDeviceInfo> extends Fragment {

    public static final String DEVICE_TYPE = "device_type";
    
    private DeviceAdapter<T> mDevicesAdapter;
    private TextView mNoDevicesTextView;
    private ListView mDevicesListView;
    protected SmartHouseActivity mActivity;
    
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static DevicesFragment<?> newInstance(final DeviceType deviceType) {
        switch (deviceType) {
        case THERMOSTAT:
            return new ThermostatsFragment();
        case TV:
            return new TvsFragment();
        case SOUND_SYSTEM:
            return new SoundSystemsFragment();
        case LAMP:
            return new LampsFragment();
            default:
                return new DevicesFragment<SmartDeviceInfo>() {

                    @Override
                    public DeviceType getDeviceType() {
                        return deviceType;
                    }
                    
                };
        }
    }

    public DevicesFragment() {
    }
    
    public abstract DeviceType getDeviceType();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (SmartHouseActivity)this.getActivity();
        mDevicesAdapter = createDeviceAdapter();
        if (!mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().register(this);
        }
    }
    
    protected DeviceAdapter<T> createDeviceAdapter() {
        return new DeviceAdapter<T>(mActivity.getDeviceStore(), mActivity, getDeviceType());
    }

    @Override
    public void onDestroy() {
        if (mActivity.getDeviceStore().getEventBus().isRegistered(this)) {
            mActivity.getDeviceStore().getEventBus().unregister(this);
        }
        mActivity = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_devices, container,
                false);
        
        mNoDevicesTextView = (TextView)rootView.findViewById(R.id.noDevicesTextView);
        
        String noDevicesText = String.format(getString(R.string.msg_no_devices),getString(getDeviceType().getTitleRes()));
        mNoDevicesTextView.setText(noDevicesText);
        
        mDevicesListView = (ListView)rootView.findViewById(R.id.devicesList);
        mDevicesListView
        .setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onDeviceClicked(position);
            }
        });
        mDevicesListView.setAdapter(mDevicesAdapter);
        mDevicesListView.setFastScrollEnabled(true);
        registerForContextMenu(mDevicesListView);
        updateDevicesVisibility();
        return rootView;
    }
    
    private void updateDevicesVisibility() {
        if (mDevicesAdapter.getCount() > 0) {
            mDevicesListView.setVisibility(View.VISIBLE);
            mNoDevicesTextView.setVisibility(View.GONE);
        }
        else {
            mDevicesListView.setVisibility(View.GONE);
            mNoDevicesTextView.setVisibility(View.VISIBLE);
        }
    }
    
    public void onEventMainThread(DeviceStore.DeviceAdded deviceAdded) {
        mDevicesAdapter.notifyDataSetChanged();
        updateDevicesVisibility();
    }
    
    public void onEventMainThread(DeviceStore.DeviceRemoved deviceRemoved) {
        mDevicesAdapter.notifyDataSetChanged();
        updateDevicesVisibility();
    }
    
    public void onEventMainThread(DeviceStore.DeviceUpdated deviceUpdated) {
        mDevicesAdapter.notifyDataSetChanged();
    }
    
    protected void onDeviceClicked(int position) {
        T device = mDevicesAdapter.getItem(position);
        if (device != null && device.getDeviceStatus()==DeviceStatus.ONLINE) {
            openDeviceControl(position);
        }
    }
    
    protected void openDeviceControl(int position) {
        this.openChildFragment(BaseControlFragment.newInstance(getDeviceType(), position));
    }
    
    protected void openChildFragment(Fragment fragment) {
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
      if (v.getId()==R.id.devicesList) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
          SmartDeviceInfo device = mDevicesAdapter.getItem(info.position);
          menu.setHeaderTitle(device.getDeviceName());
          String[] menuItems = getResources().getStringArray(R.array.deviceContextMenu);
          for (int i = 0; i<menuItems.length; i++) {
            menu.add(getDeviceType().ordinal(), i, i, menuItems[i]);
          }
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getDeviceType().ordinal() == item.getGroupId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            final SmartDeviceInfo device = mDevicesAdapter.getItem(info.position);
            int menuItemIndex = item.getItemId();
            switch (menuItemIndex) {
            case 0:
                mActivity.getSmartHouseController().deattachEndpoint(device.getEndpointKey(), new DetachEdnpointCallback(device));
            break;
            case 1:
                final String previousDeviceName = device.getDeviceName();
                RenameDeviceDialog.showRenameDeviceDialog(mActivity, 
                        previousDeviceName, 
                        new RenameDeviceDialogListener() {
                            @Override
                            public void onNewDeviceName(String newDeviceName) {
                                if (!newDeviceName.isEmpty() && !previousDeviceName.equals(newDeviceName)) {
                                    mActivity.getDeviceStore().onDeviceRenamed(device, newDeviceName);
                                }
                            }
                });
                break;
            }
            return true;
        }
        return false;
    }
    
    class DetachEdnpointCallback implements CommandCallback<Boolean> {

        private SmartDeviceInfo device;
        
        DetachEdnpointCallback(SmartDeviceInfo device) {
            this.device = device;
        }
        
        @Override
        public void onCommandFailure(Throwable t) {
        }

        @Override
        public void onCommandSuccess(Boolean result) {
            mActivity.getDeviceStore().onDeviceRemoved(device);
        }

        @Override
        public void onCommandTimeout() {
        }
        
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
