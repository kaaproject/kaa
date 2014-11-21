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
package org.kaaproject.kaa.demo.smarthousedemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressFragment extends Fragment {

    /**
     * Returns a new instance of this fragment.
     */
    public static ProgressFragment newInstance(boolean dark) {
        ProgressFragment fragment = new ProgressFragment(dark);
        return fragment;
    }

    private ProgressBar mProgressBar;
    private TextView mErrorTextView;

    private final boolean dark;

    public ProgressFragment(boolean dark) {
    	this.dark = dark;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_progress, container,
                false);
        
        int progressBarId = dark ? R.id.progressDark : R.id.progressLight;
        
        mProgressBar = (ProgressBar)rootView.findViewById(progressBarId);
        mProgressBar.setVisibility(View.VISIBLE);
        
        mErrorTextView = (TextView)rootView.findViewById(R.id.errorTextView);
        
        return rootView;
    }
    
    public void onError(String message) {
        mProgressBar.setVisibility(View.GONE);
        mErrorTextView.setVisibility(View.VISIBLE);
        mErrorTextView.setText(message);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}
