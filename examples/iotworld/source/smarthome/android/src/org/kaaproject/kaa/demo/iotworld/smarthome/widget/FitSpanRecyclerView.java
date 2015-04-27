package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FitSpanRecyclerView extends RecyclerView {
    
    public FitSpanRecyclerView(Context context) {
        super(context);
    }
    
    public FitSpanRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FitSpanRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int mGridMinSpans;
    private int mItemWidth;
    private float mItemAspectRatio;
    private LayoutRequester mLayoutRequester = new LayoutRequester();
    
    public void setGridLayoutManager( int orientation, int minSpans, int itemWidth, float itemAspectRatio) {
        GridLayoutManager layoutManager = new GridLayoutManager( getContext(), minSpans, GridLayoutManager.VERTICAL, false );
        mGridMinSpans = minSpans;
        mItemWidth = itemWidth;
        mItemAspectRatio = itemAspectRatio;
        setLayoutManager( layoutManager );        
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GridLayoutManager layoutManager = new GridLayoutManager( getContext(), mGridMinSpans, GridLayoutManager.VERTICAL, false );
        setLayoutManager( layoutManager );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
        super.onLayout( changed, left, top, right, bottom );
        if( changed ) {
            updateLayout(right - left, bottom - top);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateLayout(w, h);
    }

    private void updateLayout(int width, int height) {
        LayoutManager layoutManager = getLayoutManager();
        if( layoutManager instanceof GridLayoutManager ) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int recyclerViewWidth = getMeasuredWidth();
            int recyclerViewHeight = getMeasuredHeight();
            int itemWidth = mItemWidth;
            int itemHeight = (int) (mItemWidth * mItemAspectRatio);
            int count = getAdapter().getItemCount();
            if (count > 0) {
                int spanCount = recyclerViewWidth / itemWidth;
                itemWidth = recyclerViewWidth / spanCount;
                itemHeight = (int) (itemWidth*mItemAspectRatio);
                int rowCount = (int) Math.ceil((float)count / (float)spanCount);
                
                int computedHeight = itemHeight * rowCount;
                while (computedHeight > recyclerViewHeight) {
                    spanCount++;
                    rowCount = (int) Math.ceil((float)count / (float)spanCount);
                    itemWidth = recyclerViewWidth / spanCount;
                    itemHeight = (int) (itemWidth*mItemAspectRatio);
                    computedHeight = itemHeight * rowCount;                    
                }
                
                for (int i=0;i<count;i++) {
                    View v = this.getChildAt(i);                    
                    if (v != null) {
                        ViewGroup.LayoutParams lp = v.getLayoutParams();
                        if (lp == null) {
                            lp = new GridLayoutManager.LayoutParams(itemWidth, itemHeight);
                            v.setLayoutParams(lp);
                        } else {
                            lp.width = itemWidth;
                            lp.height = itemHeight;
                        }
                    }
                }
                int verticalPadding = (recyclerViewHeight - computedHeight) / 2;
                setPadding(0, verticalPadding, 0, verticalPadding);
                
                gridLayoutManager.setSpanCount( spanCount );
                post( mLayoutRequester );
            }
        }
    }

    private class LayoutRequester implements Runnable {
        @Override
        public void run() {
            requestLayout();
        }
    }
}
