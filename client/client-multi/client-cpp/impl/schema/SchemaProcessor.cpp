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

#include "kaa/schema/SchemaProcessor.hpp"

#include <sstream>

#include <avro/AvroParse.hh>
#include <avro/Compiler.hh>

#include "kaa/logging/Log.hpp"
#include "kaa/common/exception/KaaException.hpp"
#include "kaa/logging/Log.hpp"

namespace kaa {

void SchemaProcessor::loadSchema(const std::uint8_t * buffer, std::size_t buffer_length)
{
    KAA_LOG_INFO("Loading schema...");
    const avro::ValidSchema &schema = avro::compileJsonSchemaFromMemory(buffer, buffer_length);

    std::ostringstream stream;
    schema.toJson(stream);

    KAA_LOG_DEBUG(boost::format("New configuration schema: %1%") % stream.str());

    schema_.reset(new avro::ValidSchema(schema));
    schemaUpdatesSubscribers_(schema_);
}

void SchemaProcessor::subscribeForSchemaUpdates(ISchemaUpdatesReceiver &receiver)
{
    boost::signals2::connection c =
            schemaUpdatesSubscribers_.connect(
            boost::bind(&ISchemaUpdatesReceiver::onSchemaUpdated, &receiver, _1));
    if (!c.connected()) {
        throw KaaException("Failed to add a schema updates subscriber.");
    }
}

void SchemaProcessor::unsubscribeFromSchemaUpdates(ISchemaUpdatesReceiver &receiver)
{
    schemaUpdatesSubscribers_.disconnect(
                boost::bind(&ISchemaUpdatesReceiver::onSchemaUpdated, &receiver, _1));
}

}  // namespace kaa
