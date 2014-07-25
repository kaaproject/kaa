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

#ifndef COMMONENUM_HPP_
#define COMMONENUM_HPP_

#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/ISchemaDependent.hpp"

#include <boost/ref.hpp>

namespace kaa {

class CommonEnum : public ICommonValue, public ISchemaDependent {
public:
    CommonEnum(const avro::NodePtr &schema);

    const boost::any        getValue()  const   { return boost::cref(symbol_).get(); }
    const avro::NodePtr &   getSchema() const   { return schema_; }

    avro::GenericDatum      toAvro()    const;
    std::string             toString()  const;

    void                    setValue(const std::string &value);
private:
    std::string     symbol_;
    avro::NodePtr   schema_;
};

}  // namespace kaa


#endif /* COMMONENUM_HPP_ */
