package org.kaaproject.kaa.demo.iotworld.smarthome.nav;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.DeviceType;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    public static final int MODE_MAIN = 0;
    public static final int MODE_ACCOUNT = 1;
    
    public static final int HOME_POSITION = 1;
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    
    private final RecyclerView mRecyclerView;
    private final NavigationListener mNavigationListener;
    private final HeaderClickListener mHeaderClickListener = new HeaderClickListener();
    private final NavItemClickListener mNavItemClickListener = new NavItemClickListener();
    
    private String mUsername;
    private int mSelectedPosition = HOME_POSITION;
    
    private int mCurrentMode = MODE_MAIN;
    
    
    public NavAdapter(RecyclerView recyclerView, NavigationListener navigationListener) {
        mRecyclerView = recyclerView;
        mRecyclerView.setAdapter(this);
        mNavigationListener = navigationListener;
    }
    
    public void setUsername(String username) {
        this.mUsername = username;
        notifyItemChanged(0);
    }
    
    @Override
    public int getItemCount() {
        if (mCurrentMode == MODE_MAIN) {
            return DeviceType.values().length+2;
        } else {
            return 2;
        }
    }
    
    @Override
    public int getItemViewType(int position) { 
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public void onBindViewHolder(NavAdapter.ViewHolder holder, int position) {
        if (holder.holderId == 1) {
            if (mCurrentMode == MODE_MAIN) {
                if (position == HOME_POSITION) {
                    holder.imageView.setImageResource(R.drawable.ic_nav_home);
                    holder.textView.setText(R.string.nav_home);
                } else {
                    DeviceType deviceType = DeviceType.values()[position-2];
                    holder.imageView.setImageResource(deviceType.getNavIconResId());
                    holder.textView.setText(deviceType.getNavTitleResId());
                }
                
                holder.rowLayout.setSelected(position == mSelectedPosition);
                int style = 
                        position == mSelectedPosition ? Typeface.BOLD : Typeface.NORMAL;
                FontUtils.setFontStyle(holder.textView, style);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_nav_signout);
                holder.textView.setText(R.string.nav_signout);
                holder.rowLayout.setSelected(false);
                FontUtils.setFontStyle(holder.textView, Typeface.NORMAL);
            }
        } else {
            
            holder.navToggleIcon.setImageResource(
                    mCurrentMode == MODE_MAIN ? 
                            R.drawable.ic_nav_toggle_down : 
                            R.drawable.ic_nav_toggle_up);

            if (mUsername != null) {
                holder.usermameView.setText(mUsername);
            } else {
                holder.usermameView.setText("");
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_item_row,parent,false);
            v.setOnClickListener(mNavItemClickListener);
            FontUtils.setRobotoFont(v);
            ViewHolder vhItem = new ViewHolder(v,viewType);
            return vhItem;
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_header,parent,false);
            v.setOnClickListener(mHeaderClickListener);
            FontUtils.setRobotoFont(v);
            ViewHolder vhHeader = new ViewHolder(v,viewType);
            return vhHeader;
        }
        return null;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        
        int holderId;      

        TextView usermameView;
        ImageView navToggleIcon;

        View rowLayout;
        ImageView imageView;
        TextView textView; 
        
        public ViewHolder(View itemView, int viewType) { 
            super(itemView);
            if (viewType == TYPE_ITEM) {
                rowLayout = itemView.findViewById(R.id.rowLayout);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                textView = (TextView) itemView.findViewById(R.id.rowText);
                holderId = 1;  
            } else{
                usermameView = (TextView) itemView.findViewById(R.id.username);
                navToggleIcon = (ImageView) itemView.findViewById(R.id.navToggleIcon);
                holderId = 0;
            }
        }
    }
    
    private void toggleMode() {
        if (mCurrentMode == MODE_MAIN) {
            setMode(MODE_ACCOUNT);
        } else {
            setMode(MODE_MAIN);
        }
    }
    
    public void setMode(int mode) {
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            notifyDataSetChanged();
        }
    }
    
    public void setSelection(int position) {
        setSelection(position, true);
    }
    
    public void setSelection(int position, boolean fireListener) {
        if (mCurrentMode == MODE_MAIN) {
            if (position > 0 && mSelectedPosition != position) {
                notifyItemChanged(mSelectedPosition);
                mSelectedPosition = position;
                notifyItemChanged(mSelectedPosition);
                if (fireListener) {
                    if (mSelectedPosition == HOME_POSITION) {
                        mNavigationListener.onHomeSelected();
                    } else {
                        DeviceType deviceType = DeviceType.values()[mSelectedPosition-2];
                        mNavigationListener.onDeviceTypeSelected(deviceType);
                    }
                }
            }
        } else if (position == 1 && fireListener) {
            mNavigationListener.onSignOutSelected();
        }
    }
    
    class HeaderClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            toggleMode();
        }
        
    }
    
    class NavItemClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int itemPosition = mRecyclerView.getChildPosition(view);
            setSelection(itemPosition);
        }
        
    }
    
    public static interface NavigationListener {
        
        public void onDeviceTypeSelected(DeviceType deviceType);
        
        public void onHomeSelected();
        
        public void onSignOutSelected();
        
    }

}
