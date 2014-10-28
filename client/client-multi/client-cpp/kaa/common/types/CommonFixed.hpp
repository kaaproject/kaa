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

#ifndef ICOMMONFIXED_HPP_
#define ICOMMONFIXED_HPP_

#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/ISchemaDependent.hpp"

#include <boost/ref.hpp>
#include <cstdint>
#include <vector>
#include <iomanip>
#include <sstream>

namespace kaa {

class CommonFixed : public ICommonValue, public ISchemaDependent {
public:
    CommonFixed(const avro::NodePtr & schema);

    const boost::any        getValue()  const   { return boost::cref(value_).get(); }

    const avro::NodePtr &   getSchema() const   { return schema_; }

    avro::GenericDatum      toAvro()    const;
    std::string             toString()  const;

    void                    setValue(const std::vector<std::uint8_t> &value);
private:
    avro::NodePtr schema_;
    std::vector<std::uint8_t> value_;
};

}  // namespace kaa


#endif /* ICOMMONFIXED_HPP_ */
