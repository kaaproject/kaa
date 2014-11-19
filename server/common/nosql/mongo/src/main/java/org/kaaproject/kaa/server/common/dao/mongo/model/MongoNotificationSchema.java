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

package org.kaaproject.kaa.server.common.dao.mongo.model;

import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.model.NotificationSchema;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = MongoNotificationSchema.COLLECTION_NAME)
public final class MongoNotificationSchema extends AbstractSchema<NotificationSchemaDto> implements NotificationSchema{

    private static final long serialVersionUID = 6585856417466958172L;

    public static final String COLLECTION_NAME = "notification_schema";

    @Indexed
    @Field("notification_type")
    private NotificationTypeDto type;

    public MongoNotificationSchema() {
    }

    public MongoNotificationSchema(NotificationSchemaDto dto) {
        super(dto);
        this.type = dto.getType();
    }

    public NotificationTypeDto getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof MongoNotificationSchema) {
            MongoNotificationSchema that = (MongoNotificationSchema) other;
            if (type != that.type) {
                return false;
            }
            return super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NotificationSchema{" +
                "id='" + id + '\'' +
                ", applicationId=" + applicationId +
                ", majorVersion=" + majorVersion +
                ", type=" + type +
                ", schema='" + schema + '\'' +
                '}';
    }

    @Override
    public NotificationSchemaDto toDto() {
        NotificationSchemaDto dto = super.toDto();
        dto.setType(type);
        return dto;
    }

    @Override
    protected NotificationSchemaDto createDto() {
        return new NotificationSchemaDto();
    }

    @Override
    public int incrementVersion() {
        return ++majorVersion;
    }
}
