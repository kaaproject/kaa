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

import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_UUID;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_UNCHANGED;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessingException;
import org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor;
import org.kaaproject.kaa.server.common.dao.configuration.DefaultConfigurationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link ConfigurationMerger}.
 */
public class DefaultConfigurationMerger implements ConfigurationMerger {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurationMerger.class);

    /** The Constant ENDPOINT_GROUP_COMPARATOR. */
    private static final Comparator<EndpointGroupDto> ENDPOINT_GROUP_COMPARATOR = new Comparator<EndpointGroupDto>() {

        @Override
        public int compare(EndpointGroupDto o1, EndpointGroupDto o2) {
            if (o1.getWeight() < o2.getWeight()) {
                return -1;
            }
            if (o1.getWeight() > o2.getWeight()) {
                return 1;
            }
            return o1.getId().compareTo(o2.getId());
        }
    };

    private ConfigurationProcessor confGenerator;
    private Schema.Parser baseSchemaParser;

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.delta.merge.ConfigurationMerger#merge(List<org.kaaproject.kaa.common.dto.EndpointGroupDto>, List<org.kaaproject.kaa.common.dto.ConfigurationDto>, org.kaaproject.kaa.common.dto.ConfigurationSchemaDto)
     */
    @Override
    public byte[] merge(List<EndpointGroupDto> endpointGroups, List<ConfigurationDto> configurations, ConfigurationSchemaDto overrideDataSchema) throws MergeException {
        LOG.debug("Merging: endpointGroups={}; configurations={}; configurationSchema={}", endpointGroups, configurations, overrideDataSchema);
        if (endpointGroups.isEmpty() || configurations.isEmpty()) {
            LOG.debug("empty endpoint groups or configurations - returning empty result");
            return null;
        }
        // if we have just one configuration then return it.
        if (configurations.size() == 1) {
            return configurations.get(0).getBinaryBody();
        }

        // create sorted map to store configurations sorted by endpoint group weight
        // put all endpoint groups as keys into the map
        NavigableMap<EndpointGroupDto, ConfigurationDto> endpointGroupsConfigurations = new TreeMap<>(ENDPOINT_GROUP_COMPARATOR);
        for (EndpointGroupDto endpointGroup : endpointGroups) {
            endpointGroupsConfigurations.put(endpointGroup, null);
        }
        // put configurations into the map under corresponding endpoint group
        for (ConfigurationDto configuration : configurations) {
            boolean endpointGroupFound = false;
            for (EndpointGroupDto endpointGroup : endpointGroupsConfigurations.keySet()) {
                if (configuration.getEndpointGroupId().equals(endpointGroup.getId())) {
                    endpointGroupsConfigurations.put(endpointGroup, configuration);
                    endpointGroupFound = true;
                    break;
                }
            }
            if (!endpointGroupFound) {
                throw new MergeException("No Endpoint Group found for Configuration; Endpoint Group Id: " + configuration.getEndpointGroupId());
            }
        }

        try {
            confGenerator = new DefaultConfigurationProcessor(overrideDataSchema.getBaseSchema());
        } catch (ConfigurationProcessingException e) {
            throw new MergeException(e);
        }

        baseSchemaParser = new Schema.Parser();
        Schema baseAvroSchema = baseSchemaParser.parse(overrideDataSchema.getBaseSchema());
        Schema.Parser overrideSchemaParser = new Schema.Parser();
        Schema overrideAvroSchema = overrideSchemaParser.parse(overrideDataSchema.getOverrideSchema());

        LOG.info("converter: {}", baseAvroSchema.toString());
        GenericAvroConverter<GenericRecord> baseConverter = new GenericAvroConverter(baseAvroSchema);
        GenericAvroConverter<GenericRecord> overrideConverter = new GenericAvroConverter(overrideAvroSchema);
        GenericRecord mergedConfiguration = null;

        try {
            ArrayMergeStrategyResolver arrayMergeStrategyResolver = new ArrayMergeStrategyResolver(overrideDataSchema.getBaseSchema());
            for (Map.Entry<EndpointGroupDto, ConfigurationDto> entry : endpointGroupsConfigurations.entrySet()) {
                if (entry.getValue() == null) {
                    LOG.debug("No Configuration found for Endpoint Group; Endpoint Group Id: " + entry.getKey().getId());
                    continue;
                }
                byte [] configurationToApply = entry.getValue().getBinaryBody();

                int groupWeight = entry.getKey().getWeight();
                GenericAvroConverter<GenericRecord> converter = groupWeight == 0 ? baseConverter : overrideConverter;

                LOG.info("weight {} configurationToApply: {}", groupWeight, new String(configurationToApply));

                if (configurationToApply != null) {
                    if (mergedConfiguration == null) {
                        // this is a configuration of a group with the lowest weight - take it as an initial configuration
                        mergedConfiguration = converter.decodeJson(configurationToApply);
                    } else {
                        // else execute merge
                        LOG.debug("Override schema {}", overrideDataSchema.getOverrideSchema());
                        GenericRecord nodeToApply = converter.decodeJson(configurationToApply);
                        applyNode(mergedConfiguration, nodeToApply, arrayMergeStrategyResolver);
                    }
                }
            }
            return baseConverter.encodeToJsonBytes(mergedConfiguration);
        } catch (IOException | ConfigurationProcessingException e) {
            throw new MergeException(e);
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
     * @throws MergeException the merge exception
     * @throws ConfigurationProcessingException
     */
    private void applyNode(GenericRecord destinationRoot, GenericRecord sourceRoot, ArrayMergeStrategyResolver arrayMergeStrategyResolver) throws MergeException, ConfigurationProcessingException {
        Schema sourceRootSchema = sourceRoot.getSchema();

        // iterate over each child node and try to apply it
        for (Schema.Field field : sourceRootSchema.getFields()) {
            String sourceChildname = field.name();
            Object sourceChild = sourceRoot.get(field.pos());

            if (sourceChild instanceof GenericEnumSymbol) {
                GenericEnumSymbol sourceEnum = (GenericEnumSymbol) sourceChild;
                // If the field's value is "unchanged" and this field is empty
                // in destination data we should generate the default value for it
                if (sourceEnum.toString().equals(FIELD_UNCHANGED)) {
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
               ArrayMergeStrategy mergeStrategy = ArrayMergeStrategy.REPLACE;

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
                       if (sourceArray.get(0) instanceof GenericRecord) {
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
                       if (sourceArray.get(0) instanceof GenericRecord) {
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
            } else if (!FIELD_UUID.equals(field.name()) || destinationRoot.get(field.pos()) == null) {
                // simple node is just copied to destination node
                destinationRoot.put(sourceChildname, sourceChild);
            }
        }
    }

}
