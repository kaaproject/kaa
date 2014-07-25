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

#include "kaa/common/types/CommonArray.hpp"
#include <sstream>

#include "kaa/common/AvroDatumsComparator.hpp"
#include "kaa/common/types/SetValueHelper.hpp"

namespace kaa {

CommonArray::CommonArray(const avro::NodePtr & schema)
    : schema_(schema)
{

}

CommonArray::container_type &CommonArray::getList() const
{
    return theList_;
}

avro::GenericDatum CommonArray::toAvro() const
{
    avro::GenericDatum datum(getSchema());
    avro::GenericArray &array = datum.value<avro::GenericArray>();
    for (auto it = theList_.begin(); it != theList_.end(); ++it) {

        // TODO: Check this and make it simpler if possible

        const avro::NodePtr &innode = schema_->leafAt(0);
        avro::Type innertype = innode->type();
        if (innertype == avro::AVRO_UNION) {
            avro::GenericDatum fieldDatum = (*it)->toAvro();

            avro::GenericDatum indatum(innode);
            size_t branches = indatum.unionBranch();
            size_t currentFieldBranch = 0;

            avro::Type avro_type = fieldDatum.type();
            bool is_compound = avro::isCompound(avro_type);
            while (currentFieldBranch < branches) {
                avro::Type intype = innode->leafAt(currentFieldBranch)->type();
                indatum.selectBranch(currentFieldBranch);

                bool found = false;
                if (is_compound) {
                    ISchemaDependent * dependent = dynamic_cast<ISchemaDependent *>((*it).get());
                    if (innode->leafAt(currentFieldBranch)->hasName() &&
                            dependent->getSchema()->name().fullname() == innode->leafAt(currentFieldBranch)->name().fullname())
                    {
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
            array.value().push_back(indatum);
        } else {
            array.value().push_back((*it)->toAvro());
        }
    }

    std::sort(array.value().begin(), array.value().end(), avro_comparator());
    return datum;
}

std::string CommonArray::toString() const
{
    std::stringstream ss;
    ss << "[ ";
    for (auto it = theList_.begin(); it != theList_.end();) {
        ss << (*it)->toString();
        if (++it != theList_.end()) {
            ss << ", ";
        }
    }
    ss << " ]";
    return ss.str();
}

}  // namespace kaa
