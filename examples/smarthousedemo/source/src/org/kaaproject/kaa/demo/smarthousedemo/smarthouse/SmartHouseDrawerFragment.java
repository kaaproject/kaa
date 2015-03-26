/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.smarthouse;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.demo.smarthousedemo.R;
import org.kaaproject.kaa.demo.smarthousedemo.SmartHouseActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The fragment is used for managing interactions for and presentation of a navigation
 * drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction"
 * > design guidelines</a> for a complete explanation of the behaviors
 * implemented here.
 */
public class SmartHouseDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";


    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    
    private SmartHouseActivity mMainActivity;

    /**
     * A helper component that ties the action bar up with the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private TextView mUsernameView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    
    private DrawerMenuAdapter fragmentsAdapter;

    public SmartHouseDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user 
        // has demonstrated awareness of the drawer. 
        // See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState
                    .getInt(STATE_SELECTED_POSITION);
        }

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Indicate that this fragment intends to affect the set of
        // actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View v = inflater.inflate(
                R.layout.fragment_smarthouse_drawer, container, false);
        mDrawerListView = (ListView) v.findViewById(R.id.menuList);
        mDrawerListView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        selectItem(position);
                    }
                });
        mUsernameView = (TextView) v.findViewById(R.id.usernameLabel);
        mUsernameView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_account_inverse, 0, 0, 0);
        
        List<DrawerMenuInfo> drawerMenuItems = new ArrayList<>();
        DrawerMenuInfo info = new DrawerMenuInfo();
        info.textResId = R.string.smart_house;
        info.iconResId = R.drawable.group_home_normal;
        drawerMenuItems.add(info);
        info = new DrawerMenuInfo();
        info.textResId = R.string.sign_out;
        info.iconResId = R.drawable.ic_signout;
        drawerMenuItems.add(info);
        
        fragmentsAdapter = new DrawerMenuAdapter(getActionBar()
                .getThemedContext(), drawerMenuItems);
        mDrawerListView.setAdapter(fragmentsAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return v;
    }
    
    class DrawerMenuInfo {
    	int textResId;
    	int iconResId;
    }
    
    class DrawerMenuAdapter extends ArrayAdapter<DrawerMenuInfo> {

        private Context context;
        private List<DrawerMenuInfo> drawerMenuItems;
        
        public DrawerMenuAdapter(Context ctx, List<DrawerMenuInfo> items) {
            super(ctx, android.R.layout.simple_list_item_1, items);
            this.context = ctx;
            this.drawerMenuItems = items;
        }
        
        @Override
        public int getCount() {
            if (drawerMenuItems != null)
                return drawerMenuItems.size();
            return 0;
        }

        @Override
        public DrawerMenuInfo getItem(int position) {
            if (drawerMenuItems != null)
                return drawerMenuItems.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (drawerMenuItems != null)
                return drawerMenuItems.get(position).hashCode();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) 
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.drawer_menu_list_item, null);
            }
            DrawerMenuInfo entry = drawerMenuItems.get(position);
            TextView drawerMenuTextView = (TextView) v.findViewById(R.id.drawerMenuText);
            
            if (position == mDrawerListView.getCheckedItemPosition()) {
            	drawerMenuTextView.setTextAppearance(context, R.style.TextAppearance_Menu_Selected);
            }
            else {
            	drawerMenuTextView.setTextAppearance(context, R.style.TextAppearance_Menu);
            }
            
            drawerMenuTextView.setText(getString(entry.textResId));
        	drawerMenuTextView.setCompoundDrawablesWithIntrinsicBounds(entry.iconResId, 0, 0,0);
            
            return v;
        }
        
        public List<DrawerMenuInfo> getItemList() {
            return drawerMenuItems;
        }
     

        
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null
                && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    
    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    /**
     * The users of this fragment must call this method to set up the navigation
     * drawer interactions.
     * 
     * @param fragmentId
     *            The android:id of this fragment in its activity layout.
     * @param drawerLayout
     *            The DrawerLayout containing the UI of this fragment.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, String username) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mUsernameView.setText(username);
        
        // Set a custom shadow that overlays the main content when the drawer
        // opens.
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
       
        // Set up the list view with items for the drawer and click listener.

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /*
                                                                  * host
                                                                  * Activity
                                                                  */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        R.string.navigation_drawer_open, /*
                                          * "open drawer" description for
                                          * accessibility
                                          */
        R.string.navigation_drawer_close /*
                                          * "close drawer" description for
                                          * accessibility
                                          */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls
                                                              // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().supportInvalidateOptionsMenu(); // calls
                                                              // onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of the previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        fragmentsAdapter.notifyDataSetChanged();
    }
    
    public void onInitSuccess() {
        selectItem(mCurrentSelectedPosition);
    }
    
    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mMainActivity != null) {
            mMainActivity.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mMainActivity = (SmartHouseActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    "Activity must be instance of SmartHouseActivity.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Forward the new configuration of the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // If the drawer is open, show the global app actions in the action bar.
//        // See also
//        // showGlobalContextActionBar, which controls the top-left area of the
//        // action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            //inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
//
//        if (item.getItemId() == R.id.action_refresh_devices) {
//            mMainActivity.getSmartHouseController().discoverDevices();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * As per the navigation drawer design guidelines, updates the action bar to
     * show the global app 'context' rather than just what is on the current
     * screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

}
