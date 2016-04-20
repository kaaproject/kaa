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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.SERVER_PROFILE_SCHEMA_TABLE_NAME;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;

@Entity
@Table(name = SERVER_PROFILE_SCHEMA_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class ServerProfileSchema extends BaseSchema<ServerProfileSchemaDto> implements Serializable {

    private static final long serialVersionUID = -5685244135453079314L;

    public ServerProfileSchema() {
    }

    public ServerProfileSchema(Long id) {
        setId(id);
    }

    public ServerProfileSchema(ServerProfileSchemaDto dto) {
        super(dto);
    }

    @Override
    protected ServerProfileSchemaDto createDto() {
        return new ServerProfileSchemaDto();
    }

    @Override
    protected GenericModel<ServerProfileSchemaDto> newInstance(Long id) {
        return new ServerProfileSchema(id);
    }
}
