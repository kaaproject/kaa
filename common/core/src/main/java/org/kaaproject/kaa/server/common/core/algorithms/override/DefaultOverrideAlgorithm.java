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

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UNCHANGED;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.generation.DefaultRecordGenerationAlgorithmImpl;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.BaseDataFactory;
import org.kaaproject.kaa.server.common.core.configuration.OverrideData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link ConfigurationMerger}.
 */
public class DefaultOverrideAlgorithm implements OverrideAlgorithm {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOverrideAlgorithm.class);

    private DefaultRecordGenerationAlgorithm confGenerator;
    private Schema.Parser baseSchemaParser;

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.delta.merge.ConfigurationMerger#merge(List<org.kaaproject.kaa.common.dto.EndpointGroupDto>, List<org.kaaproject.kaa.common.dto.ConfigurationDto>, org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    public BaseData override(BaseData baseConfiguration, List<OverrideData> overrideConfigurations) throws OverrideException, IOException {
        LOG.debug("Merging:base configuration = {}; override = {}", baseConfiguration, overrideConfigurations);
        if (baseConfiguration == null) {
            LOG.debug("empty endpoint groups or configurations - returning empty result");
            return null;
        }
        // if we have just one configuration then return it.
        if (overrideConfigurations == null || overrideConfigurations.isEmpty()) {
            return baseConfiguration;
        }

        try {
            confGenerator = new DefaultRecordGenerationAlgorithmImpl(baseConfiguration.getSchema(), new BaseDataFactory());
        } catch (ConfigurationGenerationException e) {
            throw new OverrideException(e);
        }

        baseSchemaParser = new Schema.Parser();
        Schema baseAvroSchema = baseSchemaParser.parse(baseConfiguration.getSchema().getRawSchema());
        Schema.Parser overrideSchemaParser = new Schema.Parser();
        Schema overrideAvroSchema = overrideSchemaParser.parse(overrideConfigurations.get(0).getSchema().getRawSchema());

        LOG.info("converter: {}", baseAvroSchema.toString());
        GenericAvroConverter<GenericRecord> baseConverter = new GenericAvroConverter(baseAvroSchema);
        GenericAvroConverter<GenericRecord> overrideConverter = new GenericAvroConverter(overrideAvroSchema);
        GenericRecord mergedConfiguration = baseConverter.decodeJson(baseConfiguration.getRawData());

        try {
            ArrayOverrideStrategyResolver arrayMergeStrategyResolver = new ArrayOverrideStrategyResolver(baseSchemaParser.getTypes());
            for (OverrideData entry : overrideConfigurations) {
                String configurationToApply = entry.getRawData();
                // else execute merge
                LOG.debug("Override schema {}", entry.getSchema());
                GenericRecord nodeToApply = overrideConverter.decodeJson(configurationToApply);
                LOG.info("configurationToApply: {}", nodeToApply);
                applyNode(mergedConfiguration, nodeToApply, arrayMergeStrategyResolver);
            }
            return new BaseData(baseConfiguration.getSchema(), baseConverter.encodeToJson(mergedConfiguration));
        } catch (IOException | ConfigurationGenerationException e) {
            throw new OverrideException(e);
        }
    }

    private Schema getSchemaByName(String fullName) {
        return baseSchemaParser.getTypes().get(fullName);
    }

    /**
     * Apply node.
     *
     * @param destinationRoot the destination root
     * @param sourceRoot the source root
     * @param arrayMergeStrategyResolver the array merge strategy resolver
     * @throws OverrideException the merge exception
     * @throws ConfigurationGenerationException
     */
    private void applyNode(GenericRecord destinationRoot, GenericRecord sourceRoot, ArrayOverrideStrategyResolver arrayMergeStrategyResolver) throws OverrideException, ConfigurationGenerationException {
        Schema sourceRootSchema = sourceRoot.getSchema();

        // iterate over each child node and try to apply it
        for (Schema.Field field : sourceRootSchema.getFields()) {
            String sourceChildname = field.name();
            Object sourceChild = sourceRoot.get(field.pos());

            if (sourceChild instanceof GenericEnumSymbol) {
                GenericEnumSymbol sourceEnum = (GenericEnumSymbol) sourceChild;
                // If the field's value is "unchanged" and this field is empty
                // in destination data we should generate the default value for it
                if (sourceEnum.toString().equals(UNCHANGED)) {
                    if (destinationRoot.get(field.pos()) == null) {
                        GenericRecord defRec = confGenerator.getConfigurationByName(sourceRootSchema.getName(), sourceRootSchema.getNamespace());
                        destinationRoot.put(field.pos(), defRec.get(field.pos()));
                    }
                    continue;
                }
            }

            // if they are then try to merge them
            // else override destination's node with source's node
            Object destinationChild = destinationRoot.get(field.pos());
            // avro type is different - override destination's node with source's node
            if (sourceChild instanceof GenericRecord) {
                // checking schema types
                GenericRecord sourceRecord = (GenericRecord) sourceChild;
                GenericRecord destinationRecord = null;
                if (destinationChild instanceof GenericRecord) {
                    GenericRecord tempRecord = (GenericRecord) destinationChild;
                    if (tempRecord.getSchema().getFullName().equals(sourceRecord.getSchema().getFullName())) {
                        destinationRecord = tempRecord;
                    }
                }
                if (destinationRecord == null) {
                    destinationRecord = new GenericData.Record(getSchemaByName(sourceRecord.getSchema().getFullName()));
                    destinationRoot.put(field.pos(), destinationRecord);
                }
                // merge nodes
                applyNode(destinationRecord, sourceRecord, arrayMergeStrategyResolver);
            } else if (sourceChild instanceof GenericArray) {
                // merge array
               GenericArray sourceArray = (GenericArray) sourceChild;
               ArrayOverrideStrategy mergeStrategy = ArrayOverrideStrategy.REPLACE;

               if (!sourceArray.isEmpty() && destinationChild instanceof GenericArray) {
                   GenericArray destArray = (GenericArray) destinationChild;
                   // Checking if first elements have same type
                   if (!destArray.isEmpty() && destArray.get(0).getClass() == sourceArray.get(0).getClass()) {
                       boolean resolveStrategy = false;
                       if (destArray.get(0) instanceof GenericContainer) {
                           GenericContainer destFirst = (GenericContainer) destArray.get(0);
                           GenericContainer sourceFirst = (GenericContainer) sourceArray.get(0);
                           if (destFirst.getSchema().getFullName().equals(sourceFirst.getSchema().getFullName())) {
                               resolveStrategy = true;
                           }
                       } else {
                           resolveStrategy = true;
                       }
                       if (resolveStrategy) {
                           mergeStrategy = arrayMergeStrategyResolver.resolve(sourceRootSchema.getName(), sourceRootSchema.getNamespace(), sourceChildname);
                       }
                   }
               }
               switch (mergeStrategy) {
                   case REPLACE:
                       if (sourceArray.getSchema().getElementType().getType() == Schema.Type.RECORD) {
                           GenericArray destArray = new GenericData.Array<>(sourceArray.size(), sourceArray.getSchema());
                           for (Object item : sourceArray) {
                               GenericRecord recordItem = (GenericRecord) item;
                               GenericRecord destRecord = new GenericData.Record(getSchemaByName(recordItem.getSchema().getFullName()));
                               applyNode(destRecord, recordItem, arrayMergeStrategyResolver);
                               destArray.add(destRecord);
                           }
                           destinationRoot.put(sourceChildname, destArray);
                       } else {
                           destinationRoot.put(sourceChildname, sourceChild);
                       }
                       break;
                   case APPEND:
                       GenericArray destArray = (GenericArray) destinationChild;
                       if (sourceArray.getSchema().getElementType().getType() == Schema.Type.RECORD) {
                           for (Object item : sourceArray) {
                               GenericRecord recordItem = (GenericRecord) item;
                               GenericRecord destRecord = new GenericData.Record(getSchemaByName(recordItem.getSchema().getFullName()));
                               applyNode(destRecord, recordItem, arrayMergeStrategyResolver);
                               destArray.add(destRecord);
                           }
                       } else {
                           destArray.addAll(sourceArray);
                       }
                       break;
                   default:
                       break;
               }
            } else if (!UUID_FIELD.equals(field.name()) || destinationRoot.get(field.pos()) == null) {
                // simple node is just copied to destination node
                destinationRoot.put(sourceChildname, sourceChild);
            }
        }
    }

}
