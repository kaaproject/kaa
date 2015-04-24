package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card.SpacesItemDecoration;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class AutoSpanRecyclerView extends RecyclerView {
    
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnContextMenuListener mOnContextMenuListener;
    
    public AutoSpanRecyclerView(Context context) {
        super(context);
    }
    
    public AutoSpanRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AutoSpanRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private int mOrientation;
    private int mGridMinSpans;
    private int mItemWidth;
    private LayoutRequester mLayoutRequester = new LayoutRequester();
    private SpacesItemDecoration mItemDecoration;

    public void setGridLayoutManager( int orientation, int minSpans, int itemWidth, int spacing ) {
        GridLayoutManager layoutManager = new GridLayoutManager( getContext(), minSpans, orientation, false );
        mOrientation = orientation;
        mGridMinSpans = minSpans;
        mItemWidth = itemWidth;
        setLayoutManager( layoutManager );
        mItemDecoration = new SpacesItemDecoration(spacing);
        addItemDecoration(mItemDecoration);
    }
    
    public void setOnItemClickListener (OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener (OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }
    
    public void setOnContextMenuListener (OnContextMenuListener listener) {
        mOnContextMenuListener = listener;
    }

    public void onItemLongClick (View view, int position, long id) {
        if (mOnItemLongClickListener != null) {
            mOnItemLongClickListener.onItemLongClick(this, view, position, id);
        }
        if (mOnContextMenuListener != null) {
            view.showContextMenu();
        }
    }
    
    public void onItemClick (View view, int position, long id) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(this, view, position, id);
        }
    }
    
    public void onCreateContextMenu (View view, ContextMenu menu, int position, long id) {
        if (mOnContextMenuListener != null) {
            mOnContextMenuListener.onCreateContextMenu(this, menu, view, position, id);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        GridLayoutManager layoutManager = new GridLayoutManager( getContext(), mGridMinSpans, mOrientation, false );
        setLayoutManager( layoutManager );
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
        super.onLayout( changed, left, top, right, bottom );
        if( changed ) {
            updateLayout();
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateLayout();
    }
    
    private void updateLayout() {
        LayoutManager layoutManager = getLayoutManager();
        if( layoutManager instanceof GridLayoutManager ) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            int recyclerViewWidth = getMeasuredWidth();
            int spanCount = Math.max( mGridMinSpans, recyclerViewWidth / mItemWidth );
            gridLayoutManager.setSpanCount( spanCount );
            post( mLayoutRequester );
        }
    }

    private class LayoutRequester implements Runnable {
        @Override
        public void run() {
            requestLayout();
        }
    }
    
    public interface OnItemLongClickListener {
        
        boolean onItemLongClick(AutoSpanRecyclerView parent, View view, int position, long id);
        
    }
    
    public interface OnItemClickListener {

        void onItemClick(AutoSpanRecyclerView parent, View view, int position, long id);
        
    }
    
    public interface OnContextMenuListener {

        void onCreateContextMenu(AutoSpanRecyclerView parent, ContextMenu menu, View v, int position, long id);

    }

}
