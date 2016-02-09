/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.NotificationDto;

public class MongoDaoUtil {
    /**
     * Specific method for converting list of <code>EndpointGroupStateDto</code> objects
     * to list of model objects <code>EndpointGroupState</code>.
     *
     * @param stateDtoList the state dto list
     * @return converted list of <code>EndpointGroupState</code> objects
     */
    public static List<EndpointGroupState> convertDtoToModelList(List<EndpointGroupStateDto> stateDtoList) {
        List<EndpointGroupState> states = null;
        if (stateDtoList != null && !stateDtoList.isEmpty()) {
            states = new ArrayList<>();
            for (EndpointGroupStateDto dto : stateDtoList) {
                EndpointGroupState state = new EndpointGroupState();
                state.setConfigurationId(dto.getConfigurationId());
                state.setEndpointGroupId(dto.getEndpointGroupId());
                state.setProfileFilterId(dto.getProfileFilterId());
                states.add(state);
            }
        }
        return states;
    }

    /**
     * Specific method for converting list of <code>EventClassFamilyVersionStateDto</code> objects
     * to list of model objects <code>EventClassFamilyVersionState</code>
     * @param stateDtoList
     * @return converted list of <code>EndpointGroupState</code> objects
     */
    public static List<EventClassFamilyVersionState> convertECFVersionDtoToModelList(List<EventClassFamilyVersionStateDto> stateDtoList) {
        List<EventClassFamilyVersionState> states = null;
        if (stateDtoList != null && !stateDtoList.isEmpty()) {
            states = new ArrayList<>();
            for (EventClassFamilyVersionStateDto dto : stateDtoList) {
                EventClassFamilyVersionState state = new EventClassFamilyVersionState();
                state.setEcfId(dto.getEcfId());
                state.setVersion(dto.getVersion());
                states.add(state);
            }
        }
        return states;
    }

    /**
     * This method convert list of dto objects to model object.
     *
     * @param notificationList the notification list
     * @return converted list of model objects
     */
    public static List<MongoNotification> convertToModelList(List<NotificationDto> notificationList) {
        List<MongoNotification> notifications = null;
        if (notificationList != null && !notificationList.isEmpty()) {
            notifications = new ArrayList<>();
            for (NotificationDto dto : notificationList) {
                notifications.add(new MongoNotification(dto));
            }
        }
        return notifications;
    }
}
