/*
 * Copyright 2014 CyberVision, Inc.
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

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.TenantAdminDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.kaaproject.kaa.server.common.dao.model.mongo.EndpointGroupState;
import org.kaaproject.kaa.server.common.dao.model.mongo.MongoNotification;
import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;
import org.kaaproject.kaa.server.common.dao.model.sql.Tenant;
import org.kaaproject.kaa.server.common.dao.model.sql.User;
import org.kaaproject.kaa.server.common.dao.model.mongo.EventClassFamilyVersionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * The Class DaoUtil.
 */
public abstract class DaoUtil {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DaoUtil.class);

    /**
     * This method convert ObjectId to String object.
     *
     * @param objectId basic mongoDB id object type.
     * @return converted to string id.
     */
    public static String idToString(ObjectId objectId) {
        String id = null;
        if (objectId != null) {
            id = objectId.toString();
        }
        return id;
    }

    /**
     * This method take string id from <code>GenericModel</code> object
     *
     * @param the <code>GenericModel</code> object
     * @return the id as string type
     */
    public static String idToString(GenericModel<? extends Object> model) {
        String id = null;
        if (model != null) {
            Long lid = model.getId();
            id = lid != null ? lid.toString() : null;
        }
        return id;
    }

    /**
     * This method convert Long id to String object.
     *
     * @param lid basic mongoDB id object type.
     * @return converted to string id.
     */
    public static String idToString(Long lid) {
        String id = null;
        if (lid != null) {
            id = lid.toString();
        }
        return id;
    }

    /**
     * This method convert string id to ObjectId.
     *
     * @param id String id
     * @return new ObjectId object based on String id.
     */
    public static ObjectId idToObjectId(String id) {
        ObjectId objectId = null;
        if (ObjectId.isValid(id)) {
            objectId = new ObjectId(id);
        }
        return objectId;
    }


    /**
     * This method convert the list list of <code>ObjectId</code> objects
     * to the list <code>String</code> ids.
     *
     * @param ids list of <code>ObjectId</code> objects
     * @return the list of <code>String</code> ids
     */
    public static List<String> objectIdListToStringList(List<ObjectId> ids) {
        List<String> objects = null;
        if (ids != null && !ids.isEmpty()) {
            objects = new ArrayList<>();
            for (ObjectId id : ids) {
                objects.add(idToString(id));
            }
        }
        return objects;
    }

    /**
     * This method convert the list of <code>String</code> ids to
     * specific list of <code>ObjectId</code> objects.
     *
     * @param ids the list of <code>String</code> ids
     * @return the list of <code>ObjectId</code> objects
     */
    public static List<ObjectId> stringListToObjectIdList(List<String> ids) {
        List<ObjectId> objects = Collections.emptyList();
        if (ids != null && !ids.isEmpty()) {
            objects = new ArrayList<>();
            for (String id : ids) {
                objects.add(idToObjectId(id));
            }
        }
        return objects;
    }

    /**
     * This method  convert list of model objects to dto objects.
     *
     * @param <T>       Type of model object
     * @param toDtoList List of model objects.
     * @return List of converted objects.
     */
    public static <T> List<T> convertDtoList(Collection<? extends ToDto<T>> toDtoList) {
        List<T> list = Collections.emptyList();
        if (toDtoList != null && !toDtoList.isEmpty()) {
            list = new ArrayList<>();
            for (ToDto<T> object : toDtoList) {
                list.add(object.toDto());
            }
        }
        return list;
    }

    /**
     * This method  convert model object to dto object.
     *
     * @param <T> Type of model object
     * @param dto Model object
     * @return converted object
     */
    public static <T> T getDto(ToDto<T> dto) {
        T object = null;
        if (dto != null) {
            object = dto.toDto();
        }
        return object;
    }

    /**
     * This method use for coping array bytes.
     *
     * @param array bytes
     * @return copied array of bytes
     */
    public static byte[] getArrayCopy(byte[] array) {
        byte[] bytes = null;
        if (array != null) {
            bytes = Arrays.copyOf(array, array.length);
        }
        return bytes;
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

    /**
     * This method find file by name in context and convert input of file to string.
     *
     * @param name of the file
     * @param clazz the clazz
     * @return String
     */
    public static String getStringFromFile(String name, Class<?> clazz) {
        String data = "";
        if (isNotBlank(name)) {
            InputStream is = clazz.getClass().getResourceAsStream(name);
            if (is != null) {
                LOG.trace("Load input stream of file {}", is);
                try {
                    byte[] arrayData = IOUtils.toByteArray(is);
                    if (arrayData != null) {
                        data = new String(arrayData, Charset.forName("UTF-8"));
                    }
                } catch (IOException e) {
                    LOG.error("Can't read data from file", e);
                }
            }
        }
        return data;
    }


    /**
     * Convert users to tenant admins.
     *
     * @param users
     *            the users
     * @return the list of <code>TenantAdminDto</code> objects
     */
    public static List<TenantAdminDto> convertUsersToTenantAdmins(List<User> users) {
        List<TenantAdminDto> admins = Collections.emptyList();
        if (users != null && !users.isEmpty()) {
            admins = new ArrayList<>();
            for (User user : users) {
                TenantAdminDto admin = convertUserToTenantAdmin(user);
                if (admin != null) {
                    admins.add(admin);
                }
            }
        }
        return admins;
    }

    /**
     * This method convert <code>User</code> object
     * to <code>TenantAdminDto</code> object
     *
     * @param user the <code>User</code> object
     * @return converted <code>TenantAdminDto</code> object
     */
    public static TenantAdminDto convertUserToTenantAdmin(User user) {
        TenantAdminDto admin = null;
        if (user != null) {
            admin = new TenantAdminDto();
            Tenant tenant = user.getTenant();
            if (tenant != null) {
                admin.setId(String.valueOf(tenant.getId()));
                admin.setName(tenant.getName());
            }
            admin.setUserId(String.valueOf(user.getId()));
            admin.setUsername(user.getUsername());
            admin.setExternalUid(user.getExternalUid());
        }
        return admin;
    }
}
