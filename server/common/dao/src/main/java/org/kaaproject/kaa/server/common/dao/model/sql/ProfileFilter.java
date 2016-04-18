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
import org.kaaproject.kaa.common.dto.ProfileFilterDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_ENDPOINT_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_SERVER_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = PROFILE_FILTER_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class ProfileFilter extends AbstractStructure<ProfileFilterDto> implements Serializable {

    private static final long serialVersionUID = 8815798602241305612L;

    @Column(name = PROFILE_FILTER_BODY)
    private String body;

    @ManyToOne
    @JoinColumn(name = PROFILE_FILTER_ENDPOINT_SCHEMA_ID)
    private EndpointProfileSchema endpointProfileSchema;
    @ManyToOne
    @JoinColumn(name = PROFILE_FILTER_SERVER_SCHEMA_ID)
    private ServerProfileSchema serverProfileSchema;

    public ProfileFilter() {
    }

    public ProfileFilter(Long id) {
        super(id);
    }

    public ProfileFilter(ProfileFilterDto dto) {
        super(dto);
        this.body = dto.getBody();
        String endpointSchemaId = dto.getEndpointProfileSchemaId();
        if (isNotBlank(endpointSchemaId)) {
            this.endpointProfileSchema = new EndpointProfileSchema(getLongId(endpointSchemaId));
        }
        String serverSchemaId = dto.getServerProfileSchemaId();
        if (isNotBlank(serverSchemaId)) {
            this.serverProfileSchema = new ServerProfileSchema(getLongId(serverSchemaId));
        }
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    protected ProfileFilterDto createDto() {
        return new ProfileFilterDto();
    }

    @Override
    protected GenericModel<ProfileFilterDto> newInstance(Long id) {
        return new ProfileFilter(id);
    }

    public EndpointProfileSchema getEndpointProfileSchema() {
        return endpointProfileSchema;
    }

    public void setEndpointProfileSchema(EndpointProfileSchema endpointProfileSchema) {
        this.endpointProfileSchema = endpointProfileSchema;
    }

    public ServerProfileSchema getServerProfileSchema() {
        return serverProfileSchema;
    }

    public void setServerProfileSchema(ServerProfileSchema serverProfileSchema) {
        this.serverProfileSchema = serverProfileSchema;
    }

    public String getGroupId() {
        return endpointGroup.getStringId();
    }

    public Integer getEndpointProfileSchemaVersion() {
        Integer version = null;
        if (endpointProfileSchema != null) {
            version = endpointProfileSchema.getVersion();
        }
        return version;
    }

    public Integer getServerProfileSchemaVersion() {
        Integer version = null;
        if (serverProfileSchema != null) {
            version = serverProfileSchema.getVersion();
        }
        return version;
    }

    public String getEndpointProfileSchemaId() {
        String id = null;
        if (endpointProfileSchema != null) {
            id = endpointProfileSchema.getStringId();
        }
        return id;
    }

    public String getServerProfileSchemaId() {
        String id = null;
        if (serverProfileSchema != null) {
            id = serverProfileSchema.getStringId();
        }
        return id;
    }

    @Override
    public ProfileFilterDto toDto() {
        ProfileFilterDto filterDto = super.toDto();
        filterDto.setBody(body);
        if (endpointProfileSchema != null) {
            filterDto.setEndpointProfileSchemaId(endpointProfileSchema.getStringId());
            filterDto.setEndpointProfileSchemaVersion(endpointProfileSchema.getVersion());
        }
        if (serverProfileSchema != null) {
            filterDto.setServerProfileSchemaId(serverProfileSchema.getStringId());
            filterDto.setServerProfileSchemaVersion(serverProfileSchema.getVersion());
        }
        return filterDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ProfileFilter that = (ProfileFilter) o;

        if (body != null ? !body.equals(that.body) : that.body != null) {
            return false;
        }
        if (endpointProfileSchema != null ? !endpointProfileSchema.equals(that.endpointProfileSchema) : that.endpointProfileSchema != null) {
            return false;
        }
        return serverProfileSchema != null ? serverProfileSchema.equals(that.serverProfileSchema) : that.serverProfileSchema == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (endpointProfileSchema != null ? endpointProfileSchema.hashCode() : 0);
        result = 31 * result + (serverProfileSchema != null ? serverProfileSchema.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProfileFilter{" +
                "body='" + body + '\'' +
                ", endpointProfileSchema=" + endpointProfileSchema +
                ", serverProfileSchema=" + serverProfileSchema +
                "} " + super.toString();
    }
}
