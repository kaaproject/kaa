package org.kaaproject.kaa.server.admin.shared.schema;


import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;

public class ConfigurationSchemaViewDto extends BaseSchemaViewDto<ConfigurationSchemaDto> {

    private static final long serialVersionUID = 8311415263866415408L;

    public ConfigurationSchemaViewDto() {
        super();
    }

    public ConfigurationSchemaViewDto(ConfigurationSchemaDto schema, CtlSchemaFormDto ctlSchemaForm) {
        super(schema, ctlSchemaForm);
    }

    @Override
    protected ConfigurationSchemaDto createEmptySchema() {
        return new ConfigurationSchemaDto();
    }
}
