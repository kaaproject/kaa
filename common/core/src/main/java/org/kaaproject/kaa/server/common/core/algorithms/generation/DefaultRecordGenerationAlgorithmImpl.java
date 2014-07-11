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

package org.kaaproject.kaa.server.common.core.algorithms.generation;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ARRAY_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.BYTES_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.BY_DEFAULT_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ENUM_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELDS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIXED_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.MAP_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAMESPACE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAME_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NULL_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.RECORD_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.SIZE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.TYPE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.CommonUtils;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.configuration.KaaDataFactory;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of
 * {@link org.kaaproject.kaa.server.common.dao.configuration.DefaultRecordGenerationAlgorithm}
 *
 */
public class DefaultRecordGenerationAlgorithmImpl<U extends KaaSchema, T extends KaaData> implements DefaultRecordGenerationAlgorithm<T> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRecordGenerationAlgorithmImpl.class);

    /** The json mapper. */
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /** The processed types. */
    private final Map<String, Object> processedTypes = new HashMap<>();

    /** The raw base schema. */
    private final Map<String, Object> rawBaseSchema;

    /** The schema parser. */
    private final Schema.Parser schemaParser = new Schema.Parser();

    /** The root schema name. */
    private final String rootSchemaName;

    private final KaaDataFactory<U, T> dataFactory;

    private final U rootSchema;

    /**
     * Instantiates a new default configuration processor.
     *
     * @param kaaSchema the base schema
     * @throws ConfigurationGenerationException the configuration processing exception
     */
    @SuppressWarnings("unchecked")
    public DefaultRecordGenerationAlgorithmImpl(U kaaSchema, KaaDataFactory<U, T> factory) throws ConfigurationGenerationException {
        LOG.debug("Generating default configuration for configuration schema: " + kaaSchema.getRawSchema());

        try {
            this.rootSchema = kaaSchema;
            this.dataFactory = factory;
            this.rawBaseSchema = jsonMapper.readValue(kaaSchema.getRawSchema(), Map.class);
            this.rootSchemaName = getFullNameFromRaw(this.rawBaseSchema);

        } catch (IOException ioe) {
            LOG.error("Unexpected exception occurred while generating configuration.", ioe);
            throw new ConfigurationGenerationException(ioe);
        }
    }

    /**
     * Processes generic type.
     *
     * @param rawSchemaNode schema for current type.
     * @param byDefault the by default
     * @return generated value for input type.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object processType(Map<String, Object> rawSchemaNode, Object byDefault) throws ConfigurationGenerationException, IOException {
        Object typeField = rawSchemaNode.get(TYPE_FIELD);
        if (List.class.isAssignableFrom(typeField.getClass())) {
            typeField = ((List)typeField).get(0);
        }
        if (NULL_FIELD_VALUE.equals(typeField)) {
            return null;
        } else if (ARRAY_FIELD_VALUE.equals(typeField)) {
            // if this an array type then return empty array instance
            return processArray(rawSchemaNode);
        } else if (RECORD_FIELD_VALUE.equals(typeField)) {
            // if this a record type then process it in-depth
            return processRecord(rawSchemaNode);
        } else if (MAP_FIELD_VALUE.equals(typeField)) {
            throw new ConfigurationGenerationException("Map is not supported.");
        } else if (FIXED_FIELD_VALUE.equals(typeField)) {
            return processFixed(rawSchemaNode);
        } else if (ENUM_FIELD_VALUE.equals(typeField)) {
            return processEnum(rawSchemaNode);
        } else if (String.class.isAssignableFrom(typeField.getClass())) {
            // if this is a 'bytes' type then convert json bytes array to
            // avro 'bytes' representation or
            // if this is a named type - look for already processed types
            // or throw an exception because "by_default" is missed
            String typeFieldString = String.class.cast(typeField);
            if (BYTES_FIELD_VALUE.equals(typeFieldString) && byDefault != null) {
                List<Integer> bytesArray = (List<Integer>) byDefault;
                ByteBuffer byteBuffer = ByteBuffer.allocate(bytesArray.size());
                for (Integer oneByte : bytesArray) {
                    byteBuffer.put(oneByte.byteValue());
                }
                byteBuffer.flip();
                return byteBuffer;
            } else if (processedTypes.containsKey(typeFieldString)) {
                return processedTypes.get(typeFieldString);
            }
        } else if (Map.class.isAssignableFrom(typeField.getClass())) {
            Map typeFieldMap = Map.class.cast(typeField);
            return processType(typeFieldMap, byDefault);
        }
        return byDefault;
    }

    /**
     * Gets the full name from raw.
     *
     * @param schemaNode the schema node
     * @return the full name from raw
     */
    private static String getFullNameFromRaw(Map<String, Object> schemaNode) {
        String name = (String) schemaNode.get(NAME_FIELD);
        String namespace = (String) schemaNode.get(NAMESPACE_FIELD);

        // building cache key to look for already processed type
        String fullName = "";
        if (!namespace.isEmpty()) {
            fullName = namespace;
        }
        if (!name.isEmpty()) {
            fullName = fullName.isEmpty() ? name : fullName + "." + name;
        }
        return fullName;
    }

    /**
     * Gets the schema from raw.
     *
     * @param schemaNode the schema node
     * @return the schema from raw
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Schema getSchemaFromRaw(Map<String, Object> schemaNode) throws IOException {
        Schema avroSchema = schemaParser.getTypes().get(getFullNameFromRaw(schemaNode));
        if (avroSchema == null) {
            avroSchema = schemaParser.parse(jsonMapper.writeValueAsString(schemaNode));
        }
        return avroSchema;
    }

    /**
     * Processes record type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input record type.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     */
    @SuppressWarnings("unchecked")
    private Object processRecord(Map<String, Object> schemaNode) throws IOException, ConfigurationGenerationException {
        Schema avroSchema = getSchemaFromRaw(schemaNode);
        GenericRecord result = new GenericData.Record(avroSchema);
        String fullName = getFullNameFromRaw(schemaNode);
        if (!fullName.isEmpty()) {
            processedTypes.put(fullName, result);
        }

        // process each field
        List<Object> fields = (List<Object>) schemaNode.get(FIELDS_FIELD);
        for (Object field : fields) {
            Map<String, Object> fieldDefinition = (Map<String, Object>) field;
            String fieldName = (String) fieldDefinition.get(NAME_FIELD);
            Object processFieldResult = processField(fieldDefinition);
            if (processFieldResult != null) {
                result.put(fieldName, processFieldResult);
            }
        }

        return result;
    }

    /**
     * Processes array type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input array type.
     */
    private Object processArray(Map<String, Object> schemaNode) {
        Schema elementTypeSchema = Schema.create(Type.NULL);
        return new GenericData.Array<>(0, Schema.createArray(elementTypeSchema));
    }

    /**
     * Processes enum type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input enum type.
     * @throws JsonGenerationException the json generation exception
     * @throws JsonMappingException the json mapping exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Object processEnum(Map<String, Object> schemaNode) throws JsonGenerationException, JsonMappingException, IOException {
        Schema avroSchema = getSchemaFromRaw(schemaNode);
        GenericEnumSymbol result = new GenericData.EnumSymbol(avroSchema, avroSchema.getEnumSymbols().get(0));
        String fullName = getFullNameFromRaw(schemaNode);
        if (!fullName.isEmpty()) {
            processedTypes.put(fullName, result);
        }
        return result;
    }

    /**
     * Processes fixed type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input record type.
     * @throws JsonGenerationException the json generation exception
     * @throws JsonMappingException the json mapping exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Object processFixed(Map<String, Object> schemaNode) throws JsonGenerationException, JsonMappingException, IOException {
        Integer size = (Integer) schemaNode.get(SIZE_FIELD);

        byte [] bytes = new byte [size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) 0;
        }

        Schema avroSchema = getSchemaFromRaw(schemaNode);
        GenericFixed result = new GenericData.Fixed(avroSchema, bytes);
        String fullName = getFullNameFromRaw(schemaNode);
        if (!fullName.isEmpty()) {
            processedTypes.put(fullName, result);
        }

        return result;
    }

    /**
     * Process field of a record type.
     *
     * @param fieldDefinition schema for field.
     * @return generated value for field based on its definition.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     */
    private Object processField(Map<String, Object> fieldDefinition) throws IOException, ConfigurationGenerationException {
        Object nameField = fieldDefinition.get(NAME_FIELD);
        // if this a "uuid" type then generate it
        if (UUID_FIELD.equals(nameField)) {
            return CommonUtils.generateUuidObject();
        }

        return processType(fieldDefinition, fieldDefinition.get(BY_DEFAULT_FIELD));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootConfiguration()
     */
    @Override
    public final GenericRecord getRootConfiguration() throws ConfigurationGenerationException {
        int dotIndex = rootSchemaName.lastIndexOf('.');
        return getConfigurationByName(rootSchemaName.substring(dotIndex + 1), rootSchemaName.substring(0, dotIndex));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootJsonConfiguration()
     */
    @Override
    public final T getRootData() throws IOException, ConfigurationGenerationException {
        GenericRecord root = getRootConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(root.getSchema());
        try {
            return dataFactory.createData(rootSchema, converter.endcodeToJson(root));
        } catch (RuntimeException e) {
            // NPE is thrown if "null" was wriotten into a field that is not nullable
            // CCE is thrown if value of wrong type was written into a field
            LOG.error("Unexpected exception occurred while generating configuration.", e);
            throw new ConfigurationGenerationException(e);
        }
    }

    @Override
    public final GenericRecord getConfigurationByName(String name, String namespace) throws ConfigurationGenerationException {
        if (name == null || namespace == null) {
            return null;
        }
        if (processedTypes.containsKey(namespace + "." + name)) {
            return (GenericRecord) processedTypes.get(namespace + "." + name);
        }
        Map<String, Object> schema = CommonUtils.findRawSchemaByName(rawBaseSchema, name, namespace);
        if (schema != null) {
            try {
                return (GenericRecord) processType(schema, null);
            } catch (IOException ioe) {
                LOG.error("Unexpected exception occurred while generating configuration.", ioe);
                throw new ConfigurationGenerationException(ioe);
            }
        }
        return null;
    }
}
