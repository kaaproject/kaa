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

package org.kaaproject.kaa.avro.avrogenc;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

public class TypeConverter {
    private static final String DIRECTION_FIELD = "direction";

    public static String convertToCType(Schema schema) {
        return convertToCType(schema, "kaa");
    }

    public static String convertToCType(Schema schema, String namespace) {
        String cType = new String();
        switch (schema.getType()) {
        case BOOLEAN:
            cType = "KAA_BOOL";
            break;
        case INT:
            cType = "int32_t";
            break;
        case LONG:
            cType = "int64_t";
            break;
        case STRING:
            cType = "char*";
            break;
        case BYTES:
            cType = "kaa_bytes_t*";
            break;
        case ARRAY:
            cType = "kaa_list_t*";
            break;
        case UNION:
            cType = "kaa_union_t*";
            break;
        case ENUM:
            cType = namespace + "_" + StyleUtils.toLowerUnderScore(schema.getName()) + "_t";
            break;
        case RECORD:
            cType = namespace + "_" + StyleUtils.toLowerUnderScore(schema.getName()) + "_t*";
            break;
        default:
            // TODO: add handling
            break;
        }

        return cType;
    }

    public static String generateUnionName(Schema schema) {
        return generateUnionName(schema, "");
    }

    public static String generateUnionName(Schema schema, String parentName) {
        String result = new String(parentName);

        for (Schema branchSchema : schema.getTypes()) {
            result += branchSchema.getType();
            switch (branchSchema.getType()) {
            case RECORD:
                result += "_";
                result += StyleUtils.toUpperUnderScore(branchSchema.getName());
                break;
            case ARRAY:
                result += "_";
                result += StyleUtils.toUpperUnderScore(branchSchema.getElementType().getName());
                break;
            case ENUM:
                result += "_";
                result += StyleUtils.toUpperUnderScore(branchSchema.getName());
                break;
            default:
                break;
            }
            result += "_";
        }
        result += "UNION";
        return result;
    }

    public static boolean isRecordNeedDeallocator(Schema schema) {
        if (schema.getType() == Type.RECORD) {
            for (Field f : schema.getFields()) {
                Type type = f.schema().getType();
                if (type == Type.ARRAY || type == Type.BYTES || type == Type.STRING
                        || (type == Type.RECORD && isRecordNeedDeallocator(f.schema()))
                        || type == Type.UNION)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isNullType(Schema schema) {
        return (schema.getType() == Type.NULL);
    }

    public static boolean isPrimitiveType(Schema schema) {
        Type type = schema.getType();
        return (type == Type.BOOLEAN || type == Type.INT || type == Type.LONG ||
                type == Type.ENUM || type == Type.STRING);
    }

    public static boolean isBytesOrString(Schema schema) {
        return (schema.getType() == Type.BYTES || schema.getType() == Type.STRING);
    }

    public static boolean isRecordOrUnion(Schema schema) {
        return (schema.getType() == Type.UNION || schema.getType() == Type.RECORD);
    }

    public static boolean isRecordType(Schema schema) {
        return (schema.getType() == Type.RECORD);
    }

    public static boolean isUnionType(Schema schema) {
        return (schema.getType() == Type.UNION);
    }

    public static boolean isArrayType(Schema schema) {
        return (schema.getType() == Type.ARRAY);
    }

    public static boolean isEnumType(Schema schema) {
        return (schema.getType() == Type.ENUM);
    }

    public static boolean isStringType(Schema schema) {
        return (schema.getType() == Type.STRING);
    }

    public static boolean isBytes(Schema schema) {
        return (schema.getType() == Type.BYTES);
    }

    public static boolean isTypeOut(Schema schema) {
        String prop = schema.getProp(DIRECTION_FIELD);
        return (prop == null || prop.equalsIgnoreCase("out"));
    }

    public static boolean isTypeIn(Schema schema) {
        String prop = schema.getProp(DIRECTION_FIELD);
        return (prop == null || prop.equalsIgnoreCase("in"));
    }
}
