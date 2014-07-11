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

package org.kaaproject.kaa.server.common.dao.model.mongo;


import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.model.ToDto;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.common.dto.Util.getArrayCopy;

@Document(collection = EndpointNotification.COLLECTION_NAME)
public final class EndpointNotification implements Serializable, ToDto<EndpointNotificationDto> {

    private static final long serialVersionUID = -6770166693195322360L;

    public static final String COLLECTION_NAME = "endpoint_notification";

    @Id
    private String id;
    @Field("endpoint_key_hash")
    private byte[] endpointKeyHash;
    private Notification notification;

    public EndpointNotification() {
    }

    public EndpointNotification(EndpointNotificationDto dto) {
        this.id = dto.getId();
        this.endpointKeyHash = getArrayCopy(dto.getEndpointKeyHash());
        this.notification = dto.getNotificationDto() != null ? new Notification(dto.getNotificationDto()) : null;
    }

    public String getId() {
        return id;
    }

    public Notification getNotification() {
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
        if (!(o instanceof EndpointNotification)) {
            return false;
        }

        EndpointNotification that = (EndpointNotification) o;

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
