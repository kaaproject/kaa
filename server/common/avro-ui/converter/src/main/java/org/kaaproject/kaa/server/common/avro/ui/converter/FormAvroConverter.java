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

package org.kaaproject.kaa.server.common.avro.ui.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.EnumSymbol;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.codehaus.jackson.JsonNode;
import org.kaaproject.kaa.server.common.avro.ui.shared.ArrayField;
import org.kaaproject.kaa.server.common.avro.ui.shared.BooleanField;
import org.kaaproject.kaa.server.common.avro.ui.shared.EnumField;
import org.kaaproject.kaa.server.common.avro.ui.shared.FieldType;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormEnum;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormField;
import org.kaaproject.kaa.server.common.avro.ui.shared.InputType;
import org.kaaproject.kaa.server.common.avro.ui.shared.IntegerField;
import org.kaaproject.kaa.server.common.avro.ui.shared.LongField;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.common.avro.ui.shared.SizedField;
import org.kaaproject.kaa.server.common.avro.ui.shared.StringField;
import org.kaaproject.kaa.server.common.avro.ui.shared.UnionField;

/**
 * The Class FormAvroConverter.
 */
public class FormAvroConverter {

    /** The Constant DISPLAY_NAME. */
    private static final String DISPLAY_NAME = "displayName";
    
    /** The Constant DISPLAY_NAMES. */
    private static final String DISPLAY_NAMES = "displayNames";
    
    /** The Constant WEIGHT. */
    private static final String WEIGHT = "weight";
    
    /** The Constant MIN_ROW_COUNT. */
    private static final String MIN_ROW_COUNT = "minRowCount";
    
    /** The Constant MAX_LENGTH. */
    private static final String MAX_LENGTH = "maxLength";
    
    /** The Constant OPTIONAL. */
    private static final String OPTIONAL = "optional";
    
    /** The Constant INPUT_TYPE. */
    private static final String INPUT_TYPE = "inputType";

    /**
     * Creates the record field from schema.
     *
     * @param schema the schema
     * @return the record field
     */
    public static RecordField createRecordFieldFromSchema(Schema schema) {
        RecordField recordField = new RecordField();
        JsonNode displayNameVal = schema.getJsonProp(DISPLAY_NAME);
        String displayName = schema.getFullName();
        if (displayNameVal != null && displayNameVal.isTextual()) {
            displayName = displayNameVal.asText();
        }
        recordField.setDisplayName(displayName);
        recordField.setTypeName(schema.getName());
        recordField.setTypeNamespace(schema.getNamespace());
        
        parseFields(recordField, schema);
        return recordField;
    }
    
