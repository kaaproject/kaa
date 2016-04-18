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

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.HasId;

public abstract class BaseSchemaViewDto<T extends BaseSchemaDto> implements Serializable, HasId {

    private static final long serialVersionUID = -7376241037543912928L;
    
    private T schema;
    private CtlSchemaFormDto ctlSchemaForm;
    private CtlSchemaReferenceDto existingMetaInfo;
    private boolean useExistingCtlSchema = false;
    
    public BaseSchemaViewDto() {
        this(null, null);
    }
    
    public BaseSchemaViewDto(T schema) {
        this(schema, null);
    }

    public BaseSchemaViewDto(T schema, CtlSchemaFormDto ctlSchemaForm) {
        super();
        this.schema = schema != null ? schema : createEmptySchema();
        this.ctlSchemaForm = ctlSchemaForm;
    }

    public CtlSchemaFormDto getCtlSchemaForm() {
        return ctlSchemaForm;
    }

    public void setCtlSchemaForm(CtlSchemaFormDto ctlSchemaForm) {
        this.ctlSchemaForm = ctlSchemaForm;
    }

    public T getSchema() {
        return schema;
    }

    public void setSchema(T schema) {
        this.schema = schema;
    }
    
    public void setApplicationId(String applicationId) {
        schema.setApplicationId(applicationId);
    }

    @Override
    public String getId() {
        return schema.getId();
    }

    @Override
    public void setId(String id) {
        schema.setId(id);
    }
    
    public CtlSchemaReferenceDto getExistingMetaInfo() {
        return existingMetaInfo;
    }

    public void setExistingMetaInfo(CtlSchemaReferenceDto existingMetaInfo) {
        this.existingMetaInfo = existingMetaInfo;
    }
    
    public boolean useExistingCtlSchema() {
        return useExistingCtlSchema;
    }

    public void setUseExistingCtlSchema(boolean useExistingCtlSchema) {
        this.useExistingCtlSchema = useExistingCtlSchema;
    }

    protected abstract T createEmptySchema();

}
