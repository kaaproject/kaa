package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.model.sql.ModelConstants.NOTIFICATION_SCHEMA_TABLE_NAME;

@Entity
@Table(name = NOTIFICATION_SCHEMA_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class NotificationSchema extends Schema<NotificationSchemaDto> implements Serializable {

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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NotificationSchema that = (NotificationSchema) o;

        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public int incrementVersion() {
        return ++majorVersion;
    }
}
