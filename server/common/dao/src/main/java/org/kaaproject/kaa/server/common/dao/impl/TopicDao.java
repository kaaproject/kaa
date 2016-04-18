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

package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.server.common.dao.model.sql.Topic;

/**
 * The Interface TopicDao.
 *
 * @param <T> the generic type
 */
public interface TopicDao<T> extends SqlDao<T> {

    /**
     * Find topics by application id.
     *
     * @param appId the application id
     * @return the list of topics
     */
    List<T> findTopicsByAppId(String appId);

    /**
     * Find topics by application id and type.
     *
     * @param appId the application id
     * @param type  the topic type
     * @return the list of topics
     */
    List<T> findTopicsByAppIdAndType(String appId, TopicTypeDto type);

    /**
     * Find topic by application id and name.
     *
     * @param appId the application id
     * @param topicName the topic name
     * @return topic by application
     */
    T findTopicByAppIdAndName(String appId, String topicName);

    /**
     * Find topics by ids.
     *
     * @param ids the ids
     * @return the list of topics
     */
    List<T> findTopicsByIds(List<String> ids);

    /**
     * Removes the topics by application id.
     *
     * @param appId the application id
     */
    void removeTopicsByAppId(String appId);

    /**
     * Gets the next sequence number of topic.
     *
     * @param topicId the topic id
     * @return the topic with next sequence number
     */
    T getNextSeqNumber(String topicId);

    /**
     * Find vacant topics for endpoint groups.
     * This method have to return topics which not yet attached to endpoint group with specific id
     *
     * @param appId   the application id
     * @param groupId the group id
     * @return the list of vacant topics
     */
    List<Topic> findVacantTopicsByGroupId(String appId, String groupId);
}
