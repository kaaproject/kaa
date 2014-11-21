package org.kaaproject.kaa.server.admin.shared.logs;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.HasId;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderStatusDto;
import org.kaaproject.kaa.common.dto.logs.LogAppenderTypeDto;
import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.server.admin.shared.form.RecordField;

public class LogAppenderFormWrapper implements HasId, Serializable {

    private static final long serialVersionUID = 1L;
    
    private LogAppenderDto logAppender;
    
    private RecordField configuration;
    
    public LogAppenderFormWrapper() {
        this.logAppender = new LogAppenderDto();
    }
    
    public LogAppenderFormWrapper(LogAppenderDto logAppender) {
        this.logAppender = logAppender;
    }
    
    public LogAppenderDto getLogAppender() {
        return logAppender;
    }

    public RecordField getConfiguration() {
        return configuration;
    }

    public void setConfiguration(RecordField configuration) {
        this.configuration = configuration;
    }

    public String getName() {
        return logAppender.getName();
    }

    public void setName(String name) {
        logAppender.setName(name);
    }

    public String getDescription() {
        return logAppender.getDescription();
    }

    public void setDescription(String description) {
        logAppender.setDescription(description);
    }

    public String getCreatedUsername() {
        return logAppender.getCreatedUsername();
    }

    public void setCreatedUsername(String createdUsername) {
        logAppender.setCreatedUsername(createdUsername);
    }

    public long getCreatedTime() {
        return logAppender.getCreatedTime();
    }

    @Override
    public String getId() {
        return logAppender.getId();
    }

    public void setCreatedTime(long createdTime) {
        logAppender.setCreatedTime(createdTime);
    }

    @Override
    public void setId(String id) {
        logAppender.setId(id);
    }

    public String getApplicationId() {
        return logAppender.getApplicationId();
    }

    public void setApplicationId(String applicationId) {
        logAppender.setApplicationId(applicationId);
    }

    public String getApplicationToken() {
        return logAppender.getApplicationToken();
    }

    public void setApplicationToken(String applicationToken) {
        logAppender.setApplicationToken(applicationToken);
    }

    public String getTenantId() {
        return logAppender.getTenantId();
    }

    public void setTenantId(String tenantId) {
        logAppender.setTenantId(tenantId);
    }

    public SchemaDto getSchema() {
        return logAppender.getSchema();
    }

    public void setSchema(SchemaDto schema) {
        logAppender.setSchema(schema);
    }

    public LogAppenderStatusDto getStatus() {
        return logAppender.getStatus();
    }

    public void setStatus(LogAppenderStatusDto status) {
        logAppender.setStatus(status);
    }

    public LogAppenderTypeDto getType() {
        return logAppender.getType();
    }

    public void setType(LogAppenderTypeDto type) {
        logAppender.setType(type);
    }

    public LogAppenderParametersDto getProperties() {
        return logAppender.getProperties();
    }

    public void setProperties(LogAppenderParametersDto properties) {
        logAppender.setProperties(properties);
    }

    public List<LogHeaderStructureDto> getHeaderStructure() {
        return logAppender.getHeaderStructure();
    }

    public void setHeaderStructure(List<LogHeaderStructureDto> headerStructure) {
        logAppender.setHeaderStructure(headerStructure);
    }

    public String getSchemaVersion() {
        return logAppender.getSchemaVersion();
    }

    @Override
    public int hashCode() {
        return logAppender.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return logAppender.equals(obj);
    }

    @Override
    public String toString() {
        return logAppender.toString();
    }
    
    

}
