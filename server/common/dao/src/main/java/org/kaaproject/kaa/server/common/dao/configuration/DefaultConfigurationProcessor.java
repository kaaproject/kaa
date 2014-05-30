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

package org.kaaproject.kaa.server.common.dao.configuration;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of
 * {@link org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor}
 *
 */
public class DefaultConfigurationProcessor implements ConfigurationProcessor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurationProcessor.class);

    /** The Constant UUID_FIELD. */
    private static final String UUID_FIELD = "__uuid";

    /** The Constant UUID_TYPE. */
    private static final String UUID_TYPE = "org.kaaproject.configuration.uuidT";

    /** The Constant UUID_SCHEMA_NAME. */
    private static final String UUID_SCHEMA_NAME = "uuidT";

    /** The Constant UUID_SCHEMA_SPACE. */
    private static final String UUID_SCHEMA_SPACE = "org.kaaproject.configuration";

    /** The Constant TYPE_FIELD. */
    private static final String TYPE_FIELD = "type";

    /** The Constant ENUM_FIELD_VALUE. */
    private static final String ENUM_FIELD_VALUE = "enum";

    /** The Constant ARRAY_FIELD_VALUE. */
    private static final String ARRAY_FIELD_VALUE = "array";

    /** The Constant RECORD_FIELD_VALUE. */
    private static final String RECORD_FIELD_VALUE = "record";

    /** The Constant MAP_FIELD_VALUE. */
    private static final String MAP_FIELD_VALUE = "map";

    /** The Constant FIXED_FIELD_VALUE. */
    private static final String FIXED_FIELD_VALUE = "fixed";

    /** The Constant NULL_FIELD_VALUE. */
    private static final String NULL_FIELD_VALUE = "null";

    /** The Constant BYTES_FIELD_VALUE. */
    private static final String BYTES_FIELD_VALUE = "bytes";

    /** The Constant FIELDS_FIELD. */
    private static final String FIELDS_FIELD = "fields";

    /** The Constant NAME_FIELD. */
    private static final String NAME_FIELD = "name";

    /** The Constant NAMESPACE_FIELD. */
    private static final String NAMESPACE_FIELD = "namespace";

    /** The Constant BY_DEFAULT_FIELD. */
    private static final String BY_DEFAULT_FIELD = "by_default";

    /** The Constant SIZE_FIELD. */
    private static final String SIZE_FIELD = "size";

    /** The Constant UUID_SIZE. */
    private static final int UUID_SIZE = 16;

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

    /** The base schema. */
    private final String baseSchema;

    /**
     * Instantiates a new default configuration processor.
     *
     * @param baseSchema the base schema
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    @SuppressWarnings("unchecked")
    public DefaultConfigurationProcessor(String baseSchema) throws ConfigurationProcessingException {
        LOG.debug("Generating default configuration for configuration schema: " + baseSchema);

        try {
            this.baseSchema = baseSchema;
            this.rawBaseSchema = jsonMapper.readValue(baseSchema, Map.class);
            this.rootSchemaName = getFullNameFromRaw(this.rawBaseSchema);

        } catch (IOException ioe) {
            LOG.error("Unexpected exception occurred while generating configuration.", ioe);
            throw new ConfigurationProcessingException(ioe);
        }
    }

    /**
     * Generate uuid object.
     *
     * @param schema the avro schema
     * @return the generated fixed type object
     */
    private Object generateUUIDObject(Schema schema) {
        LOG.debug("Generated UUID");
        return new GenericData.Fixed(schema, generateUUIDBytes());
    }

    /**
     * Generate uuid object.
     *
     * @return the generic data. fixed
     */
    private GenericData.Fixed generateUUIDObject() {
        LOG.debug("Generated UUID");
        Schema schema = schemaParser.getTypes().get(UUID_TYPE);
        if (schema == null) {
            schema = Schema.createFixed(UUID_SCHEMA_NAME, null, UUID_SCHEMA_SPACE, UUID_SIZE);
        }
        return new GenericData.Fixed(schema, generateUUIDBytes());
    }

    /**
     * Generates UUID bytes.
     *
     * @return list of generated bytes.
     */
    protected byte [] generateUUIDBytes() {
        UUID uuid = UUID.randomUUID();

        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[UUID_SIZE]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        return byteBuffer.array();
    }


    /**
     * Processes generic type.
     *
     * @param rawSchemaNode schema for current type.
     * @param byDefault the by default
     * @return generated value for input type.
     * @throws ConfigurationProcessingException configuration processing
     * exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object processType(Map<String, Object> rawSchemaNode, Object byDefault) throws ConfigurationProcessingException, IOException {
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
            throw new ConfigurationProcessingException("Map is not supported.");
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
        if (!StringUtils.isBlank(namespace)) {
            fullName = namespace;
        }
        if (!StringUtils.isBlank(name)) {
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
     * @throws ConfigurationProcessingException configuration processing
     * exception
     */
    @SuppressWarnings("unchecked")
    private Object processRecord(Map<String, Object> schemaNode) throws IOException, ConfigurationProcessingException {
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
     * @throws ConfigurationProcessingException configuration processing
     * exception
     */
    private Object processField(Map<String, Object> fieldDefinition) throws IOException, ConfigurationProcessingException {
        Object nameField = fieldDefinition.get(NAME_FIELD);
        // if this a "uuid" type then generate it
        if (UUID_FIELD.equals(nameField)) {
            Schema avroSchema = schemaParser.getTypes().get(UUID_TYPE);
            return generateUUIDObject(avroSchema);
        }

        return processType(fieldDefinition, fieldDefinition.get(BY_DEFAULT_FIELD));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootConfiguration()
     */
    @Override
    public final GenericRecord getRootConfiguration() throws ConfigurationProcessingException {
        int dotIndex = rootSchemaName.lastIndexOf('.');
        return getConfigurationByName(rootSchemaName.substring(dotIndex + 1), rootSchemaName.substring(0, dotIndex));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootJsonConfiguration()
     */
    @Override
    public final String getRootJsonConfiguration() throws IOException, ConfigurationProcessingException {
        GenericRecord root = getRootConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(root.getSchema());
        try {
            return converter.endcodeToJson(root);
        } catch (RuntimeException e) {
            // NPE is thrown if "null" was written into a field that is not nullable
            // CCE is thrown if value of wrong type was written into a field
            LOG.error("Unexpected exception occurred while generating configuration.", e);
            throw new ConfigurationProcessingException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootJsonBytesConfiguration()
     */
    @Override
    public final byte[] getRootJsonBytesConfiguration() throws IOException, ConfigurationProcessingException {
        GenericRecord root = getRootConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(root.getSchema());
        try {
            return converter.encodeToJsonBytes(root);
        } catch (RuntimeException e) {
            // NPE is thrown if "null" was written into a field that is not nullable
            // CCE is thrown if value of wrong type was written into a field
            LOG.error("Unexpected exception occurred while generating configuration.", e);
            throw new ConfigurationProcessingException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootBinaryConfiguration()
     */
    @Override
    public final byte[] getRootBinaryConfiguration() throws IOException, ConfigurationProcessingException {
        GenericRecord root = getRootConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(root.getSchema());
        try {
            return converter.encode(root);
        } catch (RuntimeException e) {
            // NPE is thrown if "null" was written into a field that is not nullable
            // CCE is thrown if value of wrong type was written into a field
            LOG.error("Unexpected exception occurred while generating configuration.", e);
            throw new ConfigurationProcessingException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#generateUuidFields(byte[])
     */
    @Override
    public byte[] generateUuidFields(byte[] configurationBody) throws IOException {
        byte[] config = null;
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(baseSchema);
        GenericContainer genericContainer = converter.decodeBinary(configurationBody);
        GenericRecord mainRecord = null;
        if (genericContainer instanceof GenericRecord) {
            mainRecord = (GenericRecord) genericContainer;
            generateUuidField(mainRecord);
            LOG.info("Got record: {}", mainRecord);
        }
        if(mainRecord != null) {
            config = converter.encode(mainRecord);
        }
        LOG.trace("Generated uuid fields for records {}", mainRecord);
        return config;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#generateUuidFields(org.apache.avro.generic.GenericContainer)
     */
    @Override
    public byte[] generateUuidFields(GenericContainer container) throws IOException {
        byte[] config = null;
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(baseSchema);
        if (container instanceof GenericRecord) {
            GenericRecord mainRecord = (GenericRecord) container;
            generateUuidField(mainRecord);
            LOG.info("Got record: {}", mainRecord);
            if (mainRecord != null) {
                config = converter.encodeToJsonBytes(mainRecord);
            }
            LOG.trace("Generated uuid fields for records {}", mainRecord);
        }
        return config;
    }

    /**
     * Generate uuid field. If empty uuid fields exists in the record,
     * the new values will be generated.
     *
     * @param currentRecord the current record
     * @return the generic record with generated uuid fields
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private GenericRecord generateUuidField(GenericRecord currentRecord) {
        if (currentRecord != null) {
            Schema currentSchema = currentRecord.getSchema();
            LOG.debug("Current record with name {}", currentSchema.getFullName());
            Schema.Field uuidField = currentSchema.getField(UUID_FIELD);
            if (uuidField != null) {
                Object uuidValue = currentRecord.get(uuidField.pos());
                if (uuidValue == null) {
                    GenericData.Fixed uuid = generateUUIDObject();
                    LOG.info("Generate new uuid {}", uuid);
                    currentRecord.put(uuidField.pos(), uuid);
                } else {
                    LOG.info("Validate existing uuid {}", uuidValue);
                }
            }

            List<Schema.Field> fields = currentSchema.getFields();
            if (fields != null && !fields.isEmpty()) {
                for (Schema.Field field : fields) {
                    int position = field.pos();
                    Object value = currentRecord.get(position);
                    if (value instanceof GenericRecord) {
                        LOG.debug("Found record value {}", value);
                        GenericRecord record = (GenericRecord) value;
                        currentRecord.put(position, generateUuidField(record));
                    } else if (value instanceof GenericArray) {
                        LOG.debug("Found array value {}", value);
                        GenericArray array = (GenericArray) value;
                        if (array != null) {
                            LOG.debug("Found array type with name {}", array.getSchema().getFullName());
                            int size = array.size();
                            for (int i = 0; i < size; i++) {
                                Object item = array.get(i);
                                if (item instanceof GenericRecord) {
                                    GenericRecord itemRecord = (GenericRecord) item;
                                    array.set(i, generateUuidField(itemRecord));
                                }
                            }
                            currentRecord.put(position, array);
                        }
                    }
                }
            }
        }
        return currentRecord;
    }

    /**
     * Find raw schema by name.
     *
     * @param root the root record
     * @param schemaName the schema name
     * @param schemaNamespace the schema namespace
     * @return the map
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Map<String, Object> findRawSchemaByName(Map<String, Object> root, String schemaName, String schemaNamespace) {
        String name = (String) root.get(NAME_FIELD);
        String namespace = (String) root.get(NAMESPACE_FIELD);
        // looking for node that has child nodes 'name' and 'namespace' with corresponding values
        if (schemaName.equals(name) && schemaNamespace.equals(namespace)) {
            return root;
        } else {
            for (Map.Entry<String, Object> entry : root.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List items = (List) entry.getValue();
                    for (Object item : items) {
                        if (item instanceof Map) {
                            Map<String, Object> foundSchema = findRawSchemaByName((Map<String, Object>) item, schemaName, schemaNamespace);
                            if (foundSchema != null) {
                                return foundSchema;
                            }
                        }
                    }
                } else if (entry.getValue() instanceof Map) {
                    Map<String, Object> foundSchema = findRawSchemaByName((Map<String, Object>) entry.getValue(), schemaName, schemaNamespace);
                    if (foundSchema != null) {
                        return foundSchema;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public final GenericRecord getConfigurationByName(String name, String namespace) throws ConfigurationProcessingException {
        if (name == null || namespace == null) {
            return null;
        }
        if (processedTypes.containsKey(namespace + "." + name)) {
            return (GenericRecord) processedTypes.get(namespace + "." + name);
        }
        Map<String, Object> schema = findRawSchemaByName(rawBaseSchema, name, namespace);
        if (schema != null) {
            try {
                return (GenericRecord) processType(schema, null);
            } catch (IOException ioe) {
                LOG.error("Unexpected exception occurred while generating configuration.", ioe);
                throw new ConfigurationProcessingException(ioe);
            }
        }
        return null;
    }
}
