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

package org.kaaproject.kaa.server.common.core.algorithms.schema;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.DISPLAY_NAME_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELD_ACCESS_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.FIELD_ACCESS_READ_ONLY;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.KAA_NAMESPACE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.RESET;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UNCHANGED;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD_DISPLAY_NAME;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_SIZE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
            .getLogger(SchemaCreatorImpl.class);

    private static final String ADDRESSABLE_NAME    = "addressable";
    private static final String OPTIONAL_NAME       = "optional";

    private final Set<Schema> addressableRecords = new HashSet<Schema>();
    private final HashMap<String, Schema> processedRecords = new HashMap<String, Schema>();
    private Schema uuidTSchema;
    private Schema resetTSchema;
    private Schema unchangedTSchema;
    private Field uuidField;
    private final SchemaCreationStrategy<T> strategy;

    private String rootSchemaName;

    public SchemaCreatorImpl(SchemaCreationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    private Schema getUuidType() {
        if (uuidTSchema == null) {
            uuidTSchema = Schema.createFixed(UUID_TYPE, null, KAA_NAMESPACE, UUID_SIZE);
        }
        return uuidTSchema;
    }

    private Field getUuidField() {
        if (uuidField == null) {
            Schema uuidFieldSchema = null;
            if (strategy.isUuidOptional()) {
                List<Schema> union = new ArrayList<Schema>(2);
                union.add(getUuidType());
                union.add(Schema.create(Type.NULL));
                uuidFieldSchema = Schema.createUnion(union);
            } else {
                uuidFieldSchema = getUuidType();
            }
            uuidField = new Field(UUID_FIELD, uuidFieldSchema, null, null);;
            uuidField.addProp(DISPLAY_NAME_FIELD, UUID_FIELD_DISPLAY_NAME);
            uuidField.addProp(FIELD_ACCESS_FIELD, FIELD_ACCESS_READ_ONLY);
            return uuidField;
        }
        Field newUuidField = new Field(uuidField.name(), uuidField.schema(), null, null);
        AvroUtils.copyJsonProperties(uuidField, newUuidField);
        return newUuidField;
    }

    private Schema getResetType() {
        if (resetTSchema == null) {
            List<String> resetStrings = new ArrayList<String>(1);
            resetStrings.add(RESET);
            resetTSchema = Schema.createEnum(RESET + "T", null, KAA_NAMESPACE, resetStrings);
        }
        return resetTSchema;
    }

    private Schema getUnchangedType() {
        if (unchangedTSchema == null) {
            List<String> unchangedStrings = new ArrayList<String>(1);
            unchangedStrings.add(UNCHANGED);
            unchangedTSchema = Schema.createEnum(UNCHANGED + "T", null, KAA_NAMESPACE, unchangedStrings);
        }
        return unchangedTSchema;
    }

    private boolean isAddressableValue(Schema value) {
        if (value.getType().equals(Type.RECORD)) {
            if (value.getJsonProp(ADDRESSABLE_NAME) != null
                    && !value.getFullName().equals(rootSchemaName)) {
                return value.getJsonProp(ADDRESSABLE_NAME).asBoolean();
            }
            return true;
        }
        return false;
    }

    private void addResetTypeIfArray(Schema fieldType, List<Schema> union) {
        if (fieldType.getType().equals(Type.ARRAY) && strategy.isArrayEditable()) {
            union.add(0, getResetType());
        }
    }

    private Schema processArray(Schema root) throws SchemaCreationException {
        boolean hasAddressableItem = false;
        Schema copySchema = null;
        List<Schema> newItems = null;
        if (root.getElementType().getType().equals(Type.UNION)) {
            List<Schema> items = root.getElementType().getTypes();
            newItems = new ArrayList<Schema>(items.size() + 1);
            for (Schema itemIter : items) {
                Schema updatedItem = itemIter;
                if (AvroUtils.isComplexSchema(itemIter)) {
                    updatedItem = convert(itemIter);
                }
                newItems.add(updatedItem);
                if (isAddressableValue(itemIter) && strategy.isArrayEditable()) {
                    hasAddressableItem = true;
                }
            }
        } else if (strategy.isArrayEditable()) {
            hasAddressableItem = isAddressableValue(root.getElementType());
        }
        if (hasAddressableItem) {
            if (newItems == null) {
                newItems = new ArrayList<Schema>();
                newItems.add(convert(root.getElementType()));
            }
            newItems.add(getUuidType());
        }
        if (newItems != null) {
            copySchema = Schema.createArray(Schema.createUnion(newItems));
        } else {
            copySchema = Schema.createArray(convert(root.getElementType()));
        }
        AvroUtils.copyJsonProperties(root, copySchema);
        return copySchema;
    }

    private Schema processRecord(Schema root) throws SchemaCreationException {
        if (processedRecords.containsKey(root.getFullName())) {
            return processedRecords.get(root.getFullName());
        }
        Schema copySchema = Schema.createRecord(root.getName(), root.getDoc(), root.getNamespace(), root.isError());
        processedRecords.put(copySchema.getFullName(), copySchema);

        boolean addressable = isAddressableValue(root);
        List<Field> fields = root.getFields();
        List<Field> newFields = new ArrayList<Field>(fields.size() + 1);

        for (Field fieldIter : fields) {
            boolean optional = false;
            if (fieldIter.getJsonProp(OPTIONAL_NAME) != null) {
                optional = fieldIter.getJsonProp(OPTIONAL_NAME).asBoolean();
            }

            List<Schema> newUnion = new ArrayList<Schema>();

            if (AvroUtils.isComplexSchema(fieldIter.schema())) {
                addResetTypeIfArray(fieldIter.schema(), newUnion);
                newUnion.add(convert(fieldIter.schema()));
            } else if (fieldIter.schema().getType().equals(Type.UNION)) {
                List<Schema> oldUnion = fieldIter.schema().getTypes();
                for (Schema unionIter : oldUnion) {
                    Schema newItem = unionIter;
                    if (AvroUtils.isComplexSchema(unionIter)) {
                        addResetTypeIfArray(unionIter, newUnion);
                        newItem = convert(unionIter);
                    }
                    newUnion.add(newItem);
                }
            } else {
                newUnion.add(fieldIter.schema());
            }

            if (strategy.isUnchangedSupported()) {
                newUnion.add(getUnchangedType());
            }

            if (optional) {
                strategy.onOptionalField(newUnion);
            } else {
                strategy.onMandatoryField(newUnion);
            }

            Field newField = null;
            if (newUnion.size() > 1) {
                newField = new Field(fieldIter.name(), Schema.createUnion(newUnion), fieldIter.doc(), fieldIter.defaultValue());
            } else {
                newField = new Field(fieldIter.name(), newUnion.get(0), fieldIter.doc(), fieldIter.defaultValue());
            }
            AvroUtils.copyJsonProperties(fieldIter, newField);
            newFields.add(newField);
        }
        if (addressable) {
            // This record supports partial updates, adding "uuid" field
            newFields.add(getUuidField());
        }
        AvroUtils.copyJsonProperties(root, copySchema);
        copySchema.setFields(newFields);
        if (addressable) {
            // Adding addressable record's name to the storage
            String fullName = root.getFullName();;
            if (!fullName.equals(rootSchemaName)) {
                addressableRecords.add(copySchema);
            }
        }
        return copySchema;
    }

    private Schema convert(Schema root) throws SchemaCreationException {
        switch (root.getType()) {
        case ARRAY:
            return processArray(root);
        case RECORD:
            return processRecord(root);
        case MAP:
            throw new SchemaCreationException("Map is not supported");
        default:
            return root;
        }
    }

    @Override
    public T createSchema(DataSchema configSchema) throws SchemaCreationException {
        addressableRecords.clear();
        processedRecords.clear();
        Schema avroSchema = new Schema.Parser().parse(configSchema.getRawSchema());
        rootSchemaName = avroSchema.getFullName();
        Schema resultSchema = convert(avroSchema);
        return strategy.createSchema(strategy.onSchemaProcessed(resultSchema, addressableRecords));
    }
}
