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

package org.kaaproject.kaa.server.common.core.algorithms.override;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactoryImpl;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;

public class ArrayOverrideStrategyResolverTest {

    @Test(expected = OverrideException.class)
    public void testResolveFailsWhenSchemaForParentIsNotFound() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/schema.json").toURI());
        DataSchema configurationSchema = new DataSchema(new String(Files.readAllBytes(schemaUrl)));

        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(configurationSchema);

        KaaSchema baseSchemaString = generator.getBaseSchema();
        Schema.Parser baseParser = new Schema.Parser();
        baseParser.parse(baseSchemaString.getRawSchema());

        ArrayOverrideStrategyResolver arrayMergeStrategyResolver = new ArrayOverrideStrategyResolver(baseParser.getTypes());
        arrayMergeStrategyResolver.resolve("wrong_parent_name", "org.kaa.config", "child_name");
    }

    @Test
    public void testStrategyIsResolvedToOverrideWhenFieldsDefinitionIsEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/schema_for_array_merge_strategy_resolver_with_empty_fields_definition.json").toURI());
        DataSchema configurationSchema = new DataSchema(new String(Files.readAllBytes(schemaUrl)));

        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(configurationSchema);

        KaaSchema baseSchemaString = generator.getBaseSchema();
        Schema.Parser baseParser = new Schema.Parser();
        baseParser.parse(baseSchemaString.getRawSchema());

        ArrayOverrideStrategyResolver arrayMergeStrategyResolver = new ArrayOverrideStrategyResolver(baseParser.getTypes());
        ArrayOverrideStrategy actualArrayMergeStrategy = arrayMergeStrategyResolver.resolve("testT", "org.kaa.config", "child_name");
        Assert.assertTrue(ArrayOverrideStrategy.REPLACE == actualArrayMergeStrategy);
    }

    @Test
    public void testResolveSuccessForUnionWithArrayType() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/schema_for_array_merge_strategy_resolver_with_array_in_union_type.json").toURI());
        DataSchema configurationSchema = new DataSchema(new String(Files.readAllBytes(schemaUrl)));

        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(configurationSchema);

        KaaSchema baseSchemaString = generator.getBaseSchema();

        Schema.Parser baseParser = new Schema.Parser();
        baseParser.parse(baseSchemaString.getRawSchema());

        ArrayOverrideStrategyResolver arrayMergeStrategyResolver = new ArrayOverrideStrategyResolver(baseParser.getTypes());
        ArrayOverrideStrategy actualArrayMergeStrategy = arrayMergeStrategyResolver.resolve("testT", "org.kaa.config", "field1");
        Assert.assertTrue(ArrayOverrideStrategy.APPEND == actualArrayMergeStrategy);
    }
}
