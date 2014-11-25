/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.avro.ui.shared;

import java.io.Serializable;

public abstract class FormField implements Serializable, Cloneable {

    private static final long serialVersionUID = 6978997793895098628L;
    
    private String fieldName;
    private String displayName;
    private boolean optional;
    private float weight = 1f;
    
    public FormField() {
    }
    
    public FormField(String fieldName, 
            String displayName, 
            boolean optional) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.optional = optional;
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    
    public void setWeight(float weight) {
        this.weight = weight;
    }
    
    public float getWeight() {
        return weight;
    }

    public abstract FieldType getFieldType();
    
    public abstract boolean isNull();
    
    public boolean isValid() {
        if (optional) {
            return true;
        }
        else {
            return valid();
        }
    }
    
    protected abstract boolean valid();
    
    public FormField clone() {
        FormField cloned = createInstance();
        copyFields(cloned);
        return cloned;
    }

    protected abstract FormField createInstance();

    protected void copyFields (FormField cloned) {
        cloned.fieldName = fieldName;
        cloned.displayName = displayName;
        cloned.optional = optional;
        cloned.weight = weight;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FormField other = (FormField) obj;
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        return true;
    }


}
