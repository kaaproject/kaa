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

#ifndef I_SCHEMA_UPDATES_RECEIVER_HPP_
#define I_SCHEMA_UPDATES_RECEIVER_HPP_

#include <boost/signals2/trackable.hpp>
#include <avro/ValidSchema.hh>

namespace kaa {

/**
 * Interface for schema updates listeners.
 * Listeners can be subscribed/unsubscribed for updates via \v ISchemaProcessor.
 */
class ISchemaUpdatesReceiver : public boost::signals2::trackable {
public:
    typedef avro::ValidSchema Schema;

    virtual ~ISchemaUpdatesReceiver() {}

    /**
     * Called when schema update is received.
     *
     * @param schema Compiled Avro schema.
     */
    virtual void onSchemaUpdated(std::shared_ptr<avro::ValidSchema> schema) = 0;
};

}  // namespace kaa


#endif /* I_SCHEMA_UPDATES_RECEIVER_HPP_ */
