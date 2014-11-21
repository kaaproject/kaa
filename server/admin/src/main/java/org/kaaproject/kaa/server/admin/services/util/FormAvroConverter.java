package org.kaaproject.kaa.server.admin.services.util;

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
import org.kaaproject.kaa.server.admin.shared.form.ArrayField;
import org.kaaproject.kaa.server.admin.shared.form.BooleanField;
import org.kaaproject.kaa.server.admin.shared.form.EnumField;
import org.kaaproject.kaa.server.admin.shared.form.FieldType;
import org.kaaproject.kaa.server.admin.shared.form.FormEnum;
import org.kaaproject.kaa.server.admin.shared.form.FormField;
import org.kaaproject.kaa.server.admin.shared.form.IntegerField;
import org.kaaproject.kaa.server.admin.shared.form.LongField;
import org.kaaproject.kaa.server.admin.shared.form.RecordField;
import org.kaaproject.kaa.server.admin.shared.form.SizedField;
import org.kaaproject.kaa.server.admin.shared.form.StringField;
import org.kaaproject.kaa.server.admin.shared.form.UnionField;

public class FormAvroConverter {

    private static final String DISPLAY_NAME = "displayName";
    private static final String DISPLAY_NAMES = "displayNames";
    private static final String WEIGHT = "weight";
    private static final String MIN_ROW_COUNT = "minRowCount";
    private static final String MAX_LENGTH = "maxLength";
    private static final String OPTIONAL = "optional";

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
                    JsonNode displayNamesNode = field.schema().getJsonProp(DISPLAY_NAMES);
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
    
    public static RecordField createRecordFieldFromGenericRecord(GenericRecord record) {
        Schema schema = record.getSchema();
        RecordField formData = createRecordFieldFromSchema(schema);
        fillRecordFieldFromGenericRecord(formData, record);
        return formData;
    }
    
    private static void fillRecordFieldFromGenericRecord(RecordField recordField, GenericRecord record) {
        for (FormField field : recordField.getValue()) {
            Object value = record.get(field.getFieldName());
            setFormFieldValue(field, value);
        }
    }
    
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
    
    private static boolean hasType(Schema unionSchema, Schema.Type type) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType()==type) {
                return true;
            }
        }
        return false;
    }
    
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
    
    private static Schema getNotNullType(Schema unionSchema) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType() != Schema.Type.NULL) {
                return typeSchema;
            }
        }
        return null;
    }
    
    private static Schema findRecordSchema(Schema unionSchema, String fullName) {
        for (Schema typeSchema : unionSchema.getTypes()) {
            if (typeSchema.getType()==Type.RECORD && typeSchema.getFullName().equals(fullName)) {
                return typeSchema;
            }
        }
        return null;
    }
    
    
}
