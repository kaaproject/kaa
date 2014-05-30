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

package org.kaaproject.kaa.server.operations.service.delta.merge;

import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_ARRAY;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_FIELDS;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_NAME;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_NAMESPACE;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_TYPE;
import static org.kaaproject.kaa.server.operations.service.delta.merge.MergeConstants.FIELD_OVERRIDE_STRATEGY;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;


/**
 * The Class ArrayMergeStrategyResolver.
 */
public class ArrayMergeStrategyResolver {

    /** The schema root. */
    private final Map<String, Object> schemaRoot;

    /**
     * Instantiates a new array merge strategy resolver.
     *
     * @param configurationSchema the configuration schema
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ArrayMergeStrategyResolver(String configurationSchema) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        schemaRoot = mapper.readValue(configurationSchema, Map.class);
    }

    /**
     * Resolve.
     *
     * @param parent the parent
     * @param childName the child name
     * @return the array merge strategy
     * @throws MergeException the merge exception
     */
    public ArrayMergeStrategy resolve(String name, String namespace, String childName) throws MergeException {
        Map<String, Object> schemaForParent = findSchema(name, namespace, schemaRoot);
        if (schemaForParent == null) {
            throw new MergeException(MessageFormat.format("Failed to find Schema with Name ''{0}'' and namespace ''{1}''", name, namespace));
        }
        String mergeStrategyName = findMergeStrategy(schemaForParent, childName);

        ArrayMergeStrategy mergeStrategy = null;
        if (mergeStrategyName != null) {
            mergeStrategy = ArrayMergeStrategy.getByName(mergeStrategyName);
        }
        if (mergeStrategy == null) {
            mergeStrategy = ArrayMergeStrategy.REPLACE;
        }

        return mergeStrategy;
    }

    /**
     * Find schema.
     *
     * @param schemaName the schema name
     * @param schemaNamespace the schema namespace
     * @param root the root
     * @return the map
     * @throws MergeException the merge exception
     */
    private Map<String, Object> findSchema(String schemaName, String schemaNamespace, Map<String, Object> root) throws MergeException {
        String name = (String) root.get(FIELD_NAME);
        String namespace = (String) root.get(FIELD_NAMESPACE);
        // looking for node that has child nodes 'name' and 'namespace' with corresponding values
        if (schemaName.equals(name) && namespace.equals(schemaNamespace)) {
            return root;
        } else {
            for (Map.Entry<String, Object> entry : root.entrySet()) {
                if (entry.getValue() instanceof List) {
                    List items = (List) entry.getValue();
                    for (Object item : items) {
                        if (item instanceof Map) {
                            Map<String, Object> foundSchema = findSchema(schemaName, schemaNamespace, (Map<String, Object>) item);
                            if (foundSchema != null) {
                                return foundSchema;
                            }
                        }
                    }
                } else if (entry.getValue() instanceof Map) {
                    Map<String, Object> foundSchema = findSchema(schemaName, schemaNamespace, (Map<String, Object>) entry.getValue());
                    if (foundSchema != null) {
                        return foundSchema;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find merge strategy.
     *
     * @param root the root
     * @param arrayFieldName the array field name
     * @return the string
     */
    private String findMergeStrategy(Map<String, Object> root, String arrayFieldName) {
        List<Object> fields = (List<Object>) root.get(FIELD_FIELDS);
        if (fields == null) {
            return null;
        }
        for (Object field : fields) {
            Map<String, Object> fieldDefinition = (Map<String, Object>) field;
            String fieldName = (String) fieldDefinition.get(FIELD_NAME);
            if (arrayFieldName.equals(fieldName)) {
                Object fieldType = fieldDefinition.get(FIELD_TYPE);

                // if this is a union type
                if (fieldType instanceof List) {
                    List<Object> union = (List<Object>) fieldType;
                    for (Object unionItem : union) {
                        if (unionItem instanceof Map) {
                            Map<String, Object> unionItemDefinition = (Map<String, Object>) unionItem;
                            Object unionItemDefinitionType = unionItemDefinition.get(FIELD_TYPE);
                            if (unionItemDefinitionType instanceof String) {
                                if (unionItemDefinitionType.toString().equals(FIELD_ARRAY)) { //NOSONAR
                                    return (String) unionItemDefinition.get(FIELD_OVERRIDE_STRATEGY);
                                }
                            }
                        }
                    }
                } else if (fieldType instanceof Map) {
                    Map<String, Object> typeDefinition = (Map<String, Object>) fieldType;
                    Object typeDefinitionType = typeDefinition.get(FIELD_TYPE);
                    if (typeDefinitionType instanceof String) {
                        if (typeDefinitionType.toString().equals(FIELD_ARRAY)) { //NOSONAR
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
