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

#include "kaa/configuration/manager/Strategies.hpp"

#include <algorithm>
#include <avro/Generic.hh>
#include <memory>
#include <cstdint>

#include "kaa/common/AvroGenericUtils.hpp"
#include "kaa/common/CommonTypesFactory.hpp"
#include "kaa/common/CommonValueTools.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/configuration/manager/FieldProcessor.hpp"
#include "kaa/common/types/CommonArray.hpp"
#include "kaa/common/CommonValueTools.hpp"

namespace kaa {

void UuidProcessStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    avro::GenericFixed uuid_field = datum.value<avro::GenericFixed>();
    uuid_t uuid;
    std::copy(uuid_field.value().begin(), uuid_field.value().end(), uuid.begin());

    bool need_to_subscribe = true;

    // check for uuid change
    if (parent->hasField(field)) {
        ICommonRecord::fields_type uuid_field = parent->getField(field);
        if (uuid_field.get()) {
            std::vector<std::uint8_t> prev_uuid = boost::any_cast<std::vector<std::uint8_t> >(uuid_field->getValue());
            if (!prev_uuid.empty()) {
                uuid_t old_uuid;
                std::copy(prev_uuid.begin(), prev_uuid.end(), old_uuid.begin());
                if (!std::equal(old_uuid.begin(), old_uuid.end(), uuid.begin())) {
                    if (isSubscribedFn_(old_uuid)) {
                        unsubscribeFn_(old_uuid);
                    }
                } else {
                    need_to_subscribe = false;
                }
            }
        }
    }

    parent->setField(field, CommonTypesFactory::createCommon<avro::AVRO_FIXED>(datum));
    parent->setUuid(uuid);
    if (need_to_subscribe) {
        subscribeFn_(uuid, parent);
    }
}

void RecordProcessStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    const avro::GenericRecord &rec = datum.value<avro::GenericRecord>();
    std::size_t field_count = rec.fieldCount();
    uuid_t empty_uuid;
    std::shared_ptr<ICommonRecord> value;

    if (isRootRecord_) {
        value = parent;
    } else {
        if (parent->hasField(field)) {
            if(CommonValueTools::isRecord(parent->getField(field))) {
                if(CommonValueTools::getRecord(parent->getField(field)).getSchema()->name().fullname()
                        .compare(rec.schema()->name().fullname()) == 0) {
                    value = std::dynamic_pointer_cast<ICommonRecord, ICommonValue>(parent->getField(field));
                } else {
                    value = CommonTypesFactory::createCommonRecord(empty_uuid, rec.schema());
                }
            } else {
                value = CommonTypesFactory::createCommonRecord(empty_uuid, rec.schema());
            }
        } else {
            value = CommonTypesFactory::createCommonRecord(empty_uuid, rec.schema());
        }
    }

    for (size_t i = 0; i < field_count; ++i) {
        const std::string& field_name = rec.schema()->nameAt(i);
        const avro::GenericDatum& innerDatum = rec.fieldAt(i);

        if (AvroGenericUtils::isUnchanged(innerDatum)) {
            continue;
        }

        std::unique_ptr<FieldProcessor> fp(new FieldProcessor(value, field_name));
        AbstractStrategy *strategy = nullptr;
        switch (innerDatum.type()) {
            case avro::AVRO_RECORD: {
                strategy = new RecordProcessStrategy(isSubscribedFn_, subscribeFn_, unsubscribeFn_);
                break;
            }
            case avro::AVRO_ARRAY: {
                if (parent->hasField(field_name)) {
                    value->setField(field_name, parent->getField(field_name));
                }

                strategy = new ArrayProcessStrategy(isSubscribedFn_, subscribeFn_, unsubscribeFn_);
                break;
            }
            case avro::AVRO_NULL: {
                strategy = new NullProcessStrategy();
                break;
            }
            case avro::AVRO_FIXED: {
                if (AvroGenericUtils::isUuid(innerDatum)) {
                    strategy = new UuidProcessStrategy(isSubscribedFn_, subscribeFn_, unsubscribeFn_);
                } else {
                    strategy = new CommonProcessStrategy();
                }
                break;
            }
            case avro::AVRO_ENUM: {
                if (AvroGenericUtils::isReset(innerDatum)) {
                    fp.reset(new FieldProcessor(parent, field_name));
                    strategy = new ArrayResetStrategy(isSubscribedFn_, unsubscribeFn_);
                } else {
                    strategy = new CommonProcessStrategy();
                }
                break;
            }
            case avro::AVRO_MAP:
            case avro::AVRO_UNKNOWN: {
                throw KaaException(boost::format("Unsupported field type %1%") % datum.type());
            }
            default: {
                strategy = new CommonProcessStrategy();
                break;
            }
        }
        fp->setStrategy(strategy);
        fp->process(innerDatum);
    }
    if (!isRootRecord_) {
        parent->setField(field, value);
    } else {
        auto map_ = value->getFields();
        for (auto it = map_.begin(); it != map_.end(); ++it) {
            parent->setField(it->first, it->second);
        }
    }
}

void ArrayResetStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    ICommonRecord::fields_type array = parent->getField(field);
    ICommonArray * array_ptr = dynamic_cast<ICommonArray *>(array.get());
    if (array_ptr) {
        unregisterArray(*array_ptr);
        array_ptr->getList().clear();
    }
}

void ArrayResetStrategy::unregisterRecord(ICommonRecord &record)
{
    auto map = record.getFields();
    if (record.hasField("__uuid")) {
        uuid_t uuid;
        auto uuid_field = record.getField("__uuid");
        std::vector<std::uint8_t> uuid_raw =  boost::any_cast<std::vector<std::uint8_t> >(uuid_field->getValue());
        std::copy(uuid_raw.begin(), uuid_raw.end(), uuid.begin());
        if (isSubscribedFn_(uuid)) {
            unsubscribeFn_(uuid);
        }
    }

    for (auto it = map.begin(); it != map.end(); ++it) {
        switch (it->second->getCommonType()) {
            case CommonValueType::COMMON_RECORD: {
                unregisterRecord(dynamic_cast<ICommonRecord &>(*(*it).second));
                break;
            }
            case CommonValueType::COMMON_ARRAY: {
                unregisterArray(dynamic_cast<ICommonArray &>(*(*it).second));
                break;
            }
            default:
                break;
            }
    }
}

void ArrayResetStrategy::unregisterArray(ICommonArray &array)
{
    auto list = array.getList();
    for (auto it = list.begin(); it != list.end(); ++it) {
        switch ((*it)->getCommonType()) {
            case CommonValueType::COMMON_RECORD: {
                unregisterRecord(dynamic_cast<ICommonRecord &>(*(*it)));
                break;
            }
            case CommonValueType::COMMON_ARRAY: {
                unregisterArray(dynamic_cast<ICommonArray &>(*(*it)));
                break;
            }
            default:
                break;
        }
    }
}

const std::string ArrayProcessStrategy::array_holder_field = "___array__value___";

void ArrayProcessStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    std::shared_ptr<ICommonValue> commonValue;
    std::shared_ptr<ICommonArray> commonArray;
    if (parent->hasField(field) && CommonValueTools::isArray(parent->getField(field))) {
        commonValue = parent->getField(field);
        commonArray.reset(new CommonArray(*dynamic_cast<CommonArray *>(commonValue.get())));
    } else {
        commonArray = CommonTypesFactory::createCommonArray(datum.value<avro::GenericArray>().schema());
    }

    avro::GenericArray array = datum.value<avro::GenericArray>();
    std::vector<avro::GenericDatum> vec = array.value();

    if (!vec.empty()) {
        auto first_element = vec.begin();
        if (AvroGenericUtils::isUuid(*first_element)) {
            for (auto it = first_element; it != vec.end(); ++it) {
                avro::GenericFixed uuid_field = (*it).value<avro::GenericFixed>();
                uuid_t uuid;
                std::copy(uuid_field.value().begin(), uuid_field.value().end(), uuid.begin());
                ICommonArray::container_type & inner_list = commonArray->getList();
                for (auto record_it = inner_list.begin(); record_it != inner_list.end(); ++record_it) {
                    ICommonArray::elements_type record = *record_it;
                    ICommonRecord * r_ptr = dynamic_cast<ICommonRecord *>(record.get());
                    if (r_ptr) {
                        if (uuid == r_ptr->getUuid()) {
                            ArrayResetStrategy ars(isSubscribedFn_, unsubscribeFn_);
                            ars.unregisterRecord(*r_ptr);
                            commonArray->getList().erase(record_it);
                            break;
                        }
                    }
                }
            }
        } else {
            for (auto it = first_element; it != vec.end(); ++it) {
                const avro::GenericDatum & innerDatum = *it;
                uuid_t empty_uuid;
                std::shared_ptr<ICommonRecord> record = CommonTypesFactory::createCommonRecord(empty_uuid, datum.value<avro::GenericArray>().schema());
                std::unique_ptr<FieldProcessor> fp(new FieldProcessor(record, array_holder_field));
                AbstractStrategy *strategy = nullptr;
                switch (innerDatum.type()) {
                    case avro::AVRO_RECORD: {
                        strategy = new RecordProcessStrategy(isSubscribedFn_, subscribeFn_, unsubscribeFn_);
                        break;
                    }
                    case avro::AVRO_ARRAY: {
                        strategy = new ArrayProcessStrategy(isSubscribedFn_, subscribeFn_, unsubscribeFn_);
                        break;
                    }
                    case avro::AVRO_NULL: {
                        strategy = new NullProcessStrategy();
                        break;
                    }
                    case avro::AVRO_MAP:
                    case avro::AVRO_UNKNOWN: {
                        throw KaaException(boost::format("Unsupported field type %1%") % datum.type());
                    }
                    default: {
                        strategy = new CommonProcessStrategy();
                        break;
                    }
                }
                fp->setStrategy(strategy);
                fp->process(innerDatum);

                commonArray->getList().push_back(record->getField(array_holder_field));
            }
        }
        parent->setField(field, commonArray);
    }
}

void NullProcessStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    parent->setField(field, CommonTypesFactory::createCommon<avro::AVRO_NULL>(datum));
}

void CommonProcessStrategy::run(std::shared_ptr<ICommonRecord> parent, const std::string &field, const avro::GenericDatum &datum)
{
    CommonTypesFactory::return_type result;
    switch (datum.type()) {
        case avro::AVRO_INT: {
            result = CommonTypesFactory::createCommon<avro::AVRO_INT>(datum);
            break;
        }
        case avro::AVRO_LONG: {
            result = CommonTypesFactory::createCommon<avro::AVRO_LONG>(datum);
            break;
        }
        case avro::AVRO_FLOAT: {
            result = CommonTypesFactory::createCommon<avro::AVRO_FLOAT>(datum);
            break;
        }
        case avro::AVRO_DOUBLE: {
            result = CommonTypesFactory::createCommon<avro::AVRO_DOUBLE>(datum);
            break;
        }
        case avro::AVRO_BOOL: {
            result = CommonTypesFactory::createCommon<avro::AVRO_BOOL>(datum);
            break;
        }
        case avro::AVRO_ENUM: {
            result = CommonTypesFactory::createCommon<avro::AVRO_ENUM>(datum);
            break;
        }
        case avro::AVRO_STRING: {
            result = CommonTypesFactory::createCommon<avro::AVRO_STRING>(datum);
            break;
        }
        case avro::AVRO_FIXED: {
            result = CommonTypesFactory::createCommon<avro::AVRO_FIXED>(datum);
            break;
        }
        case avro::AVRO_BYTES: {
            result = CommonTypesFactory::createCommon<avro::AVRO_BYTES>(datum);
            break;
        }
        default: throw KaaException("Not a common type");
    }
    parent->setField(field, result);
}

}  // namespace kaa
