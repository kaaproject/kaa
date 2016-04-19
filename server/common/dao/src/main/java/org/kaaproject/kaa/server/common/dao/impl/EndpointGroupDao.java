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

/**
 * The interface Endpoint group dao.
 *
 * @param <T> the type parameter
 */
public interface EndpointGroupDao<T> extends SqlDao<T> {


    /**
     * Find endpoint group by application id.
     *
     * @param applicationId the application id
     * @return the list of endpoint group
     */
    List<T> findByApplicationId(String applicationId);

    /**
     *  Find endpoint group by application id and weight.
     *  This method used for validation. For one application can be
     *  used unique group weight
     *
     * @param applicationId the application id
     * @param weight the weight
     * @return the endpoint group object
     */
    T findByAppIdAndWeight(String applicationId, int weight);
    
    /**
     *  Find endpoint group by application id and group name.
     *  This method used for validation. For one application can be
     *  used unique group name
     *
     * @param applicationId the application id
     * @param name the group name
     * @return the endpoint group object
     */
    T findByAppIdAndName(String applicationId, String name);

    /**
     * Removes the topic from endpoint group.
     *
     * @param id the endpoint group id
     * @param topicId the topic id
     * @return the endpoint group object
     */
    T removeTopicFromEndpointGroup(String id, String topicId);

    /**
     * Find endpoint groups by topic id and application id.
     *
     * @param appId the application id
     * @param topicId the topic id
     * @return the list of endpoint groups
     */
    List<T> findEndpointGroupsByTopicIdAndAppId(String appId, String topicId);

    /**
     * Adds the topic to endpoint group.
     *
     * @param id the id
     * @param topicId the topic id
     * @return the endpoint group object
     */
    T addTopicToEndpointGroup(String id, String topicId);

}
