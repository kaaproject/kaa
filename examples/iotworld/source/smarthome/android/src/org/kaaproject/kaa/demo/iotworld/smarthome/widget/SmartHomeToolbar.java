package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.ColorUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.widget.PopupWindowCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class SmartHomeToolbar extends LinearLayout {
    
    private Toolbar mToolbar;
    private TextView mToolbarTitleView;
    private TextView mToolbarSubtitleView;
    private LinearLayout mToolbarCustomContentView;
    
    private int mCustomContentMinWidth;
    private int mCustomContentHeight;
    
    private int mCustomContentViewWidth = 0;
    
    private int mBackgroundColor = Color.BLACK;

    private PopupWindow mPopup;
    private CardView mPopupContainer;
    private View mCustomContent;
    
    private LinearLayout.LayoutParams mCustomContentViewLayoutParams =
            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    private LayoutParams mCustomContentPopupViewLayoutParams = 
            new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    
    private boolean mCustomContentVisible = false;
    
    public SmartHomeToolbar(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmartHomeToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SmartHomeToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SmartHomeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setOrientation(VERTICAL);
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.toolbar, this);        
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitleView = (TextView) findViewById(R.id.toolbar_title);
        mToolbarSubtitleView = (TextView) findViewById(R.id.toolbar_subtitle);
        mToolbarCustomContentView = (LinearLayout) findViewById(R.id.toolbar_custom_content);
        
        mPopupContainer = new CardView(getContext());
        
        int cardContentPadding = getResources().getDimensionPixelSize(R.dimen.card_content_padding);
        mPopupContainer.setContentPadding(cardContentPadding, cardContentPadding, cardContentPadding, cardContentPadding);
        mPopupContainer.setUseCompatPadding(true);      
        mPopupContainer.setRadius(0);
        mPopupContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        super.setBackgroundColor(mBackgroundColor);
        
        mCustomContentMinWidth = getResources().getDimensionPixelSize(R.dimen.custom_toolbar_content_min_width);
        mCustomContentHeight = getResources().getDimensionPixelSize(R.dimen.custom_toolbar_content_height);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCustomContentViewWidth = mToolbarCustomContentView.getMeasuredWidth();
    }
    
    public Toolbar getToolbar() {
        return mToolbar;
    }
    
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            mToolbarTitleView.setVisibility(View.VISIBLE);
            mToolbarTitleView.setText(title);
        } else {
            mToolbarTitleView.setVisibility(View.GONE);
        }
    }
    
    public void setSubtitle(String subtitle) {
        if (!TextUtils.isEmpty(subtitle)) {
            mToolbarSubtitleView.setVisibility(View.VISIBLE);
            mToolbarSubtitleView.setText(subtitle);
        } else {
            mToolbarSubtitleView.setVisibility(View.GONE);
        }
    }
    
    public void setCustomToolbarContent(View content) {
        mToolbarCustomContentView.removeAllViews();
        mCustomContent = content;
    }
    
    public void setCustomToolbarContentEnabled(boolean enabled) {
        mCustomContentVisible = enabled;
        if (mCustomContent != null && mCustomContentVisible) {
            if (mCustomContentViewWidth < mCustomContentMinWidth) {
                if (mToolbarCustomContentView.getChildCount() > 0) {
                    mToolbarCustomContentView.removeAllViews();
                }
                if (mPopupContainer.getChildCount() == 0) {
                    mPopupContainer.addView(mCustomContent, mCustomContentPopupViewLayoutParams);
                    mPopupContainer.setCardBackgroundColor(mBackgroundColor);
                }
                int popupWidth = (int) (getWidth()*0.7);
                if (mPopup == null) {
                    mPopup = new PopupWindow(mPopupContainer, popupWidth, mCustomContentHeight, true);
                    mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mPopup.setOnDismissListener(new OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            mCustomContentVisible = false;
                        }
                      });
                } else {
                    mPopup.setWidth(popupWidth);
                }                
                PopupWindowCompat.showAsDropDown(mPopup, this, getWidth()-popupWidth, 0, Gravity.TOP | Gravity.LEFT);                
            } else {
                if (mPopupContainer.getChildCount() > 0) {
                    mPopupContainer.removeAllViews();
                }
                if (mToolbarCustomContentView.getChildCount() == 0) {
                    mToolbarCustomContentView.addView(mCustomContent, mCustomContentViewLayoutParams);
                }
            }
        }
        if (!mCustomContentVisible && mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
        if (mCustomContent != null) {
            mCustomContent.setVisibility(mCustomContentVisible ? View.VISIBLE : View.GONE);
        }
    }
    
    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        super.setBackgroundColor(mBackgroundColor);
    }

    public void toggleCustomToolbarContent() {
        boolean enabled = !mCustomContentVisible;
        setCustomToolbarContentEnabled(enabled);
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        setCustomToolbarContentEnabled(false);
    }

}
