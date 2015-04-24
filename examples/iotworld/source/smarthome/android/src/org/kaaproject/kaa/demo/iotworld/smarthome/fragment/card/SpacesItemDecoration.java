package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.card;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    
    private int mSpacing;

    public SpacesItemDecoration(int spacing) {
        mSpacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, 
        RecyclerView parent, RecyclerView.State state) {
          outRect.left = mSpacing;
          outRect.right = mSpacing;
          outRect.bottom = mSpacing;
          outRect.top = mSpacing;
    }
  }
