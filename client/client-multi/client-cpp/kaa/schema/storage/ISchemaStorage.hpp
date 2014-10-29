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

#ifndef ISCHEMASTORAGE_HPP_
#define ISCHEMASTORAGE_HPP_

#include <vector>
#include <cstdint>

namespace kaa {

/**
 * Interface which is used by \c SchemaPersistenceManager
 * to use user-defined routines for persisting/loading binary data schema.
 *
 * Should be defined by user.
 */
class ISchemaStorage {
public:
    typedef std::vector<std::uint8_t> byte_buffer;
    virtual ~ISchemaStorage() {}

    /**
     * Specifies routine to persist data schema.
     *
     * @param bytes Serialized data schema.
     */
    virtual void        saveSchema(const byte_buffer &data) = 0;

    /**
     * Specifies routine to load data schema.
     *
     * @return Serialized data schema.
     */
    virtual byte_buffer loadSchema() = 0;
};

}  // namespace kaa


#endif /* ISCHEMASTORAGE_HPP_ */
