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
import android.os.Bundle;
import android.util.Log;

import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.demo.kaanotification.KaaNotificationApp;
import org.kaaproject.kaa.demo.kaanotification.TopicInfoHolder;
import org.kaaproject.kaa.demo.kaanotification.TopicModel;
import org.kaaproject.kaa.demo.kaanotification.adapter.NotificationArrayAdapter;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.LinkedList;
import java.util.List;

public class NotificationListActivity extends ListActivity {

    private NotificationListener notificationListener;
    private KaaNotificationApp app;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        app = (KaaNotificationApp) getApplicationContext();
        this.setListAdapter(new NotificationArrayAdapter(this,
                getNotificationList()));
        createNotificationListener();
        app.getKaaClient().addNotificationListener(notificationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.getKaaClient().removeNotificationListener(notificationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        app.getKaaClient().removeNotificationListener(notificationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setListAdapter(new NotificationArrayAdapter(this,
                getNotificationList()));
        app.getKaaClient().addNotificationListener(notificationListener);
    }

    public void createNotificationListener() {
        notificationListener = new NotificationListener() {
            public void onNotification(final String topicId,
                                       final Notification notification) {
                Log.i("KAA",
                        "NOTIFICATION RECEIVED: " + notification.toString());
                TopicInfoHolder.holder.addNotification(topicId, notification);

                NotificationListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationListActivity activity = NotificationListActivity.this;
                        int pos = activity.getIntent().getExtras()
                                .getInt("position");
                        List<Notification> list = TopicInfoHolder.holder
                                .getTopicModelList().get(pos)
                                .getNotifications();
                        activity.setListAdapter(new NotificationArrayAdapter(
                                activity, list));
                        app.showPopup(NotificationListActivity.this, topicId,
                                notification);
                    }
                });

            }
        };
    }

    private List<Notification> getNotificationList() {
        int position = getIntent().getExtras().getInt("position");
        List<TopicModel> list = TopicInfoHolder.holder.getTopicModelList();
        if (list != null) {
            TopicModel model = list.get(position);
            if (model != null) {
                return model.getNotifications();
            } else {
                return new LinkedList<Notification>();
            }
        }
        return new LinkedList<Notification>();
    }

}
