package org.kaaproject.kaa.demo.kaanotification.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.kaaproject.www.kaanotification.R;
import org.kaaproject.kaa.demo.kaanotification.application.ImageCache;

import org.kaaproject.kaa.schema.example.Notification;

import java.util.List;

/**
 * Created by serb on 14.02.15.
 */
public class NotificationArrayAdapter extends ArrayAdapter {
    private final List<Notification> list;
    private final Activity context;

    public NotificationArrayAdapter(Activity context, List<Notification> list) {
        super(context, R.layout.notification, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView message;
        protected ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.notification, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.message = (TextView) view.findViewById(R.id.notification_message);
            viewHolder.image = (ImageView) view.findViewById(R.id.notification_img);
            view.setTag(viewHolder);
            viewHolder.message.setTag(list.get(position));
        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        Notification notification = (Notification) holder.message.getTag();
        holder.message.setText(notification.getMessage());
        holder.image.setImageBitmap(ImageCache.cache.getImage(notification.getImage()));
        return view;
    }

}
