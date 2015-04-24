package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.RippleView;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class AddDeviceCard extends RippleView {
    
    private CardView mCardView;
    
    public AddDeviceCard(Context context) {
        super(context);
        init();
    }

    private void init() {
        int cardsWidth = getResources().getDimensionPixelSize(R.dimen.card_width);
        int cardsHeight = getResources().getDimensionPixelSize(R.dimen.card_height);
        
        LayoutParams lp = new LayoutParams(cardsWidth, cardsHeight);
        setLayoutParams(lp);
        
        setRippleType(RECTANGLE);
        setCentered(false);
        setDuration(200);
        
        mCardView = new CardView(getContext());
        
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mCardView, lp);
        
        int cardContentPadding = getResources().getDimensionPixelSize(R.dimen.card_content_padding);
        mCardView.setContentPadding(cardContentPadding, cardContentPadding, cardContentPadding, cardContentPadding);
        mCardView.setUseCompatPadding(true);
        
        int cardCornerRadius = getResources().getDimensionPixelSize(R.dimen.card_corner_radius);
        mCardView.setRadius(cardCornerRadius);
        
        RelativeLayout rl = new RelativeLayout(getContext());
        lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mCardView.addView(rl, lp);
        
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_add_device, rl, true);
        
        mCardView.setCardBackgroundColor(getResources().getColor(R.color.add_device_color));
    }
    
}
