package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.lightning.BulbsListAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.lightning.BulbsListAdapter.BulbItemListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LightningDeviceFragment 
            extends AbstractGeoFencingDeviceFragment<LightningDevice> 
            implements BulbItemListener {
    
    private TextView mNoDataText;
    private RecyclerView mRecyclerView;
    private BulbsListAdapter mBulbsListAdapter;
    
    public LightningDeviceFragment() {
        super();
    }

    public LightningDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_lightning_device;
    }

    @Override
    public String getFragmentTag() {
        return LightningDeviceFragment.class.getSimpleName();
    }
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true); 
        
        
        mBulbsListAdapter = new BulbsListAdapter(mRecyclerView, mDevice, this);
        
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        mBulbsListAdapter.notifyDataSetChanged();
        if (mBulbsListAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBulbBrightnessChanged(String bulbId, int value) {
        mDevice.changeBulbBrightness(bulbId, value);
    }

    @Override
    public void onBulbStateChanged(String bulbId, boolean enabled) {
        mDevice.changeBulbState(bulbId, enabled ? BulbStatus.ON : BulbStatus.OFF);
    }

}
