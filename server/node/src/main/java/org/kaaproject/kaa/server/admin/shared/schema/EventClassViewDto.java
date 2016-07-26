package org.kaaproject.kaa.server.admin.shared.schema;


import org.kaaproject.kaa.common.dto.event.EventClassDto;

public class EventClassViewDto extends BaseSchemaViewDto<EventClassDto>{

    private static final long serialVersionUID = -5111268279407697111L;

    public EventClassViewDto() {
        super();
    }

    public EventClassViewDto(EventClassDto schema, CtlSchemaFormDto ctlSchemaForm) {
        super(schema, ctlSchemaForm);
    }

    @Override
    protected EventClassDto createEmptySchema() {
        return new EventClassDto();
    }
}
