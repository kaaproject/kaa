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

package org.kaaproject.kaa.server.common.core.algorithms.validator;

import static org.kaaproject.kaa.server.common.core.algorithms.CommonConstants.UUID_FIELD;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.configuration.KaaDataFactory;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUuidValidator<U extends KaaSchema, T extends KaaData> implements UuidValidator<T> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUuidValidator.class);

    private final U schema;

    private final KaaDataFactory<U, T> dataFactory;

    private final Set<GenericFixed> processedUuids = new HashSet<>();

    public DefaultUuidValidator(U schema, KaaDataFactory<U, T> factory) {
        this.schema = schema;
        this.dataFactory = factory;
    }

    private static boolean isRecordHaveUuid(GenericRecord record) {
        if (record != null) {
            Schema schema = record.getSchema();
            return schema.getField(UUID_FIELD) != null;
        }
        return false;
    }

    @SuppressWarnings({"rawtypes"})
    private static GenericRecord findRecordByUuid(GenericRecord rootRecord, Object uuid) {
        if (rootRecord != null && uuid != null) {
            if (isRecordHaveUuid(rootRecord)) {
                Object uuidValue = rootRecord.get(UUID_FIELD);
                if (uuid.equals(uuidValue)) {
                    return rootRecord;
                }
            }
            List<Schema.Field> fields = rootRecord.getSchema().getFields();
            if (fields != null && !fields.isEmpty()) {
                for (Schema.Field field : fields) {
                    int position = field.pos();
                    Object value = rootRecord.get(position);
                    if (value instanceof GenericRecord) {
                        GenericRecord record = (GenericRecord) value;
                        return findRecordByUuid(record, uuid);
                    } else if (value instanceof GenericArray) {
                        GenericArray array = (GenericArray) value;
                        if (array != null) {
                            for (Object item : array) {
                                if (item instanceof GenericRecord) {
                                    GenericRecord result = findRecordByUuid((GenericRecord) item, uuid);
                                    if (result != null) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void generateUuidForRecord(GenericRecord record) {
        GenericData.Fixed newUuid = AvroUtils.generateUuidObject();
        LOG.trace("Generated new UUID {}", newUuid);
        record.put(UUID_FIELD, newUuid);
        processedUuids.add(newUuid);
    }

    private void copyUuid(GenericRecord destRecord, GenericRecord srcRecord) {
        GenericData.Fixed uuid = (GenericData.Fixed) srcRecord.get(UUID_FIELD);
        LOG.trace("Replacing with previous UUID {}", uuid);
        destRecord.put(UUID_FIELD, uuid);
        processedUuids.add(uuid);
    }

    /**
     * Validating record's uuid field. If empty uuid fields exists in the record,
     * the new values will be generated.
     *
     * @param currentRecord the current record.
     * @param previousRecord the previous record.
     * @param rootPreviousRecord the root previous record.
     * @return the generic record with updated uuid fields.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private GenericRecord validateRecord(GenericRecord currentRecord, GenericRecord previousRecord, GenericRecord rootPreviousRecord) {
        if (currentRecord != null) {
            LOG.trace("Processing new record: {}, old record: {}", currentRecord, previousRecord);
            if (isRecordHaveUuid(currentRecord)) {
                GenericData.Fixed uuidValue = (GenericData.Fixed) currentRecord.get(UUID_FIELD);
                if (uuidValue == null) {
                    if (previousRecord != null) {
                        copyUuid(currentRecord, previousRecord);
                    } else {
                        generateUuidForRecord(currentRecord);
                    }
                } else {
                    LOG.trace("Validating existing UUID {}", uuidValue);
                    if (!processedUuids.contains(uuidValue)) {
                        if (previousRecord == null) {
                            GenericRecord validatingRecord = findRecordByUuid(rootPreviousRecord, uuidValue);
                            if (validatingRecord == null
                                    || !validatingRecord.getSchema().getFullName().equals(currentRecord.getSchema().getFullName())) {
                                LOG.trace("Unknown UUID {}. Generating a new one", uuidValue);
                                generateUuidForRecord(currentRecord);
                            } else if (!uuidValue.equals(validatingRecord.get(UUID_FIELD))) {
                                copyUuid(currentRecord, validatingRecord);
                            } else {
                                LOG.trace("UUID {} is valid", uuidValue);
                                processedUuids.add(uuidValue);
                            }
                        } else if (!uuidValue.equals(previousRecord.get(UUID_FIELD))) {
                            copyUuid(currentRecord, previousRecord);
                        } else {
                            LOG.trace("UUID {} is valid", uuidValue);
                            processedUuids.add(uuidValue);
                        }
                    } else {
                        LOG.trace("UUID {} is already in use. Generating a new one", uuidValue);
                        generateUuidForRecord(currentRecord);
                    }
                }
            }

            List<Schema.Field> fields = currentRecord.getSchema().getFields();
            if (fields != null && !fields.isEmpty()) {
                for (Schema.Field field : fields) {
                    int position = field.pos();
                    Object currentValue = currentRecord.get(position);
                    Object previousValue = null;
                    if (previousRecord != null) {
                        previousValue = previousRecord.get(position);
                    }
                    if (currentValue instanceof GenericRecord) {
                        GenericRecord subResult = null;
                        GenericRecord currentSubRecord = (GenericRecord) currentValue;
                        if (previousValue != null && previousValue instanceof GenericRecord) {
                            GenericRecord previousSubRecord = (GenericRecord) previousValue;
                            String currentName = currentSubRecord.getSchema().getFullName();
                            String previousName = previousSubRecord.getSchema().getFullName();
                            subResult = validateRecord(currentSubRecord,
                                    currentName.equals(previousName) ?
                                            previousSubRecord :
                                            null, rootPreviousRecord);
                        } else {
                            subResult = validateRecord(currentSubRecord, null, rootPreviousRecord);
                        }
                        currentRecord.put(position, subResult);
                    } else if (currentValue instanceof GenericArray) {
                        LOG.trace("Found array value {}", currentValue);
                        GenericArray array = (GenericArray) currentValue;
                        if (array != null) {
                            int size = array.size();
                            for (int i = 0; i < size; i++) {
                                Object item = array.get(i);
                                if (item instanceof GenericRecord) {
                                    GenericRecord itemRecord = (GenericRecord) item;
                                    array.set(i, validateRecord(itemRecord, null, rootPreviousRecord));
                                }
                            }
                            currentRecord.put(position, array);
                        }
                    }
                }
            }
        }
        return currentRecord;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.core.algorithms.validator.UuidValidator#validateUuidFields(T, T)
     */
    @Override
    public T validateUuidFields(T configurationToValidate, T previousConfiguration) throws IOException {
        processedUuids.clear();
        String config = null;
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema.getRawSchema());
        GenericRecord currentRecord = converter.decodeJson(configurationToValidate.getRawData());
        GenericRecord previousRecord = null;
        if (previousConfiguration != null) {
            previousRecord = converter.decodeJson(previousConfiguration.getRawData());
        }
        validateRecord(currentRecord, previousRecord, previousRecord);
        if(currentRecord != null) {
            config = converter.encodeToJson(currentRecord);
        }
        LOG.trace("Generated uuid fields for records {}", currentRecord);
        return dataFactory.createData(schema, config);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.core.algorithms.validator.UuidValidator#validateUuidFields(org.apache.avro.generic.GenericRecord, org.apache.avro.generic.GenericRecord)
     */
    @Override
    public T validateUuidFields(GenericRecord configurationToValidate, GenericRecord previousConfiguration) throws IOException {
        processedUuids.clear();
        String config = null;
        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(schema.getRawSchema());
        validateRecord(configurationToValidate, previousConfiguration, previousConfiguration);
        if(configurationToValidate != null) {
            config = converter.encodeToJson(configurationToValidate);
        }
        LOG.trace("Generated uuid fields for records {}", configurationToValidate);
        return dataFactory.createData(schema, config);
    }

}
