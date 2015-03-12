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

package org.kaaproject.kaa.demo.notification.fragment;

import org.kaaproject.kaa.demo.notification.NotificationDemoActivity;
import org.kaaproject.kaa.demo.notification.TopicInfoHolder;
import org.kaaproject.kaa.demo.notification.adapter.TopicArrayAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TopicFragment extends ListFragment {

	public TopicFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setListAdapter(new TopicArrayAdapter(inflater, TopicInfoHolder.holder.getTopicModelList()));
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt("position", position);
		NotificationDemoActivity demoActivity = (NotificationDemoActivity) getActivity();
		demoActivity.saveFragmentData(dataBundle);
		demoActivity.showNotifications();
	}

	
	public String getFragmentTag() {
		return TopicFragment.class.getSimpleName();
	}
}
