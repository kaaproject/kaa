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

package org.kaaproject.kaa.server.admin.shared.schema;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.VersionDto;

public class SchemaInfoDto extends VersionDto {

    private static final long serialVersionUID = -8792655520011059405L;
    
    private String schemaName;
    private RecordField schemaForm;

    public SchemaInfoDto() {
        super();
    }
    
    public SchemaInfoDto(VersionDto versionDto) {
        this.id = versionDto.getId();
        this.version = versionDto.getVersion();
    }
    
    public RecordField getSchemaForm() {
        return schemaForm;
    }

    public void setSchemaForm(RecordField schemaForm) {
        this.schemaForm = schemaForm;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
 
}
