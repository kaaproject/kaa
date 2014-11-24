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

package org.kaaproject.kaa.server.common.core.algorithms.schema;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ARRAY_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ENUM_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELDS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIXED_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.ITEMS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.KAA_NAMESPACE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.MAP_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAMESPACE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NAME_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.NULL_FIELD_VALUE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.RESET;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.SIZE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.SYMBOLS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.TYPE_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UNCHANGED;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_SIZE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link SchemaCreator}.
 *
 * @author Yaroslav Zeygerman
 */
public class SchemaCreatorImpl<T extends KaaSchema> implements SchemaCreator<T> {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(SchemaCreatorImpl.class);

    private static final String ADDRESSABLE_NAME    = "addressable";
    private static final String OPTIONAL_NAME       = "optional";

    private Set<String> addressableRecords;
    private boolean isUuidDeclared;
    private boolean isResetDeclared;
    private boolean isUnchangedDeclared;
    private final SchemaCreationStrategy<T> strategy;

    private String rootSchemaName;

    public SchemaCreatorImpl(SchemaCreationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    private static String getFullName(Map<String, Object> node) {
        return node.get(NAMESPACE_FIELD) + "." + node.get(NAME_FIELD);
    }

    private void resetTriggers() {
        addressableRecords = new HashSet<String>();
        isUuidDeclared = false;
        isResetDeclared = false;
        isUnchangedDeclared = false;
    }

    private Object getUuidType() {
        if (isUuidDeclared) {
            return KAA_NAMESPACE + "." + UUID_TYPE;
        } else {
            Map<String, Object> uuid = new HashMap<String, Object>();
            uuid.put(NAME_FIELD, UUID_TYPE);
            uuid.put(NAMESPACE_FIELD, KAA_NAMESPACE);
            uuid.put(TYPE_FIELD, FIXED_FIELD_VALUE);
            uuid.put(SIZE_FIELD, UUID_SIZE);
            isUuidDeclared = true;
            return uuid;
        }
    }

    private Map<String, Object> getUuidField() {
        Map<String, Object> uuidField = new HashMap<String, Object>();
        uuidField.put(NAME_FIELD, UUID_FIELD);
        if (strategy.isUuidOptional()) {
            List<Object> union = new ArrayList<Object>(2);
            union.add(getUuidType());
            union.add(NULL_FIELD_VALUE);
            uuidField.put(TYPE_FIELD, union);
        } else {
            uuidField.put(TYPE_FIELD, getUuidType());
        }
        return uuidField;
    }

    private Map<String, Object> getEnum(String name) {
        Map<String, Object> reset = new HashMap<String, Object>();
        List<Object> symbols = new ArrayList<Object>();
        symbols.add(name);
        reset.put(SYMBOLS_FIELD, symbols);
        reset.put(NAMESPACE_FIELD, KAA_NAMESPACE);
        reset.put(NAME_FIELD, name + "T");
        reset.put(TYPE_FIELD, ENUM_FIELD_VALUE);
        return reset;
    }

    private Object getResetType() {
        if (isResetDeclared) {
            return KAA_NAMESPACE + "." + RESET + "T";
        } else {
            isResetDeclared = true;
            return getEnum(RESET);
        }
    }

    private Object getUnchangedType() {
        if (isUnchangedDeclared) {
            return KAA_NAMESPACE + "." + UNCHANGED + "T";
        } else {
            isUnchangedDeclared = true;
            return getEnum(UNCHANGED);
        }
    }

    private Boolean isAddressableValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> record = (Map<String, Object>) value;
            if (record.get(ADDRESSABLE_NAME) != null
                    && !getFullName(record).equals(rootSchemaName)) {
                return (Boolean) record.get(ADDRESSABLE_NAME);
            }
            return Boolean.TRUE;
        } else if (addressableRecords.contains(value)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private void addResetTypeIfArray(Map<String, Object> fieldType, List<Object> union) {
        if (fieldType.get(TYPE_FIELD).equals(ARRAY_FIELD_VALUE) && strategy.isArrayEditable()) {
            union.add(0, getResetType());
        }
    }

    private void processArray(Map<String, Object> root) throws SchemaCreationException {
        boolean hasAddressableItem = false;
        List<Object> newItems = null;
        if (root.get(ITEMS_FIELD) instanceof List) {
            List<Object> items = (List<Object>) root.get(ITEMS_FIELD);
            for (Object itemIter : items) {
                if (itemIter instanceof Map) {
                    Map<String, Object> item = (Map<String, Object>) itemIter;
                    convert(item);
                }
                if (isAddressableValue(itemIter) && strategy.isArrayEditable()) {
                    hasAddressableItem = true;
                }
            }
            newItems = items;
        } else {
            Object rawItems = root.get(ITEMS_FIELD);
            if (rawItems instanceof Map) {
                convert((Map<String, Object>) rawItems);
            }

            if (strategy.isArrayEditable()) {
                hasAddressableItem = isAddressableValue(rawItems);
                if (hasAddressableItem) {
                    newItems = new ArrayList<Object>();
                    newItems.add(root.get(ITEMS_FIELD));
                    root.put(ITEMS_FIELD, newItems);
                }
            }
        }
        if (hasAddressableItem) {
            newItems.add(getUuidType());
        }
    }

    private void processRecord(Map<String, Object> root) throws SchemaCreationException {
        if (root.get(NAMESPACE_FIELD) == null) {
            throw new SchemaCreationException(new StringBuilder()
                    .append("Namepsace not found for \"")
                    .append((String) root.get(NAME_FIELD)).append("\"").toString());
        }
        Boolean addressable = isAddressableValue(root);

        List<Object> fields = (List<Object>) root.get(FIELDS_FIELD);

        for (Object fieldIter : fields) {
            Map<String, Object> field = (Map<String, Object>) fieldIter;

            Boolean optional = Boolean.FALSE;
            if (field.get(OPTIONAL_NAME) != null) {
                optional = (Boolean) field.get(OPTIONAL_NAME);
            }

            List<Object> newUnion = new ArrayList<Object>();

            boolean isUnionField = false;
            if (field.get(TYPE_FIELD) instanceof Map) {
                newUnion.add(field.get(TYPE_FIELD));
                Map<String, Object> fieldType = (Map<String, Object>) field.get(TYPE_FIELD);
                addResetTypeIfArray(fieldType, newUnion);
                convert(fieldType);
            } else if (field.get(TYPE_FIELD) instanceof List) {
                // Looks like the type of the field is union
                List<Object> oldUnion = (List<Object>) field.get(TYPE_FIELD);
                for (Object unionIter : oldUnion){
                    if (unionIter instanceof Map) {
                        Map<String, Object> fieldType = (Map<String, Object>) unionIter;
                        addResetTypeIfArray(fieldType, newUnion);
                        convert(fieldType);
                    }
                }
                newUnion.addAll(oldUnion);
                isUnionField = true;
            } else {
                newUnion.add(field.get(TYPE_FIELD));
            }

            if (strategy.isUnchangedSupported()) {
                newUnion.add(getUnchangedType());
            }

            if (optional) {
                strategy.onOptionalField(newUnion);
            } else {
                strategy.onMandatoryField(newUnion);
            }

            if (newUnion.size() > 1) {
                field.put(TYPE_FIELD, newUnion);
            }
        }

        if (addressable)  {
            // This record supports partial updates, adding "uuid" field
            fields.add(getUuidField());
            // Adding addressable record's name to the storage
            String fullName = getFullName(root);
            if (!fullName.equals(rootSchemaName)) {
                addressableRecords.add(fullName);
            }
        }
    }

    private void convert(Map<String, Object> root) throws SchemaCreationException {
        if (root.get(TYPE_FIELD).equals(ARRAY_FIELD_VALUE)) {
            processArray(root);
        } else if (root.get(FIELDS_FIELD) != null) {
            processRecord(root);
        } else if (root.get(TYPE_FIELD).equals(MAP_FIELD_VALUE)) {
            throw new SchemaCreationException("Map is not supported");
        }
    }

    @Override
    public T createSchema(DataSchema configSchema) throws SchemaCreationException {
        resetTriggers();
        String schema;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rootSchema = mapper.readValue(configSchema.getRawSchema(), Map.class);
            rootSchemaName = getFullName(rootSchema);
            convert(rootSchema);
            schema = mapper.writeValueAsString(strategy.onSchemaProcessed(rootSchema, addressableRecords));
        } catch (IOException ex) {
            LOG.error("Can't generate schema.", ex);
            throw new SchemaCreationException("Can't generate schema based on config schema.", ex);
        }
        return strategy.createSchema(schema);
    }
}
