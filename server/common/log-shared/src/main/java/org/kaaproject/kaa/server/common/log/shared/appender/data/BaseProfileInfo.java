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

package org.kaaproject.kaa.server.common.log.shared.appender.data;

public class BaseProfileInfo implements ProfileInfo {

    private final BaseSchemaInfo schemaInfo;
    private final String body;

    public BaseProfileInfo(BaseSchemaInfo schemaInfo, String body) {
        super();
        this.schemaInfo = schemaInfo;
        this.body = body;
    }

    @Override
    public String getSchemaId() {
        return schemaInfo.getSchemaId();
    }

    @Override
    public String getSchema() {
        return schemaInfo.getSchema();
    }

    @Override
    public String getBody() {
        return body;
    }

}
