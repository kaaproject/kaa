package org.kaaproject.kaa.demo.kaanotification.application;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.kaaproject.kaa.demo.kaanotification.NotificationListActivity;
import org.kaaproject.www.kaanotification.R;
import org.kaaproject.kaa.demo.kaanotification.TopicInfoHolder;
import org.kaaproject.kaa.demo.kaanotification.TopicListActivity;
import org.kaaproject.kaa.demo.kaanotification.adapters.NotificationArrayAdapter;
import org.kaaproject.kaa.demo.kaanotification.adapters.TopicArrayAdapter;

import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaAndroid;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.notification.AbstractNotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.client.profile.AbstractProfileContainer;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.base.Profile;
import org.kaaproject.kaa.schema.example.Notification;

import java.io.IOException;
import java.util.List;


public class KaaManager {

    private static volatile KaaManager instance;


    private PopupWindow popupWindow;
    private View popup;

    private Context context;

    private KaaClient client;

    public static KaaManager getInstance(Context context) {
        KaaManager localInstance = instance;
        if (localInstance == null) {
            synchronized (KaaManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new KaaManager(context);
                }
            }
        }
        return localInstance;
    }

    private KaaManager(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        init(context);
        initPopup();
    }


    private void initPopup() {
        LinearLayout layoutOfPopup = new LinearLayout(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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


    void init(final Context context) {
        Kaa kaa;
        try {
            kaa = new KaaAndroid(context);
            client = kaa.getClient();
            client.getProfileManager().setProfileContainer(new AbstractProfileContainer() {
                @Override
                public Profile getProfile() {
                    return new Profile();
                }
            });
            try {
                kaa.start();
                Log.i("KAA", "Kaa SDK client started!");
                client.getNotificationManager().addTopicListListener(new NotificationTopicListListener() {
                    @Override
                    public void onListUpdated(List<Topic> topicList) {
                        Log.i("KAA", "Topic list updated" + topicList.toString());
                        TopicInfoHolder.holder.updateTopics(topicList);

                        final Activity activity = ((KaaNotificationApp) context.getApplicationContext()).getCurrentActivity();
                        if (null != activity && activity.getClass() == TopicListActivity.class) {
                            Log.i("KAA", "listActivity");
                            activity.runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            ((ListActivity) activity).setListAdapter(
                                                    new TopicArrayAdapter(activity, TopicInfoHolder.holder.getTopicModelList()));
                                        }
                                    }
                            );
                        }
                    }
                });

                client.getNotificationManager().addNotificationListener(new AbstractNotificationListener() {
                    @Override
                    public void onNotification(final String topicId, Notification notification) {

                        Log.i("KAA", "NOTIFICATION RECEIVED: " + notification.toString());

                        final String message = notification.getMessage();
                        final String imageUrl = notification.getImage();
                        final Activity activity = ((KaaNotificationApp) context.getApplicationContext()).getCurrentActivity();

                        TopicInfoHolder.holder.addNotification(topicId, notification);

                        if (null != activity) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    ((TextView) popup.findViewById(R.id.popup_notification)).setText(message);
                                    ((TextView) popup.findViewById(R.id.popup_topic)).setText(TopicInfoHolder.holder.getTopicName(topicId));
                                    ((ImageView) popup.findViewById(R.id.popup_image)).setImageBitmap(ImageCache.cache.getImage(imageUrl));

                                    Activity activity = ((KaaNotificationApp) context.getApplicationContext()).getCurrentActivity();

                                    if (null != activity) {
                                        if (activity.getClass() == TopicListActivity.class) {
                                            ((TopicListActivity) activity).setListAdapter(new TopicArrayAdapter(activity, TopicInfoHolder.holder.getTopicModelList()));
                                        } else if (activity.getClass() == NotificationListActivity.class) {
                                            NotificationListActivity notificationListActivity = (NotificationListActivity) activity;
                                            int pos = notificationListActivity.getIntent().getExtras().getInt("position");
                                            List<Notification> list = TopicInfoHolder.holder.getTopicModelList().get(pos).getNotifications();
                                            notificationListActivity.setListAdapter(new NotificationArrayAdapter(activity, list));
                                        }
                                    }

                                    View view = activity.getCurrentFocus();
                                    if (null != view)
                                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                                }
                            });

                        }
                    }
                });
            } catch (IOException | TransportException e) {
                Log.e("KAA-ERR", e.getMessage());
            }
        } catch (Exception e) {
            Log.i("KAA-ERR", e.getMessage());
        }
    }

    public void subscribeTo(String topicId) {
        try {
            client.getNotificationManager().subscribeToTopic(topicId, true);
            Log.i("KAA", "SUBSCRIBING TO " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e("KAA-ERR", e.getMessage());
        }
    }

    public void unsubscribeFrom(String topicId) {
        try {
            client.getNotificationManager().unsubscribeFromTopic(topicId, true);
            Log.i("KAA", "UNSUBSCRIBING FROM " + topicId);
        } catch (UnavailableTopicException e) {
            Log.e("KAA-ERR", e.getMessage());
        }
    }


}
