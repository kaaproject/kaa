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
package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.AbstractDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.RippleView;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class AbstractDeviceCard<T extends AbstractDevice> extends RippleView {
    
    private CardView mCardView;
    private ImageView mDeviceIconView;
    private TextView mDeviceTitleView;
    private View mDeviceNoInfoView;
    protected ViewGroup mDeviceDetailsLayout;
    
    public AbstractDeviceCard(Context context) {
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
        inflater.inflate(getCardLayout(), rl, true);
        
        mDeviceIconView = (ImageView) findViewById(R.id.deviceIcon);
        mDeviceTitleView = (TextView) findViewById(R.id.deviceTitle);
        
        mDeviceNoInfoView = findViewById(R.id.deviceNoInfoLayout);
        mDeviceDetailsLayout = (ViewGroup) findViewById(R.id.deviceDetailsLayout);
    }
    
    protected void setDetailsVisible(boolean visible) {
        mDeviceNoInfoView.setVisibility(visible ? View.GONE : View.VISIBLE);
        mDeviceDetailsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    public void bind(T device) {
        mCardView.setCardBackgroundColor(getResources().getColor(device.getDeviceType().getBaseColorResId()));
        mDeviceIconView.setImageResource(device.getDeviceType().getCardIconResId());
        
        String deviceTitle = null;
        if (device.getDeviceInfo() != null && device.getDeviceInfo().getName() != null) {
            deviceTitle = device.getDeviceInfo().getName();
        } else {
            deviceTitle = getResources().getString(R.string.unknown);
        }
        mDeviceTitleView.setText(deviceTitle);
    }
    
    protected int getContentWidth() {
        return getLayoutParams().width - mCardView.getContentPaddingLeft() - mCardView.getContentPaddingRight();
    }
    
    protected abstract int getCardLayout();
}
