package org.kaaproject.kaa.demo.kaanotification.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.kaaproject.www.kaanotification.R;
import org.kaaproject.kaa.demo.kaanotification.application.KaaManager;
import org.kaaproject.kaa.demo.kaanotification.TopicModel;

public class TopicArrayAdapter extends ArrayAdapter<TopicModel> {

    private final List<TopicModel> list;
    private final Activity context;

    public TopicArrayAdapter(Activity context, List<TopicModel> list) {
        super(context, R.layout.topics, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView topic;
        protected TextView notificationCount;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.topics, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.topic = (TextView) view.findViewById(R.id.label);
            viewHolder.notificationCount = (TextView) view.findViewById(R.id.notifications);
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
            viewHolder.checkbox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            TopicModel element = (TopicModel) viewHolder.checkbox.getTag();
                            element.setSelected(buttonView.isChecked());

                            if (element.isSelected()) {
                                viewHolder.notificationCount.setText("" + element.getNotificationsCount());
                                if (!element.isMandatoryTopic())
                                    if (!element.isSubscribedTo()) {
                                        element.setSubscribedTo(true);
                                        KaaManager.getInstance(getContext()).subscribeTo(element.getTopicId());
                                    }
                            } else {
                                if (!element.isMandatoryTopic())
                                    if (element.isSubscribedTo()) {
                                        element.setSubscribedTo(false);
                                        KaaManager.getInstance(getContext()).unsubscribeFrom(element.getTopicId());
                                    }
                                viewHolder.notificationCount.setText("");
                            }
                        }


                    });
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.topic.setText(list.get(position).getTopicName());
        TopicModel model = (TopicModel) holder.checkbox.getTag();
        if (model.isMandatoryTopic()) {
            holder.notificationCount.setText("" + model.getNotificationsCount());
            holder.checkbox.setEnabled(false);
            holder.checkbox.setChecked(true);
        } else {
            holder.notificationCount.setText("");
            holder.checkbox.setChecked(model.isSelected());
            holder.checkbox.setEnabled(true);
        }
        return view;
    }
}