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

#include "kaa/common/types/CommonRecord.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/common/exception/KaaException.hpp"

#include <algorithm>
#include <exception>
#include <sstream>

#include <avro/Generic.hh>
#include "kaa/common/types/SetValueHelper.hpp"

namespace kaa {

CommonRecord::CommonRecord(uuid_t uuid, const avro::NodePtr & schema)
    : uuid_(uuid)
    , schema_(schema)
{

}

void CommonRecord::setUuid(uuid_t uuid)
{
    std::copy(uuid.begin(), uuid.end(), uuid_.begin());
}

uuid_t CommonRecord::getUuid()
{
    return uuid_;
}

bool CommonRecord::hasField(const keys_type &field_name) const
{
    auto it = fields_.find(field_name);
    return it != fields_.end();
}

void CommonRecord::setField(const keys_type &field_name, fields_type value)
{
    if (hasField(field_name)) {
        fields_[field_name] = value;
    } else {
        auto res = fields_.insert(std::make_pair(field_name, value));
        if (!res.second) {
            throw KaaException(boost::format("Failed to insert field \"%1%\" into record") % field_name);
        }
    }
}

void CommonRecord::removeField(const keys_type &field_name)
{
    if (!hasField(field_name)) {
        throw KaaException(boost::format("Field with name %1% was not found in record") % field_name);
    } else {
        fields_.erase(field_name);
    }
}

ICommonRecord::fields_type CommonRecord::getField(const keys_type &field_name) const
{
    if (!hasField(field_name)) {
        throw KaaException(boost::format("Field with name %1% was not found in record") % field_name);
    }
    auto it = fields_.find(field_name);
    return it->second;
}

const avro::NodePtr &CommonRecord::getSchema() const
{
    return schema_;
}

avro::GenericDatum CommonRecord::toAvro() const
{
    avro::GenericDatum datum(getSchema());
    avro::GenericRecord &record = datum.value<avro::GenericRecord>();

    size_t fieldsCount = schema_->names();
    for (size_t fieldIndex = 0; fieldIndex < fieldsCount; ++fieldIndex) {
        const std::string & fieldName = schema_->nameAt(fieldIndex);
        auto it = getFields().find(fieldName);
        if (it != getFields().end()) {
            const avro::NodePtr &innode = schema_->leafAt(fieldIndex);

            avro::GenericDatum fieldDatum = it->second->toAvro();
            if (innode->type() == avro::AVRO_UNION) {

                avro::Type avro_type = fieldDatum.type();

                avro::GenericDatum indatum(innode);
                size_t branches = indatum.unionBranch();
                size_t currentFieldBranch = 0;

                bool is_compound = avro::isCompound(avro_type);

                while (currentFieldBranch < branches) {
                    avro::Type intype = innode->leafAt(currentFieldBranch)->type();
                    indatum.selectBranch(currentFieldBranch);

                    bool found = false;
                    if (is_compound) {
                        ISchemaDependent * dependent = dynamic_cast<ISchemaDependent *>(it->second.get());
                        if (innode->leafAt(currentFieldBranch)->hasName() && dependent->getSchema()->hasName() &&
                                dependent->getSchema()->name().fullname() == innode->leafAt(currentFieldBranch)->name().fullname()) {
                            found = SetAvroValueHelper::setValue(avro_type, indatum, fieldDatum);
                        } else if (innode->leafAt(currentFieldBranch)->type() == avro::Type::AVRO_ARRAY
                                && dependent->getSchema()->type() == avro::Type::AVRO_ARRAY) {
                            found = SetAvroValueHelper::setValue(avro::Type::AVRO_ARRAY, indatum, fieldDatum);
                        }
                    } else if (intype == avro_type) {
                        found = SetAvroValueHelper::setValue(avro_type, indatum, fieldDatum);
                    }
                    if (found) {
                        break;
                    }
                    ++currentFieldBranch;
                }
                record.setField(it->first, indatum);
            } else {
                record.setField(it->first, fieldDatum);
            }
        }
    }
    return datum;
}

std::string CommonRecord::toString() const
{
    std::stringstream ss;
    ss << "[ ";
    for (auto it = getFields().begin(); it != getFields().end();) {
        ss << "{ \"" << it->first << "\": " << it->second->toString() <<" }";
        if (++it != getFields().end()) {
            ss << ", ";
        }
    }
    ss << " ]";
    return ss.str();
}

}  // namespace kaa

#endif
