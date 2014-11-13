package org.kaaproject.kaa.server.common.dao.mongo.model;

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
