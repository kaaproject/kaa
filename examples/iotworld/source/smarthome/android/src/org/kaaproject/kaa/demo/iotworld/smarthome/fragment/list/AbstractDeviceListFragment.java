package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.list;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceRemovedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.event.DeviceUpdatedEvent;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.AbstractSmartHomeFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public abstract class AbstractDeviceListFragment<D extends AbstractDevice> extends AbstractSmartHomeFragment {

    private TextView mNoDataText;
    private ListView mList;
    
    public AbstractDeviceListFragment() {
        super();
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_device_list, container,
                false);
        setupView(rootView);
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mNoDataText.setText(R.string.no_devices);
        mList = (ListView) rootView.findViewById(R.id.list);
        
        showContent();
        
        notifyDataChanged();
        
        return rootView;
    }
    
    public void onEventMainThread(DeviceUpdatedEvent deviceUpdatedEvent) {
        if (deviceUpdatedEvent.getDeviceType() == getDeviceType()) {
            notifyDataChanged();
        }
    }
    
    public void onEventMainThread(DeviceRemovedEvent deviceRemovedEvent) {
        if (deviceRemovedEvent.getDeviceType() == getDeviceType()) {
            notifyDataChanged();
        }
    }
    
    protected void notifyDataChanged() {
//      mListAdapter.notifyDataSetChanged();
//    if (mListAdapter.getCount() > 0) {
      if (mDeviceStore.getDevices(getDeviceType()).size() > 0) {
          mNoDataText.setVisibility(View.GONE);
          mList.setVisibility(View.VISIBLE);
      } else {
          mList.setVisibility(View.GONE);
          mNoDataText.setVisibility(View.VISIBLE);
      }
  }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            discoverDevices();
            notifyDataChanged();
            return true;
        } 
        return super.onOptionsItemSelected(item);
    }
    
    private void discoverDevices() {
        mDeviceStore.discoverDevices(true, getDeviceType());
    }
    
    @Override
    protected int getBarsBackgroundColor() {
        return getResources().getColor(getDeviceType().getBaseColorResId());
    }
    
    protected abstract DeviceType getDeviceType();
    
    @Override
    protected boolean showNavigationDrawer() {
        return true;
    }
    
    @Override
    protected boolean displayHomeAsUp() {
        return false;
    }

}
