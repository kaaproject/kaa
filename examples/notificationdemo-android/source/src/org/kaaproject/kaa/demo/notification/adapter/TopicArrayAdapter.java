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

package org.kaaproject.kaa.demo.notification.adapter;

import java.util.List;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import org.kaaproject.kaa.demo.notification.KaaNotificationApp;
import org.kaaproject.kaa.demo.notification.R;
import org.kaaproject.kaa.demo.notification.TopicModel;

public class TopicArrayAdapter extends ArrayAdapter<TopicModel> {

	private final List<TopicModel> list;
	private final LayoutInflater inflater;
	private final KaaNotificationApp app;
	private static final String EMPTY_STRING="";

	public TopicArrayAdapter(LayoutInflater inflator, List<TopicModel> list) {
		super(inflator.getContext(), R.layout.topics, list);
		this.list = list;
		this.inflater = inflator;
		this.app = (KaaNotificationApp) inflator.getContext().getApplicationContext();
	}

	static class ViewHolder {
		protected TextView topic;
		protected TextView notificationCount;
		protected CheckBox checkbox;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = inflater.inflate(R.layout.topics, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.topic = (TextView) view.findViewById(R.id.label);
			viewHolder.notificationCount = (TextView) view.findViewById(R.id.notifications);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					TopicModel element = (TopicModel) viewHolder.checkbox.getTag();
					element.setSelected(buttonView.isChecked());
					if (element.isSelected()) {
						viewHolder.notificationCount.setText(String.valueOf(element.getNotificationsCount()));
						if (!element.isMandatoryTopic()) {
							if (!element.isSubscribedTo()) {
								element.setSubscribedTo(true);
								app.subscribeToTopic(element.getTopicId());
							}
						}
					} else {
						if (!element.isMandatoryTopic()) {
							if (element.isSubscribedTo()) {
								element.setSubscribedTo(false);
								app.unsubscribeFromTopic(element.getTopicId());
							}
							viewHolder.notificationCount.setText(EMPTY_STRING);
						}
					}
				}

			});
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(list.get(position));
		} else {
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.topic.setText(list.get(position).getTopicName());
		TopicModel model = (TopicModel) holder.checkbox.getTag();
		if (model.isMandatoryTopic()) {
			holder.notificationCount.setText(String.valueOf(model.getNotificationsCount()));
			holder.checkbox.setEnabled(false);
			holder.checkbox.setChecked(true);
		} else {
			holder.notificationCount.setText(EMPTY_STRING);
			holder.checkbox.setChecked(model.isSelected());
			holder.checkbox.setEnabled(true);
		}
		return view;
	}
}