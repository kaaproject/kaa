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

package org.kaaproject.kaa.server.common.dao.model.sql;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.ConfigurationDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_CONFIGURATION_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_CONFIGURATION_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_CONFIGURATION_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CONFIGURATION_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.binaryToString;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.stringToBinary;

@Entity
@Table(name = CONFIGURATION_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class Configuration extends AbstractStructure<ConfigurationDto> implements Serializable {

    private static final long serialVersionUID = -216908432141461265L;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Column(name = CONFIGURATION_CONFIGURATION_SCHEMA_VERSION)
    protected int schemaVersion;

    @Lob
    @Column(name = CONFIGURATION_CONFIGURATION_BODY)
    private byte[] configurationBody;

    @ManyToOne
    @JoinColumn(name = CONFIGURATION_CONFIGURATION_SCHEMA_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ConfigurationSchema configurationSchema;

    public Configuration() {
    }

    public Configuration(Long id) {
        this.id = id;
    }

    public Configuration(ConfigurationDto dto) {
        super(dto);
        if (dto != null) {
            Long schemaId = getLongId(dto.getSchemaId());
            this.configurationSchema = schemaId != null ? new ConfigurationSchema(schemaId) : null;
            this.configurationBody = stringToBinary(dto.getBody());
            this.schemaVersion = dto.getSchemaVersion();
        }
    }

    public byte[] getConfigurationBody() {
        return configurationBody;
    }

    public void setConfigurationBody(byte[] configurationBody) {
        this.configurationBody = configurationBody;
    }

    public ConfigurationSchema getConfigurationSchema() {
        return configurationSchema;
    }

    public void setConfigurationSchema(ConfigurationSchema configurationSchema) {
        this.configurationSchema = configurationSchema;
    }

    public String getSchemaId() {
        return configurationSchema != null ? configurationSchema.getStringId() : null;
    }

    public String getApplicationId() {
        return application != null ? application.getStringId() : null;
    }

    public String getEndpointGroupId() {
        return endpointGroup != null ? endpointGroup.getStringId() : null;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + super.hashCode();
        result = prime * result + Arrays.hashCode(configurationBody);
        result = prime * result + ((configurationSchema == null) ? 0 : configurationSchema.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Configuration other = (Configuration) obj;
        if (!Arrays.equals(configurationBody, other.configurationBody)) {
            return false;
        }
        if (configurationSchema == null) {
            if (other.configurationSchema != null) {
                return false;
            }
        } else if (!configurationSchema.equals(other.configurationSchema)) {
            return false;
        }
        return true;
    }

    @Override
    public ConfigurationDto toDto() {
        ConfigurationDto dto = super.toDto();
        dto.setBody(binaryToString(configurationBody));
        dto.setSchemaId(ModelUtils.getStringId(configurationSchema.getId()));
        dto.setSchemaVersion(schemaVersion);
        dto.setProtocolSchema(configurationSchema != null ? configurationSchema.getProtocolSchema() : null);
        return dto;
    }

    @Override
    protected ConfigurationDto createDto() {
        return new ConfigurationDto();
    }

    @Override
    protected GenericModel<ConfigurationDto> newInstance(Long id) {
        return new Configuration(id);
    }

    @Override
    public String getBody() {
        if (configurationBody != null) {
            return new String(configurationBody, UTF8);
        }
        return null;
    }

    @Override
    public void setBody(String body) {
        if (body != null) {
            configurationBody = body.getBytes(UTF8);
        } else {
            configurationBody = null;
        }
    }

    @Override
    public String toString() {
        return "Configuration [sequenceNumber=" + sequenceNumber + ", schemaVersion="
                + schemaVersion + ", description=" + description + ", createdTime=" + createdTime + ", lastModifyTime="
                + lastModifyTime + ", activatedTime=" + activatedTime + ", deactivatedTime=" + deactivatedTime + ", createdUsername=" + createdUsername
                + ", modifiedUsername=" + modifiedUsername + ", activatedUsername=" + activatedUsername + ", deactivatedUsername=" + deactivatedUsername
                + ", endpointCount=" + endpointCount + ", status=" + status + ", id=" + id + ", version=" + getVersion() + "]";
    }

}
