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

import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.demo.notification.NotificationDemoActivity;
import org.kaaproject.kaa.demo.notification.TopicInfoHolder;
import org.kaaproject.kaa.demo.notification.TopicModel;
import org.kaaproject.kaa.demo.notification.adapter.NotificationArrayAdapter;
import org.kaaproject.kaa.schema.example.Notification;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NotificationFragment extends ListFragment {

	public NotificationFragment() {
		super();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setListAdapter(new NotificationArrayAdapter(inflater, getNotificationList()));
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private List<Notification> getNotificationList() {
		Bundle bundle = ((NotificationDemoActivity)getActivity()).getFragmentData();
		if (null != bundle) {
			List<TopicModel> list = TopicInfoHolder.holder.getTopicModelList();
			if (null != list) {
				Integer position = bundle.getInt("position");
				TopicModel model = list.get(position);
				if (null != model){
					return model.getNotifications();
				}
				else{
					return new LinkedList<Notification>();
				}
			}
		}
		return new LinkedList<Notification>();
	}

	public String getFragmentTag() {
		return NotificationFragment.class.getSimpleName();
	}

}
