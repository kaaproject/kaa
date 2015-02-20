package org.kaaproject.kaa.demo.kaanotification;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.kaaproject.kaa.demo.kaanotification.adapters.TopicArrayAdapter;
import org.kaaproject.kaa.demo.kaanotification.application.KaaManager;
import org.kaaproject.kaa.demo.kaanotification.application.KaaNotificationApp;
import org.kaaproject.kaa.demo.kaanotification.application.MyListActivity;

public class TopicListActivity extends MyListActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setListAdapter(new TopicArrayAdapter(this, TopicInfoHolder.holder.getTopicModelList()));
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setListAdapter(new TopicArrayAdapter(this, TopicInfoHolder.holder.getTopicModelList()));
        Log.i("KAA","Starting KAA initialization");
        KaaManager.getInstance(KaaNotificationApp.getContext());
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, NotificationListActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

}
