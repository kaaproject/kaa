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
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.PROFILE_SCHEMA_TABLE_NAME;

@Entity
@Table(name = PROFILE_SCHEMA_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class EndpointProfileSchema extends BaseSchema<EndpointProfileSchemaDto> implements Serializable {

    private static final long serialVersionUID = 953188575107921799L;

    public EndpointProfileSchema() {
    }

    public EndpointProfileSchema(Long id) {
        setId(id);
    }

    public EndpointProfileSchema(EndpointProfileSchemaDto dto) {
        super(dto);
    }

    @Override
    protected EndpointProfileSchemaDto createDto() {
        return new EndpointProfileSchemaDto();
    }

    @Override
    protected GenericModel<EndpointProfileSchemaDto> newInstance(Long id) {
        return new EndpointProfileSchema(id);
    }
}
