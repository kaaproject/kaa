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

import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;

/**
 * This is base schema strategy for generating schema for group "all" configurations
 */
public class BaseDataSchemaStrategy extends AbstractSchemaStrategy<BaseSchema> {

    @Override
    public boolean isUuidOptional() {
        return true;
    }

    @Override
    public boolean isUnchangedSupported() {
        return false;
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
        return rootSchema;
    }

    @Override
    public boolean isArrayEditable() {
        return false;
    }

    @Override
    public BaseSchema createSchema(Schema schema) {
        return getSchemaFactory().createBaseSchema(schema.toString());
    }

}
