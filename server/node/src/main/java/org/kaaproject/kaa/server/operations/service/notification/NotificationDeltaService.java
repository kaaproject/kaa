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

package org.kaaproject.kaa.server.operations.service.notification;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationRequest;
import org.kaaproject.kaa.server.operations.pojo.GetNotificationResponse;
import org.kaaproject.kaa.server.operations.service.cache.TopicListCacheEntry;


/**
 * The Interface NotificationDeltaService.
 */
public interface NotificationDeltaService {

    /**
     * Gets the notification delta.
     *
     * @param request the request
     * @return the notification delta
     */
    GetNotificationResponse getNotificationDelta(GetNotificationRequest request);

    /**
     * Find notification by id.
     *
     * @param notificationId the notification id
     * @return the notification dto
     */
    NotificationDto findNotificationById(String notificationId);

    /**
     * Find unicast notification by id.
     *
     * @param unicastNotificationId the unicast notification id
     * @return the notification dto
     */
    NotificationDto findUnicastNotificationById(String unicastNotificationId);

    /**
     * Calculate topic list hash for given profile
     * @param appToken - application token
     * @param endpointId - endpoint id
     * @param profile - endpoint profile
     * @return topic list cache entry
     */
    TopicListCacheEntry getTopicListHash(String appToken, String endpointId, EndpointProfileDto profile);

}
