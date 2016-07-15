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

/*
``` * Copyright 2014 CyberVision, Inc.
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
import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

import javax.persistence.Entity;
import javax.persistence.Table;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.LOG_SCHEMA_TABLE_NAME;

@Entity
@Table(name = LOG_SCHEMA_TABLE_NAME)
@OnDelete(action = OnDeleteAction.CASCADE)
public class LogSchema extends BaseSchema<LogSchemaDto> {

    private static final long serialVersionUID = 5801830095239766386L;

    public LogSchema() {
    }

    public LogSchema(Long id) {
        this.id = id;
    }

    public LogSchema(LogSchemaDto dto) {
        super(dto);
    }

    @Override
    public String toString() {
        return "LogSchema{" + super.toString()
                + '}';
    }

    @Override
    protected LogSchemaDto createDto() {
        return new LogSchemaDto();
    }

    @Override
    protected GenericModel<LogSchemaDto> newInstance(Long id) {
        return new LogSchema(id);
    }

}
