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

#ifndef ILOGSTORAGESTATUS_HPP_
#define ILOGSTORAGESTATUS_HPP_

#include <cstdint>

namespace kaa {

/**
 * @brief The public interface to represent the current log storage state.
 *
 * Extend this interface to get extra information about the specific @c ILogStorage implementation.
 * The default implementation can be found in @c MemoryLogStorage.
 */
class ILogStorageStatus {
public:
    /**
     * @brief Returns amount of bytes collected logs are consumed.
     *
     * @return Size (in bytes).
     */
    virtual std::size_t getConsumedVolume() = 0;

    /**
     * @brief Returns the number of collected logs.
     *
     * @return The number of collected logs.
     */
    virtual std::size_t getRecordsCount() = 0;

    virtual ~ILogStorageStatus() {}
};

}  // namespace kaa

#endif /* ILOGSTORAGESTATUS_HPP_ */
