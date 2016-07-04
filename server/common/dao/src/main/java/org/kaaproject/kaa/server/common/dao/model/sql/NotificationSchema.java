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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.NOTIFICATION_SCHEMA_TABLE_NAME;

@Entity
@Table(name = NOTIFICATION_SCHEMA_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class NotificationSchema extends BaseSchema<NotificationSchemaDto> implements Serializable {

    private static final long serialVersionUID = 6585856417466958172L;

    private NotificationTypeDto type;

    public NotificationSchema() {
    }

    public NotificationSchema(Long id) {
        this.id = id;
    }

    public NotificationSchema(NotificationSchemaDto dto) {
        super(dto);
        if (dto != null) {
            this.type = dto.getType();
        }
    }

    @Override
    protected NotificationSchemaDto createDto() {
        return new NotificationSchemaDto();
    }

    @Override
    protected GenericModel<NotificationSchemaDto> newInstance(Long id) {
        return new NotificationSchema(id);
    }

    @Override
    public NotificationSchemaDto toDto() {
        NotificationSchemaDto dto = super.toDto();
        dto.setType(type);
        return dto;
    }

    public NotificationTypeDto getType() {
        return type;
    }

    public void setType(NotificationTypeDto type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NotificationSchema that = (NotificationSchema) o;

        if (type != that.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public int incrementVersion() {
        return ++version;
    }
}
