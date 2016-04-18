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

package org.kaaproject.kaa.server.common.core.algorithms.generation;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.BY_DEFAULT_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
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
public class DefaultRecordGenerationAlgorithmImpl<U extends KaaSchema, T extends KaaData<U>> implements DefaultRecordGenerationAlgorithm<T> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultRecordGenerationAlgorithmImpl.class);

    /** The processed types. */
    private final Map<String, GenericRecord> processedTypes = new HashMap<>();

    /** The avro schema parser. */
    private final Schema.Parser avroSchemaParser;

    /** The avro base schema. */
    private final Schema avroBaseSchema;

    /** The data factory. */
    private final KaaDataFactory<U, T> dataFactory;

    /** The root schema. */
    private final U rootSchema;

    /**
     * Instantiates a new default configuration processor.
     *
     * @param kaaSchema the base schema
     * @throws ConfigurationGenerationException the configuration processing exception
     */
    public DefaultRecordGenerationAlgorithmImpl(U kaaSchema, KaaDataFactory<U, T> factory) throws ConfigurationGenerationException {
        LOG.debug("Generating default configuration for configuration schema: " + kaaSchema.getRawSchema());

        this.rootSchema = kaaSchema;
        this.dataFactory = factory;
        this.avroSchemaParser = new Schema.Parser();
        this.avroBaseSchema = this.avroSchemaParser.parse(kaaSchema.getRawSchema());
    }


    /**
     * Applies the default value.
     *
     * @param schemaNode the schema node.
     * @param byDefault the default value.
     * @return generated value.
     * @throws ConfigurationGenerationException the configuration processing exception.
     */
    private Object applyDefaultValue(Schema schemaNode, JsonNode byDefault) throws ConfigurationGenerationException {
        if (byDefault.isArray() && AvroUtils.getSchemaByType(schemaNode, Type.BYTES) != null) {
            // if this is a 'bytes' type then convert json bytes array to
            // avro 'bytes' representation or
            // if this is a named type - look for already processed types
            // or throw an exception because "by_default" is missed
            ByteBuffer byteBuffer = ByteBuffer.allocate(byDefault.size());
            for (JsonNode oneByte : byDefault) {
                byteBuffer.put((byte) oneByte.asInt());
            }
            byteBuffer.flip();
            return byteBuffer;
        }
        if (byDefault.isBoolean() && AvroUtils.getSchemaByType(schemaNode, Type.BOOLEAN) != null) {
            return byDefault.asBoolean();
        }
        if (byDefault.isDouble()) {
            if (AvroUtils.getSchemaByType(schemaNode, Type.DOUBLE) != null) {
                return byDefault.asDouble();
            } else if (AvroUtils.getSchemaByType(schemaNode, Type.FLOAT) != null) {
                return (float) byDefault.asDouble();
            }
        }
        if (byDefault.isIntegralNumber() && AvroUtils.getSchemaByType(schemaNode, Type.INT) != null) {
            return byDefault.asInt();
        }
        if (byDefault.isIntegralNumber() && AvroUtils.getSchemaByType(schemaNode, Type.LONG) != null) {
            return byDefault.asLong();
        }
        if (byDefault.isTextual()) {
            Schema enumSchema = AvroUtils.getSchemaByType(schemaNode, Type.ENUM);
            if (enumSchema != null) {
                String textDefaultValue = byDefault.asText();
                if (enumSchema.hasEnumSymbol(textDefaultValue)) {
                    return new GenericData.EnumSymbol(enumSchema, textDefaultValue);
                }
            }
            if (AvroUtils.getSchemaByType(schemaNode, Type.STRING) != null) {
                return byDefault.asText();
            }
        }
        throw new ConfigurationGenerationException("Default value " + byDefault.toString() + " is not applicable for the field");
    }

    /**
     * Processes generic type.
     *
     * @param schemaNode schema for current type.
     * @param byDefault the by default.
     * @return generated value for input type.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     */
    private Object processType(Schema schemaNode, JsonNode byDefault) throws ConfigurationGenerationException {
        if (byDefault != null && !byDefault.isNull()) {
            return applyDefaultValue(schemaNode, byDefault);
        }
        if (AvroUtils.getSchemaByType(schemaNode, Type.NULL) != null) {
            return null;
        }

        Schema schemaToProcess = schemaNode;
        if (schemaToProcess.getType().equals(Type.UNION)) {
            schemaToProcess = schemaToProcess.getTypes().get(0);
        }
        switch (schemaToProcess.getType()) {
        case ARRAY:
            // if this an array type then return empty array instance
            return processArray();
        case RECORD:
            return processRecord(schemaToProcess);
        case FIXED:
            return processFixed(schemaToProcess);
        case ENUM:
            return processEnum(schemaToProcess);
        case BYTES:
            ByteBuffer byteBuffer = ByteBuffer.allocate(byDefault.size());
            byteBuffer.put((byte) 0);
            byteBuffer.flip();
            return byteBuffer;
        case MAP:
            throw new ConfigurationGenerationException("Map is not supported.");
        case INT:
            return new Integer(0);
        case BOOLEAN:
            return Boolean.FALSE;
        case DOUBLE:
            return new Double(0.0);
        case LONG:
            return new Long(0);
        case STRING:
            return new String("");
        case FLOAT:
            return new Float(0.0);
        default:
            return null;
        }
    }

    /**
     * Processes record type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input record type.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     */
    private Object processRecord(Schema schemaNode) throws ConfigurationGenerationException {
        GenericRecord result = new GenericData.Record(schemaNode);
        processedTypes.put(schemaNode.getFullName(), result);

        // process each field
        List<Field> fields = schemaNode.getFields();
        for (Field field : fields) {
            Object processFieldResult = processField(field);
            if (processFieldResult != null) {
                result.put(field.name(), processFieldResult);
            }
        }

        return result;
    }

    /**
     * Processes array type.
     *
     * @return generated value for input array type.
     */
    private Object processArray() {
        Schema elementTypeSchema = Schema.create(Type.NULL);
        return new GenericData.Array<>(0, Schema.createArray(elementTypeSchema));
    }

    /**
     * Processes enum type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input enum type.
     */
    private Object processEnum(Schema schemaNode) {
        GenericEnumSymbol result = new GenericData.EnumSymbol(schemaNode, schemaNode.getEnumSymbols().get(0));
        return result;
    }

    /**
     * Processes fixed type.
     *
     * @param schemaNode schema for current type.
     * @return generated value for input record type.
     */
    private Object processFixed(Schema schemaNode) {
        int size = schemaNode.getFixedSize();

        byte [] bytes = new byte [size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) 0;
        }

        GenericFixed result = new GenericData.Fixed(schemaNode, bytes);

        return result;
    }

    /**
     * Process field of a record type.
     *
     * @param fieldDefinition schema for field.
     * @return generated value for field based on its definition.
     * @throws ConfigurationGenerationException configuration processing
     * exception
     */
    private Object processField(Field fieldDefinition) throws ConfigurationGenerationException {
        // if this a "uuid" type then generate it
        if (UUID_FIELD.equals(fieldDefinition.name())) {
            return AvroUtils.generateUuidObject();
        }

        return processType(fieldDefinition.schema(), fieldDefinition.getJsonProp(BY_DEFAULT_FIELD));
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootConfiguration()
     */
    @Override
    public final GenericRecord getRootConfiguration() throws ConfigurationGenerationException {
        return getConfigurationByName(avroBaseSchema.getName(), avroBaseSchema.getNamespace());
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessor#getRootJsonConfiguration()
     */
    @Override
    public final T getRootData() throws IOException, ConfigurationGenerationException {
        GenericRecord root = getRootConfiguration();
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(root.getSchema());
        try {
            return dataFactory.createData(rootSchema, converter.encodeToJson(root));
        } catch (RuntimeException e) {
            // NPE is thrown if "null" was written into a field that is not nullable
            // CGE is thrown if value of wrong type was written into a field
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
            return processedTypes.get(namespace + "." + name);
        }
        Schema schema = avroSchemaParser.getTypes().get(namespace + "." + name);
        if (schema != null) {
            return (GenericRecord) processType(schema, null);
        }
        return null;
    }
}
