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

#include "kaa/configuration/delta/DefaultConfigurationDeltaFactory.hpp"

#include <cstdint>
#include <string>

#include <cstdint>

#include <avro/Generic.hh>

#include "kaa/common/AvroGenericUtils.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/configuration/delta/DefaultDeltaType.hpp"
#include "kaa/configuration/delta/ResetDeltaType.hpp"
#include "kaa/configuration/delta/ValueDeltaType.hpp"
#include "kaa/configuration/delta/AddedItemsDeltaType.hpp"
#include "kaa/configuration/delta/RemovedItemsDeltaType.hpp"
#include "kaa/configuration/delta/DefaultConfigurationDelta.hpp"

namespace kaa {

ConfigurationDeltaPtr DefaultConfigurationDeltaFactory::createDelta(const avro::GenericDatum& genericDelta)
{
    DefaultConfigurationDelta *configurationDelta = nullptr;

    if (genericDelta.value<avro::GenericRecord>().hasField("__uuid")) {
        DeltaHandlerId handlerId(AvroGenericUtils::getUuidFromDatum(genericDelta));
        configurationDelta = new DefaultConfigurationDelta(handlerId);
    } else {
        configurationDelta = new DefaultConfigurationDelta();
    }

    ConfigurationDeltaPtr configurationDeltaPtr(configurationDelta);
    std::size_t fieldCount = genericDelta.value<avro::GenericRecord>().fieldCount();

    for (std::size_t index = 0; index < fieldCount; ++index) {
        const avro::GenericDatum& datum = genericDelta.value<avro::GenericRecord>().fieldAt(index);

        if (!AvroGenericUtils::isUnchanged(datum)) {
            DeltaTypePtr deltaType;
            const std::string& fieldName = genericDelta.value<avro::GenericRecord>().schema()->nameAt(index);

            if (AvroGenericUtils::isNull(datum)) {
                deltaType.reset(new DefaultDeltaType);
            } else if (AvroGenericUtils::isReset(datum)) {
                deltaType.reset(new ResetDeltaType);
            } else if (AvroGenericUtils::isArray(datum)) {
                deltaType = createArrayDeltaType(datum.value<avro::GenericArray>());
            } else if (AvroGenericUtils::isRecord(datum)) {
                deltaType.reset(new ValueDeltaType(createDelta(datum), avro::AVRO_RECORD));
            } else {
                avro::Type type;
                IDeltaType::DeltaValue value = createDeltaValue(datum, type);
                deltaType.reset(new ValueDeltaType(value, type));
            }

            configurationDelta->updateFieldDeltaType(fieldName, deltaType);
        }
    }

    return configurationDeltaPtr;
}

DeltaTypePtr DefaultConfigurationDeltaFactory::createArrayDeltaType(const avro::GenericArray& array)
{
    DeltaTypePtr delta;
    const avro::GenericArray::Value& deltaArray = array.value();

    if (!deltaArray.empty()) {
        if (AvroGenericUtils::isUuid(deltaArray.front())) {
            RemovedItemsDeltaType* removedItems = new RemovedItemsDeltaType;
            delta.reset(removedItems);

            for (const auto& item : deltaArray) {
                removedItems->addHandlerId(AvroGenericUtils::getDeltaIDFromDatum(item));
            }
        } else {
            avro::Type type = deltaArray.front().type();
            AddedItemsDeltaType* addedItems = new AddedItemsDeltaType;

            delta.reset(addedItems);

            for (const auto& item : deltaArray) {
                if (type == avro::AVRO_RECORD) {
                    addedItems->addItem(createDelta(item));
                } else {
                    avro::Type type;
                    IDeltaType::DeltaValue value = createDeltaValue(item, type);
                    DeltaTypePtr deltaType(new ValueDeltaType(value, type));
                    addedItems->addItem(deltaType);
                }
            }
        }
    } else {
        delta.reset(new AddedItemsDeltaType);
    }

    return delta;
}

IDeltaType::DeltaValue DefaultConfigurationDeltaFactory::createDeltaValue(const avro::GenericDatum& datum, avro::Type& type)
{
    IDeltaType::DeltaValue value;

    type = datum.type();

    switch (type) {
        case avro::AVRO_BOOL: {
            value = datum.value<bool>();
            break;
        }
        case avro::AVRO_INT: {
            value = datum.value<int32_t>();
            break;
        }
        case avro::AVRO_LONG: {
            value = datum.value<int64_t>();
            break;
        }
        case avro::AVRO_FLOAT: {
            value = datum.value<float>();
            break;
        }
        case avro::AVRO_DOUBLE: {
            value = datum.value<double>();
            break;
        }
        case avro::AVRO_STRING: {
            value = datum.value<std::string>();
            break;
        }
        case avro::AVRO_ENUM: {
            value = datum.value<avro::GenericEnum>().symbol();
            break;
        }
        case avro::AVRO_FIXED: {
            value = datum.value<avro::GenericFixed>().value();
            break;
        }
        case avro::AVRO_BYTES: {
            value = datum.value<std::vector<std::uint8_t> >();
            break;
        }
        default: throw KaaException("Not a common type");
    }

    return value;
}

} /* namespace kaa */
