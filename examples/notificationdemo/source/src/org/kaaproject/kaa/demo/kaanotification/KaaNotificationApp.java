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

package org.kaaproject.kaa.demo.kaanotification;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
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
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.schema.example.Notification;
import org.kaaproject.www.kaanotification.R;

public class KaaNotificationApp extends Application {

    private static Context mContext;
    private KaaClient mClient;
    private boolean mKaaStarted;

    public void onCreate() {
        super.onCreate();
        mContext = this;
        mClient = Kaa.newClient(new AndroidKaaPlatformContext(this));
        initPopup();
        mClient.start();
    }

    public static Context getContext() {
        return mContext;
    }

    public KaaClient getKaaClient() {
        return mClient;
    }

    public void pause() {
        mClient.pause();
    }

    public void resume() {
        mClient.resume();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mClient.stop();
        mKaaStarted = false;
    }


    public void subscribeToTopic(String topicId) {
        try {
            mClient.subscribeToTopic(topicId, true);
            Log.i("KAA", "SUBSCRIBING TO " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e("KAA-ERR", e.getMessage());
        }
    }

    public void unsubscribeFromTopic(String topicId) {
        try {
            mClient.unsubscribeFromTopic(topicId, true);
            Log.i("KAA", "UNSUBSCRIBING FROM " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e("KAA-ERR", e.getMessage());
        }
    }


    private PopupWindow popupWindow;
    private View popup;


    public void showPopup(Activity context, String topicId, Notification notification){
        ((TextView) popup.findViewById(R.id.popup_notification)).setText(notification.getMessage());
        ((TextView) popup.findViewById(R.id.popup_topic)).setText(TopicInfoHolder.holder.getTopicName(topicId));
        ((ImageView) popup.findViewById(R.id.popup_image)).setImageBitmap(ImageCache.cache.getImage(notification.getImage()));
        View view = context.getCurrentFocus();
        if (null != view)
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
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
        popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(popup);
    }
}