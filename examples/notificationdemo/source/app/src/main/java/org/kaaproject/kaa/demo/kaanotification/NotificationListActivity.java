package org.kaaproject.kaa.demo.kaanotification;

import android.os.Bundle;

import org.kaaproject.kaa.demo.kaanotification.adapters.NotificationArrayAdapter;
import org.kaaproject.kaa.demo.kaanotification.application.MyListActivity;

import org.kaaproject.kaa.schema.example.Notification;

import java.util.Collections;
import java.util.List;


public class NotificationListActivity extends MyListActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.setListAdapter(new NotificationArrayAdapter(this, getNotificationList()));
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.setListAdapter(new NotificationArrayAdapter(this, getNotificationList()));
    }


    private List<Notification> getNotificationList() {
        int position = getIntent().getExtras().getInt("position");
        List<TopicModel> list = TopicInfoHolder.holder.getTopicModelList();
        if (null != list) {
            TopicModel model = list.get(position);
            if (null != model)
                return model.getNotifications();
            else
                return Collections.EMPTY_LIST;
        }
        return Collections.EMPTY_LIST;
    }
}
