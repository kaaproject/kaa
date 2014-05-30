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

package org.kaaproject.kaa.server.common.dao.schema;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaCreatorImpl implements SchemaCreator {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(SchemaCreatorImpl.class);

    private static final String UUID_NAME           = "uuid";
    private static final String UUID_FIELD_NAME     = "__" + UUID_NAME;
    private static final String RESET_NAME          = "reset";
    private static final String UNCHANGED_NAME      = "unchanged";
    private static final String ADDRESSABLE_NAME    = "addressable";
    private static final String OPTIONAL_NAME       = "optional";       
    private static final String NAMESPACE           = "org.kaaproject.configuration";

    private Set<String> addressableRecords;
    private boolean isUuidDeclared;
    private boolean isResetDeclared;
    private boolean isUnchangedDeclared;
    private final SchemaCreationStrategy strategy;

    private String rootSchemaName;

    public SchemaCreatorImpl(SchemaCreationStrategy strategy) {
        this.strategy = strategy;
    }

    private static String getFullName(Map<String, Object> node) {
        return node.get("namespace") + "." + node.get("name");
    }

    private void resetTriggers() {
        addressableRecords = new HashSet<String>();
        isUuidDeclared = false;
        isResetDeclared = false;
        isUnchangedDeclared = false;
    }

    private Object getUuidType() {
        if (isUuidDeclared) {
            return NAMESPACE + "." + UUID_NAME + "T";
        } else {
            Map<String, Object> uuid = new HashMap<String, Object>();
            uuid.put("name", UUID_NAME + "T");
            uuid.put("namespace", NAMESPACE);
            uuid.put("type", "fixed");
            uuid.put("size", 16);
            isUuidDeclared = true;
            return uuid;
        }
    }

    private Map<String, Object> getUuidField() {
        Map<String, Object> uuidField = new HashMap<String, Object>();
        uuidField.put("name", UUID_FIELD_NAME);
        if (strategy.isUuidOptional()) {
            List<Object> union = new ArrayList<Object>(2);
            union.add(getUuidType());
            union.add("null");
            uuidField.put("type", union);
        } else {
            uuidField.put("type", getUuidType());
        }
        return uuidField;
    }

    private Map<String, Object> getEnum(String name) {
        Map<String, Object> reset = new HashMap<String, Object>();
        List<Object> symbols = new ArrayList<Object>();
        symbols.add(name);
        reset.put("symbols", symbols);
        reset.put("namespace", NAMESPACE);
        reset.put("name", name + "T");
        reset.put("type", "enum");
        return reset;
    }

    private Object getResetType() {
        if (isResetDeclared) {
            return NAMESPACE + "." + RESET_NAME + "T";
        } else {
            isResetDeclared = true;
            return getEnum(RESET_NAME);
        }
    }

    private Object getUnchangedType() {
        if (isUnchangedDeclared) {
            return NAMESPACE + "." + UNCHANGED_NAME + "T";
        } else {
            isUnchangedDeclared = true;
            return getEnum(UNCHANGED_NAME);
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

    private void checkIfArray(Map<String, Object> fieldType, List<Object> union) {
        if (fieldType.get("type").equals("array") && strategy.isArrayEditable()) {
            union.add(getResetType());
        }
    }

    private void processArray(Map<String, Object> root) throws SchemaCreationException {
        boolean hasAddressableItem = false;
        List<Object> newItems = null;
        if (root.get("items") instanceof List) {
            List<Object> items = (List<Object>) root.get("items");
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
            Object rawItems = root.get("items");
            if (rawItems instanceof Map) {
                convert((Map<String, Object>) rawItems);
            }

            if (strategy.isArrayEditable()) {
                hasAddressableItem = isAddressableValue(rawItems);
                if (hasAddressableItem) {
                    newItems = new ArrayList<Object>();
                    newItems.add(root.get("items"));
                    root.put("items", newItems);
                }
            }
        }
        if (hasAddressableItem) {
            newItems.add(getUuidType());
        }
    }

    private void processRecord(Map<String, Object> root) throws SchemaCreationException {
        if (root.get("namespace") == null) {
            throw new SchemaCreationException(new StringBuilder()
                    .append("Namepsace not found for \"")
                    .append((String) root.get("name")).append("\"").toString());
        }
        Boolean addressable = isAddressableValue(root);

        List<Object> fields = (List<Object>) root.get("fields");

        for (Object fieldIter : fields) {
            Map<String, Object> field = (Map<String, Object>) fieldIter;

            Boolean optional = Boolean.FALSE;
            if (field.get(OPTIONAL_NAME) != null) {
                optional = (Boolean) field.get(OPTIONAL_NAME);
            }

            List<Object> newUnion = new ArrayList<Object>();

            boolean isUnionField = false;
            if (field.get("type") instanceof Map) {
                newUnion.add(field.get("type"));
                Map<String, Object> fieldType = (Map<String, Object>) field.get("type");
                checkIfArray(fieldType, newUnion);
                convert(fieldType);
            } else if (field.get("type") instanceof List) {
                // Looks like the type of the field is union
                List<Object> oldUnion = (List<Object>) field.get("type");
                for (Object unionIter : oldUnion){
                    if (unionIter instanceof Map) {
                        Map<String, Object> fieldType = (Map<String, Object>) unionIter;
                        checkIfArray(fieldType, newUnion);
                        convert(fieldType);
                    }
                }
                newUnion.addAll(oldUnion);
                isUnionField = true;
            } else {
                newUnion.add(field.get("type"));
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
                field.put("type", newUnion);
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
        if (root.get("type").equals("array")) {
            processArray(root);
        } else if (root.get("fields") != null) {
            processRecord(root);
        } else if (root.get("type").equals("map")) {
            throw new SchemaCreationException("Map is not supported");
        }
    }

    public String createSchema(Reader configSchema) throws SchemaCreationException {
        resetTriggers();
        String schema;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> rootSchema = mapper.readValue(configSchema, Map.class);
            rootSchemaName = getFullName(rootSchema);
            convert(rootSchema);
            schema = mapper.writeValueAsString(strategy.onSchemaProcessed(rootSchema, addressableRecords));
        } catch (IOException ex) {
            LOG.error("Can't generate schema.", ex);
            throw new SchemaCreationException("Can't generate schema based on config schema.", ex);
        }
        return schema;
    }
}
