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

#ifndef ISCHEMAPERSISTENCEMANAGER_HPP_
#define ISCHEMAPERSISTENCEMANAGER_HPP_

#include <memory>

#include "kaa/schema/storage/ISchemaStorage.hpp"
#include "kaa/schema/ISchemaUpdatesReceiver.hpp"

namespace kaa {

/**
 * Interface for schema persistence manager.
 */
class ISchemaPersistenceManager : public ISchemaUpdatesReceiver {
public:
    virtual ~ISchemaPersistenceManager() {}

    /**
     * Registers new data schema persistence routines. Replaces previously set value.
     * Memory pointed by given parameter should be managed by user.
     *
     * @param storage User-defined persistence routines. See \c ISchemaStorage
     */
    virtual void setSchemaStorage(ISchemaStorage *storage) = 0;
};

typedef std::shared_ptr<ISchemaPersistenceManager> ISchemaPersistenceManagerPtr;

}  // namespace kaa


#endif /* ISCHEMAPERSISTENCEMANAGER_HPP_ */
