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

#ifndef COMMONTYPESFACTORY_HPP_
#define COMMONTYPESFACTORY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Generic.hh>

#include "kaa/common/types/ICommonValue.hpp"
#include "kaa/common/types/ICommonRecord.hpp"
#include "kaa/common/types/ICommonArray.hpp"
#include <cstdint>
#include <memory>

namespace kaa {

class CommonTypesFactory {
public:
    CommonTypesFactory() {};
    typedef std::shared_ptr<ICommonValue> return_type;

    static std::shared_ptr<ICommonRecord> createCommonRecord(uuid_t uuid, const avro::NodePtr schema);
    static std::shared_ptr<ICommonArray> createCommonArray(const avro::NodePtr &schema);

    template<avro::Type T>
    static return_type createCommon(const avro::GenericDatum & d);
};

}  // namespace kaa

#endif

#endif /* COMMONTYPESFACTORY_HPP_ */
