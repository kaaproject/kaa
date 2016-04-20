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

package org.kaaproject.kaa.server.common.core.algorithms.schema;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.DELTA;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.KAA_NAMESPACE;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
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
    public void onOptionalField(List<Schema> union) {
        Schema nullSchema = Schema.create(Type.NULL);
        if (!union.contains(nullSchema)) {
            union.add(0, nullSchema);
        }
    }

    @Override
    public void onMandatoryField(List<Schema> union) {
        // Nothing to do
    }

    @Override
    public Schema onSchemaProcessed(Schema rootSchema, Set<Schema> addressableRecords) {
        List<Schema> deltaTypes = new ArrayList<Schema>(addressableRecords.size() + 1);
        deltaTypes.add(rootSchema);
        deltaTypes.addAll(addressableRecords);


        Field deltaTypesField = new Field(DELTA, Schema.createUnion(deltaTypes), null, null);

        List<Field> deltaFields = new ArrayList<Field>();
        deltaFields.add(deltaTypesField);

        Schema delta = Schema.createRecord(DELTA + "T", null, KAA_NAMESPACE, false);
        delta.setFields(deltaFields);

        return Schema.createArray(delta);
    }

    @Override
    public boolean isArrayEditable() {
        return true;
    }

    @Override
    public ProtocolSchema createSchema(Schema schema) {
        return getSchemaFactory().createProtocolSchema(schema.toString());
    }
}
