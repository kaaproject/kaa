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

package org.kaaproject.kaa.demo.photoframe.fragment;

import org.kaaproject.kaa.demo.photoframe.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Implementation of the {@link PhotoFrameFragment} class. Used as a superclass for application fragments 
 * representing list views. Provides functions for switching between the empty data view and the list view.
 * Implements the 'refresh' and 'logOut' menu commands available in all list fragments.    
 */
public abstract class ListFragment extends PhotoFrameFragment {
    
    protected TextView mNoDataText;
    protected ListView mList;
    protected BaseAdapter mListAdapter;
    
    public ListFragment() {
        super();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_list, container,
                false);
        setupView(rootView);
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        mNoDataText.setText(getNoDataText());
        mList = (ListView) rootView.findViewById(R.id.list);
        mListAdapter = createAdapter();
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onItemClicked(position);
            }
        });
        
        showContent();
        
        notifyDataChanged();
        
        return rootView;
    }
    
    protected void notifyDataChanged() {
        mListAdapter.notifyDataSetChanged();
        if (mListAdapter.getCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        } else {
            mList.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public String getFragmentTag() {
        return null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.refresh).setVisible(true);
        menu.findItem(R.id.logOut).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            onRefresh();
            notifyDataChanged();
            return true;
        } else if (id == R.id.logOut) {
            mController.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected abstract BaseAdapter createAdapter();
    
    protected abstract String getNoDataText();
    
    protected abstract void onRefresh();
    
    protected abstract void onItemClicked(int position);
    
    @Override
    protected boolean displayHomeAsUp() {
        return true;
    }

}
