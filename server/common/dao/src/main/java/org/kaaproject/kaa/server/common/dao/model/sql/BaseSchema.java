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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_CTL_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_FK_APP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_FK_CTL_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.BASE_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.BaseSchemaDto;
import org.kaaproject.kaa.common.dto.VersionDto;

@Entity
@Table(name = BASE_SCHEMA_TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseSchema<T extends BaseSchemaDto> extends GenericModel<T> implements HasVersion {

    private static final long serialVersionUID = 2866125011338808891L;

    @Column(name = BASE_SCHEMA_VERSION)
    protected int version;
 
    @Column(name = BASE_SCHEMA_NAME)
    protected String name;

    @Column(name = BASE_SCHEMA_DESCRIPTION)
    protected String description;

    @Column(name = BASE_SCHEMA_CREATED_USERNAME)
    protected String createdUsername;

    @Column(name = BASE_SCHEMA_CREATED_TIME)
    protected long createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = BASE_SCHEMA_APPLICATION_ID, nullable=false, foreignKey = @ForeignKey(name = BASE_SCHEMA_FK_APP_ID))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = BASE_SCHEMA_CTL_SCHEMA_ID, nullable=false, foreignKey = @ForeignKey(name = BASE_SCHEMA_FK_CTL_SCHEMA_ID))
    private CTLSchema ctlSchema;

    public BaseSchema() {
    }

    public BaseSchema(Long id) {
        this.id = id;
    }

    public BaseSchema(T dto) {
        if (dto != null) {
            this.id = getLongId(dto);
            this.version = dto.getVersion();
            Long appId = getLongId(dto.getApplicationId());
            this.application = appId != null ? new Application(appId) : null;
            Long ctlSchemaId = getLongId(dto.getCtlSchemaId());
            this.ctlSchema = ctlSchemaId != null ? new CTLSchema(ctlSchemaId) : null;
            this.name = dto.getName();
            this.description = dto.getDescription();
            this.createdUsername = dto.getCreatedUsername();
            this.createdTime = dto.getCreatedTime();
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
    
    public CTLSchema getCtlSchema() {
        return ctlSchema;
    }

    public void setCtlSchema(CTLSchema ctlSchema) {
        this.ctlSchema = ctlSchema;
    }

    public String getApplicationId() {
        Long id = null;
        if (application != null) {
            id = application.getId();
        }
        return id != null ? String.valueOf(id) : null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((application == null) ? 0 : application.hashCode());
        result = prime * result + (int) (createdTime ^ (createdTime >>> 32));
        result = prime * result
                + ((createdUsername == null) ? 0 : createdUsername.hashCode());
        result = prime * result
                + ((ctlSchema == null) ? 0 : ctlSchema.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
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
        BaseSchema<T> other = (BaseSchema<T>) obj;
        if (application == null) {
            if (other.application != null) {
                return false;
            }
        } else if (!application.equals(other.application)) {
            return false;
        }
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
        if (ctlSchema == null) {
            if (other.ctlSchema != null) {
                return false;
            }
        } else if (!ctlSchema.equals(other.ctlSchema)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public T toDto() {
        T dto = createDto();
        dto.setId(getStringId());
        if (application != null) {
            dto.setApplicationId(application.getStringId());
        }
        if (ctlSchema != null) {
            dto.setCtlSchemaId(ctlSchema.getStringId());
        }
        dto.setVersion(version);
        dto.setName(name);
        dto.setDescription(description);
        dto.setCreatedUsername(createdUsername);
        dto.setCreatedTime(createdTime);
        return dto;
    }

    public VersionDto toVersionDto() {
        VersionDto dto = new VersionDto();
        dto.setId(getStringId());
        dto.setVersion(version);
        return dto;
    }
}
