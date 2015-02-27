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

package org.kaaproject.kaa.demo.kaanotification.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.demo.kaanotification.KaaNotificationApp;
import org.kaaproject.kaa.demo.kaanotification.TopicInfoHolder;
import org.kaaproject.kaa.demo.kaanotification.adapter.TopicArrayAdapter;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.List;

public class TopicListActivity extends ListActivity {

    private NotificationTopicListListener topicListListener;
    private NotificationListener notificationListener;

    private KaaNotificationApp app;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        app = (KaaNotificationApp) getApplicationContext();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setListAdapter(new TopicArrayAdapter(this, TopicInfoHolder.holder.getTopicModelList()));
        topicListListener = createNotificationTopicListListener();
        app.getKaaClient().addTopicListListener(topicListListener);
        notificationListener = createNotificationListener();
        app.getKaaClient().addNotificationListener(notificationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.getKaaClient().removeTopicListListener(topicListListener);
        app.getKaaClient().removeNotificationListener(notificationListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        app.getKaaClient().addTopicListListener(topicListListener);
        app.getKaaClient().addNotificationListener(notificationListener);
        setListAdapter(new TopicArrayAdapter(this, TopicInfoHolder.holder.getTopicModelList()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.getKaaClient().removeTopicListListener(topicListListener);
        app.getKaaClient().removeNotificationListener(notificationListener);
    }

    public NotificationListener createNotificationListener() {
        return new NotificationListener() {
            @Override
            public void onNotification(final String topicId, final Notification notification) {
                Log.i("KAA", "NOTIFICATION RECEIVED: " + notification.toString());
                TopicInfoHolder.holder.addNotification(topicId, notification);
                TopicListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TopicListActivity activity = TopicListActivity.this;
                        activity.setListAdapter(new TopicArrayAdapter(activity, TopicInfoHolder.holder.getTopicModelList()));
                        app.showPopup(TopicListActivity.this, topicId, notification);
                    }
                });

            }
        };
    }

    public NotificationTopicListListener createNotificationTopicListListener() {
        return new NotificationTopicListListener() {
            @Override
            public void onListUpdated(List<Topic> topicList) {
                Log.i("KAA", "Topic list updated" + topicList.toString());
                TopicInfoHolder.holder.updateTopics(topicList);
                Log.i("KAA", "listActivity");
                TopicListActivity.this.runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                TopicListActivity.this.setListAdapter(
                                        new TopicArrayAdapter(TopicListActivity.this, TopicInfoHolder.holder.getTopicModelList()));
                            }
                        }
                );

            }
        };
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, NotificationListActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }



}
