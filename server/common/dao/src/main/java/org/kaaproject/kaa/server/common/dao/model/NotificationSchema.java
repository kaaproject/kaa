package org.kaaproject.kaa.server.common.dao.model;

import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.SchemaDto;

public interface NotificationSchema extends ToDto<NotificationSchemaDto>{

    int incrementVersion();

    int getMajorVersion();

    String getApplicationId();

    NotificationTypeDto getType();

    String getSchema();
    
    SchemaDto toSchemaDto();

}
