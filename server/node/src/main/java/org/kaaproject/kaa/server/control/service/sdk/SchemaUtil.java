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

package org.kaaproject.kaa.server.control.service.sdk;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicBean;
import org.kaaproject.kaa.server.control.service.sdk.compiler.JavaDynamicCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaUtil.class);

    private SchemaUtil() {
    }

    public static boolean isEqualSchemas(Schema s1, Schema s2) {

        if (!(s1.getType().equals(s2.getType()) && s1.getFullName().equals(s2.getFullName()))) {
            return false;
        }

        switch (s1.getType()) {
            case RECORD:
                return isEqualRecords(s1, s2);
            case UNION:
                return isEqualUnions(s1, s2);
            case ARRAY:
                return isEqualSchemas(s1.getElementType(), s2.getElementType());
            case MAP:
                return isEqualSchemas(s1.getValueType(), s2.getValueType());
            case ENUM:
                return isEqualEnums(s1, s2);
            case FIXED:
                return s1.getFixedSize() == s2.getFixedSize();
            default:
                return true;
        }

    }

    private static boolean isEqualEnums(Schema s1, Schema s2) {
        List<String> symbols1 = s1.getEnumSymbols();
        List<String> symbols2 = s2.getEnumSymbols();
        if (symbols1.size() != symbols2.size()) {
            return false;
        } else {
            Collections.sort(symbols1);
            Collections.sort(symbols2);
            return symbols1.equals(symbols2);
        }
    }

    private static boolean isEqualUnions(Schema s1, Schema s2) {

        SortedMap<String, Schema> types1 = new TreeMap<String, Schema>();
        SortedMap<String, Schema> types2 = new TreeMap<String, Schema>();

        for (Schema schema : s1.getTypes()) {
            types1.put(schema.getName(), schema);
        }
        for (Schema schema : s2.getTypes()) {
            types2.put(schema.getName(), schema);
        }
        return isEqualSchemaMaps(types1, types2);
    }

    private static boolean isEqualRecords(Schema s1, Schema s2) {

        if (s1.getFields().size() != s2.getFields().size()) {
            return false;
        }

        SortedMap<String, Schema> fields1 = new TreeMap<String, Schema>();
        SortedMap<String, Schema> fields2 = new TreeMap<String, Schema>();

        for (Schema.Field field : s1.getFields()) {
            fields1.put(field.name(), field.schema());
        }
        for (Schema.Field field : s2.getFields()) {
            fields2.put(field.name(), field.schema());
        }

        return isEqualSchemaMaps(fields1, fields2);
    }

    private static boolean isEqualSchemaMaps(SortedMap<String, Schema> map1, SortedMap<String, Schema> map2) {
        if (!map1.keySet().equals(map2.keySet())) {
            return false;
        }
        for (String fieldKey : map1.keySet()) {
            if (!isEqualSchemas(map1.get(fieldKey), map2.get(fieldKey))) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, Schema> getUniqueSchemasMap(Collection<Schema> schemas) throws Exception {
        Map<String, Schema> map = new HashMap<String, Schema>();

        List<Schema> allPossible = new LinkedList<Schema>();

        for (Schema schema : schemas) {
            allPossible.addAll(getChildSchemas(schema));
        }

        for (Schema schema : allPossible) {

            String key = schema.getFullName();
            if (!map.containsKey(key)) {
                map.put(key, schema);
            } else {
                if (!SchemaUtil.isEqualSchemas(schema, map.get(key))) {
                    LOG.debug("classes {} are not the same: \n{}\n\n{}", key, schema.toString(), map.get(key).toString());
                    throw new IllegalArgumentException("multiple occurrences of "+key+" with different fields");
                }
            }
        }

        return map;
    }
    
    private static List<Schema> getChildSchemas(Schema parent) {
        Map<String, Schema> namedSchemaMap = new HashMap<>();
        parseChildSchemas(parent, namedSchemaMap);
        return new LinkedList<Schema>(namedSchemaMap.values());
    }

    private static void parseChildSchemas(Schema parent, Map<String, Schema> namedSchemaMap) {
        switch (parent.getType()) {
            case RECORD:
            case ENUM:
            case FIXED:
                if (!namedSchemaMap.containsKey(parent.getFullName())) {
                    namedSchemaMap.put(parent.getFullName(), parent);
                    if (parent.getType() == Type.RECORD) {
                        for (Schema.Field field : parent.getFields()) {
                            parseChildSchemas(field.schema(), namedSchemaMap);
                        }
                    }
                }
                break;
            case UNION:
                for (Schema schema : parent.getTypes()) {
                    parseChildSchemas(schema, namedSchemaMap);
                }
                break;
            case ARRAY:
                parseChildSchemas(parent.getElementType(), namedSchemaMap);
                break;
            case MAP:
                parseChildSchemas(parent.getValueType(), namedSchemaMap);
                break;
            default:
                break;
        }
    }

    public static Collection<JavaDynamicBean> compileAvroSchema(Schema avroSchema) {
        try {
            LOG.debug("Compiling {}", avroSchema);
            Map<String, Schema> uniqueSchemas = SchemaUtil.getUniqueSchemasMap(Collections.singletonList(avroSchema));
            List<JavaDynamicBean> javaSources = JavaSdkGenerator.generateSchemaSources(avroSchema, uniqueSchemas);
            JavaDynamicCompiler compiler = new JavaDynamicCompiler();
            compiler.init();
            return compiler.compile(javaSources);
        } catch (Exception cause) {
            LOG.error("Failed to compile {}", avroSchema, cause);
            String userMessage = "Failed to compile the schema: " + cause.getMessage();
            throw new IllegalArgumentException(userMessage, cause);
        }
    }
}

