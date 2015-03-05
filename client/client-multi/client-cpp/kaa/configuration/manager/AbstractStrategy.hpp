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

#ifndef ABSTRACTSTRATEGY_HPP_
#define ABSTRACTSTRATEGY_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_CONFIGURATION

#include <avro/Generic.hh>
#include <memory>
#include "kaa/common/types/ICommonRecord.hpp"

namespace kaa {

/**
 * Abstract strategy to convert Avro datum to Common type.
 */
class AbstractStrategy {
public:
    /**
     * Routine for processing avro datum and insert field into a record by given name.
     *
     * @param parent    Record which has to contain data represented in datum.
     * @param field     Name of a field in a given record.
     * @param datum     Avro datum containing data to be converted.
     */
    virtual void run(std::shared_ptr<ICommonRecord> parent, const std::string &field,
                     const avro::GenericDatum &datum) = 0;

    virtual ~AbstractStrategy()
    {
    }
};

}  // namespace kaa

#endif

#endif /* ABSTRACTSTRATEGY_HPP_ */
