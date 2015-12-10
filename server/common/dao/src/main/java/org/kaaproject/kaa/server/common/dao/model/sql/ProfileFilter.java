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
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_SCHEMA_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_FILTER_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getGenericModelIdds;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getGenericModelVersions;

@Entity
@Table(name = PROFILE_FILTER_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public final class ProfileFilter extends AbstractStructure<ProfileFilterDto> implements Serializable {

    private static final long serialVersionUID = 8815798602241305612L;

    @Column(name = PROFILE_FILTER_BODY)
    private String body;

    @ManyToMany
    @JoinColumn(name = PROFILE_FILTER_SCHEMA_ID, nullable = false)
    private Set<ProfileSchema> profileSchemas = new HashSet<>();
    @ManyToMany
    @JoinColumn(name = PROFILE_FILTER_SCHEMA_ID, nullable = false)
    private Set<ServerProfileSchema> serverProfileSchemas = new HashSet<>();

    public ProfileFilter() {
    }

    public ProfileFilter(Long id) {
        super(id);
    }

    public ProfileFilter(ProfileFilterDto dto) {
        super(dto);
        this.body = dto.getBody();
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

    @Override
    public ProfileFilterDto toDto() {
        ProfileFilterDto filterDto = super.toDto();
        filterDto.setBody(body);
        filterDto.setEndpointSchemaVersions(getGenericModelVersions(profileSchemas));
        filterDto.setServerSchemaVersions(getGenericModelVersions(serverProfileSchemas));
        return filterDto;
    }

    public String getGroupId() {
        return endpointGroup.getStringId();
    }


}
