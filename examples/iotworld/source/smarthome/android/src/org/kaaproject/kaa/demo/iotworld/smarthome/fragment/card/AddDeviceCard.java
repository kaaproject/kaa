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
