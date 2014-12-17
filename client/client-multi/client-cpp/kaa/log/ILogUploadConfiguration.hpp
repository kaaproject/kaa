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

#ifndef ILOGUPLOADCONFIGURATION_HPP_
#define ILOGUPLOADCONFIGURATION_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_LOGGING

#include <cstdint>

namespace kaa {

/**
 * Interface which determines parameters
 * for log upload strategy. \c ILogUploadStrategy.
 *
 * You may extend this to provide some specific configurations
 * used in \c ILogUploadStrategy implementation.
 */
class ILogUploadConfiguration {
public:
    /**
     * Returns size of single log pack which should be sent
     * to the server within single message
     *
     * \return Amount of bytes for sending at single message.
     */
    virtual std::size_t  getBlockSize() const = 0;

    /**
     * Returns maximal size which can be used by \c ILogStorage.
     * When this value is exceeded system will remove oldest log entries.
     *
     * \return Size (in bytes) of the storage.
     */
    virtual std::size_t  getMaxStorageVolume() const = 0;

    /**
     * Amount of collected log messages to start log upload.
     *
     * \return Size (in bytes) of stored log records to start upload.
     */
    virtual std::size_t  getVolumeThreshold() const = 0;

    /**
     * Maximum time to wait log delivery response.
     *
     * \return Time in seconds.
     */
    virtual std::size_t getLogUploadTimeout() const = 0;

    virtual ~ILogUploadConfiguration() {}
};

}  // namespace kaa

#endif

#endif /* ILOGUPLOADCONFIGURATION_HPP_ */
