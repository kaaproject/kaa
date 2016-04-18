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

package org.kaaproject.kaa.server.common.core.algorithms.override;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;


/**
 * The Class ArrayMergeStrategyResolver.
 */
public class ArrayOverrideStrategyResolver {

    public static final String FIELD_OVERRIDE_STRATEGY = "overrideStrategy";

    /** The schema root. */
    private final Map<String, Schema> schemaTypes;

    /**
     * Instantiates a new array merge strategy resolver.
     *
     * @param configurationSchema the configuration schema
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ArrayOverrideStrategyResolver(Map<String, Schema> types) throws IOException {
        this.schemaTypes = types;
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
        Schema schemaForParent = schemaTypes.get(namespace + "." + name);
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
    private String findMergeStrategy(Schema root, String arrayFieldName) {
        if (root.getType() != Type.RECORD) {
            return null;
        }
        List<Field> fields = root.getFields();
        for (Field field : fields) {
            if (arrayFieldName.equals(field.name())) {
                return field.getProp(FIELD_OVERRIDE_STRATEGY);
            }
        }
        return null;
    }
}
