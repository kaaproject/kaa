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


import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.OPT_LOCK;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.ENDPOINT_NOTIFICATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.EP_NF_ENDPOINT_KEY_HASH;

import java.io.Serializable;
import java.util.Arrays;

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointNotification;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = ENDPOINT_NOTIFICATION)
public final class MongoEndpointNotification implements EndpointNotification, Serializable {

    private static final long serialVersionUID = -6770166693195322360L;

    @Id
    private String id;
    @Field(EP_NF_ENDPOINT_KEY_HASH)
    private byte[] endpointKeyHash;
    private MongoNotification notification;

    public MongoEndpointNotification() {
    }

    public MongoEndpointNotification(EndpointNotificationDto dto) {
        this.id = dto.getId();
        this.endpointKeyHash = getArrayCopy(dto.getEndpointKeyHash());
        this.notification = dto.getNotificationDto() != null ? new MongoNotification(dto.getNotificationDto()) : null;
    }

    public String getId() {
        return id;
    }

    public MongoNotification getNotification() {
        return notification;
    }

    public byte[] getEndpointKeyHash() {
        return endpointKeyHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MongoEndpointNotification)) {
            return false;
        }

        MongoEndpointNotification that = (MongoEndpointNotification) o;

        if (!Arrays.equals(endpointKeyHash, that.endpointKeyHash)) {
            return false;
        }
        if (notification != null ? !notification.equals(that.notification) : that.notification != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? Arrays.hashCode(endpointKeyHash) : 0;
        result = 31 * result + (notification != null ? notification.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointNotification{" +
                "id='" + id + '\'' +
                ", endpointKeyHash=" + Arrays.toString(endpointKeyHash) +
                ", notification=" + notification +
                '}';
    }

    @Override
    public EndpointNotificationDto toDto() {
        EndpointNotificationDto dto = new EndpointNotificationDto();
        dto.setId(id);
        dto.setEndpointKeyHash(getArrayCopy(endpointKeyHash));
        dto.setNotificationDto(notification != null ? notification.toDto() : null);
        return dto;
    }
}
