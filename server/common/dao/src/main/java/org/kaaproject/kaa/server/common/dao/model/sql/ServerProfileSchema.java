/*
 * Copyright 2015 CyberVision, Inc.
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

import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_APP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_FK_APP_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_FK_CTL_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_TABLE_NAME;

@Entity
@Table(name = SERVER_PROFILE_SCHEMA_TABLE_NAME)
public class ServerProfileSchema extends GenericModel<ServerProfileSchemaDto> implements Serializable {

    private static final long serialVersionUID = -1449864115582350072L;

    @Column(name = SERVER_PROFILE_SCHEMA_CREATED_TIME)
    private Long createdTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SERVER_PROFILE_SCHEMA_APP_ID, foreignKey = @ForeignKey(name = SERVER_PROFILE_SCHEMA_FK_APP_ID))
    private Application application;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SERVER_PROFILE_SCHEMA_CTL_SCHEMA_ID, foreignKey = @ForeignKey(name = SERVER_PROFILE_SCHEMA_FK_CTL_SCHEMA_ID))
    private CTLSchema ctlSchema;

    public ServerProfileSchema() {
    }

    public ServerProfileSchema(ServerProfileSchemaDto dto) {
        this.id = ModelUtils.getLongId(dto);
        this.createdTime = dto.getCreatedTime();
        String appId = dto.getApplicationId();
        if (!isBlank(appId)) {
            this.application = new Application(ModelUtils.getLongId(appId));
        }
        CTLSchemaDto ctlSchemaDto = dto.getSchemaDto();
        if (ctlSchemaDto != null) {
            this.ctlSchema = new CTLSchema(ctlSchemaDto);
        }
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
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

    @Override
    protected ServerProfileSchemaDto createDto() {
        return new ServerProfileSchemaDto();
    }

    @Override
    public ServerProfileSchemaDto toDto() {
        ServerProfileSchemaDto dto = new ServerProfileSchemaDto();
        dto.setId(getStringId());
        dto.setApplicationId(ModelUtils.getStringId(application));
        dto.setCreatedTime(createdTime);
        dto.setSchemaDto(DaoUtil.getDto(ctlSchema));
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerProfileSchema that = (ServerProfileSchema) o;

        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) return false;
        if (application != null ? !application.equals(that.application) : that.application != null) return false;
        return ctlSchema != null ? ctlSchema.equals(that.ctlSchema) : that.ctlSchema == null;

    }

    @Override
    public int hashCode() {
        int result = createdTime != null ? createdTime.hashCode() : 0;
        result = 31 * result + (application != null ? application.hashCode() : 0);
        result = 31 * result + (ctlSchema != null ? ctlSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerProfileSchema{" +
                "createdTime=" + createdTime +
                ", application=" + application +
                ", ctlSchema=" + ctlSchema +
                '}';
    }
}
