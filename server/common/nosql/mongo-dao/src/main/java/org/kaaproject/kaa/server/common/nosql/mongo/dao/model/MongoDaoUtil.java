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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.NotificationDto;

public class MongoDaoUtil {

    private MongoDaoUtil() {
    }

    private static final BiMap<Character, Character> RESERVED_CHARACTERS = HashBiMap.create();
    static {
        RESERVED_CHARACTERS.put('.', (char) 0xFF0E);
        RESERVED_CHARACTERS.put('$', (char) 0xFF04);
    }

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
     * @param stateDtoList the stateDtoList
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

    /**
     * Specific method for recursive substitute the reserved $ and . characters in the key names of the DBObject.
     * @param profileBody the profileBody
     * @return encoded DBObject
     */
    public static DBObject encodeReservedCharacteres(DBObject profileBody) {
        if (profileBody == null) {
            return null;
        }
        if (profileBody instanceof BasicDBList) {
            BasicDBList dbList = (BasicDBList)profileBody;
            BasicDBList modifiedList = new BasicDBList();
            for (Object value : dbList) {
                if(value instanceof DBObject) {
                    modifiedList.add(encodeReservedCharacteres((DBObject) value));
                } else {
                    modifiedList.add(value);
                }
            }
            return modifiedList;
        } else {
            Set<String> keySet = profileBody.keySet();
            DBObject modifiedNode = new BasicDBObject();
            if (keySet != null) {
                for (String key : keySet) {
                    Object value = profileBody.get(key);
                    for (char symbolToReplace : RESERVED_CHARACTERS.keySet()) {
                        key = key.replace(symbolToReplace, RESERVED_CHARACTERS.get(symbolToReplace));
                    }
                    if (value instanceof DBObject) {
                        modifiedNode.put(key, encodeReservedCharacteres((DBObject) value));
                    } else {
                        modifiedNode.put(key, value);
                    }
                }
            }
            return modifiedNode;
        }
    }

    /**
     * Specific method for recursive decoding the reserved $ and . characters in the key names of the DBObject.
     * @param profileBody the profileBody
     * @return decoded DBObject
     */
    public static DBObject decodeReservedCharacteres(DBObject profileBody) {

        if (profileBody == null) {
            return null;
        }
        if (profileBody instanceof BasicDBList) {
            BasicDBList dbList = (BasicDBList) profileBody;
            BasicDBList modifiedList = new BasicDBList();
            for (Object value : dbList) {
                if (value instanceof DBObject) {
                    modifiedList.add(decodeReservedCharacteres((DBObject) value));
                } else {
                    modifiedList.add(value);
                }
            }
            return modifiedList;
        } else {
            Set<String> keySet = profileBody.keySet();
            DBObject modifiedNode = new BasicDBObject();
            if (keySet != null) {
                for (String key : keySet) {
                    Object value = profileBody.get(key);
                    for (char symbolToReplace : RESERVED_CHARACTERS.values()) {
                        key = key.replace(symbolToReplace, RESERVED_CHARACTERS.inverse().get(symbolToReplace));
                    }
                    if (value instanceof DBObject) {
                        modifiedNode.put(key, decodeReservedCharacteres((DBObject) value));
                    } else {
                        modifiedNode.put(key, value);
                    }
                }
            }
            return modifiedNode;
        }
    }
}
