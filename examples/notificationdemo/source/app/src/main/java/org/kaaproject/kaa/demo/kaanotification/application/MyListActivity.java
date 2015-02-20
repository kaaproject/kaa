package org.kaaproject.kaa.demo.kaanotification.application;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

public class MyListActivity extends ListActivity {
    protected KaaNotificationApp mKaaNotificationApp;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKaaNotificationApp = (KaaNotificationApp) this.getApplicationContext();
    }

    protected void onResume() {
        super.onResume();
        mKaaNotificationApp.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = mKaaNotificationApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            mKaaNotificationApp.setCurrentActivity(null);
    }
}

