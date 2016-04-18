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

package org.kaaproject.kaa.avro.avrogen;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

public class TypeConverter {
    private static final String DIRECTION_FIELD = "direction";

    private TypeConverter() {
    }

    public static String convertToCType(Schema schema) {
        return convertToCType("kaa", schema);
    }

    public static String convertToCType(String namespace, Schema schema) {
        String cType = "";
        switch (schema.getType()) {
        case BOOLEAN:
            cType = "int8_t";
            break;
        case INT:
            cType = "int32_t";
            break;
        case LONG:
            cType = "int64_t";
            break;
        case FLOAT:
            cType = "float";
            break;
        case DOUBLE:
            cType = "double";
            break;
        case STRING:
            cType = "kaa_string_t *";
            break;
        case BYTES:
        case FIXED:
            cType = "kaa_bytes_t *";
            break;
        case ARRAY:
            cType = "kaa_list_t *";
            break;
        case UNION:
            cType = "kaa_union_t *";
            break;
        case ENUM:
            cType = namespace + "_" + StyleUtils.toLowerUnderScore(schema.getName()) + "_t";
            break;
        case RECORD:
            cType = namespace + "_" + StyleUtils.toLowerUnderScore(schema.getName()) + "_t *";
            break;
        default:
            // TODO: add handling
            break;
        }

        return cType;
    }

    public static String convertToObjCType(Schema schema) {
        String objCType = "";
        switch (schema.getType()) {
            case BOOLEAN:
                objCType = "BOOL";
                break;
            case INT:
                objCType = "int32_t";
                break;
            case LONG:
                objCType = "int64_t";
                break;
            case FLOAT:
                objCType = "float";
                break;
            case DOUBLE:
                objCType = "double";
                break;
            case STRING:
                objCType = "NSString *";
                break;
            case BYTES:
            case FIXED:
                objCType = "NSData *";
                break;
            case ARRAY:
                objCType = "NSArray *";
                break;
            case UNION:
                objCType = "KAAUnion *";
                break;
            case ENUM:
                objCType = schema.getName();
                break;
            case RECORD:
                objCType = schema.getName() + " *";
                break;
            default:
                // TODO: add handling
                break;
        }

        return objCType;
    }

    public static String generateUnionName(Schema schema) {
        return generateUnionName("", schema);
    }

    public static String generateUnionName(String prefix, Schema schema) {
        StringBuilder builder = new StringBuilder(prefix + "_UNION_");

        List<Schema> branches = schema.getTypes();
        int branchCounter = branches.size();

        for (Schema branchSchema : branches) {
            switch (branchSchema.getType()) {
            case RECORD:
                builder.append(StyleUtils.toUpperUnderScore(branchSchema.getName()));
                break;
            case ARRAY:
                builder.append(branchSchema.getType().toString());
                builder.append('_');
                builder.append(StyleUtils.toUpperUnderScore(branchSchema.getElementType().getName()));
                break;
            case ENUM:
                builder.append(StyleUtils.toUpperUnderScore(branchSchema.getName()));
                break;
            default:
                builder.append(branchSchema.getType().toString());
                break;
            }

            if (--branchCounter > 0) {
                builder.append("_OR_");
            }
        }

        return builder.toString();
    }

    public static boolean isRecordNeedDeallocator(Schema schema) {
        if (schema.getType() == Type.RECORD) {
            for (Field f : schema.getFields()) {
                Type type = f.schema().getType();
                if (type == Type.ARRAY || type == Type.BYTES || type == Type.STRING ||
                    type == Type.FIXED || type == Type.RECORD || type == Type.UNION) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAvroPrimitive(Schema schema) {
        Type type = schema.getType();
        return type == Type.BOOLEAN || type == Type.INT || type == Type.LONG ||
                type == Type.ENUM || type == Type.FLOAT || type == Type.DOUBLE;
    }

    public static boolean isAvroNull(Schema schema) {
        return schema.getType() == Type.NULL;
    }

    public static boolean isAvroFixed(Schema schema) {
        return schema.getType() == Type.FIXED;
    }

    public static boolean isAvroRecord(Schema schema) {
        return schema.getType() == Type.RECORD;
    }

    public static boolean isAvroUnion(Schema schema) {
        return schema.getType() == Type.UNION;
    }

    public static boolean isAvroArray(Schema schema) {
        return schema.getType() == Type.ARRAY;
    }

    public static boolean isAvroEnum(Schema schema) {
        return schema.getType() == Type.ENUM;
    }

    public static boolean isAvroString(Schema schema) {
        return schema.getType() == Type.STRING;
    }

    public static boolean isAvroBytes(Schema schema) {
        return schema.getType() == Type.BYTES;
    }

    public static boolean isAvroFloat(Schema schema) {
        return schema.getType() == Type.FLOAT;
    }

    public static boolean isAvroDouble(Schema schema) {
        return schema.getType() == Type.DOUBLE;
    }

    public static boolean isAvroInt(Schema schema) {
        return schema.getType() == Type.INT;
    }

    public static boolean isAvroLong(Schema schema) {
        return schema.getType() == Type.LONG;
    }

    public static boolean isAvroBoolean(Schema schema) {
        return schema.getType() == Type.BOOLEAN;
    }

    public static boolean isTypeOut(Schema schema) {
        String prop = schema.getProp(DIRECTION_FIELD);
        return prop == null || prop.equalsIgnoreCase("out");
    }

    public static boolean isTypeIn(Schema schema) {
        String prop = schema.getProp(DIRECTION_FIELD);
        return prop == null || prop.equalsIgnoreCase("in");
    }

    public static String getLastBranchNumber(Schema schema) {
        return "" + (schema.getTypes().size() - 1);
    }

    public static boolean containsUnion(Schema schema) {
        for (Field field : schema.getFields()) {
            if (isAvroUnion(field.schema())) {
                return true;
            }
        }

        return false;
    }
}
