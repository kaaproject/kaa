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

package org.kaaproject.kaa.common.dto.event;

import java.io.Serializable;

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.HasId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"schemaForm"})
public class EventSchemaVersionDto implements HasId, Serializable {

    private static final long serialVersionUID = -6565622945148633465L;

    private String id;
    private String schema;
    protected RecordField schemaForm;
    private int version;
    private String createdUsername;
    private long createdTime;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getSchema() {
        return schema;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public RecordField getSchemaForm() {
        return schemaForm;
    }

    public void setSchemaForm(RecordField schemaForm) {
        this.schemaForm = schemaForm;
    }

    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + version;
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
        EventSchemaVersionDto other = (EventSchemaVersionDto) obj;
        if (createdTime != other.createdTime) {
            return false;
        }
        if (createdUsername == null) {
            if (other.createdUsername != null) {
                return false;
            }
        } else if (!createdUsername.equals(other.createdUsername)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        return version == other.version;
    }

    @Override
    public String toString() {
        return "EventSchemaVersionDto [schema=" + schema + ", version="
                + version + ", createdUsername=" + createdUsername
                + ", createdTime=" + createdTime + "]";
    }
    
}
