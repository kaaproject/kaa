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

#ifndef COMMON_RECORD_HPP_
#define COMMON_RECORD_HPP_

#include "kaa/common/types/ICommonRecord.hpp"

#include <avro/Schema.hh>
#include <boost/ref.hpp>
#include <map>

namespace kaa {

class CommonRecord : public ICommonRecord {
public:
    CommonRecord(uuid_t uuid, const avro::NodePtr & schema);

    const boost::any        getValue()  const   { return boost::cref(*this).get(); }
    const avro::NodePtr &   getSchema() const;

    avro::GenericDatum      toAvro()    const;
    std::string             toString()  const;

    void                    setUuid(uuid_t uuid);
    uuid_t                  getUuid();

    bool                    hasField    (const keys_type &field_name) const;
    fields_type             getField    (const keys_type &field_name) const;
    void                    setField    (const keys_type &field_name, fields_type value);
    void                    removeField (const keys_type &field_name);
    const container_type &  getFields   () const { return fields_; }
private:
    uuid_t uuid_;
    container_type fields_;
    avro::NodePtr schema_;
};

}  // namespace kaa

#endif /* COMMON_RECORD_HPP_ */
