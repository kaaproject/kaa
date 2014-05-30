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

package org.kaaproject.kaa.server.common.dao.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is base schema strategy for generating schema for group "all" configurations
 */
public class BaseDataSchemaStrategy implements SchemaCreationStrategy {

    @Override
    public boolean isUuidOptional() {
        return true;
    }

    @Override
    public boolean isUnchangedSupported() {
        return false;
    }

    @Override
    public void onOptionalField(List<Object> union) {
        if (!union.contains("null")) {
            union.add(0, "null");
        }
    }

    @Override
    public void onMandatoryField(List<Object> union) {
        // Nothing to do
    }

    @Override
    public Map<String, Object> onSchemaProcessed(
            Map<String, Object> rootSchema, Set<String> addressableRecords) {
        return rootSchema;
    }

    @Override
    public boolean isArrayEditable() {
        return false;
    }

}
