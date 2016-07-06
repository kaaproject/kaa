package org.kaaproject.kaa.server.admin.shared.schema;


import org.kaaproject.kaa.common.dto.NotificationSchemaDto;

public class NotificationSchemaViewDto extends BaseSchemaViewDto<NotificationSchemaDto>{

    private static final long serialVersionUID = -5289268279407697111L;

    public NotificationSchemaViewDto() {
        super();
    }

    public NotificationSchemaViewDto(NotificationSchemaDto schema,
                                CtlSchemaFormDto ctlSchemaForm) {
        super(schema, ctlSchemaForm);
    }

    @Override
    protected NotificationSchemaDto createEmptySchema() {
        return new NotificationSchemaDto();
    }
}
