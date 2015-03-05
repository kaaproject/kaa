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

#ifndef I_COMMON_ARRAY_HPP_
#define I_COMMON_ARRAY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include "kaa/common/types/ISchemaDependent.hpp"
#include "kaa/common/types/ICommonValue.hpp"

#include <list>
#include <memory>

namespace kaa {

/**
 * Common array interface
 */
template<class Container>
class ICommonArrayBase: public ICommonValue, public ISchemaDependent {
public:
    typedef Container container_type;
    typedef typename Container::value_type elements_type;

    ICommonArrayBase()
            : ICommonValue(CommonValueType::COMMON_ARRAY)
    {
    }

    /**
     * Retrieves list of common values
     * @see CommonValue
     */
    virtual container_type & getList() const = 0;

    virtual ~ICommonArrayBase()
    {
    }
};

typedef ICommonArrayBase<std::list<std::shared_ptr<ICommonValue> > > ICommonArray;

}  // namespace kaa

#endif

#endif /* I_COMMON_ARRAY_HPP_ */
