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

#ifndef ILOGSTORAGESTATUS_HPP_
#define ILOGSTORAGESTATUS_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include <cstdint>

namespace kaa {

/**
 * Interface to retrieve status of log storage.
 *
 * Extend this interface to get extra information about
 * specific \c ILogStorage implementation.
 *
 * Default implementation can be found in \c MemoryLogStorage
 * \see MemoryLogStorage
 */
class ILogStorageStatus {
public:
    /**
     * Returns amount of bytes consumed by log storage.
     *
     * \return Size (in bytes) of consumed storage
     */
    virtual std::size_t getConsumedVolume() const = 0;

    /**
     * Returns amount of stored records.
     *
     * \return Amount of stored records
     */
    virtual std::size_t getRecordsCount() const = 0;

    virtual ~ILogStorageStatus()
    {
    }
};

}  // namespace kaa

#endif

#endif /* ILOGSTORAGESTATUS_HPP_ */