    /**
     * Parses the fields.
     *
     * @param formData the form data
     * @param schema the schema
     */
    private static void parseFields(RecordField formData, Schema schema) {
        List<Field> schemaFields = schema.getFields();
        for (Field field : schemaFields) {
            String fieldName = field.name();
            String displayName = fieldName;
            JsonNode displayNameVal = field.getJsonProp(DISPLAY_NAME);
            if (displayNameVal != null && displayNameVal.isTextual()) {
                displayName = displayNameVal.asText();
            }
            JsonNode optionalVal = field.getJsonProp(OPTIONAL);
            boolean optional = false;
            if (optionalVal != null && optionalVal.isBoolean()) {
                optional = optionalVal.asBoolean();
            }
            FieldType fieldType = toFieldType(field.schema());
            FormField formField;
            if (fieldType == FieldType.UNION) {
                UnionField unionField = createField(fieldType, fieldName, displayName, optional);
                List<RecordField> acceptableValues = new ArrayList<>();
                List<Schema> acceptableTypes = field.schema().getTypes();
                for (int i=0;i<acceptableTypes.size();i++) {
                    RecordField acceptableValue = createRecordFieldFromSchema(acceptableTypes.get(i));
                    acceptableValues.add(acceptableValue);
                }
                unionField.setAcceptableValues(acceptableValues);
                formField = unionField;
            }
            else if (fieldType == FieldType.RECORD) {
                RecordField recordField = createField(fieldType, fieldName, displayName, optional);
                recordField.setTypeName(field.schema().getName());
                recordField.setTypeNamespace(field.schema().getNamespace());
                parseFields(recordField, field.schema());
                formField = recordField;
            }
            else if (fieldType == FieldType.ARRAY) {
                ArrayField arrayField = createField(fieldType, fieldName, displayName, optional);
                JsonNode minRowCountVal = field.getJsonProp(MIN_ROW_COUNT);
                if (minRowCountVal != null && minRowCountVal.isInt()) {
                    arrayField.setMinRowCount(minRowCountVal.asInt());
                }
                RecordField elementMetadata = createRecordFieldFromSchema(field.schema().getElementType());
                arrayField.setElementMetadata(elementMetadata);
                for (int i=0; i<arrayField.getMinRowCount();i++) {
                    arrayField.addArrayData((RecordField) elementMetadata.clone());
                }
                formField = arrayField;
            }
            else {
                JsonNode defaultValueVal = field.defaultValue();
                if (fieldType == FieldType.ENUM) {
                    EnumField enumField = createField(fieldType, fieldName, displayName, optional);
                    List<String> enumSymbols = field.schema().getEnumSymbols();
                    List<FormEnum> enumValues = new ArrayList<>(enumSymbols.size());
                    JsonNode displayNamesNode = field.getJsonProp(DISPLAY_NAMES);
                    for (int i=0;i<enumSymbols.size();i++) {
                        String enumSymbol = enumSymbols.get(i);
                        String displayValue = enumSymbol;
                        if (displayNamesNode != null && displayNamesNode.isArray()) {
                            displayValue = displayNamesNode.get(i).getTextValue();
                        }
                        FormEnum formEnum = new FormEnum(enumSymbol, displayValue);
                        enumValues.add(formEnum);
                    }
                    enumField.setEnumValues(enumValues);
                    String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    enumField.setDefaultValueFromSymbol(defaultValue);
                    enumField.setValueFromSymbol(defaultValue);
                    formField = enumField;
                }
                else if (fieldType == FieldType.BOOLEAN) {
                    BooleanField booleanField = createField(fieldType, fieldName, displayName, optional);
                    Boolean defaultValue = convertJsonValue(fieldType, defaultValueVal);
                    booleanField.setDefaultValue(defaultValue);
                    booleanField.setValue(defaultValue);
                    formField = booleanField;
                }
                else {
                    SizedField sizedField = createField(fieldType, fieldName, displayName, optional);
                    JsonNode maxLengthVal = field.getJsonProp(MAX_LENGTH);
                    if (maxLengthVal != null && maxLengthVal.isInt()) {
                        sizedField.setMaxLength(maxLengthVal.asInt());
                    }
                    if (fieldType == FieldType.STRING) {
                        String defaultValue = convertJsonValue(fieldType, defaultValueVal);
                        ((StringField)sizedField).setDefaultValue(defaultValue);
                        ((StringField)sizedField).setValue(defaultValue);
                        JsonNode inputTypeNode = field.getJsonProp(INPUT_TYPE);
                        if (inputTypeNode != null && inputTypeNode.isTextual()) {
                            InputType inputType = InputType.valueOf(inputTypeNode.asText().toUpperCase());
                            ((StringField)sizedField).setInputType(inputType);
                        }
                    }
                    else if (fieldType == FieldType.INTEGER) {
                        Integer defaultValue = convertJsonValue(fieldType, defaultValueVal);
                        ((IntegerField)sizedField).setDefaultValue(defaultValue);
                        ((IntegerField)sizedField).setValue(defaultValue);
                    }
                    else if (fieldType == FieldType.LONG) {
                        Long defaultValue = convertJsonValue(fieldType, defaultValueVal);
                        ((LongField)sizedField).setDefaultValue(defaultValue);
                        ((LongField)sizedField).setValue(defaultValue);
                    }
                    formField = sizedField;
                }
                JsonNode weightVal = field.getJsonProp(WEIGHT);
                if (weightVal != null && weightVal.isNumber()) {
                    Number weight = weightVal.getNumberValue();
                    formField.setWeight(weight.floatValue());
                }
            }
            formData.addField(formField);
        }
    }
    
