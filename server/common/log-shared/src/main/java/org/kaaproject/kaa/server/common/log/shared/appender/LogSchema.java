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

package org.kaaproject.kaa.server.common.log.shared.appender;

import org.kaaproject.kaa.common.dto.logs.LogSchemaDto;

public final class LogSchema {

    private final LogSchemaDto logSchemaDto;

    public LogSchema(LogSchemaDto logSchemaDto) {
        this.logSchemaDto = logSchemaDto;
    }

    public String getId() {
        return logSchemaDto.getId();
    }

    public String getApplicationId() {
        return logSchemaDto.getApplicationId();
    }

    public String getSchema() {
        return logSchemaDto.getSchema();
    }

    public int getVersion() {
        return logSchemaDto.getVersion();
    }

    @Override
    public String toString() {
        return "LogSchema [logSchemaDto=" + logSchemaDto + "]";
    }

}
