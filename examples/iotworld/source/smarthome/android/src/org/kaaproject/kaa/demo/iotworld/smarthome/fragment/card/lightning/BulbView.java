package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.lightning;

import org.kaaproject.kaa.demo.iotworld.light.BulbInfo;
import org.kaaproject.kaa.demo.iotworld.light.BulbStatus;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BulbView extends ImageView {

    public BulbView(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BulbView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BulbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BulbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setImageResource(R.drawable.bulb);
        setScaleType(ScaleType.FIT_CENTER);
        setAdjustViewBounds(true);
    }
    
    public void bind(BulbInfo bulb) {
        setEnabled(bulb.getStatus()==BulbStatus.ON);
    }
    

}
