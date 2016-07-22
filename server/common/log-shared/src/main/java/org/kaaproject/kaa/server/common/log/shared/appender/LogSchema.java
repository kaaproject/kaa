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

package org.kaaproject.kaa.server.common.log.shared.appender;

import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

public final class LogSchema {

    private final LogSchemaDto logSchemaDto;

    private final String schema;

    public LogSchema(LogSchemaDto logSchemaDto, String schema) {
        this.logSchemaDto = logSchemaDto;
        this.schema = schema;
    }

    public String getId() {
        return logSchemaDto.getId();
    }

    public String getApplicationId() {
        return logSchemaDto.getApplicationId();
    }

    public String getCtlSchemaId() {
        return logSchemaDto.getCtlSchemaId();
    }

    public int getVersion() {
        return logSchemaDto.getVersion();
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return "LogSchema [logSchemaDto=" + logSchemaDto + "]";
    }

}
