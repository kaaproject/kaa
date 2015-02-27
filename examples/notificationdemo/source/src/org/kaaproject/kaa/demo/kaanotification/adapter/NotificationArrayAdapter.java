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

package org.kaaproject.kaa.demo.kaanotification.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.kaaproject.www.kaanotification.R;
import org.kaaproject.kaa.demo.kaanotification.ImageCache;

import org.kaaproject.kaa.schema.example.Notification;

import java.util.List;


public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
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
