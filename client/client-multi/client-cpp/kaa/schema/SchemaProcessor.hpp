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

#ifndef SCHEMA_PROCESSOR_HPP_
#define SCHEMA_PROCESSOR_HPP_

#include <boost/signals2.hpp>

#include "kaa/schema/ISchemaProcessor.hpp"

namespace kaa {

/**
 * \class SchemaProcessor
 *
 * This class converts marshaled schema to an object and notifies
 * subscribers (\c ISchemaUpdatesReceiver) about schema was updated.
 *
 */
class SchemaProcessor : public ISchemaProcessor
{
public:
    SchemaProcessor() {}
    ~SchemaProcessor() {}

    /**
     * \c ISchemaProcessor implementation
     */
    void loadSchema(const std::uint8_t * buffer, std::size_t size);

    /**
     * \c ISchemaObservable implementation
     */
    void subscribeForSchemaUpdates(ISchemaUpdatesReceiver &receiver);
    void unsubscribeFromSchemaUpdates(ISchemaUpdatesReceiver &receiver);

    virtual SchemaPtr getSchema() const { return schema_; }

private:
    typedef avro::ValidSchema Schema;

    SchemaPtr schema_;
    boost::signals2::signal<void (std::shared_ptr<avro::ValidSchema>)> schemaUpdatesSubscribers_;
};

}  // namespace kaa


#endif /* SCHEMA_PROCESSOR_HPP_ */
