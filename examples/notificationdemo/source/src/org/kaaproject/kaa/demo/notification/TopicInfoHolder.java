/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.notification;

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
        if (null != model){
            return model.getTopicName();
        }
        else return "";
    }

    public List<TopicModel> getTopicModelList() {
        List<TopicModel> list = new ArrayList<>(topicModelMap.values());
        Collections.reverse(list);
        return list;
    }

    public void addNotification(String topicId, Notification notification) {
        TopicModel model = topicModelMap.get(topicId);
        if (null != model){
            model.addNotification(notification);
        }
    }

    public synchronized void updateTopics(List<Topic> updatedTopics) {
        Set<String> newIds = new HashSet<>();

        for (Topic topic : updatedTopics) {
            String topicId = topic.getId();
            if (!topicModelMap.containsKey(topicId)){
                topicModelMap.put(topicId, new TopicModel(topic));
            }
            newIds.add(topicId);
        }
        Iterator<Map.Entry<String, TopicModel>> iter = topicModelMap.entrySet().iterator();
        while (iter.hasNext()) {
            String id = iter.next().getKey();
            if (!newIds.contains(id)){
                iter.remove();
            }
        }
    }


}
