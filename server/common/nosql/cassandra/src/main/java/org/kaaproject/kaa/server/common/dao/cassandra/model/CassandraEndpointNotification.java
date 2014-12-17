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

package org.kaaproject.kaa.server.common.dao.cassandra.model;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.*;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointNotification;

import java.io.Serializable;
import java.nio.ByteBuffer;

@Table(name = CassandraModelConstants.ENDPOINT_NOTIFICATION_COLUMN_FAMILY_NAME)
public final class CassandraEndpointNotification implements EndpointNotification, Serializable {

    @Transient
    private static final long serialVersionUID = -6770166693195322360L;

    @PartitionKey
    @Column(name = ENDPOINT_NOTIFICATION_ENDPOINT_KEY_HASH_PROPERTY)
    private ByteBuffer endpointKeyHash;
    @ClusteringColumn
    @Column(name = ENDPOINT_NOTIFICATION_ID_PROPERTY)
    private String id;
    @Transient
    private CassandraNotification notification;

    public CassandraEndpointNotification() {
    }

    public CassandraEndpointNotification(EndpointNotificationDto dto) {
        this.id = dto.getId();
        this.endpointKeyHash = ByteBuffer.wrap(dto.getEndpointKeyHash());
        this.notification = dto.getNotificationDto() != null ? new CassandraNotification(dto.getNotificationDto()) : null;
    }

    public String getId() {
        return id;
    }

    public ByteBuffer getEndpointKeyHash() {
        return endpointKeyHash;
    }

    public void setEndpointKeyHash(ByteBuffer endpointKeyHash) {
        this.endpointKeyHash = endpointKeyHash;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CassandraNotification getNotification() {
        return notification;
    }

    public void setNotification(CassandraNotification notification) {
        this.notification = notification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CassandraEndpointNotification that = (CassandraEndpointNotification) o;

        if (endpointKeyHash != null ? !endpointKeyHash.equals(that.endpointKeyHash) : that.endpointKeyHash != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (notification != null ? !notification.equals(that.notification) : that.notification != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = endpointKeyHash != null ? endpointKeyHash.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (notification != null ? notification.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CassandraEndpointNotification{" +
                "endpointKeyHash=" + endpointKeyHash +
                ", id='" + id + '\'' +
                ", notification=" + notification +
                '}';
    }

    @Override
    public EndpointNotificationDto toDto() {
        EndpointNotificationDto dto = new EndpointNotificationDto();
        dto.setId(getId());
        dto.setEndpointKeyHash(endpointKeyHash != null ? endpointKeyHash.array() : null);
        dto.setNotificationDto(notification != null ? notification.toDto() : null);
        return dto;
    }
}
