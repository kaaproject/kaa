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
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactoryImpl;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.OverrideData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.OverrideSchema;

public class DefaultOverrideAlgorithmTest {

    @Test
    public void testMergeOf3Configurations() throws Exception {
        // Create Configuration Schema
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/schema.json").toURI());
        DataSchema configuraionSchema = new DataSchema(new String(Files.readAllBytes(schemaUrl)));

        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(configuraionSchema);

        BaseSchema baseSchemaString = generator.getBaseSchema();
        OverrideSchema overrideSchemaString = generator.getOverrideSchema();

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/config_weight_0.json").toURI());
        BaseData baseData = new BaseData(baseSchemaString, new String(Files.readAllBytes(configuraionWeight0Path)));

        // Create Configuration weight 1
        Path configuraionWeight1Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/config_weight_1.json").toURI());
        OverrideData data1 = new OverrideData(overrideSchemaString, new String(Files.readAllBytes(configuraionWeight1Path)));

        // Create Configuration weight 2
        Path configuraionWeight2Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/config_weight_2.json").toURI());
        OverrideData data2 = new OverrideData(overrideSchemaString, new String(Files.readAllBytes(configuraionWeight2Path)));

        List<OverrideData> configurations = new ArrayList<>();
        configurations.add(data1);
        configurations.add(data2);

        Path mergedConfiguraionPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/merged_config.json").toURI());
        String mergedConfiguraionBody = new String(Files.readAllBytes(mergedConfiguraionPath));

        OverrideAlgorithm merger = new DefaultOverrideAlgorithm();
        BaseData mergeResult = merger.override(baseData, configurations);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(baseSchemaString.getRawSchema());
        GenericRecord mergeResultGenericRecord = converter.decodeJson(mergeResult.getRawData());
        GenericRecord expectedMergedConfiguraionGenericRecord = converter.decodeJson(mergedConfiguraionBody);

        Assert.assertTrue(mergeResultGenericRecord.equals(expectedMergedConfiguraionGenericRecord));
    }

    @Test
    public void testMergeWhenConfigurationsIsEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("override/schema.json").toURI());
        DataSchema configuraionSchema = new DataSchema(new String(Files.readAllBytes(schemaUrl)));

        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(configuraionSchema);

        List<OverrideData> configurations = new ArrayList<>();

        OverrideAlgorithm merger = new DefaultOverrideAlgorithm();
        BaseData mergeResult = merger.override(null, configurations);

        Assert.assertNull(mergeResult);
    }

}
