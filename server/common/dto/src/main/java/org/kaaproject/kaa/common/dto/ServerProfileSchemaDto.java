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

package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

import org.kaaproject.avro.ui.shared.RecordField;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "schemaForm" })
public class ServerProfileSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = -4059563981228353624L;
    private String id;
    private Long createdTime;
    //TODO: save this field.
    private String createdUser;
    private String applicationId;
    private String ctlSchemaId;
    private RecordField schemaForm;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCtlSchemaId() {
        return ctlSchemaId;
    }

    public void setCtlSchemaId(String ctlSchemaId) {
        this.ctlSchemaId = ctlSchemaId;
    }

    public RecordField getSchemaForm() {
        return schemaForm;
    }

    public void setSchemaForm(RecordField schemaForm) {
        this.schemaForm = schemaForm;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((applicationId == null) ? 0 : applicationId.hashCode());
        result = prime * result + ((ctlSchemaId == null) ? 0 : ctlSchemaId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerProfileSchemaDto other = (ServerProfileSchemaDto) obj;
        if (applicationId == null) {
            if (other.applicationId != null)
                return false;
        } else if (!applicationId.equals(other.applicationId))
            return false;
        if (ctlSchemaId == null) {
            if (other.ctlSchemaId != null)
                return false;
        } else if (!ctlSchemaId.equals(other.ctlSchemaId))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ServerProfileSchemaDto [id=" + id + ", createdTime=" + createdTime + ", createdUser=" + createdUser + ", applicationId="
                + applicationId + ", ctlSchemaId=" + ctlSchemaId + ", schemaForm=" + schemaForm + "]";
    }

}
