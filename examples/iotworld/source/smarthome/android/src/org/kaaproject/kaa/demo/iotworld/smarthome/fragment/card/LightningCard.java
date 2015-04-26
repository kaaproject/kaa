package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import java.util.List;

import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.LightningDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.lightning.BulbsAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.FitSpanRecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;

public class LightningCard extends AbstractGeoFencingDeviceCard<LightningDevice> {

    private FitSpanRecyclerView mRecyclerView;
    private BulbsAdapter mBulbsAdapter;
    
    public LightningCard(Context context) {
        super(context);
        mRecyclerView = (FitSpanRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        Drawable drawable = getResources().getDrawable(R.drawable.bulb);        
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        float aspectRatio = (float)height/(float)width;
        mRecyclerView.setGridLayoutManager(GridLayoutManager.VERTICAL, 1, width, aspectRatio);
        mBulbsAdapter = new BulbsAdapter(mRecyclerView, null);
    }

    @Override
    protected int getCardLayout() {
        return R.layout.card_lightning_device;
    }
    
    @Override
    public void bind(LightningDevice device) {
        super.bind(device);
        List<BulbInfo> bulbs = device.getBulbs();
        if (bulbs != null) {
            setDetailsVisible(true);
            mBulbsAdapter.setDevice(device);
        } else {
            setDetailsVisible(false);
        }
    }

}
