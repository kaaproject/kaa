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

#ifndef COMMONARRAY_HPP_
#define COMMONARRAY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/common/types/ICommonArray.hpp"
#include <boost/ref.hpp>

namespace kaa {

class CommonArray: public ICommonArray {
public:
    CommonArray(const avro::NodePtr &schema);

    const boost::any getValue() const
    {
        return boost::cref(theList_).get();
    }
    const avro::NodePtr & getSchema() const
    {
        return schema_;
    }

    avro::GenericDatum toAvro() const;
    std::string toString() const;

    container_type & getList() const;
private:
    avro::NodePtr schema_;
    mutable container_type theList_;
};

} // namespace kaa

#endif

#endif /* COMMONARRAY_HPP_ */
