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

package org.kaaproject.kaa.server.operations.service.delta.merge;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.dao.schema.BaseDataSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreator;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreatorImpl;
import org.kaaproject.kaa.server.operations.service.delta.merge.ArrayMergeStrategy;
import org.kaaproject.kaa.server.operations.service.delta.merge.ArrayMergeStrategyResolver;
import org.kaaproject.kaa.server.operations.service.delta.merge.MergeException;

public class ArrayMergeStrategyResolverTest {

    @Test(expected = MergeException.class)
    public void testResolveFailsWhenSchemaForParentIsNotFound() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ArrayMergeStrategyResolver arrayMergeStrategyResolver = new ArrayMergeStrategyResolver(baseSchemaString);
        arrayMergeStrategyResolver.resolve("wrong_parent_name", "org.kaa.config", "child_name");
    }

    @Test
    public void testStrategyIsResolvedToOverrideWhenFieldsDefinitionIsMissed() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema_for_array_merge_strategy_resolver_with_missed_fields_definition.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ArrayMergeStrategyResolver arrayMergeStrategyResolver = new ArrayMergeStrategyResolver(baseSchemaString);
        ArrayMergeStrategy actualArrayMergeStrategy = arrayMergeStrategyResolver.resolve("testT", "org.kaa.config", "child_name");
        Assert.assertTrue(ArrayMergeStrategy.REPLACE == actualArrayMergeStrategy);
    }

    @Test
    public void testStrategyIsResolvedToOverrideWhenFieldsDefinitionIsEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema_for_array_merge_strategy_resolver_with_empty_fields_definition.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ArrayMergeStrategyResolver arrayMergeStrategyResolver = new ArrayMergeStrategyResolver(baseSchemaString);
        ArrayMergeStrategy actualArrayMergeStrategy = arrayMergeStrategyResolver.resolve("testT", "org.kaa.config", "child_name");
        Assert.assertTrue(ArrayMergeStrategy.REPLACE == actualArrayMergeStrategy);
    }

    @Test
    public void testResolveSuccessForUnionWithArrayType() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema_for_array_merge_strategy_resolver_with_array_in_union_type.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ArrayMergeStrategyResolver arrayMergeStrategyResolver = new ArrayMergeStrategyResolver(baseSchemaString);
        ArrayMergeStrategy actualArrayMergeStrategy = arrayMergeStrategyResolver.resolve("testT", "org.kaa.config", "field1");
        Assert.assertTrue(ArrayMergeStrategy.APPEND == actualArrayMergeStrategy);
    }
}
