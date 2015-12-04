/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.shared.schema;

import java.util.List;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;

public class CtlSchemaFormDto extends SchemaFqnDto {

    private static final long serialVersionUID = -5383847121615543863L;
    
    private String ctlSchemaId;
    private Integer version;
    private CTLSchemaScopeDto scope;
    private String applicationId;
    private RecordField schema;
    private boolean hasDependencies;
    
    private String description;
    private String createdUsername;
    private long createdTime;
    
    private List<Integer> availableVersions;
    
    public CtlSchemaFormDto() {
        super();
    }
    
    public CtlSchemaFormDto(String fqnString) {
        super(fqnString);
    }
    
    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public CTLSchemaScopeDto getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScopeDto scope) {
        this.scope = scope;
    }
    
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public RecordField getSchema() {
        return schema;
    }

    public void setSchema(RecordField schema) {
        this.schema = schema;
    }
    
    public boolean hasDependencies() {
        return hasDependencies;
    }

    public void setHasDependencies(boolean hasDependencies) {
        this.hasDependencies = hasDependencies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUsername() {
        return createdUsername;
    }

    public void setCreatedUsername(String createdUsername) {
        this.createdUsername = createdUsername;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public List<Integer> getAvailableVersions() {
        return availableVersions;
    }

    public void setAvailableVersions(List<Integer> availableVersions) {
        this.availableVersions = availableVersions;
    }

}
