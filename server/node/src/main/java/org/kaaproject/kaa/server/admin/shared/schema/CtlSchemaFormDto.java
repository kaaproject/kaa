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
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

public class CtlSchemaFormDto extends CTLSchemaDto {

    private static final long serialVersionUID = -5383847121615543863L;
    
    private RecordField schema;
    private boolean hasDependencies;
    
    public CtlSchemaFormDto() {
        super();
    }

    public CtlSchemaFormDto(CTLSchemaDto ctlSchema) {
        super();
        setId(ctlSchema.getId());            
        setMetaInfo(ctlSchema.getMetaInfo());
        setVersion(ctlSchema.getVersion());
        setCreatedTime(ctlSchema.getCreatedTime());
        setCreatedUsername(ctlSchema.getCreatedUsername());
        setHasDependencies(ctlSchema.getDependencySet() != null && !ctlSchema.getDependencySet().isEmpty());
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

}
