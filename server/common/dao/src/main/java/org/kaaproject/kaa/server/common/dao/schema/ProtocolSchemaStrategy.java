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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is protocol schema strategy for generating schema protocol schema. This schema used in transport layer
 * between client and endpoint server.
 */
public class ProtocolSchemaStrategy implements SchemaCreationStrategy {

    @Override
    public boolean isUuidOptional() {
        return false;
    }

    @Override
    public boolean isUnchangedSupported() {
        return true;
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
    public Map<String, Object> onSchemaProcessed(Map<String, Object> rootSchema,
            Set<String> addressableRecords) {
        List<Object> deltaTypes = new ArrayList<Object>();
        deltaTypes.add(rootSchema);
        for (String type : addressableRecords) {
            deltaTypes.add(type);
        }

        Map<String, Object> deltaTypesField = new HashMap<String, Object>();
        deltaTypesField.put("name", "delta");
        deltaTypesField.put("type", deltaTypes);

        List<Object> deltaFields = new ArrayList<Object>();
        deltaFields.add(deltaTypesField);

        Map<String, Object> delta = new HashMap<String, Object>();
        delta.put("name", "deltaT");
        delta.put("namespace", "org.kaaproject.configuration");
        delta.put("type", "record");
        delta.put("fields", deltaFields);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("type", "array");
        result.put("items", delta);

        return result;
    }

    @Override
    public boolean isArrayEditable() {
        return true;
    }

}
