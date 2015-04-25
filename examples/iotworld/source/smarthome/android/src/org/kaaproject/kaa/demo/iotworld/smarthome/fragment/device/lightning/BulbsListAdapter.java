package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.lightning;

import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class BulbsListAdapter extends RecyclerView.Adapter<BulbsListAdapter.ViewHolder> {

    private final RecyclerView mRecyclerView;
    private final BulbItemListener mBulbItemListener;
    private final BulbItemBrightnessListener mBulbItemBrightnessListener = new BulbItemBrightnessListener();
    private final BulbItemStateListener mBulbItemStateListener = new BulbItemStateListener();
    private final LightningDevice mDevice;
    
    public BulbsListAdapter(RecyclerView recyclerView, 
                            LightningDevice device,
                            BulbItemListener bulbItemListener) {
        mRecyclerView = recyclerView;
        mDevice = device;
        mRecyclerView.setAdapter(this);
        mBulbItemListener = bulbItemListener;
    }
    
    @Override
    public int getItemCount() {
        return mDevice.getBulbs().size();
    }
 
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BulbInfo bulb = mDevice.getBulbs().get(position);
        holder.bind(position, bulb);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bulb_list_item,parent,false);
        FontUtils.setRobotoFont(v);
        ViewHolder vhItem = new ViewHolder(v);
        vhItem.brightnessControlView.setOnSeekBarChangeListener(mBulbItemBrightnessListener);
        vhItem.bulbSwitchView.setOnClickListener(mBulbItemStateListener);
        return vhItem;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView bulbView;        
        private TextView bulbTitleView;
        private ImageView brightnessView;
        private SeekBar brightnessControlView;
        private SwitchCompat bulbSwitchView;
        
        public ViewHolder(View itemView) {
            super(itemView);         
            bulbView = (ImageView) itemView.findViewById(R.id.bulbView);
            bulbTitleView = (TextView) itemView.findViewById(R.id.bulbTitleView);
            brightnessView = (ImageView) itemView.findViewById(R.id.brightnessView);
            brightnessControlView = (SeekBar) itemView.findViewById(R.id.brightnessControlView);
            bulbSwitchView = (SwitchCompat) itemView.findViewById(R.id.bulbSwitchView);
        }
        
        public void bind(int position, BulbInfo bulb) {
            boolean enabled = bulb.getStatus()==BulbStatus.ON;
            bulbView.setEnabled(enabled);
            bulbTitleView.setText((position+1)+". " + bulb.getBulbId());
            bulbTitleView.setEnabled(enabled);
            brightnessView.setEnabled(enabled);
            brightnessControlView.setTag(bulb.getBulbId());
            brightnessControlView.setMax(bulb.getMaxBrightness());
            if (!bulb.getIgnoreBrightnessUpdate()) {
                brightnessControlView.setProgress(bulb.getBrightness());
            }
            brightnessControlView.setEnabled(!enabled);
            brightnessControlView.setEnabled(enabled);
            bulbSwitchView.setTag(bulb.getBulbId());
            bulbSwitchView.setChecked(enabled);
        }
    }
    
    class BulbItemBrightnessListener implements OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                String bulbId = (String) seekBar.getTag();
                mBulbItemListener.onBulbBrightnessChanged(bulbId, progress);
            }
            
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
        
    }
    
    class BulbItemStateListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            SwitchCompat bulbSwitchView = (SwitchCompat)v;
            String bulbId = (String) bulbSwitchView.getTag();
            boolean enabled = bulbSwitchView.isChecked();
            mBulbItemListener.onBulbStateChanged(bulbId, enabled);
        }
        
    }
    
    public static interface BulbItemListener {
        
        public void onBulbBrightnessChanged(String bulbId, int value);
        
        public void onBulbStateChanged(String bulbId, boolean enabled);
        
    }
    
}
