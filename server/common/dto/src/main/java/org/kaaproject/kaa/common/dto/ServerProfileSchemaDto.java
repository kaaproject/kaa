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

import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

import java.io.Serializable;

public class ServerProfileSchemaDto implements HasId, Serializable {

    private String id;
    private Long createdTime;
    private String applicationId;
    private CTLSchemaDto schemaDto;
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

    public CTLSchemaDto getSchemaDto() {
        return schemaDto;
    }

    public void setSchemaDto(CTLSchemaDto schemaDto) {
        this.schemaDto = schemaDto;
    }

    public RecordField getSchemaForm() {
        return schemaForm;
    }

    public void setSchemaForm(RecordField schemaForm) {
        this.schemaForm = schemaForm;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        ServerProfileSchemaDto that = (ServerProfileSchemaDto) o;
//
//        if (id != null ? !id.equals(that.id) : that.id != null) return false;
//        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) return false;
//        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
//            return false;
//        return schemaDto != null ? schemaDto.equals(that.schemaDto) : that.schemaDto == null;
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = id != null ? id.hashCode() : 0;
//        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
//        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
//        result = 31 * result + (schemaDto != null ? schemaDto.hashCode() : 0);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "ServerProfileSchemaDto{" +
//                "id='" + id + '\'' +
//                ", createdTime=" + createdTime +
//                ", applicationId='" + applicationId + '\'' +
//                ", schemaDto=" + schemaDto +
//                '}';
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerProfileSchemaDto that = (ServerProfileSchemaDto) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        if (schemaDto != null ? !schemaDto.equals(that.schemaDto) : that.schemaDto != null) return false;
        return !(schemaForm != null ? !schemaForm.equals(that.schemaForm) : that.schemaForm != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (schemaDto != null ? schemaDto.hashCode() : 0);
        result = 31 * result + (schemaForm != null ? schemaForm.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerProfileSchemaDto{" +
                "id='" + id + '\'' +
                ", createdTime=" + createdTime +
                ", applicationId='" + applicationId + '\'' +
                ", schemaDto=" + schemaDto +
                ", schemaForm=" + schemaForm +
                '}';
    }
}
