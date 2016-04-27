/*
 * Copyright 2014-2016 CyberVision, Inc.
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

#ifndef ICONFIGURATIONSTORAGE_HPP_
#define ICONFIGURATIONSTORAGE_HPP_

#include <vector>
#include <memory>
#include <cstdint>

namespace kaa {

/**
 * Interface which is used by @link IConfigurationPersistenceManager @endlink
 * to use user-defined routines for persisting/loading binary configuration data.
 *
 * Should be defined by user.
 */
class IConfigurationStorage {
public:
    /**
     * Specifies routine to persist configuration data.
     *
     * @param bytes Configuration binary data.
     */
    virtual void saveConfiguration(const std::vector<std::uint8_t>& bytes) = 0;

    /**
     * Specifies routine to load configuration data.
     *
     * @return Configuration binary data.
     */
    virtual std::vector<std::uint8_t> loadConfiguration() = 0;

    /**
     * Clear configuration data (file).
     */
    virtual void clearConfiguration() = 0;

    virtual ~IConfigurationStorage() = default;
};

typedef std::shared_ptr<IConfigurationStorage> IConfigurationStoragePtr;

}  // namespace kaa

#endif /* ICONFIGURATIONSTORAGE_HPP_ */
