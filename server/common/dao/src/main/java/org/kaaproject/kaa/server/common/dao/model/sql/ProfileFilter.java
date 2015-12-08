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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = PROFILE_FILTER_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public final class ProfileFilter extends AbstractStructure<ProfileFilterDto> implements Serializable {

    private static final long serialVersionUID = 8815798602241305612L;

    @Column(name = PROFILE_FILTER_BODY)
    private String body;

    @ManyToOne
    @JoinColumn(name = PROFILE_FILTER_SCHEMA_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProfileSchema profileSchema;

    public ProfileFilter() {
    }

    public ProfileFilter(Long id) {
        super(id);
    }

    public ProfileFilter(ProfileFilterDto dto) {
        super(dto);
        this.body = dto.getBody();
        Long schemaId = getLongId(dto.getSchemaId());
        this.profileSchema = schemaId != null ? new ProfileSchema(schemaId) : null;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    public ProfileSchema getProfileSchema() {
        return profileSchema;
    }

    public void setProfileSchema(ProfileSchema profileSchema) {
        this.profileSchema = profileSchema;
    }

    @Override
    protected ProfileFilterDto createDto() {
        return new ProfileFilterDto();
    }

    @Override
    public ProfileFilterDto toDto() {
        ProfileFilterDto filterDto = super.toDto();
        filterDto.setBody(body);
        filterDto.setSchemaId(profileSchema.getStringId());
        return filterDto;
    }

    public String getSchemaId() {
        return profileSchema.getStringId();
    }

    public String getGroupId() {
        return endpointGroup.getStringId();
    }

    @Override
    public int hashCode() {
        final int prime = 43;
        int result = 1;
        result = prime * result + super.hashCode();
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((profileSchema == null) ? 0 : profileSchema.hashCode());
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
        ProfileFilter other = (ProfileFilter) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (profileSchema == null) {
            if (other.profileSchema != null) {
                return false;
            }
        } else if (!profileSchema.equals(other.profileSchema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ProfileFilter [body=" + body + ", sequenceNumber=" + sequenceNumber + ", schemaVersion=" + schemaVersion 
                + ", description=" + description + ", createdTime=" + createdTime + ", lastModifyTime=" + lastModifyTime + ", activatedTime=" + activatedTime
                + ", deactivatedTime=" + deactivatedTime + ", createdUsername=" + createdUsername + ", modifiedUsername=" + modifiedUsername
                + ", activatedUsername=" + activatedUsername + ", deactivatedUsername=" + deactivatedUsername + ", endpointCount=" + endpointCount
                + ", status=" + status + ", id=" + id + "]";
    }

}
