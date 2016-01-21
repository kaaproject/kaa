package org.kaaproject.kaa.client.notification;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.common.endpoint.gen.Topic;

public class TopicListHashCalculator {
    public static final Integer NULL_LIST_HASH = 0;
    public static final Integer EMPTRY_LIST_HASH = 1;

    public static Integer calculateTopicListHash(List<Topic> topics) {
        if (topics == null)
            return NULL_LIST_HASH;

        int result = EMPTRY_LIST_HASH;
        if (!topics.isEmpty()) {
            List<Topic> newTopics = new LinkedList<>(topics);
            Collections.sort(newTopics, new Comparator<Topic>() {
                @Override
                public int compare(Topic o1, Topic o2) {
                    return o1.getId() < o2.getId() ? -1 : (o1.getId() < o2.getId() ) ? 1 : 0;
                }
            });

            for (Topic topic : newTopics) {
                long topicId = topic.getId();
                int elementHash = (int)(topicId ^ (topicId >>> 32));
                result = 31 * result + elementHash;
            }
        }

        return result;
    }
}
