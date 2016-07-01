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

package org.kaaproject.kaa.server.admin.services.schema;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.BooleanNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.TextNode;
import org.kaaproject.avro.ui.converter.CtlSource;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;
import org.kaaproject.avro.ui.shared.ArrayField.OverrideStrategy;
import org.kaaproject.avro.ui.shared.FormField.FieldAccess;
import org.kaaproject.avro.ui.shared.RecordField;

/**
 * The Class ConfigurationSchemaFormAvroConverter.
 */
public class ConfigurationSchemaFormAvroConverter extends SchemaFormAvroConverter {

    /** The Constant OVERRIDE_STRATEGY_TYPE_NAME. */
    private static final String OVERRIDE_STRATEGY_TYPE_NAME = "OverrideStrategy";
    
    /**
     * Instantiates a new configuration schema form avro converter.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ConfigurationSchemaFormAvroConverter() throws IOException {
        super();
    }


    public ConfigurationSchemaFormAvroConverter(CtlSource ctlSource) throws IOException {
        super(ctlSource);
    }

    /**
     * Creates the addressable field.
     *
     * @return the field
     */
    private Field createAddressableField() {
        Field addressableField = new Field(ADDRESSABLE, Schema.createUnion(Arrays.asList(
                Schema.create(Type.BOOLEAN), Schema.create(Type.NULL))), null, null);
        addressableField.addProp(DISPLAY_NAME, "Is addressable");
        addressableField.addProp(BY_DEFAULT, BooleanNode.valueOf(true));
        addressableField.addProp(DISPLAY_PROMPT, "Click to enable/disable partial updates support");
        return addressableField;
    }
    
    /**
     * Creates the override strategy field.
     *
     * @return the field
     */
    private Field createOverrideStrategyField() {
        List<String> overrideStrategySymbols = Arrays.asList(OverrideStrategy.APPEND.name(), 
                OverrideStrategy.REPLACE.name());        
        Schema overrideStrategyEnum = Schema.createEnum(OVERRIDE_STRATEGY_TYPE_NAME, null, 
                BASE_SCHEMA_FORM_NAMESPACE, overrideStrategySymbols);        
        Field overrideStrategyField = new Field(OVERRIDE_STRATEGY,  Schema.createUnion(Arrays.asList(
                overrideStrategyEnum, Schema.create(Type.NULL))), null, null);
        overrideStrategyField.addProp(DISPLAY_NAME, "Override strategy");        
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;        
        ArrayNode displayNamesNode = jsonFactory.arrayNode();
        displayNamesNode.add(TextNode.valueOf("Append"));
        displayNamesNode.add(TextNode.valueOf("Replace"));        
        overrideStrategyField.addProp(DISPLAY_NAMES, displayNamesNode);
        overrideStrategyField.addProp(DISPLAY_PROMPT, "Select array override strategy");
        return overrideStrategyField;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeRecordFields(org.apache.avro.Schema, java.util.List)
     */
    @Override
    protected void customizeRecordFields(Schema recordSchema, List<Field> fields) {
        if (recordSchema.getName().equals(RECORD_FIELD_TYPE)) {
            int index = getFieldIndex(fields, FIELDS);
            if (index > -1) {
                fields.add(index, createAddressableField());
            }
        } else if (recordSchema.getName().equals(ARRAY_FIELD_TYPE)) {
            int index = getFieldIndex(fields, ARRAY_ITEM);
            if (index > -1) {
                fields.add(index, createOverrideStrategyField());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeType(org.apache.avro.generic.GenericData.Record, org.apache.avro.Schema)
     */
    @Override
    protected void customizeType(Record record, Schema fieldTypeSchema) {
        if (record != null && record.getSchema().getName().equals(RECORD_FIELD_TYPE)) {
            JsonNode addressableNode = fieldTypeSchema.getJsonProp(ADDRESSABLE);
            if (addressableNode != null && addressableNode.isBoolean()) {
                record.put(ADDRESSABLE, addressableNode.asBoolean());
            } else {
                record.put(ADDRESSABLE, true);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeFormField(org.apache.avro.generic.GenericData.Record, org.apache.avro.Schema.Field)
     */
    @Override
    protected void customizeFormField(Record fieldType, Field field) {
        if (fieldType.getSchema().getName().equals(ARRAY_FIELD_TYPE)) {
            JsonNode overrideStrategyNode = field.getJsonProp(OVERRIDE_STRATEGY);
            Schema overrideStrategySchema = 
                    fieldType.getSchema().getField(OVERRIDE_STRATEGY).schema();
            if (overrideStrategyNode != null && overrideStrategyNode.isTextual()) {
                fieldType.put(OVERRIDE_STRATEGY, 
                        new GenericData.EnumSymbol(overrideStrategySchema, overrideStrategyNode.asText().toUpperCase()));
            } else {
                fieldType.put(OVERRIDE_STRATEGY, 
                        new GenericData.EnumSymbol(overrideStrategySchema, OverrideStrategy.REPLACE.name()));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeFieldSchema(org.apache.avro.Schema, org.apache.avro.generic.GenericRecord)
     */
    @Override
    protected void customizeFieldSchema(Schema fieldSchema, GenericRecord fieldType) {
        if (fieldType != null && fieldType.getSchema().getName().equals(RECORD_FIELD_TYPE)) {
            Boolean addressable = (Boolean) fieldType.get(ADDRESSABLE);
            if (addressable != null && !addressable) {
                fieldSchema.addProp(ADDRESSABLE, BooleanNode.getFalse());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeSchemaField(org.apache.avro.Schema.Field, org.apache.avro.generic.GenericData.Record)
     */
    @Override
    protected void customizeSchemaField(Field avroField, Record fieldType) {
        if (fieldType.getSchema().getName().equals(ARRAY_FIELD_TYPE)) {
            GenericData.EnumSymbol overrideStrategy = 
                    (GenericData.EnumSymbol)fieldType.get(OVERRIDE_STRATEGY);
            if (overrideStrategy != null && !overrideStrategy.toString().equalsIgnoreCase(OverrideStrategy.REPLACE.name())) {
                avroField.addProp(OVERRIDE_STRATEGY, overrideStrategy.toString().toLowerCase());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeUiForm(org.kaaproject.avro.ui.shared.RecordField)
     */
    @Override
    protected RecordField customizeUiForm(RecordField field) {
        field.setDisplayName("Configuration schema");
        field.getFieldByName(ADDRESSABLE).setFieldAccess(FieldAccess.HIDDEN);
        return field;
    }

}
