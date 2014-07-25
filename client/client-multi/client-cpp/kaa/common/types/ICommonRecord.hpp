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

#ifndef I_COMMON_RECORD_HPP_
#define I_COMMON_RECORD_HPP_

#include "kaa/common/types/ISchemaDependent.hpp"
#include "kaa/common/types/ICommonValue.hpp"

#include <boost/uuid/uuid.hpp>
#include <map>

namespace kaa {

typedef boost::uuids::uuid uuid_t;

/**
 * Represents Avro record data structure.
 */
template<class Container>
class ICommonRecordBase: public ISchemaDependent, public ICommonValue {
public:
    typedef Container                       container_type;
    typedef typename Container::mapped_type fields_type;
    typedef typename Container::key_type    keys_type;

    ICommonRecordBase() : ICommonValue(CommonValueType::COMMON_RECORD) {}
    virtual ~ICommonRecordBase() {}

    /**
     * Set uuid object to the record
     */
    virtual void                    setUuid(uuid_t uuid) = 0;

    /**
     * Retrieves uuid object to the record
     */
    virtual uuid_t                  getUuid() = 0;

    /**
     * Check if the field is present in the record
     *
     * @param field_name Name of the field to be checked
     * @return True if it is present, false otherwise
     */
    virtual bool                    hasField    (const keys_type &field_name) const = 0;

    /**
     * Retrieves value of the field.
     *
     * @param field_name Name of the field which value being returned
     * @return Value of the field
     */
    virtual fields_type             getField    (const keys_type &field_name) const = 0;

    /**
     * Set new value to the field.
     *
     * @param field_name Name of the field which value is being changed
     */
    virtual void                    setField    (const keys_type &field_name, fields_type value) = 0;

    /**
     * Remove field from the record.
     *
     * @param field_name Name of the field which value is being removed
     */
    virtual void                    removeField (const keys_type &field_name) = 0;

    /**
     * Retrieves container with set of field pairs (name/value).
     */
    virtual const container_type &  getFields   () const = 0;
};

typedef ICommonRecordBase<std::map<std::string, boost::shared_ptr<ICommonValue> > > ICommonRecord;

}  // namespace kaa


#endif /* I_COMMON_RECORD_HPP_ */
