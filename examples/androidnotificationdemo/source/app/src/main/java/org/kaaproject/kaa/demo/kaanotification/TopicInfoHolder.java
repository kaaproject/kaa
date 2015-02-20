package org.kaaproject.kaa.demo.kaanotification;

import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.schema.example.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TopicInfoHolder {

    private final Map<String, TopicModel> topicModelMap;

    public static final TopicInfoHolder holder = new TopicInfoHolder();

    private TopicInfoHolder() {
        topicModelMap = new LinkedHashMap<>();
    }


    public String getTopicName(String topicId) {
        TopicModel model = topicModelMap.get(topicId);
        if (null != model)
            return model.getTopicName();
        else return "";
    }

    public List<TopicModel> getTopicModelList() {
        List<TopicModel> list = new ArrayList<>(topicModelMap.values());
        Collections.reverse(list);
        return list;
    }

    public void addNotification(String topicId, Notification notification) {
        TopicModel model = topicModelMap.get(topicId);
        if (null != model)
            model.addNotification(notification);
    }


    public synchronized void updateTopics(List<Topic> updatedTopics) {
        Set<String> newIds = new HashSet<>();

        for (Topic topic : updatedTopics) {
            String topicId = topic.getId();
            if (!topicModelMap.containsKey(topicId))
                topicModelMap.put(topicId, new TopicModel(topic));
            newIds.add(topicId);
        }
        Iterator<Map.Entry<String, TopicModel>> iter = topicModelMap.entrySet().iterator();
        while (iter.hasNext()) {
            String id = iter.next().getKey();
            if (!newIds.contains(id))
                iter.remove();
        }
    }


}
