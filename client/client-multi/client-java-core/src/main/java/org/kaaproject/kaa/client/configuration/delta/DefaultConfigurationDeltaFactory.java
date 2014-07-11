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

package org.kaaproject.kaa.client.configuration.delta;

import java.util.List;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.client.common.AvroGenericUtils;

/**
 * Default {@link ConfigurationDeltaFactory} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultConfigurationDeltaFactory implements
        ConfigurationDeltaFactory {

    private static final String UUID_FIELD = "__uuid";
    
    private DeltaType createArrayDeltaType(GenericArray array) {
        if (!array.isEmpty()) {
            Object rawItem = array.get(0);
            if (AvroGenericUtils.isFixed(rawItem)) {
                GenericArray<GenericFixed> arrayFixed = (GenericArray<GenericFixed>) array;
                if (AvroGenericUtils.isUuid(rawItem)) {
                    RemovedItemsDeltaType removedItems = new RemovedItemsDeltaType();
                    for (GenericFixed fixed : arrayFixed) {
                        UUID uuid = AvroGenericUtils.createUuidFromFixed(fixed);
                        removedItems.addHandlerId(new DeltaHandlerId(uuid));
                    }
                    return removedItems;
                } else {
                    AddedItemsDeltaType addedItems = new AddedItemsDeltaType();
                    for (GenericFixed fixed : arrayFixed) {
                        addedItems.addItem(fixed.bytes());
                    }
                    return addedItems;
                }
            } else if (AvroGenericUtils.isRecord(rawItem)) {
                GenericArray<GenericRecord> arrayRecord = (GenericArray<GenericRecord>) array;
                AddedItemsDeltaType addedItems = new AddedItemsDeltaType();
                for (GenericRecord record : arrayRecord) {
                    addedItems.addItem(createDelta(record));
                }
                return addedItems;
            } else {
                AddedItemsDeltaType addedItems = new AddedItemsDeltaType();
                for (Object item : array) {
                    addedItems.addItem(item);
                }
                return addedItems;
            }
        }
        return new AddedItemsDeltaType();
    }

    @Override
    public ConfigurationDelta createDelta(GenericRecord genericDelta) {
        DefaultConfigurationDelta resultDelta;
        GenericFixed uuidFixed = (GenericFixed) genericDelta.get(UUID_FIELD);
        if (uuidFixed != null) {
            DeltaHandlerId handlerId = new DeltaHandlerId(
                    AvroGenericUtils.createUuidFromFixed(uuidFixed));
            resultDelta = new DefaultConfigurationDelta(handlerId);
        } else {
            resultDelta = new DefaultConfigurationDelta();
        }

        Schema deltaSchema = genericDelta.getSchema();
        List<Schema.Field> fields = deltaSchema.getFields();
        for (Schema.Field field : fields) {
            Object rawField = genericDelta.get(field.name());
            if (!AvroGenericUtils.isUnchanged(rawField)) {
                if (rawField == null) {
                    resultDelta.updateFieldDeltaType(field.name(), new DefaultDeltaType());
                } else if (AvroGenericUtils.isReset(rawField)) {
                    resultDelta.updateFieldDeltaType(field.name(), new ResetDeltaType());
                } else if (AvroGenericUtils.isArray(rawField)) {
                    resultDelta.updateFieldDeltaType(field.name(), createArrayDeltaType((GenericArray) rawField));
                } else if (AvroGenericUtils.isFixed(rawField)) {
                    GenericFixed fixed = (GenericFixed) rawField;
                    resultDelta.updateFieldDeltaType(field.name(), new ValueDeltaType(fixed.bytes()));
                } else if (AvroGenericUtils.isEnum(rawField)) {
                    GenericEnumSymbol symbol = (GenericEnumSymbol) rawField;
                    resultDelta.updateFieldDeltaType(field.name(), new ValueDeltaType(symbol.toString()));
                } else if (AvroGenericUtils.isRecord(rawField)) {
                    GenericRecord record = (GenericRecord) rawField;
                    resultDelta.updateFieldDeltaType(field.name(), new ValueDeltaType(createDelta(record)));
                } else {
                    resultDelta.updateFieldDeltaType(field.name(), new ValueDeltaType(rawField));
                }
            }
        }
        return resultDelta;
    }

}
