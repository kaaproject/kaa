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

#ifndef I_SCHEMA_PROCESSOR_HPP_
#define I_SCHEMA_PROCESSOR_HPP_

#include <boost/cstdint.hpp>
#include <boost/smart_ptr/shared_ptr.hpp>
#include <avro/ValidSchema.hh>

#include "kaa/schema/ISchemaObservable.hpp"

namespace kaa {

typedef boost::shared_ptr<avro::ValidSchema> SchemaPtr;

/**
 * Interface for data schema processor.
 */
class ISchemaProcessor : public ISchemaObservable {
public:
    virtual ~ISchemaProcessor() {}

    /**
     * Data schema receiving routine.
     *
     * @param buffer    Pointer to a memory block where data schema is placed.
     * @param size      Size of buffer.
     */
    virtual void loadSchema(const boost::uint8_t * buffer, size_t size) = 0;
    virtual SchemaPtr getSchema() const = 0;
};

typedef boost::shared_ptr<ISchemaProcessor> ISchemaProcessorPtr;

}  // namespace kaa


#endif /* I_SCHEMA_PROCESSOR_HPP_ */
