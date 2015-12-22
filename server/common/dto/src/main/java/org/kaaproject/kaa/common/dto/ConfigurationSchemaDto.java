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

package org.kaaproject.kaa.common.dto;

public class ConfigurationSchemaDto extends AbstractSchemaDto {

    private static final long serialVersionUID = 7053272285029134851L;

    private String protocolSchema;
    private String baseSchema;
    private String overrideSchema;
    private UpdateStatus status;

    public String getProtocolSchema() {
        return protocolSchema;
    }

    public void setProtocolSchema(String protocolSchema) {
        this.protocolSchema = protocolSchema;
    }

    public String getBaseSchema() {
        return baseSchema;
    }

    public void setBaseSchema(String baseSchema) {
        this.baseSchema = baseSchema;
    }

    public String getOverrideSchema() {
        return overrideSchema;
    }

    public void setOverrideSchema(String overrideSchema) {
        this.overrideSchema = overrideSchema;
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateStatus status) {
        this.status = status;
    }

//    private ProtocolSchema protocolSchema;
//    private BaseSchema baseSchema;
//    private OverrideSchema overrideSchema;


//    public ProtocolSchema getProtocolSchema() {
//        return protocolSchema;
//    }
//
//    public void setProtocolSchema(String protocolSchema) {
//        this.protocolSchema = new KaaSchemaFactoryImpl().createProtocolSchema(protocolSchema);
//    }
//
//    public void setProtocolSchema(ProtocolSchema protocolSchema) {
//        this.protocolSchema = protocolSchema;
//    }



//    public BaseSchema getBaseSchema() {
//        return baseSchema;
//    }
//
//    public void setBaseSchema(String baseSchema) {
//        this.baseSchema = new KaaSchemaFactoryImpl().createBaseSchema(baseSchema);
//    }
//
//    public void setBaseSchema(BaseSchema baseSchema) {
//        this.baseSchema = baseSchema;
//    }
//
//    public OverrideSchema getOverrideSchema() {
//        return overrideSchema;
//    }
//
//    public void setOverrideSchema(String overrideSchema) {
//        this.overrideSchema = new KaaSchemaFactoryImpl().createOverrideSchema(overrideSchema);
//    }
//
//    public void setOverrideSchema(OverrideSchema overrideSchema) {
//        this.overrideSchema = overrideSchema;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigurationSchemaDto)) {
            return false;
        }

        ConfigurationSchemaDto that = (ConfigurationSchemaDto) o;

        if (version != that.version) {
            return false;
        }
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null) {
            return false;
        }
        if (baseSchema != null ? !baseSchema.equals(that.baseSchema) : that.baseSchema != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (overrideSchema != null ? !overrideSchema.equals(that.overrideSchema) : that.overrideSchema != null) {
            return false;
        }
        if (protocolSchema != null ? !protocolSchema.equals(that.protocolSchema) : that.protocolSchema != null) {
            return false;
        }
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + version;
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        result = 31 * result + (protocolSchema != null ? protocolSchema.hashCode() : 0);
        result = 31 * result + (baseSchema != null ? baseSchema.hashCode() : 0);
        result = 31 * result + (overrideSchema != null ? overrideSchema.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationSchemaDto{" +
                "id='" + id + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", version=" + version +
                ", status=" + status +
                '}';
    }
}
