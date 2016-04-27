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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.TextNode;
import org.kaaproject.avro.ui.shared.ArrayField;
import org.kaaproject.avro.ui.shared.FormContext;
import org.kaaproject.avro.ui.shared.FormField;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.avro.ui.shared.UnionField;

/**
 * The Class EcfSchemaFormAvroConverter.
 */
public class EcfSchemaFormAvroConverter extends SimpleSchemaFormAvroConverter {

    /** The Constant EVENT. */
    private static final String EVENT = "EVENT";

    /** The Constant OBJECT. */
    private static final String OBJECT = "OBJECT";

    /** The Constant CLASS_TYPE. */
    private static final String CLASS_TYPE = "classType";
    
    /** The Constant CLASS_TYPE_TYPE_NAME. */
    private static final String CLASS_TYPE_TYPE_NAME = "ClassType";
    
    /** The event class types. */
    private static Set<String> eventClassTypes = new HashSet<>(); 
    static {
        eventClassTypes.add(RECORD_FIELD_TYPE);
        eventClassTypes.add(ENUM_FIELD_TYPE);
        eventClassTypes.add(FIXED_FIELD_TYPE);
    }
    
    /**
     * Instantiates a new ecf schema form avro converter.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public EcfSchemaFormAvroConverter() throws IOException {
        super();
    }
    
    /**
     * Creates the class type field.
     *
     * @return the field
     */
    private Field createClassTypeField() {
        List<String> classTypeSymbols = Arrays.asList(OBJECT, EVENT);        
        Schema classTypeEnum = Schema.createEnum(CLASS_TYPE_TYPE_NAME, null, 
                BASE_SCHEMA_FORM_NAMESPACE, classTypeSymbols);        
        Field classTypeField = new Field(CLASS_TYPE, classTypeEnum, null, null);
        classTypeField.addProp(DISPLAY_NAME, "Class type");        
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;        
        ArrayNode displayNamesNode = jsonFactory.arrayNode();
        displayNamesNode.add(TextNode.valueOf("Object"));
        displayNamesNode.add(TextNode.valueOf("Event"));        
        classTypeField.addProp(DISPLAY_NAMES, displayNamesNode);
        classTypeField.addProp(DISPLAY_PROMPT, "Select class type");
        classTypeField.addProp(BY_DEFAULT, OBJECT);
        return classTypeField;
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#createConverterSchema()
     */
    @Override
    protected Schema createConverterSchema() throws IOException {
        Schema initialSchema = getBaseSchemaFormSchema();
        Map<String, Schema> recordSchemaMap = new HashMap<>();
        copySchema(initialSchema, recordSchemaMap);
        return recordSchemaMap.get(BASE_SCHEMA_FORM_NAMESPACE + "." + UNION_FIELD_TYPE);
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeRecordFields(org.apache.avro.Schema, java.util.List)
     */
    @Override
    protected void customizeRecordFields(Schema recordSchema, List<Field> fields) {
        super.customizeRecordFields(recordSchema, fields);
        if (eventClassTypes.contains(recordSchema.getName())) {
            int index = getFieldIndex(fields, RECORD_NAMESPACE);
            if (index > -1) {
                fields.add(index+1, createClassTypeField());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeType(org.apache.avro.generic.GenericData.Record, org.apache.avro.Schema)
     */
    @Override
    protected void customizeType(Record record, Schema fieldTypeSchema) {
        if (record != null && eventClassTypes.contains(record.getSchema().getName())) {
            JsonNode classTypeNode = fieldTypeSchema.getJsonProp(CLASS_TYPE);
            Schema enumSchema = record.getSchema().getField(CLASS_TYPE).schema();
            if (classTypeNode != null && classTypeNode.isTextual()) {
                record.put(CLASS_TYPE, 
                        new GenericData.EnumSymbol(enumSchema, 
                                classTypeNode.asText().toUpperCase()));
            } else {
                record.put(CLASS_TYPE, new GenericData.EnumSymbol(enumSchema, OBJECT));
            }
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeFieldSchema(org.apache.avro.Schema, org.apache.avro.generic.GenericRecord)
     */
    @Override
    protected void customizeFieldSchema(Schema fieldSchema, GenericRecord fieldType) {
        if (eventClassTypes.contains(fieldType.getSchema().getName())) {
            GenericData.EnumSymbol classType = (GenericData.EnumSymbol)fieldType.get(CLASS_TYPE);
            if (classType != null) {
                fieldSchema.addProp(CLASS_TYPE, classType.toString().toLowerCase());
            } else {
                fieldSchema.addProp(CLASS_TYPE, OBJECT.toLowerCase());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeUiForm(org.kaaproject.avro.ui.shared.RecordField)
     */
    @Override
    protected RecordField customizeUiForm(RecordField field) {
        field.setDisplayName("Event class family schema");
        ArrayField acceptableValuesField = (ArrayField)field.getFieldByName(ACCEPTABLE_VALUES);
        acceptableValuesField.setDisplayName("Event classes");
        UnionField valuesUnion = (UnionField)acceptableValuesField.getElementMetadata();
        valuesUnion.finalizeMetadata();
        valuesUnion.setOptional(false);
        valuesUnion.setDisplayName("Event class");
        updatePossibleEventClasses(valuesUnion, true);
        List<FormField> acceptableValues = valuesUnion.getAcceptableValues();
        for (FormField acceptableValue : acceptableValues) {
            RecordField recordValue = (RecordField)acceptableValue;
            recordValue.getFieldByName(RECORD_NAME).setKeyIndex(1);
            recordValue.getFieldByName(RECORD_NAME).setWeight(0.3f);
            recordValue.getFieldByName(RECORD_NAMESPACE).setKeyIndex(2);
            recordValue.getFieldByName(RECORD_NAMESPACE).setWeight(0.5f);
            recordValue.getFieldByName(CLASS_TYPE).setKeyIndex(3);
            recordValue.getFieldByName(CLASS_TYPE).setWeight(0.2f);
        }
        if (acceptableValuesField.getValue() != null) {
            for (FormField row : acceptableValuesField.getValue()) {
                row.setOptional(false);
                UnionField rowUnion = (UnionField)row;
                rowUnion.setDisplayName("Event class");
                updatePossibleEventClasses(rowUnion, true);
                updatePossibleEventClasses((RecordField)rowUnion.getValue());
            }
        }       
        
        //RecordField rootRecord = field.getRootRecord();
        FormContext context = field.getContext();
        RecordField rec = context.getRecordMetadata(BASE_SCHEMA_FORM_NAMESPACE, FIELD);
        UnionField typeField = (UnionField)rec.getFieldByName(FIELD_TYPE);
        updatePossibleEventClasses(typeField, false);
        rec = context.getRecordMetadata(BASE_SCHEMA_FORM_NAMESPACE, ARRAY_FIELD_TYPE);
        typeField = (UnionField)rec.getFieldByName(ARRAY_ITEM);
        updatePossibleEventClasses(typeField, false);
        rec = context.getRecordMetadata(BASE_SCHEMA_FORM_NAMESPACE, UNION_FIELD_TYPE);
        acceptableValuesField = (ArrayField)rec.getFieldByName(ACCEPTABLE_VALUES);
        typeField = (UnionField)acceptableValuesField.getElementMetadata();
        updatePossibleEventClasses(typeField, false);
        return field;
    }
    
    /**
     * Update possible event classes.
     *
     * @param valuesUnion the values union
     * @param include the include
     */
    private void updatePossibleEventClasses(UnionField valuesUnion, boolean include) {
        List<FormField> acceptableValues = valuesUnion.getAcceptableValues();
        List<FormField> filteredAcceptableValues = new ArrayList<>();
        for (FormField acceptableValue : acceptableValues) {
            if (acceptableValue != null && acceptableValue instanceof RecordField) {
                RecordField recordValue = (RecordField)acceptableValue;
                if (include && eventClassTypes.contains(recordValue.getFqn().getName())) {
                    filteredAcceptableValues.add(acceptableValue);
                } else if (!include && !eventClassTypes.contains(recordValue.getFqn().getName())) {
                    filteredAcceptableValues.add(acceptableValue);
                }
            }
        }
        valuesUnion.setAcceptableValues(filteredAcceptableValues);
    }
    
    /**
     * Update possible event classes.
     *
     * @param row the row
     */
    private void updatePossibleEventClasses(RecordField row) {
        if (row.getTypeFullname().equals(BASE_SCHEMA_FORM_NAMESPACE + "." + RECORD_FIELD_TYPE)) {
            ArrayField arrayField = (ArrayField)row.getFieldByName(FIELDS);
            for (FormField valueRow : arrayField.getValue()) {
                UnionField fieldType = (UnionField)((RecordField)valueRow).getFieldByName(FIELD_TYPE);
                updatePossibleEventClasses(fieldType, false);
            }
        }
    }

}
