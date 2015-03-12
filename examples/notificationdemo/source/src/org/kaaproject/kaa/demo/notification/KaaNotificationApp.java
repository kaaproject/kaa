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

package org.kaaproject.kaa.demo.notification;

import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.demo.notification.adapter.NotificationArrayAdapter;
import org.kaaproject.kaa.demo.notification.adapter.TopicArrayAdapter;
import org.kaaproject.kaa.demo.notification.fragment.NotificationFragment;
import org.kaaproject.kaa.demo.notification.fragment.TopicFragment;
import org.kaaproject.kaa.schema.example.Notification;

/**
 * The Class KaaNotificationApp.
 * Implementation of base {@link Application} class. Performs initialization of
 * application resources including initialization of Kaa client. Handles Kaa client lifecycle.
 */
public class KaaNotificationApp extends Application {

	public static final String TAG = KaaNotificationApp.class.getSimpleName();

	private static Context mContext;
	private KaaClient mClient;

	private NotificationDemoActivity demoActivity;
	private NotificationListener notificationListener;
	private NotificationTopicListListener topicListListener;

	public void onCreate() {
		super.onCreate();
		mContext = this;
		/*
		 * Initialize Kaa client using android context.
		 */
		mClient = Kaa.newClient(new AndroidKaaPlatformContext(this));
		initPopup();

        /*
         * Initializing notification listener and adding it to Kaa client
         */
		initNotificationListener();
		mClient.addNotificationListener(notificationListener);

        /*
         * Initializing topicList listener and adding it to Kaa client
         */
		initNotificationTopicListListener();
		mClient.addTopicListListener(topicListListener);
        /*
         * Start Kaa client workflow.
         */
		mClient.start();
	}

	public void setDemoActivity(NotificationDemoActivity demoActivity){
		this.demoActivity = demoActivity;
	}
	
	public static Context getContext() {
		return mContext;
	}

	public KaaClient getKaaClient() {
		return mClient;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

        /*
         * Stop Kaa client. Release all network connections and application
         * resources. Shutdown all Kaa client tasks.
         */
		mClient.stop();
	}

    /*
     * This method subscribes Kaa client to voluntary topic
     */
	public void subscribeToTopic(String topicId) {
		try {
			mClient.subscribeToTopic(topicId, true);
			Log.i(TAG, "Subscribing to topic with id: " + topicId);
		} catch (UnavailableTopicException e) {
			Log.e(TAG, e.getMessage());
		}
	}


    /*
     * This method unbscribes Kaa client from voluntary topic
     */
	public void unsubscribeFromTopic(String topicId) {
		try {
			mClient.unsubscribeFromTopic(topicId, true);
			Log.i(TAG, "Unsubscribing from topic with id: " + topicId);
		} catch (UnavailableTopicException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private PopupWindow popupWindow;
	private View popup;

	public void showPopup(Activity context, String topicId, Notification notification) {
		((TextView) popup.findViewById(R.id.popup_notification)).setText(notification.getMessage());
		((TextView) popup.findViewById(R.id.popup_topic)).setText(TopicInfoHolder.holder.getTopicName(topicId));
		((ImageView) popup.findViewById(R.id.popup_image)).setImageBitmap(ImageCache.cache.getImage(notification
				.getImage()));
		View view = context.getCurrentFocus();
		if (null != view) {
			popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
		}
	}

	private void initPopup() {
		LinearLayout layoutOfPopup = new LinearLayout(this);
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popup = layoutInflater.inflate(R.layout.popup_notification, layoutOfPopup);
		popup.findViewById(R.id.popup_ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == R.id.popup_ok) {
					popupWindow.dismiss();
				}
			}
		});
		popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(popup);
	}

	public void initNotificationListener() {
		this.notificationListener = new NotificationListener() {
			public void onNotification(final String topicId, final Notification notification) {
				Log.i(TAG, "Notification received: " + notification.toString());
				TopicInfoHolder.holder.addNotification(topicId, notification);
				if (null != demoActivity) {
					demoActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Fragment fragment;
							fragment = demoActivity.getSupportFragmentManager().findFragmentByTag(
									NotificationFragment.class.getSimpleName());
							if (null != fragment && fragment.isVisible()) {
								int pos = demoActivity.getFragmentData().getInt("position");
								List<Notification> list = TopicInfoHolder.holder.getTopicModelList().get(pos)
										.getNotifications();
								((ListFragment) fragment).setListAdapter(new NotificationArrayAdapter(demoActivity
										.getLayoutInflater(), list));
							} else {
								fragment = demoActivity.getSupportFragmentManager().findFragmentByTag(
										TopicFragment.class.getSimpleName());
								if (null != fragment && fragment.isVisible()) {
									((ListFragment) fragment).setListAdapter(new TopicArrayAdapter(demoActivity
											.getLayoutInflater(), TopicInfoHolder.holder.getTopicModelList()));
								}
							}
							showPopup(demoActivity, topicId, notification);
						}
					});
				}
			}
		};
	}

	public void initNotificationTopicListListener() {
		this.topicListListener = new NotificationTopicListListener() {
			public void onListUpdated(List<Topic> topicList) {
				Log.i(TAG, "Topic list updated with topics: ");
				for(Topic topic: topicList){
					Log.i(TAG, topic.toString());
				}
				TopicInfoHolder.holder.updateTopics(topicList);
				if (null != demoActivity) {
					final TopicFragment topicFragment = (TopicFragment) demoActivity.getSupportFragmentManager()
							.findFragmentByTag(TopicFragment.class.getSimpleName());
					if (null!=topicFragment && topicFragment.isVisible()) {
						demoActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								topicFragment.setListAdapter(new TopicArrayAdapter(demoActivity.getLayoutInflater(),
										TopicInfoHolder.holder.getTopicModelList()));
							}
						});
					}
				}
			}
		};
	}

}