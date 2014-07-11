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

package org.kaaproject.kaa.server.common.core.algorithms.override;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ARRAY_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELDS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAME_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.TYPE_FIELD;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.core.algorithms.CommonUtils;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;


/**
 * The Class ArrayMergeStrategyResolver.
 */
public class ArrayOverrideStrategyResolver {

    public static final String FIELD_OVERRIDE_STRATEGY = "overrideStrategy";

    /** The schema root. */
    private final Map<String, Object> schemaRoot;

    /**
     * Instantiates a new array merge strategy resolver.
     *
     * @param configurationSchema the configuration schema
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ArrayOverrideStrategyResolver(KaaSchema configurationSchema) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        schemaRoot = mapper.readValue(configurationSchema.getRawSchema(), Map.class);
    }

    /**
     * Resolve.
     *
     * @param parent the parent
     * @param childName the child name
     * @return the array merge strategy
     * @throws OverrideException the merge exception
     */
    public ArrayOverrideStrategy resolve(String name, String namespace, String childName) throws OverrideException {
        Map<String, Object> schemaForParent = CommonUtils.findRawSchemaByName(schemaRoot, name, namespace);
        if (schemaForParent == null) {
            throw new OverrideException(MessageFormat.format("Failed to find Schema with Name ''{0}'' and namespace ''{1}''", name, namespace));
        }
        String mergeStrategyName = findMergeStrategy(schemaForParent, childName);

        ArrayOverrideStrategy mergeStrategy = null;
        if (mergeStrategyName != null) {
            mergeStrategy = ArrayOverrideStrategy.getByName(mergeStrategyName);
        }
        if (mergeStrategy == null) {
            mergeStrategy = ArrayOverrideStrategy.REPLACE;
        }

        return mergeStrategy;
    }

    /**
     * Find merge strategy.
     *
     * @param root the root
     * @param arrayFieldName the array field name
     * @return the string
     */
    private String findMergeStrategy(Map<String, Object> root, String arrayFieldName) {
        List<Object> fields = (List<Object>) root.get(FIELDS_FIELD);
        if (fields == null) {
            return null;
        }
        for (Object field : fields) {
            Map<String, Object> fieldDefinition = (Map<String, Object>) field;
            String fieldName = (String) fieldDefinition.get(NAME_FIELD);
            if (arrayFieldName.equals(fieldName)) {
                Object fieldType = fieldDefinition.get(TYPE_FIELD);

                // if this is a union type
                if (fieldType instanceof List) {
                    List<Object> union = (List<Object>) fieldType;
                    for (Object unionItem : union) {
                        if (unionItem instanceof Map) {
                            Map<String, Object> unionItemDefinition = (Map<String, Object>) unionItem;
                            Object unionItemDefinitionType = unionItemDefinition.get(TYPE_FIELD);
                            if (unionItemDefinitionType instanceof String) {
                                if (unionItemDefinitionType.toString().equals(ARRAY_FIELD_VALUE)) { //NOSONAR
                                    return (String) unionItemDefinition.get(FIELD_OVERRIDE_STRATEGY);
                                }
                            }
                        }
                    }
                } else if (fieldType instanceof Map) {
                    Map<String, Object> typeDefinition = (Map<String, Object>) fieldType;
                    Object typeDefinitionType = typeDefinition.get(TYPE_FIELD);
                    if (typeDefinitionType instanceof String) {
                        if (typeDefinitionType.toString().equals(ARRAY_FIELD_VALUE)) { //NOSONAR
                            return (String) typeDefinition.get(FIELD_OVERRIDE_STRATEGY);
                        }
                    }
                }

                break;
            }
        }
        return null;
    }
}
