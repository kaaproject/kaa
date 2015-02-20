package org.kaaproject.kaa.demo.kaanotification.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class KaaNotificationApp extends Application {

    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public static Context getContext() {
        return mContext;
    }

}