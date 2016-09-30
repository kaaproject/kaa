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

package org.kaaproject.kaa.client.configuration.manager;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.client.common.AvroGenericUtils;
import org.kaaproject.kaa.client.common.CommonFactory;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonValue;
import org.kaaproject.kaa.client.common.DefaultCommonFactory;
import org.kaaproject.kaa.client.configuration.ConfigurationProcessedObserver;
import org.kaaproject.kaa.client.configuration.GenericDeltaReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default @{link ConfigurationManager} implementation.
 *
 * @author Yaroslav Zeygerman
 */
public class DefaultConfigurationManager implements GenericDeltaReceiver, ConfigurationManager,
        ConfigurationProcessedObserver {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigurationManager.class);
  private static final String UUID = "__uuid";

  private final CommonFactory commonFactory = new DefaultCommonFactory();
  private final Map<UUID, CommonRecord> records = new HashMap<UUID, CommonRecord>();
  private final List<ConfigurationReceiver> subscribers = new LinkedList<ConfigurationReceiver>();
  private CommonRecord rootRecord;

  public DefaultConfigurationManager() {

  }

  private CommonRecord createCommonRecord(GenericRecord avroRecord) {
    GenericFixed uuidFixed = (GenericFixed) avroRecord.get(UUID);
    if (uuidFixed != null) {
      UUID uuid = AvroGenericUtils.createUuidFromFixed(uuidFixed);
      CommonRecord newRecord = commonFactory.createCommonRecord(uuid, avroRecord.getSchema());
      records.put(uuid, newRecord);
      return newRecord;
    } else {
      return commonFactory.createCommonRecord(avroRecord.getSchema());
    }
  }

  private void processRecordField(
          CommonRecord record, GenericRecord deltaRecord, String fieldName) {
    CommonRecord nextRecord = null;
    CommonValue nextValue = record.getField(fieldName);
    if (nextValue != null
        && nextValue.isRecord()
        && nextValue.getRecord().getSchema().getFullName()
        .equals(deltaRecord.getSchema().getFullName())) {
      nextRecord = nextValue.getRecord();
      GenericFixed uuidFixed = (GenericFixed) deltaRecord.get(UUID);
      if (uuidFixed != null) {
        UUID uuid = AvroGenericUtils.createUuidFromFixed(uuidFixed);
        // Checking if the uuid was changed
        if (!uuid.equals(nextRecord.getUuid())) {
          records.remove(nextRecord.getUuid());
          records.put(uuid, nextRecord);
          nextRecord.setUuid(uuid);
        }
      }
    } else {
      nextRecord = createCommonRecord(deltaRecord);
      record.setField(fieldName, commonFactory.createCommonValue(nextRecord));
    }
    updateRecord(nextRecord, deltaRecord);
  }

  private void processArrayField(CommonRecord record, GenericArray array, String fieldName) {
    List<CommonValue> currentArray;
    CommonValue arrayValue = record.getField(fieldName);
    if (arrayValue != null && arrayValue.isArray()) {
      currentArray = arrayValue.getArray().getList();
    } else {
      currentArray = new LinkedList<CommonValue>();
      record.setField(fieldName, commonFactory.createCommonValue(
              commonFactory.createCommonArray(array.getSchema(), currentArray)));
    }
    if (!array.isEmpty()) {
      Object rawItem = array.get(0);
      if (AvroGenericUtils.isRecord(rawItem)) {
        GenericArray<GenericRecord> recordItems = (GenericArray<GenericRecord>) array;
        // Adding new records
        for (GenericRecord item : recordItems) {
          CommonRecord newRecord = createCommonRecord(item);
          updateRecord(newRecord, item);
          currentArray.add(commonFactory.createCommonValue(newRecord));
        }
      } else if (AvroGenericUtils.isFixed(rawItem)) {
        GenericArray<GenericFixed> fixedItems = (GenericArray<GenericFixed>) array;
        if (AvroGenericUtils.isUuid(rawItem)) {
          // Removing items with given uuids
          for (GenericFixed item : fixedItems) {
            UUID currentUuid = AvroGenericUtils.createUuidFromFixed(item);
            Iterator<CommonValue> valueIt = currentArray.iterator();
            while (valueIt.hasNext()) {
              CommonRecord currentRecord = valueIt.next().getRecord();
              if (currentRecord.getUuid().equals(currentUuid)) {
                valueIt.remove();
                records.remove(currentUuid);
                break;
              }
            }
          }
        } else {
          for (GenericFixed item : fixedItems) {
            currentArray.add(commonFactory.createCommonValue(
                    commonFactory.createCommonFixed(item.getSchema(), item.bytes())));
          }
        }
      } else {
        // Adding new primitive items
        for (Object item : array) {
          currentArray.add(commonFactory.createCommonValue(item));
        }
      }
    }
  }

  private void processEnumField(CommonRecord record, GenericEnumSymbol symbol, String fieldName) {
    Schema enumSchema = symbol.getSchema();
    if (AvroGenericUtils.isReset(symbol)) {
      record.getField(fieldName).getArray().getList().clear();
    } else if (!AvroGenericUtils.isUnchanged(symbol)) {
      record.setField(fieldName, commonFactory.createCommonValue(
              commonFactory.createCommonEnum(enumSchema, symbol.toString())));
    }
  }

  private void processFixedField(CommonRecord record, GenericFixed fixed, String fieldName) {
    record.setField(fieldName, commonFactory.createCommonValue(
            commonFactory.createCommonFixed(fixed.getSchema(), fixed.bytes())));
  }

  private void updateRecord(CommonRecord record, GenericRecord delta) {
    List<Field> deltaFields = delta.getSchema().getFields();
    for (Field deltaField : deltaFields) {
      String fieldName = deltaField.name();
      Object rawDeltaField = delta.get(fieldName);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Processing field \"{}\", current value: {}",
            fieldName, record.getField(fieldName) != null ? record
                .getField(fieldName).toString() : null);
      }
      if (AvroGenericUtils.isRecord(rawDeltaField)) {
        processRecordField(record, (GenericRecord) rawDeltaField, fieldName);
      } else if (AvroGenericUtils.isArray(rawDeltaField)) {
        processArrayField(record, (GenericArray) rawDeltaField, fieldName);
      } else if (AvroGenericUtils.isEnum(rawDeltaField)) {
        processEnumField(record, (GenericEnumSymbol) rawDeltaField, fieldName);
      } else if (AvroGenericUtils.isFixed(rawDeltaField)) {
        processFixedField(record, (GenericFixed) rawDeltaField, fieldName);
      } else {
        record.setField(fieldName, commonFactory.createCommonValue(rawDeltaField));
      }
    }
  }

  @Override
  public synchronized void onDeltaReceived(int index, GenericRecord data, boolean fullResync) {
    GenericFixed uuidFixed = (GenericFixed) data.get(UUID);
    UUID uuid = AvroGenericUtils.createUuidFromFixed(uuidFixed);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Processing delta with uuid {}", uuidFixed.toString());
    }
    CommonRecord currentRecord = null;
    if (!fullResync && records.containsKey(uuid)) {
      currentRecord = records.get(uuid);
    } else {
      records.clear();
      currentRecord = createCommonRecord(data);
      rootRecord = currentRecord;
    }
    updateRecord(currentRecord, data);
  }

  @Override
  public void onConfigurationProcessed() {
    CommonRecord copyRecord = commonFactory.createCommonRecord(rootRecord);
    synchronized (subscribers) {
      for (ConfigurationReceiver receiver : subscribers) {
        receiver.onConfigurationUpdated(copyRecord);
      }
    }
  }

  @Override
  public void subscribeForConfigurationUpdates(ConfigurationReceiver receiver) {
    synchronized (subscribers) {
      if (receiver != null && !subscribers.contains(receiver)) {
        subscribers.add(receiver);
      }
    }
  }

  @Override
  public void unsubscribeFromConfigurationUpdates(ConfigurationReceiver receiver) {
    synchronized (subscribers) {
      if (receiver != null) {
        subscribers.remove(receiver);
      }
    }
  }

  @Override
  public synchronized CommonRecord getConfiguration() {
    if (rootRecord != null) {
      return commonFactory.createCommonRecord(rootRecord);
    }
    return null;
  }

}
