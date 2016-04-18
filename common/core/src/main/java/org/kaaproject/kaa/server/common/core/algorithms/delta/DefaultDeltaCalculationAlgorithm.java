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

package org.kaaproject.kaa.server.common.core.algorithms.delta;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.DELTA;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.KAA_NAMESPACE;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.RESET;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UNCHANGED;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;
import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_TYPE;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.AvroDataCanonizationUtils;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link DeltaCalculationAlgorithm}.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultDeltaCalculationAlgorithm implements DeltaCalculationAlgorithm {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultDeltaCalculationAlgorithm.class);

    /** The delta schema. */
    private final Schema deltaSchema;

    private final Schema baseSchema;

    private Set<RecordTuple> processedRecords;

    /** The last uuid delta. */
    private RecordTuple lastUuidDelta;

    /** The last uuid records. */
    private RecordTuple lastUuidRecords;

    /** The result delta. */
    private AvroBinaryDelta resultDelta;

    /* FieldAttribute */
    /**
     * The Class FieldAttribute.
     */
    private class FieldAttribute {

        /** The field schema. */
        private final Schema fieldSchema;

        /** The field name. */
        private final String fieldName;

        /**
         * Instantiates a new field attribute.
         *
         * @param fieldSchema the field schema
         * @param fieldName the field name
         */
        public FieldAttribute(Schema fieldSchema, String fieldName) {
            this.fieldSchema = fieldSchema;
            this.fieldName = fieldName;
        }

        /**
         * Gets the field schema.
         *
         * @return the field schema
         */
        public Schema getFieldSchema() {
            return fieldSchema;
        }

        /**
         * Gets the field name.
         *
         * @return the field name
         */
        public String getFieldName() {
            return fieldName;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
            result = prime * result + ((fieldSchema == null) ? 0 : fieldSchema.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            FieldAttribute other = (FieldAttribute) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (fieldName == null) {
                if (other.fieldName != null) {
                    return false;
                }
            } else if (!fieldName.equals(other.fieldName)) {
                return false;
            }
            if (fieldSchema == null) {
                if (other.fieldSchema != null) {
                    return false;
                }
            } else if (!fieldSchema.equals(other.fieldSchema)) {
                return false;
            }
            return true;
        }

        /**
         * Gets the outer type.
         *
         * @return the outer type
         */
        private DefaultDeltaCalculationAlgorithm getOuterType() {
            return DefaultDeltaCalculationAlgorithm.this;
        }

    }

    /* RecordTuple */
    /**
     * The Class RecordTuple.
     */
    private class RecordTuple {

        /** The old record. */
        private final GenericRecord oldRecord;

        /** The new record. */
        private final GenericRecord newRecord;

        /**
         * Instantiates a new record tuple.
         *
         * @param oldRecord the old record
         * @param newRecord the new record
         */
        public RecordTuple(GenericRecord oldRecord, GenericRecord newRecord) {
            this.oldRecord = oldRecord;
            this.newRecord = newRecord;
        }

        /**
         * Gets the old record.
         *
         * @return the old record
         */
        public GenericRecord getOldRecord() {
            return oldRecord;
        }

        /**
         * Gets the new record.
         *
         * @return the new record
         */
        public GenericRecord getNewRecord() {
            return newRecord;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((newRecord == null) ? 0 : newRecord.hashCode());
            result = prime * result + ((oldRecord == null) ? 0 : oldRecord.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RecordTuple other = (RecordTuple) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (newRecord == null) {
                if (other.newRecord != null) {
                    return false;
                }
            } else if (!newRecord.equals(other.newRecord)) {
                return false;
            }
            if (oldRecord == null) {
                if (other.oldRecord != null) {
                    return false;
                }
            } else if (!oldRecord.equals(other.oldRecord)) {
                return false;
            }
            return true;
        }

        /**
         * Gets the outer type.
         *
         * @return the outer type
         */
        private DefaultDeltaCalculationAlgorithm getOuterType() {
            return DefaultDeltaCalculationAlgorithm.this;
        }

    }

    /**
     * Instantiates a new default delta calculator.
     *
     * @param deltaSchema the schema
     * @param baseSchema the schema
     */
    public DefaultDeltaCalculationAlgorithm(Schema deltaSchema, Schema baseSchema) {
        this.deltaSchema = deltaSchema;
        this.baseSchema = baseSchema;
    }

    /**
     * Gets the delta schema by full name.
     *
     * @param fullName the full name
     * @return the delta schema by full name
     */
    private Schema getDeltaSchemaByFullName(String fullName) {
        Schema deltaT = deltaSchema.getElementType();
        Schema deltaUnion = deltaT.getField(DELTA).schema();
        List<Schema> deltas = deltaUnion.getTypes();
        for (Schema delta : deltas) {
            if (delta.getFullName().equals(fullName)) {
                return delta;
            }
        }
        return null;
    }

    /**
     * Gets the full name.
     *
     * @param record the record
     * @return the full name
     */
    private static String getFullName(GenericContainer record) {
        return record.getSchema().getFullName();
    }

    /**
     * Gets the array schema.
     *
     * @param delta the delta
     * @param field the field
     * @return the array schema
     */
    private static Schema getArraySchema(GenericRecord delta, String field) {
        List<Schema> fieldTypes = delta.getSchema().getField(field).schema().getTypes();
        for (Schema type : fieldTypes) {
            if (type.getType() == Schema.Type.ARRAY) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the schema by full name.
     *
     * @param types the types
     * @param fullName the full name
     * @return the schema by full name
     */
    private static Schema getSchemaByFullName(List<Schema> types, String fullName) {
        for (Schema type : types) {
            if (type.getFullName().equals(fullName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets the schema by full name.
     *
     * @param delta the delta
     * @param field the field
     * @param fullName the full name
     * @return the schema by full name
     */
    private static Schema getSchemaByFullName(GenericRecord delta, String field, String fullName) {
        Schema fieldSchema = delta.getSchema().getField(field).schema();
        if (fieldSchema.getType() == Schema.Type.UNION) {
            List<Schema> fieldTypes = fieldSchema.getTypes();
            return getSchemaByFullName(fieldTypes, fullName);
        } else {
            return fieldSchema.getFullName().equals(fullName) ? fieldSchema : null;
        }
    }

    /**
     * Gets the schema by full name.
     *
     * @param arraySchema the array schema
     * @param fullName the full name
     * @return the schema by full name
     */
    private static Schema getSchemaByFullName(Schema arraySchema, String fullName) {
        if (arraySchema.getElementType().getType() == Schema.Type.UNION) {
            List<Schema> itemTypes = arraySchema.getElementType().getTypes();
            return getSchemaByFullName(itemTypes, fullName);
        } else {
            return arraySchema.getElementType().getFullName().equals(fullName) ? arraySchema.getElementType() : null;
        }
    }

    /**
     * Put notchaged.
     *
     * @param delta the delta
     * @param field the field
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private static void putUnchanged(GenericRecord delta, String field) throws DeltaCalculatorException {
        Schema unchangedSchema = getSchemaByFullName(delta, field, KAA_NAMESPACE + "." + UNCHANGED + "T");
        if (unchangedSchema != null) {
            GenericEnumSymbol unchanged = new GenericData.EnumSymbol(unchangedSchema, UNCHANGED);
            delta.put(field, unchanged);
        } else {
            throw new DeltaCalculatorException(new StringBuilder()
                    .append("Failed to find schema for \"unchanged\" type ").append(" in ")
                    .append(delta.getSchema().getFullName()).append(" field ").append(field).toString());
        }
    }

    /**
     * Put reset.
     *
     * @param delta the delta
     * @param field the field
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private static void putReset(GenericRecord delta, String field) throws DeltaCalculatorException {
        Schema resetSchema = getSchemaByFullName(delta, field, KAA_NAMESPACE + "." + RESET + "T");
        if (resetSchema != null) {
            GenericEnumSymbol reset = new GenericData.EnumSymbol(resetSchema, RESET);
            delta.put(field, reset);
        } else {
            throw new DeltaCalculatorException(new StringBuilder().append("Failed to find schema for \"reset\" type ")
                    .append(" in ").append(delta.getSchema().getFullName()).append(" field ").append(field).toString());
        }
    }

    /**
     * Creates the sub delta.
     *
     * @param delta the delta
     * @param field the field
     * @param record the record
     * @return the generic record
     */
    private static GenericRecord createSubDelta(GenericRecord delta, String field, GenericRecord record) {
        Schema recordType = getSchemaByFullName(delta, field, getFullName(record));
        return recordType == null ? null : new GenericData.Record(recordType);
    }

    /**
     * Fill delta array fields.
     *
     * @param delta the delta
     * @param resetFields the reset fields
     * @param uuidFields the uuid fields
     * @param fieldQueue the field queue
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private static void fillDeltaArrayFields(GenericRecord delta, Set<String> resetFields,
            Map<String, List<byte[]>> uuidFields, Queue<FieldAttribute> fieldQueue) throws DeltaCalculatorException {
        List<Schema.Field> fields = delta.getSchema().getFields();

        if (fieldQueue.isEmpty()) {
            for (Schema.Field field : fields) {
                if (resetFields.contains(field.name())) {
                    putReset(delta, field.name());
                } else if (uuidFields.containsKey(field.name())) {
                    Schema arraySchema = getArraySchema(delta, field.name());
                    Schema uuidSchema = getSchemaByFullName(arraySchema, KAA_NAMESPACE + "." + UUID_TYPE);

                    List<byte[]> uuids = uuidFields.get(field.name());
                    GenericArray arrayField = new GenericData.Array(uuids.size(), arraySchema);
                    for (byte[] uuid : uuids) {
                        GenericFixed uuidFixed = new GenericData.Fixed(uuidSchema, uuid);
                        arrayField.add(uuidFixed);
                    }
                    delta.put(field.name(), arrayField);
                } else if (!field.name().equals(UUID_FIELD)) {
                    putUnchanged(delta, field.name());
                }
            }
        } else {
            FieldAttribute nextField = fieldQueue.poll();
            for (Schema.Field field : fields) {
                if (field.name().equals(nextField.getFieldName())) {
                    GenericRecord subDelta = new GenericData.Record(nextField.getFieldSchema());
                    fillDeltaArrayFields(subDelta, resetFields, uuidFields, fieldQueue);
                    delta.put(field.name(), subDelta);
                } else if (!field.name().equals(UUID_FIELD)) {
                    putUnchanged(delta, field.name());
                }
            }
        }
    }

    /**
     * Adds the complex item to array.
     *
     * @param container the record
     * @param array the array
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private void addComplexItemToArray(GenericContainer container, GenericArray array) throws DeltaCalculatorException {
        Schema itemSchema = getSchemaByFullName(array.getSchema(), getFullName(container));
        if (itemSchema.getType() == Type.RECORD) {
            GenericRecord subDelta = new GenericData.Record(itemSchema);
            fillDeltaWithoutMerge(subDelta, (GenericRecord) container);
            array.add(subDelta);
        } else {
            array.add(container);
        }
    }

    /**
     * Process complex field.
     *
     * @param delta the delta
     * @param field the field
     * @param newRecordValue the new record value
     * @param oldRecordValue the old record value
     * @param fieldQueue the field queue
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private void processComplexField(GenericRecord delta, String field, GenericContainer newRecordValue,
            GenericContainer oldRecordValue, Queue<FieldAttribute> fieldQueue) throws DeltaCalculatorException {
        boolean fieldChanged = false;
        if (newRecordValue.getSchema().getType() == Type.RECORD) {
            GenericRecord subDelta = createSubDelta(delta, field, (GenericRecord) newRecordValue);
            if (subDelta != null) {
                boolean hasChanges = false;
                if (oldRecordValue != null && oldRecordValue.getSchema().getFullName().equals(newRecordValue.getSchema().getFullName())) {
                    FieldAttribute fieldPair = new FieldAttribute(getSchemaByFullName(delta, field,
                            getFullName(newRecordValue)), field);
                    Queue<FieldAttribute> newFieldQueue = new LinkedList<FieldAttribute>(fieldQueue);
                    newFieldQueue.offer(fieldPair);
                    hasChanges = fillDelta(subDelta, (GenericRecord) oldRecordValue, (GenericRecord) newRecordValue,
                            newFieldQueue);
                } else {
                    fillDeltaWithoutMerge(subDelta, (GenericRecord) newRecordValue);
                    hasChanges = true;
                }
                if (hasChanges) {
                    delta.put(field, subDelta);
                    fieldChanged = true;
                }
            } else {
                throw new DeltaCalculatorException(new StringBuilder().append("Failed to find subdelta schema \"")
                        .append(getFullName(newRecordValue)).append("\"").toString());
            }
        } else if (oldRecordValue == null || field.equals(UUID_FIELD)
                || !newRecordValue.equals(oldRecordValue)) {
            delta.put(field, newRecordValue);
            fieldChanged = true;
        }
        if (!fieldChanged) {
            putUnchanged(delta, field);
        }
    }

    /**
     * Fill delta without merge.
     *
     * @param delta the delta
     * @param root the root
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private void fillDeltaWithoutMerge(GenericRecord delta, GenericRecord root) throws DeltaCalculatorException {
        Schema rootSchema = root.getSchema();
        for (Field field : rootSchema.getFields()) {
            Object value = root.get(field.name());
            if (value instanceof List) {
                List<Object> values = (List<Object>) value;
                Schema arraySchema = getArraySchema(delta, field.name());
                GenericArray deltaArray = new GenericData.Array(values.size(), arraySchema);
                for (Object item : values) {
                    if (item instanceof GenericContainer) {
                        GenericContainer record = (GenericContainer) item;
                        addComplexItemToArray(record, deltaArray);
                    } else {
                        deltaArray.add(item);
                    }
                }
                delta.put(field.name(), deltaArray);
            } else if (value instanceof GenericContainer) {
                processComplexField(delta, field.name(),  (GenericContainer) value, null, null);
            } else {
                delta.put(field.name(), value);
            }
        }
    }

    /**
     * Fill delta.
     *
     * @param delta the delta
     * @param oldRoot the old root
     * @param newRoot the new root
     * @param fieldQueue the field queue
     * @return true, if successful
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private boolean fillDelta(GenericRecord delta, GenericRecord oldRoot, GenericRecord newRoot,
            Queue<FieldAttribute> fieldQueue) throws DeltaCalculatorException {
        boolean hasChanges = false;
        Set<String> resetFields = new HashSet<String>();
        Map<String, List<byte[]>> uuidsToRemove = new HashMap<String, List<byte[]>>();

        Schema oldSchema = oldRoot.getSchema();
        Schema newSchema = newRoot.getSchema();

        if (oldSchema.getField(UUID_FIELD) != null) {
            lastUuidDelta = new RecordTuple(oldRoot, newRoot);
            fieldQueue.clear();
        }
        RecordTuple currentUuidRecord = lastUuidDelta;

        for (Field newField : newSchema.getFields()) {
            Object oldValue = oldRoot.get(newField.name());
            Object newValue = newRoot.get(newField.name());
            if (newValue instanceof List) {
                List<byte[]> uuids = new LinkedList<byte[]>();

                List<Object> oldArrayItems = (oldValue instanceof List) ? (List) oldValue : null;
                List<Object> newArrayItems = new LinkedList<Object>((List<Object>) newValue);

                if (!newArrayItems.isEmpty()) {
                    if (newArrayItems.get(0) instanceof GenericRecord) {
                        // Item is a complex type
                        if (oldArrayItems != null && !oldArrayItems.isEmpty() && oldArrayItems.get(0) instanceof GenericRecord) {
                            for (Object oldItem : oldArrayItems) {
                                GenericRecord oldItemRecord = (GenericRecord) oldItem;
                                Schema oldItemSchema = oldItemRecord.getSchema();
                                if (oldItemSchema.getField(UUID_FIELD) != null) {
                                    // Addressable array item. Looking for the
                                    // record with the same uuid in new items
                                    boolean isRecordExists = false;
                                    GenericFixed uuid = (GenericFixed) oldItemRecord.get(UUID_FIELD);
                                    Iterator it = newArrayItems.iterator();
                                    while (it.hasNext()) {
                                        GenericRecord newItemRecord = (GenericRecord) it.next();
                                        if (uuid.equals(newItemRecord.get(UUID_FIELD))) {
                                            processDifferences(oldItemRecord, newItemRecord);
                                            isRecordExists = true;
                                            // This new item has been processed.
                                            // Removing it from the list
                                            it.remove();
                                            break;
                                        }
                                    }
                                    if (!isRecordExists) {
                                        // Adding uuid to list to remove this
                                        // item
                                        GenericFixed uuidFixed = (GenericFixed) oldItemRecord.get(UUID_FIELD);
                                        byte[] uuidRaw = uuidFixed.bytes();
                                        uuids.add(uuidRaw);
                                        hasChanges = true;
                                    }
                                } else {
                                    // Non-addressable complex item. We can't
                                    // create the partial update delta for it
                                    boolean itemChanged = true;
                                    if (oldArrayItems.size() == newArrayItems.size()) {
                                        Iterator it = newArrayItems.iterator();
                                        while (it.hasNext()) {
                                            GenericRecord newItemRecord = (GenericRecord) it.next();
                                            if (newItemRecord.equals(oldItemRecord)) {
                                                // This new item has been
                                                // processed. Removing it from
                                                // the list
                                                it.remove();
                                                itemChanged = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (itemChanged) {
                                        resetFields.add(newField.name());
                                        newArrayItems = new LinkedList<Object>((List<Object>) newValue);
                                        hasChanges = true;
                                        break;
                                    }
                                }
                            }
                        } else if (oldArrayItems != null && !oldArrayItems.isEmpty()) {
                            resetFields.add(newField.name());
                        }
                        if (!newArrayItems.isEmpty()) {
                            Schema arraySchema = getArraySchema(delta, newField.name());
                            GenericArray deltaArray = new GenericData.Array(newArrayItems.size(), arraySchema);
                            // Adding all new elements to delta
                            for (Object item : newArrayItems) {
                                GenericContainer newItemRecord = (GenericContainer) item;
                                addComplexItemToArray(newItemRecord, deltaArray);
                            }
                            delta.put(newField.name(), deltaArray);
                            hasChanges = true;
                        } else {
                            putUnchanged(delta, newField.name());
                        }
                    } else {
                        // Item is a primitive type
                        if (!newArrayItems.equals(oldArrayItems)) {
                            // Field should be reseted
                            if (oldArrayItems != null) {
                                resetFields.add(newField.name());
                            }
                            // Adding all elements from new array
                            Schema arraySchema = getArraySchema(delta, newField.name());
                            GenericArray deltaArray = new GenericData.Array(arraySchema, newArrayItems);
                            delta.put(newField.name(), deltaArray);
                            hasChanges = true;
                        } else {
                            putUnchanged(delta, newField.name());
                        }
                    }
                } else if (oldArrayItems == null) {
                    delta.put(newField.name(), new GenericData.Array(0, getArraySchema(delta, newField.name())));
                    hasChanges = true;
                } else if (!oldArrayItems.isEmpty()) {
                    resetFields.add(newField.name());
                    hasChanges = true;
                } else {
                    putUnchanged(delta, newField.name());
                }

                if (!uuids.isEmpty()) {
                    if (oldArrayItems != null && uuids.size() == oldArrayItems.size()) {
                        // If all items should be deleted, we can set "reset" to
                        // this field
                        resetFields.add(newField.name());
                    } else {
                        uuidsToRemove.put(newField.name(), uuids);
                    }
                }
            } else if (newValue instanceof GenericContainer) {
                GenericContainer newRecordValue = (GenericContainer) newValue;
                GenericContainer oldRecordValue = (oldValue instanceof GenericContainer) ? (GenericContainer) oldValue : null;
                processComplexField(delta, newField.name(), newRecordValue, oldRecordValue, fieldQueue);
            } else if ((newValue == null && oldValue != null)
                    || (newValue != null && !newValue.equals(oldValue))) {
                delta.put(newField.name(), newValue);
                hasChanges = true;
            } else {
                putUnchanged(delta, newField.name());
            }
        }

        lastUuidDelta = currentUuidRecord;

        if (!uuidsToRemove.isEmpty() || !resetFields.isEmpty()) {
            GenericRecord arrayDelta = new GenericData.Record(
                    getDeltaSchemaByFullName(getFullName(lastUuidDelta.getNewRecord())));
            GenericFixed uuid = (GenericFixed) lastUuidDelta.getNewRecord().get(UUID_FIELD);
            arrayDelta.put(UUID_FIELD, uuid);
            Queue<FieldAttribute> newFieldQueue = new LinkedList<FieldAttribute>(fieldQueue);
            fillDeltaArrayFields(arrayDelta, resetFields, uuidsToRemove, newFieldQueue);
            resultDelta.addDelta(arrayDelta);
        }
        return hasChanges;
    }

    /**
     * Process differences.
     *
     * @param oldRoot the old root
     * @param newRoot the new root
     * @throws DeltaCalculatorException the delta calculator exception
     */
    private void processDifferences(GenericRecord oldRoot, GenericRecord newRoot)
            throws DeltaCalculatorException {
        Schema oldSchema = oldRoot.getSchema();

        boolean hasDifferences = false;
        if (oldSchema.getField(UUID_FIELD) != null) {
            if (oldRoot.get(UUID_FIELD).equals(newRoot.get(UUID_FIELD))) {
                lastUuidRecords = new RecordTuple(oldRoot, newRoot);
            } else {
                hasDifferences = true;
            }
        }

        LinkedList<RecordTuple> nextRecords = new LinkedList<RecordTuple>(); //NOSONAR
        if (!hasDifferences) {
            for (Field oldField : oldSchema.getFields()) {
                Object newValue = newRoot.get(oldField.name());
                Object oldValue = oldRoot.get(oldField.name());
                if (newValue instanceof GenericRecord && oldValue instanceof GenericRecord) {
                    GenericRecord oldRecord = (GenericRecord) oldValue;
                    GenericRecord newRecord = (GenericRecord) newValue;
                    if (oldRecord.getSchema().getFullName().equals(newRecord.getSchema().getFullName())) {
                        nextRecords.offer(new RecordTuple(oldRecord, newRecord));
                    } else {
                        hasDifferences = true;
                        break;
                    }
                } else if (newValue instanceof List && oldValue instanceof List) {
                    List<Object> oldArray = (List<Object>) oldValue;
                    List<Object> newArray = (List<Object>) newValue;

                    hasDifferences = !(oldArray.size() == newArray.size());
                    if (!hasDifferences) {
                        if (!newArray.isEmpty() && newArray.get(0) instanceof GenericRecord && oldArray.get(0) instanceof GenericRecord) {
                            GenericRecord uuidCheckRecord = (GenericRecord) newArray.get(0);
                            Schema uuidCheckSchema = uuidCheckRecord.getSchema();
                            if (uuidCheckSchema.getField(UUID_FIELD) != null) {
                                for (Object oldItem : oldArray) {
                                    GenericRecord oldItemRecord = (GenericRecord) oldItem;
                                    boolean isRecordExists = false;
                                    GenericFixed uuid = (GenericFixed) oldItemRecord.get(UUID_FIELD);
                                    if (uuid != null) {
                                        for (Object it : newArray) {
                                            GenericRecord newItemRecord = (GenericRecord) it;
                                            GenericFixed newUuid = (GenericFixed) newItemRecord.get(UUID_FIELD);
                                            if (uuid.equals(newUuid)) {
                                                nextRecords.offer(new RecordTuple(oldItemRecord, newItemRecord));
                                                isRecordExists = true;
                                                break;
                                            }
                                        }
                                        if (!isRecordExists) {
                                            hasDifferences = true;
                                            break;
                                        }
                                    } else {
                                        hasDifferences = true;
                                        break;
                                    }
                                }
                            } else {
                                hasDifferences = !newArray.equals(oldArray);
                            }
                            if (hasDifferences) {
                                break;
                            }
                        } else if (!newArray.equals(oldArray)) {
                            hasDifferences = true;
                            break;
                        }
                    }
                } else if ((newValue == null && oldValue != null)
                        || (newValue != null && !newValue.equals(oldValue))) {
                    hasDifferences = true;
                    break;
                }
            }
        }
        String lastUuidRecordName = getFullName(lastUuidRecords.getOldRecord());
        if (hasDifferences && !processedRecords.contains(lastUuidRecords)) {
            Schema deltaSubSchema = getDeltaSchemaByFullName(lastUuidRecordName);
            if (deltaSubSchema == null) {
                throw new DeltaCalculatorException(new StringBuilder().append("Failed to find schema for \"")
                        .append(lastUuidRecordName).append("\"").toString());
            }
            GenericRecord delta = new GenericData.Record(deltaSubSchema);
            fillDelta(delta, lastUuidRecords.getOldRecord(), lastUuidRecords.getNewRecord(),
                    new LinkedList<FieldAttribute>());
            resultDelta.addDelta(delta);
            processedRecords.add(lastUuidRecords);
        } else {
            RecordTuple currentUuidRecords = lastUuidRecords;
            for (RecordTuple tuple : nextRecords) {
                processDifferences(tuple.getOldRecord(), tuple.getNewRecord());
                if (!lastUuidRecords.equals(currentUuidRecords)) {
                    lastUuidRecords = currentUuidRecords;
                }
            }
        }
    }

    @Override
    public RawBinaryDelta calculate(BaseData endpointConfiguration, BaseData newConfigurationBody)
            throws IOException, DeltaCalculatorException {
        GenericRecord oldRoot = getRootNode(endpointConfiguration, baseSchema);
        GenericRecord newRoot = getRootNode(newConfigurationBody, baseSchema);
        return calculate(oldRoot, newRoot);
    }

    @Override
    public RawBinaryDelta calculate(BaseData newConfigurationBody) throws IOException, DeltaCalculatorException {
        GenericRecord newRoot = getRootNode(newConfigurationBody, baseSchema);
        return calculate(newRoot);
    }

    public RawBinaryDelta calculate(GenericRecord oldConfig, GenericRecord newConfig) throws DeltaCalculatorException {
        resultDelta = new AvroBinaryDelta(deltaSchema);
        processedRecords = new HashSet<>();
        processDifferences(oldConfig, newConfig);
        return resultDelta;
    }

    public RawBinaryDelta calculate(GenericRecord newConfig) throws DeltaCalculatorException {
        resultDelta = new AvroBinaryDelta(deltaSchema);
        Schema deltaSubSchema = getDeltaSchemaByFullName(getFullName(newConfig));
        if (deltaSubSchema == null) {
            throw new DeltaCalculatorException(new StringBuilder().append("Failed to find schema for \"")
                    .append(getFullName(newConfig)).append("\"").toString());
        }
        GenericRecord delta = new GenericData.Record(deltaSubSchema);
        fillDeltaWithoutMerge(delta, newConfig);
        AvroDataCanonizationUtils.canonizeRecord(delta);
        resultDelta.addDelta(delta);
        return resultDelta;
    }

    /**
     * Gets the root node.
     *
     * @param data the base data object
     * @return the root node
     */
    private GenericRecord getRootNode(BaseData data, Schema schema) throws IOException {
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<GenericRecord>(schema);
        return converter.decodeJson(data.getRawData());
    }

}