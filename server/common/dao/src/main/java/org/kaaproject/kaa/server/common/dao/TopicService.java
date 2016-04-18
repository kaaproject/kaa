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

package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.TopicTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;

import java.util.List;

/**
 * The Interface TopicService.
 */
public interface TopicService {

    /**
     * Save topic object.
     *
     * @param topicDto the topic dto
     * @return the topic dto
     */
    TopicDto saveTopic(TopicDto topicDto);

    /**
     * Find topic by id.
     *
     * @param id the id
     * @return the topic dto
     */
    TopicDto findTopicById(String id);

    /**
     * Find topics by application id.
     *
     * @param appId the application id
     * @return the list of topics
     */
    List<TopicDto> findTopicsByAppId(String appId);

    /**
     * Find topics by application id and type.
     *
     * @param appId the application id
     * @param typeDto the type dto
     * @return the list of topics
     */
    List<TopicDto> findTopicsByAppIdAndType(String appId, TopicTypeDto typeDto);

    /**
     * Find topics by endpoint group id.
     *
     * @param endpointGroupId the endpoint group id
     * @return the list
     */
    List<TopicDto> findTopicsByEndpointGroupId(String endpointGroupId);

    /**
     * Find vacant topics by endpoint group id.
     * Find all topics where aren't attached to current endpoint group
     *
     * @param endpointGroupId the endpoint group id
     * @return the list of topics
     */
    List<TopicDto> findVacantTopicsByEndpointGroupId(String endpointGroupId);

    /**
     * Removes the topic by id.
     *
     * @param id the id
     * @return the list of update notification dto
     */
    List<UpdateNotificationDto<EndpointGroupDto>> removeTopicById(String id);

    /**
     * Removes the topics by application id.
     *
     * @param appId the application id
     */
    void removeTopicsByAppId(String appId);

}