    /**
     * Creates the field.
     *
     * @param <T> the generic type
     * @param type the type
     * @param fieldName the field name
     * @param displayName the display name
     * @param optional the optional
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private static <T extends FormField> T createField(FieldType type, 
            String fieldName, 
            String displayName, 
            boolean optional) {
        T field = null;
        switch (type) {
        case STRING:
            field = (T) new StringField(fieldName, displayName, optional);
            break;
        case ARRAY:
            field = (T) new ArrayField(fieldName, displayName, optional);
            break;
        case BOOLEAN:
            field = (T) new BooleanField(fieldName, displayName, optional);
            break;
        case ENUM:
            field = (T) new EnumField(fieldName, displayName, optional);
            break;
        case INTEGER:
            field = (T) new IntegerField(fieldName, displayName, optional);
            break;
        case LONG:
            field = (T) new LongField(fieldName, displayName, optional);
            break;
        case RECORD:
            field = (T) new RecordField(fieldName, displayName, optional);
            break;
        case UNION:
            field = (T) new UnionField(fieldName, displayName, optional);
            break;
        default:
            break;
        }
        
        return field;
    }
    
    /**
     * Convert json value.
     *
     * @param <T> the generic type
     * @param type the type
     * @param jsonValue the json value
     * @return the t
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertJsonValue(FieldType type, JsonNode jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        T value = null;
        switch (type) {
        case BOOLEAN:
            value = (T) new Boolean(jsonValue.asBoolean());
            break;
        case INTEGER:
            value = (T) new Integer(jsonValue.asInt());
            break;
        case LONG:
            value = (T) new Long(jsonValue.asLong());
            break;
        case STRING:
            value = (T) jsonValue.asText();
            break;
        case ENUM:
            value = (T) jsonValue.asText();
            break;
        default:
            break;
        }
        return value;
    }
    
    /**
     * To field type.
     *
     * @param schema the schema
     * @return the field type
     */
    private static FieldType toFieldType(Schema schema) {
        switch(schema.getType()) {
            case RECORD:
                return FieldType.RECORD;
            case STRING:
                return FieldType.STRING;
            case INT:
                return FieldType.INTEGER;
            case LONG:
                return FieldType.LONG;
            case BOOLEAN:
                return FieldType.BOOLEAN;
            case ENUM:
                return FieldType.ENUM;
            case ARRAY:
                return FieldType.ARRAY;
            case UNION:
                if (isNullTypeSchema(schema)) {
                    for (Schema typeSchema : schema.getTypes()) {
                        FieldType type = toFieldType(typeSchema);
                        if (type != null) {
                            return type;
                        }
                    }
                    throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
                }
                else {
                    return FieldType.UNION;
                }
            case NULL:
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported avro field type: " + schema.getType());
        }
    }
    
    /**
     * Creates the generic record form record field.
     *
     * @param recordField the record field
     * @param schema the schema
     * @return the generic record
     */
    public static GenericRecord createGenericRecordFormRecordField(RecordField recordField, Schema schema) {
        GenericRecordBuilder builder = new GenericRecordBuilder(schema);
        for (FormField formField : recordField.getValue()) {
            String fieldName = formField.getFieldName();
            Field field = schema.getField(fieldName);
            Schema fieldSchema = field.schema();
            Object fieldValue = convertValue(formField, fieldSchema);
            builder.set(field, fieldValue);
        }
        GenericRecord record = builder.build();        
        return record;
    }
    
    /**
     * Creates the record field from generic record.
     *
     * @param record the record
     * @return the record field
     */
    public static RecordField createRecordFieldFromGenericRecord(GenericRecord record) {
        Schema schema = record.getSchema();
        RecordField formData = createRecordFieldFromSchema(schema);
        fillRecordFieldFromGenericRecord(formData, record);
        return formData;
    }
    
    /**
     * Fill record field from generic record.
     *
     * @param recordField the record field
     * @param record the record
     */
    private static void fillRecordFieldFromGenericRecord(RecordField recordField, GenericRecord record) {
        for (FormField field : recordField.getValue()) {
            Object value = record.get(field.getFieldName());
            setFormFieldValue(field, value);
        }
    }
    
