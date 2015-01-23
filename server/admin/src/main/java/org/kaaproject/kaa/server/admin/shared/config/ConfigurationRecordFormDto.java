package org.kaaproject.kaa.server.admin.shared.config;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ConfigurationDto;

public class ConfigurationRecordFormDto extends ConfigurationDto {

    private static final long serialVersionUID = -1934826391482071313L;
    
    private RecordField configurationRecord;
    
    public ConfigurationRecordFormDto() {
        super();
    }
    
    public ConfigurationRecordFormDto(ConfigurationDto configuration) {
        super(configuration);
    }

    public RecordField getConfigurationRecord() {
        return configurationRecord;
    }

    public void setConfigurationRecord(RecordField configurationRecord) {
        this.configurationRecord = configurationRecord;
    }

}
