/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.client.notification;

import org.kaaproject.kaa.common.endpoint.gen.Topic;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * The Class TopicListHashCalculator.
 */
public class TopicListHashCalculator {

  /**
   * The Constant NULL_LIST_HASH.
   */
  public static final Integer NULL_LIST_HASH = 0;

  /**
   * The Constant EMPTRY_LIST_HASH.
   */
  public static final Integer EMPTRY_LIST_HASH = 1;

  /**
   * Calculate topic list hash.
   *
   * @param topics the topics
   * @return the integer
   */
  public static Integer calculateTopicListHash(List<Topic> topics) {
    if (topics == null) {
      return NULL_LIST_HASH;
    }

    int result = EMPTRY_LIST_HASH;
    if (!topics.isEmpty()) {
      List<Topic> newTopics = new LinkedList<>(topics);
      Collections.sort(newTopics, new Comparator<Topic>() {
        @Override
        public int compare(Topic o1, Topic o2) {
          return o1.getId() < o2.getId() ? -1 : (o1.getId() > o2.getId()) ? 1 : 0;
        }
      });

      for (Topic topic : newTopics) {
        long topicId = topic.getId();
        int elementHash = (int) (topicId ^ (topicId >>> 32));
        result = 31 * result + elementHash;
      }
    }

    return result;
  }
}
