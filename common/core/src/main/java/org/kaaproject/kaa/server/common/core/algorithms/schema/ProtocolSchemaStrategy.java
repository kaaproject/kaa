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

package org.kaaproject.kaa.server.common.core.algorithms.schema;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ARRAY_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.DELTA;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELDS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ITEMS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.KAA_NAMESPACE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAMESPACE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAME_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NULL_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.RECORD_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.TYPE_FIELD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;

/**
 * This is protocol schema strategy for generating schema protocol schema. This schema used in transport layer
 * between client and endpoint server.
 */
public class ProtocolSchemaStrategy extends AbstractSchemaStrategy<ProtocolSchema> {

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
        if (!union.contains(NULL_FIELD_VALUE)) {
            union.add(0, NULL_FIELD_VALUE);
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
        deltaTypesField.put(NAME_FIELD, DELTA);
        deltaTypesField.put(TYPE_FIELD, deltaTypes);

        List<Object> deltaFields = new ArrayList<Object>();
        deltaFields.add(deltaTypesField);

        Map<String, Object> delta = new HashMap<String, Object>();
        delta.put(NAME_FIELD, DELTA + "T");
        delta.put(NAMESPACE_FIELD, KAA_NAMESPACE);
        delta.put(TYPE_FIELD, RECORD_FIELD_VALUE);
        delta.put(FIELDS_FIELD, deltaFields);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(TYPE_FIELD, ARRAY_FIELD_VALUE);
        result.put(ITEMS_FIELD, delta);

        return result;
    }

    @Override
    public boolean isArrayEditable() {
        return true;
    }

    @Override
    public ProtocolSchema createSchema(String schema) {
        return getSchemaFactory().createProtocolSchema(schema);
    }
}