    /**
     * Sets the form field value.
     *
     * @param field the field
     * @param value the value
     */
    @SuppressWarnings("unchecked")
    private static void setFormFieldValue(FormField field, Object value) {
        switch(field.getFieldType()) {
        case RECORD:
            GenericRecord record = (GenericRecord)value;
            fillRecordFieldFromGenericRecord((RecordField)field, record);
            break;
        case UNION:
            RecordField unionValue = createRecordFieldFromGenericRecord((GenericRecord)value);
            ((UnionField)field).setValue(unionValue);
            break;
        case STRING:
            ((StringField)field).setValue((String)value);
            break;
        case INTEGER:
            ((IntegerField)field).setValue((Integer)value);
            break;
        case LONG:
            ((LongField)field).setValue((Long)value);
            break;
        case BOOLEAN:
            ((BooleanField)field).setValue((Boolean)value);
            break;
        case ENUM:
            EnumSymbol enumSymbol = (EnumSymbol)value;
            EnumField enumField = (EnumField)field;
            if (enumSymbol != null) {
                enumField.setValueFromSymbol(enumSymbol.toString());
            }
            else {
                enumField.setValue(null);
            }
            break;
        case ARRAY:
            ArrayField arrayField = (ArrayField)field;
            arrayField.getValue().clear();
            GenericData.Array<GenericRecord> genericArrayData = (GenericData.Array<GenericRecord>)value;
            for (GenericRecord arrayRecord : genericArrayData) {
                ((ArrayField)field).addArrayData(createRecordFieldFromGenericRecord(arrayRecord));
            }
            break;
        default:
            break;
        }
    }
    
    /**
     * Convert value.
     *
     * @param formField the form field
     * @param fieldSchema the field schema
     * @return the object
     */
    private static Object convertValue(FormField formField, Schema fieldSchema) {
        switch(fieldSchema.getType()) {
        case RECORD:
            return createGenericRecordFormRecordField((RecordField)formField, fieldSchema);
        case STRING:
            return ((StringField)formField).getValue();
        case INT:
            return ((IntegerField)formField).getValue();
        case LONG:
            return ((LongField)formField).getValue();
        case BOOLEAN:
            return ((BooleanField)formField).getValue();
        case ENUM:
            String enumSymbol = ((EnumField)formField).getValue().getEnumSymbol();
            return new GenericData.EnumSymbol(fieldSchema, enumSymbol);
        case ARRAY:
            List<RecordField> arrayData = ((ArrayField)formField).getValue();
            GenericData.Array<GenericRecord> genericArrayData = new GenericData.Array<>(arrayData.size(), fieldSchema);
            for (RecordField recordField : arrayData) {
                GenericRecord record = createGenericRecordFormRecordField(recordField, fieldSchema.getElementType());
                genericArrayData.add(record);
            }
            return genericArrayData;
        case UNION:
            if (formField.isNull()) {
                if (hasType(fieldSchema, Schema.Type.NULL)) {
                    return null;
                }
                else {
                    throw new UnsupportedOperationException("Avro field doesn't support null values!");
                }
            }
            else {
                if (isNullTypeSchema(fieldSchema)) {
                    Schema notNullSchema = getNotNullType(fieldSchema);
                    if (notNullSchema != null) {
                        return convertValue(formField, notNullSchema);
                    }
                    else {
                        throw new UnsupportedOperationException("Avro field doesn't support not null values!");
                    }
                }
                else {
                    UnionField unionField = (UnionField)formField;
                    RecordField recordValue = unionField.getValue();
                    Schema recordSchema = findRecordSchema(fieldSchema, recordValue.getTypeFullname());
                    if (recordSchema != null) {
                        return createGenericRecordFormRecordField(unionField.getValue(), recordSchema);
                    }
                    else {
                        throw new IllegalArgumentException("Union schema doesn't contains record value schema: " + recordValue.getTypeFullname());
                    }
                }
            }
        default:
            throw new UnsupportedOperationException("Unsupported avro field type: " + fieldSchema.getType());
        }
    }
    
    /**
     * Checks for type.
     *
     * @param unionSchema the union schema
     * @param type the type
     * @return true, if successful
     */
    private static boolean hasType(Schema unionSchema, Schema.Type type) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType()==type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if is null type schema.
     *
     * @param unionSchema the union schema
     * @return true, if is null type schema
     */
    private static boolean isNullTypeSchema(Schema unionSchema) {
        if (unionSchema.getTypes().size()==2) {
            for (Schema typeSchema : unionSchema.getTypes()) {
                if (typeSchema.getType() == Schema.Type.NULL) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Gets the not null type.
     *
     * @param unionSchema the union schema
     * @return the not null type
     */
    private static Schema getNotNullType(Schema unionSchema) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType() != Schema.Type.NULL) {
                return typeSchema;
            }
        }
        return null;
    }
    
    /**
     * Find record schema.
     *
     * @param unionSchema the union schema
     * @param fullName the full name
     * @return the schema
     */
    private static Schema findRecordSchema(Schema unionSchema, String fullName) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType()==Type.RECORD && typeSchema.getFullName().equals(fullName)) {
                return typeSchema;
            }
        }
        return null;
    }
    
    
}
