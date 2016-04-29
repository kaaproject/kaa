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

import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;
import org.kaaproject.kaa.server.common.core.schema.ProtocolSchema;

public class SchemaGenerationAlgorithmImpl implements SchemaGenerationAlgorithm{

    private final DataSchema initialSchema;

    private BaseSchema baseSchema;
    private OverrideSchema overrideSchema;
    private ProtocolSchema protocolSchema;

    private <T extends KaaSchema> SchemaCreator<T> getSchemaCreator(SchemaCreationStrategy<T> strategy) {
        return new SchemaCreatorImpl<T>(strategy);
    }

    public SchemaGenerationAlgorithmImpl(DataSchema schema) {
        this.initialSchema = schema;
    }

    @Override
    public BaseSchema getBaseSchema() throws SchemaCreationException {
        if (baseSchema == null) {
            baseSchema = getSchemaCreator(new BaseDataSchemaStrategy()).createSchema(initialSchema);
        }
        return baseSchema;
    }

    @Override
    public OverrideSchema getOverrideSchema() throws SchemaCreationException {
        if (overrideSchema == null) {
            overrideSchema = getSchemaCreator(new OverrideDataSchemaStrategy()).createSchema(initialSchema);
        }
        return overrideSchema;
    }

    @Override
    public ProtocolSchema getProtocolSchema() throws SchemaCreationException {
        if (protocolSchema == null) {
            protocolSchema = getSchemaCreator(new ProtocolSchemaStrategy()).createSchema(initialSchema);
        }
        return protocolSchema;
    }

}
